package cn.fengin.tiny.demo;

import cn.fengin.tiny.demo.http.RouterRegistry;
import cn.fengin.tiny.demo.tcp.message.CenterMessageCodec;
import cn.fengin.tiny.demo.tcp.processor.CenterMessageProcessor;
import cn.fengin.tiny.http.HttpServer;
import cn.fengin.tiny.tcp.TcpServer;
import cn.fengin.tiny.tcp.message.Message;
import cn.fengin.tiny.tcp.register.TcpRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import cn.fengin.tiny.tcp.ConnectionManager;

/**
 * 应用启动示例，包启HttpServer和TcpServer，实际使用过程需根据实际需求进行修改
 */
public class DemoApplication {
    private static final Logger logger = LoggerFactory.getLogger(DemoApplication.class);
    
    public static void main(String[] args) {
        try {
            // 设置运行环境，默认加载src/main/application.yaml配置
            // 可以在启动时由环境变量指定app.profile配置文件
            //System.setProperty("app.profile", "front");

            // 启动Web应用服务
            startHttpServer();

            // 启动TCP服务器
            startTcpServer();

        } catch (Exception e) {
            logger.error("Failed to start front application", e);
            System.exit(1);
        }
    }

    /**
     * 启动HTTP服务，根据实际需要启动步骤需要
     * 1、按需求情况是否注册http请求拦截器
     * 2、按实际情况注册路由
     * 3、启动服务
     */
    private static void startHttpServer() {
        //注册拦截器、路由信息
        RouterRegistry.register();
        // 启动HTTP服务器
        HttpServer httpServer = new HttpServer();
        new Thread(httpServer::start).start();
    }
    /**
     * 启动TCP服务，根据实际需要启动步骤需要
     * 1、注册消息编解码器
     * 2、注册业务处理器
     * 3、启动服务
     */
    private static void startTcpServer() {
        //启动TCP服务器，指定使用default的连接配置
        TcpRegistry registry = new TcpRegistry("default");
        //加载编解码器、处理器等驱动
        registry.registerCodec(new CenterMessageCodec())
                .registerProcessor(new CenterMessageProcessor());
        //启动服务
        TcpServer tcpServer = new TcpServer(registry);
        new Thread(tcpServer::start).start();
    }
} 