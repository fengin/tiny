package cn.fengin.tiny.demo.tcp.test;

import cn.fengin.tiny.demo.tcp.message.CenterMessage;
import cn.fengin.tiny.tcp.message.Message;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.*;

/**
 * 模拟集中器边缘设备，发送数据
 */
public class CenterClient {
    private Map<Socket,String> connectMap = new HashMap<>();
    //TCP服务器地址和端口
    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 8081;

    //这里运行客户端
    public static void main(String[] args) {
        CenterClient client = new CenterClient();
        //init方法只是为了测试启动异步心跳保活设备
        client.init();
        //以下发送设备数据消息
        try {
            String deviceId = "device1";
            //创建连接通道
            Socket socket = client.createTestClient(deviceId);
            //发送消息
            client.sendMessage(socket, new CenterMessage(deviceId, 1, "This is the demo device center test message,It is come from client!"));

            String deviceId2 = "device2";
            Socket socket2 = client.createTestClient(deviceId2);
            //发送消息
            client.sendMessage(socket2, new CenterMessage(deviceId2, 1, "This is the demo device center another test message,这是来自边缘设备!"));
            for(int i=0;i<60;i++){
                Thread.sleep(5000);
                client.testMore(i);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void testMore(int n) throws IOException {
        for(int i=0;i<500;i++){
            String deviceN = "deviceN" +n+i;
            Socket socketN = this.createTestClient(deviceN);
            this.sendMessage(socketN, new CenterMessage(deviceN, 1, "This is the demo device"+n+i+" center another test message,这是来自边缘设备!"));
        }
    }
    /**
     * 创建和服务器端的连接通道
     * @param deviceId
     * @return
     * @throws IOException
     */
    private Socket createTestClient(String deviceId) throws IOException {
        Socket socket = new Socket();
        socket.connect(new InetSocketAddress(SERVER_HOST, SERVER_PORT));
        connectMap.put(socket,deviceId);
        return socket;
    }

    private void init(){
        //心跳时间，50秒发送一次心跳，保活设备，正常一般1分钟
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                for(Map.Entry<Socket,String> entry:connectMap.entrySet()){
                    try {
                        sendMessage(entry.getKey(), createBeatMessage(entry.getValue()));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }, 5000, 50000);
    }


    /**
     * 往连接通道发送消息，写入数据结构协议服务器解码器需要一致
     * [总长度(4字节)][设备ID长度(4字节)][设备ID][消息类型(4字节)][消息内容]
     * @param socket
     * @param msg
     * @throws IOException
     */
    private void sendMessage(Socket socket, Message msg) throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(1024);

        byte[] deviceIdBytes = msg.getDeviceId().getBytes();
        byte[] content = msg.getPayload();

        int totalLength = 12 + deviceIdBytes.length + content.length;

        buffer.putInt(totalLength);
        buffer.putInt(deviceIdBytes.length);
        buffer.put(deviceIdBytes);
        buffer.putInt(msg.getMessageType());
        buffer.put(content);

        buffer.flip();
        socket.getOutputStream().write(buffer.array(), 0, buffer.limit());
        socket.getOutputStream().flush();
    }

    private  CenterMessage createBeatMessage(String deviceId) {
        return new CenterMessage(deviceId, 0, "beat message");
    }

}
