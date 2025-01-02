package cn.fengin.tiny;

import cn.fengin.tiny.http.HttpServer;
import cn.fengin.tiny.http.Router;
import cn.fengin.tiny.tcp.TcpServer;
import cn.fengin.tiny.tcp.register.TcpRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import cn.fengin.tiny.tcp.message.Message;
import cn.fengin.tiny.tcp.message.MessageCodec;
import cn.fengin.tiny.tcp.message.MessageProcessor;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;

/**
 * 这只是一个启动示例类，具体看demo工程的启动方式
 */
public class App {
    private static final Logger logger = LoggerFactory.getLogger(App.class);
    public static void main(String[] args) {
        try {
            // 设置运行环境
            //System.setProperty("app.profile", "front");
            long start = System.currentTimeMillis();
            //启动HTTP服务器    
            startHttpServer();

            //启动TCP服务器
            startTcpServer();

            logger.info("Front application started successfully, cost time:{}ms", System.currentTimeMillis() - start);
        } catch (Exception e) {
            logger.error("Failed to start front application", e);
            System.exit(1);
        }
    }
    //启动HTTP服务器
    private static void startHttpServer(){
        //注册拦截器、路由信息
        //1.注册拦截器
        //Router.addInterceptor(new ApiAuthInterceptor());
        //初始化服务和控制器
        //UserController userController = new UserController();
        //注册接口
        //Router.post("/api/login", userController::login);
       HttpServer httpServer = new HttpServer();
       new Thread(httpServer::start).start();
    }
    
    private static void startTcpServer() {
 
        // 启动TCP服务器
        TcpRegistry registry = new TcpRegistry("default");
        // 注册消息编解码器
        registry.registerCodec(new MessageCodec() {
            @Override
            public Message decode(ByteBuf in) throws Exception {
                // 解码消息处理逻辑
                return null;
            }

            @Override
            public void encode(Message msg, ByteBuf out) throws Exception {
                // 编码消息处理逻辑
            }
        });
        // 注册消息处理器
        registry.registerProcessor(new MessageProcessor() {
            @Override
            public void process(Message msg, Channel channel) {
                logger.info("Received message: {}", msg);
            }
        });
        //启动TCP服务器
        TcpServer tcpServer = new TcpServer(registry);
        new Thread(tcpServer::start).start();
    }
} 