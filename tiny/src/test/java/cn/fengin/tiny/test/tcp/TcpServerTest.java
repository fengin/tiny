package cn.fengin.tiny.test.tcp;

import cn.fengin.tiny.config.ServerConfig;
import cn.fengin.tiny.context.ApplicationContext;
import cn.fengin.tiny.tcp.TcpServer;
import cn.fengin.tiny.tcp.message.Message;
import cn.fengin.tiny.tcp.message.MessageCodec;
import cn.fengin.tiny.tcp.message.MessageProcessor;
import cn.fengin.tiny.tcp.register.TcpRegistry;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

/**
 * TCP服务器单元测试类
 * 测试TCP服务器的基本功能，包括连接管理、消息收发、并发处理等
 * 使用JUnit 5的@TestMethodOrder注解确保测试按顺序执行
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TcpServerTest {
    private static final Logger logger = LoggerFactory.getLogger(TcpServerTest.class);
    private static TcpServer server;
    private static final int TEST_PORT = 18082;
    private static List<Socket> testClients;
    private static TestMessageProcessor processor;

    /**
     * 测试用的消息类
     * 实现Message接口，用于测试消息的编解码和处理
     */
    public static class TestMessage implements Message {
        private String deviceId;
        private int type;
        private byte[] payload;

        /**
         * 构造测试消息
         * @param deviceId 设备ID
         * @param type 消息类型
         * @param content 消息内容
         */
        public TestMessage(String deviceId, int type, String content) {
            this.deviceId = deviceId;
            this.type = type;
            this.payload = content.getBytes();
        }

        @Override
        public String getDeviceId() {
            return deviceId;
        }

        @Override
        public int getMessageType() {
            return type;
        }

        @Override
        public byte[] getPayload() {
            return payload;
        }
    }

    /**
     * 测试用的消息编解码器
     * 实现MessageCodec接口，处理消息的序列化和反序列化
     * 消息格式：
     * [总长度(4字节)][设备ID长度(4字节)][设备ID][消息类型(4字节)][消息内容]
     */
    public static class TestMessageCodec implements MessageCodec {
        @Override
        public Message decode(ByteBuf in) throws Exception {
            // 检查是否有足够的数据可读
            if (in.readableBytes() < 8) return null;

            // 标记当前读取位置，以便需要时可以重置
            in.markReaderIndex();
            int totalLength = in.readInt();
            if (in.readableBytes() < totalLength - 4) {
                in.resetReaderIndex();
                return null;
            }

            // 读取设备ID
            int deviceIdLength = in.readInt();
            byte[] deviceIdBytes = new byte[deviceIdLength];
            in.readBytes(deviceIdBytes);
            String deviceId = new String(deviceIdBytes);

            // 读取消息类型和内容
            int type = in.readInt();
            int contentLength = totalLength - 12 - deviceIdLength;
            byte[] content = new byte[contentLength];
            in.readBytes(content);

            return new TestMessage(deviceId, type, new String(content));
        }

        @Override
        public void encode(Message msg, ByteBuf out) throws Exception {
            TestMessage message = (TestMessage) msg;
            byte[] deviceIdBytes = message.getDeviceId().getBytes();
            byte[] content = message.getPayload();

            int totalLength = 12 + deviceIdBytes.length + content.length;

            out.writeInt(totalLength);
            out.writeInt(deviceIdBytes.length);
            out.writeBytes(deviceIdBytes);
            out.writeInt(message.getMessageType());
            out.writeBytes(content);
        }
    }

    /**
     * 测试用的消息处理器
     * 实现MessageProcessor接口，处理接收到的消息和连接事件
     * 使用CountDownLatch来同步测试过程，确保消息被正确处理
     */
    public static class TestMessageProcessor implements MessageProcessor {
        private final List<Message> receivedMessages = new ArrayList<>();
        private final CountDownLatch messageLatch;
        private final AtomicReference<String> lastConnectedDevice = new AtomicReference<>();
        private final AtomicReference<String> lastDisconnectedDevice = new AtomicReference<>();

        /**
         * 构造消息处理器
         * @param expectedMessages 预期接收的消息数量
         */
        public TestMessageProcessor(int expectedMessages) {
            this.messageLatch = new CountDownLatch(expectedMessages);
        }

        @Override
        public void process(Message msg, Channel channel) {
            receivedMessages.add(msg);
            messageLatch.countDown();
        }

        @Override
        public void onConnected(String deviceId) {
            lastConnectedDevice.set(deviceId);
        }

        @Override
        public void onDisconnected(String deviceId) {
            lastDisconnectedDevice.set(deviceId);
        }

        public List<Message> getReceivedMessages() {
            return receivedMessages;
        }

        public boolean waitForMessages(long timeout, TimeUnit unit) throws InterruptedException {
            return messageLatch.await(timeout, unit);
        }

        public String getLastConnectedDevice() {
            return lastConnectedDevice.get();
        }

        public String getLastDisconnectedDevice() {
            return lastDisconnectedDevice.get();
        }
    }

    /**
     * 测试环境初始化
     * 在所有测试方法执行之前运行
     * 配置并启动TCP服务器，初始化测试环境
     */
    @BeforeAll
    static void setup() throws Exception {
        // 创建TCP服务器配置
        System.setProperty("app.config", "application-template.yaml");
        ServerConfig.TcpServerConfig config = new ServerConfig.TcpServerConfig();
        config.setName("test");
        config.setPort(TEST_PORT);
        config.setBossThreads(1);
        config.setWorkerThreads(2);
        config.setBacklog(128);
        config.setDeviceIdleTime(60);
        config.setDeviceIdleTimeout(180);
        ApplicationContext context =  ApplicationContext.getInstance();
        List<ServerConfig.TcpServerConfig> tcpServers = context.getServerConfig().getTcpServers();
        tcpServers.add(config);

        // 创建TCP服务注册器并注册编解码器和消息处理器
        TcpRegistry registry = new TcpRegistry("test");
        registry.registerCodec(new TestMessageCodec());
        processor = new TestMessageProcessor(1);
        registry.registerProcessor(processor);

        // 创建并启动TCP服务器
        server = new TcpServer(registry);
        CompletableFuture.runAsync(() -> {
            try {
                server.start();
            } catch (Exception e) {
                logger.error("Failed to start TCP server", e);
            }
        });

        // 等待服务器启动完成
        Thread.sleep(2000);

        testClients = new ArrayList<>();
    }

    /**
     * 测试环境清理
     * 在所有测试方法执行完后运行
     * 关闭所有测试客户端和TCP服务器
     */
    @AfterAll
    static void tearDown() {
        // 关闭测试客户端
        for (Socket client : testClients) {
            try {
                client.close();
            } catch (IOException e) {
                logger.error("Error closing test client", e);
            }
        }

        // 关闭服务器
        if (server != null) {
            server.shutdown();
        }
    }

    /**
     * 创建测试客户端
     * @return 新创建的Socket客户端
     */
    private Socket createTestClient() throws IOException {
        Socket socket = new Socket();
        socket.connect(new InetSocketAddress("localhost", TEST_PORT));
        testClients.add(socket);
        return socket;
    }

    /**
     * 发送测试消息
     * 将消息编码并通过Socket发送
     * @param socket 客户端Socket
     * @param message 要发送的测试消息
     */
    private void sendMessage(Socket socket, TestMessage message) throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(1024);

        byte[] deviceIdBytes = message.getDeviceId().getBytes();
        byte[] content = message.getPayload();

        int totalLength = 12 + deviceIdBytes.length + content.length;

        buffer.putInt(totalLength);
        buffer.putInt(deviceIdBytes.length);
        buffer.put(deviceIdBytes);
        buffer.putInt(message.getMessageType());
        buffer.put(content);

        buffer.flip();
        socket.getOutputStream().write(buffer.array(), 0, buffer.limit());
        socket.getOutputStream().flush();
    }

    /**
     * 测试连接建立和消息发送
     * 验证服务器能够正确处理客户端连接和消息接收
     */
    @Test
    @Order(1)
    void testConnectionAndMessage() throws Exception {
        // 创建测试客户端并发送消息
        Socket client = createTestClient();
        TestMessage testMsg = new TestMessage("test-device-1", 1, "Hello, Server!");
        sendMessage(client, testMsg);

        // 等待消息处理
        assertTrue(processor.waitForMessages(5, TimeUnit.SECONDS));

        // 验证消息是否正确接收和处理
        List<Message> receivedMessages = processor.getReceivedMessages();
        assertEquals(1, receivedMessages.size());

        TestMessage receivedMsg = (TestMessage) receivedMessages.get(0);
        assertEquals("test-device-1", receivedMsg.getDeviceId());
        assertEquals(1, receivedMsg.getMessageType());
        assertEquals("Hello, Server!", new String(receivedMsg.getPayload()));
    }

    /**
     * 测试连接生命周期
     * 验证服务器能够正确处理客户端连接的建立和断开
     */
    @Test
    @Order(2)
    void testConnectionLifecycle() throws Exception {
        // 创建并关闭连接
        Socket client = createTestClient();
        TestMessage testMsg = new TestMessage("test-device-2", 1, "Connect");
        sendMessage(client, testMsg);

        Thread.sleep(1000); // 等待连接建立
        assertEquals("test-device-2", processor.getLastConnectedDevice());

        client.close();
        Thread.sleep(1000); // 等待连接断开处理
        assertEquals("test-device-2", processor.getLastDisconnectedDevice());
    }

    /**
     * 测试多客户端并发连接
     * 验证服务器能够正确处理多个客户端的并发连接和消息处理
     */
    @Test
    @Order(3)
    void testMultipleClients() throws Exception {
        int clientCount = 5;
        int initialMessageCount = processor.getReceivedMessages().size();
        
        List<Socket> clients = new ArrayList<>();
        // 创建多个客户端并发送消息
        for (int i = 0; i < clientCount; i++) {
            Socket client = createTestClient();
            clients.add(client);
            TestMessage testMsg = new TestMessage(
                "test-device-" + i,
                1,
                "Message from client " + i
            );
            sendMessage(client, testMsg);
            Thread.sleep(100); // 给一点时间间隔，避免消息发送太快
        }

        // 等待一段时间确保消息都被处理
        Thread.sleep(2000);
        
        // 验证接收到的消息数量
        List<Message> receivedMessages = processor.getReceivedMessages();
        assertEquals(initialMessageCount + clientCount, receivedMessages.size(), 
            "Incorrect number of messages received");
        
        // 验证每个设备的消息都被正确接收
        for (int i = 0; i < clientCount; i++) {
            final String deviceId = "test-device-" + i;
            assertTrue(
                receivedMessages.stream()
                    .skip(initialMessageCount)  // 跳过之前的消息
                    .anyMatch(msg -> msg.getDeviceId().equals(deviceId)),
                "Message from " + deviceId + " not found"
            );
        }
    }
} 