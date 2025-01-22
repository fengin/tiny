package cn.fengin.tiny.test.http;

import cn.fengin.tiny.config.ServerConfig;
import cn.fengin.tiny.context.ApplicationContext;
import cn.fengin.tiny.http.*;
import com.fasterxml.jackson.databind.util.JSONPObject;
import io.netty.channel.ChannelHandlerContext;
import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;

/**
 * HTTP服务器单元测试类
 * 测试HTTP服务器的基本功能，包括GET请求、POST请求、参数处理和错误处理
 * 使用JUnit 5的@TestMethodOrder注解确保测试按顺序执行
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class HttpServerTest {
    private static final Logger logger = LoggerFactory.getLogger(HttpServerTest.class);
    private static HttpServer server;
    private static final int TEST_PORT = 18081;
    /**
     * 测试控制器类
     * 包含所有测试端点的处理方法
     */
    public static class TestController {
        /**
         * 处理基本GET请求
         */
        public void test(HttpRequest request, HttpResponse response) {
            response.write("Hello, World!");
        }

        /**
         * 处理带参数的GET请求
         */
        public void echo(HttpRequest request, HttpResponse response) {
            String message = request.getParameter("message");
            response.write(message);
        }

        /**
         * 处理POST请求和JSON数据
         */
        public void handleData(HttpRequest request,HttpResponse response) {
            String message = request.getParameter("message");
            response.write(message+" processed");
        }
    }

    /**
     * 注册测试路由
     * 设置测试所需的HTTP端点
     */
    private static void registerTestRoutes() {
        TestController controller = new TestController();
        
        // 注册GET请求路由
        Router.get("/test", controller::test);
        Router.get("/echo", controller::echo);
        
        // 注册POST请求路由
        Router.post("/data", controller::handleData);
    }

    /**
     * 测试环境初始化
     * 在所有测试方法执行之前运行
     * 配置并启动HTTP服务器，创建HTTP客户端
     */
    @BeforeAll
    static void setup() throws Exception {
        // 创建HTTP服务器配置
        System.setProperty("app.config", "application-template.yaml");
        ApplicationContext context = ApplicationContext.getInstance();
        ServerConfig.HttpConfig config = context.getServerConfig().getHttp();
        config.setPort(TEST_PORT);
        config.setBossThreads(1);
        config.setWorkerThreads(2);
        config.setBacklog(128);
        config.setKeepAlive(true);

        // 注册测试路由
        registerTestRoutes();

        // 创建并启动HTTP服务器
        server = new HttpServer();
        CompletableFuture.runAsync(() -> {
            try {
                server.start();
            } catch (Exception e) {
                logger.error("Failed to start HTTP server", e);
            }
        });

        // 等待服务器启动完成
        Thread.sleep(2000);
    }

    /**
     * 测试环境清理
     * 在所有测试方法执行完后运行
     * 关闭HTTP服务器
     */
    @AfterAll
    static void tearDown() {
        if (server != null) {
            server.stop();
        }
    }

    /**
     * 发送HTTP请求并获取响应
     * @param urlStr 请求URL
     * @param method 请求方法
     * @param requestBody 请求体（可选）
     * @return 响应内容
     */
    private String sendRequest(String urlStr, String method, String requestBody) throws IOException {
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod(method);
        
        if (requestBody != null) {
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "application/json");
            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = requestBody.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }
        }

        int responseCode = conn.getResponseCode();
        if (responseCode == 404) {
            return null;
        }

        StringBuilder response = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) {
                response.append(line);
            }
        }

        return response.toString();
    }

    /**
     * 测试基本的GET请求
     * 验证服务器能够正确处理简单的GET请求并返回预期响应
     */
    @Test
    @Order(1)
    void testGetRequest() throws Exception {
        String response = sendRequest("http://localhost:" + TEST_PORT + "/test", "GET", null);
        assertEquals("Hello, World!", response);
    }

    /**
     * 测试带参数的GET请求
     * 验证服务器能够正确处理URL查询参数并返回预期响应
     */
    @Test
    @Order(2)
    void testGetWithParams() throws Exception {
        String testMessage = "Hello-from-test";
        String response = sendRequest("http://localhost:" + TEST_PORT + "/echo?message=" + testMessage, "GET", null);
        assertEquals(testMessage, response);
    }

    /**
     * 测试POST请求和JSON处理
     * 验证服务器能够正确处理POST请求、JSON请求体的序列化和反序列化
     */
    @Test
    @Order(3)
    void testPostRequest() throws Exception {
        String json = "{\"message\":\"test message\"}";
        String response = sendRequest("http://localhost:" + TEST_PORT + "/data", "POST", json);
        assertTrue(response.contains("test message processed"));
    }

    /**
     * 测试404错误处理
     * 验证服务器能够正确处理访问不存在的路径的情况
     */
    @Test
    @Order(4)
    void test404NotFound() throws Exception {
        URL url = new URL("http://localhost:" + TEST_PORT + "/nonexistent");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        assertEquals(404, conn.getResponseCode());
    }
} 