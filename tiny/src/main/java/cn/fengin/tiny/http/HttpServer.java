package cn.fengin.tiny.http;

import cn.fengin.tiny.config.ServerConfig;
import cn.fengin.tiny.context.ApplicationContext;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * HTTP服务器
 * 基于Netty实现的HTTP服务器，采用主从Reactor模式
 *
 * @author fengin
 * @since 1.0.0
 */
public class HttpServer {
    private static final Logger logger = LoggerFactory.getLogger(HttpServer.class);
    
    private final ServerConfig.HttpConfig config;
    private final EventLoopGroup bossGroup;
    private final EventLoopGroup workerGroup;
    
    public HttpServer() {
        ServerConfig serverConfig = ApplicationContext.getInstance().getServerConfig();
        this.config = serverConfig.getHttp();
        this.bossGroup = new NioEventLoopGroup(config.getBossThreads());
        this.workerGroup = new NioEventLoopGroup(config.getWorkerThreads());
    }
    
    /**
     * 启动HTTP服务器
     * 配置并启动Netty服务器，采用链式调用方式配置参数
     */
    public void start() {
        logger.info("Starting HTTP server...");
        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, config.getBacklog())
                    .option(ChannelOption.SO_REUSEADDR, true)
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    .childOption(ChannelOption.TCP_NODELAY, true)
                    .childHandler(new HttpServerInitializer());

            ChannelFuture future = bootstrap.bind(config.getPort()).sync();
            logger.info("HTTP server started on port {}, cost time {} ms", config.getPort(), System.currentTimeMillis() - ApplicationContext.startTime);
            // 注册关闭钩子
            Runtime.getRuntime().addShutdownHook(new Thread(this::stop));
            
            // 等待服务器关闭
            future.channel().closeFuture().sync();
        } catch (Exception e) {
            logger.error("Failed to start HTTP server", e);
            stop();
        }
    }
    
    /**
     * 停止HTTP服务器
     * 优雅关闭，确保资源正确释放
     */
    public void stop() {
        try {
            if (bossGroup != null) {
                bossGroup.shutdownGracefully();
            }
            if (workerGroup != null) {
                workerGroup.shutdownGracefully();
            }
            logger.info("HTTP server stopped");
        } catch (Exception e) {
            logger.error("Error while stopping HTTP server", e);
        }
    }
} 