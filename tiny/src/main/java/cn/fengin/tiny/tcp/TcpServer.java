package cn.fengin.tiny.tcp;

import cn.fengin.tiny.config.ServerConfig;
import cn.fengin.tiny.context.ApplicationContext;
import cn.fengin.tiny.tcp.register.TcpRegistry;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * TCP服务器
 * 负责创建和管理基于Netty的TCP服务器实例
 */
public class TcpServer {
    private static final Logger logger = LoggerFactory.getLogger(TcpServer.class);

    /** TCP服务器配置，从application.yaml中加载 */
    private ServerConfig.TcpServerConfig config;
    
    /** TCP服务注册器，包含服务名称、编解码器和消息处理器 */
    private final TcpRegistry registry;
    
    /** Netty的Boss线程组，用于接收连接 */
    private EventLoopGroup bossGroup;
    
    /** Netty的Worker线程组，用于处理IO */
    private EventLoopGroup workerGroup;
    
    /** 服务器Channel */
    private Channel serverChannel;
    
    /**
     * 构造TCP服务器
     * @param registry TCP服务注册器，包含服务配置和处理组件
     */
    public TcpServer(TcpRegistry registry) {
        this.registry = registry;
    }
    
    /**
     * 启动TCP服务器
     * 1. 验证注册器配置
     * 2. 加载服务器配置
     * 3. 初始化Netty组件
     * 4. 启动服务器并等待关闭
     */
    public void start() {
        logger.info("Starting TCP server...");
        try {
            // 验证注册器配置是否完整
            registry.validate();
            
            // 从配置中获取对应名称的服务器配置
            ServerConfig serverConfig = ApplicationContext.getInstance().getServerConfig();
            for (ServerConfig.TcpServerConfig tcpServerConfig : serverConfig.getTcpServers()) {
                if (registry.getServerName().equals(tcpServerConfig.getName())) {
                    this.config = tcpServerConfig;
                    break;
                }
            }
            
            if (this.config == null) {
                throw new RuntimeException("No TCP server configuration found for name: " + registry.getServerName());
            }
            
            // 配置Netty线程组
            bossGroup = new NioEventLoopGroup(config.getBossThreads());
            workerGroup = new NioEventLoopGroup(config.getWorkerThreads());
            // 创建并配置ServerBootstrap
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .option(ChannelOption.SO_BACKLOG, config.getBacklog())
                .option(ChannelOption.SO_REUSEADDR, true)
                .childOption(ChannelOption.SO_KEEPALIVE, true)
                .childOption(ChannelOption.TCP_NODELAY, true)
                .childHandler(new TcpServerInitializer(registry.getCodec(), registry.getProcessor(),config));
            // 绑定端口并启动服务器
            ChannelFuture future = bootstrap.bind(config.getPort()).sync();
            serverChannel = future.channel();
            logger.info("TCP server {} started on port {}, cost time {} ms", config.getName(),config.getPort(), System.currentTimeMillis() - ApplicationContext.startTime);
            // 等待服务器关闭
            serverChannel.closeFuture().sync();
        } catch (Exception e) {
            logger.error("Failed to start TCP server " + registry.getServerName(), e);
            throw new RuntimeException("Failed to start TCP server", e);
        }
    }
    
    /**
     * 关闭TCP服务器
     * 1. 关闭服务器Channel
     * 2. 优雅关闭线程组
     */
    public void shutdown() {
        if (serverChannel != null) {
            serverChannel.close();
        }
        if (bossGroup != null) {
            bossGroup.shutdownGracefully();
        }
        if (workerGroup != null) {
            workerGroup.shutdownGracefully();
        }
    }
} 