package cn.fengin.tiny.http;

import cn.fengin.tiny.context.ApplicationContext;
import cn.fengin.tiny.http.handler.ExceptionHandler;
import cn.fengin.tiny.http.handler.HttpRequestHandler;
import cn.fengin.tiny.http.handler.SecurityHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.handler.timeout.IdleStateHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

/**
 * HTTP服务器初始化器
 * 配置HTTP处理管道，包含：
 * 1. 基础HTTP编解码
 * 2. 空闲连接检测
 * 3. 安全处理器
 * 4. 添加业务处理器（包含静态资源处理和路由分发）
 * 5. 添加异常处理器（需要前面的Handler出现异常都fire出来），统一异常处理
 * 6.记录连接信息，用于安全检查
 *
 * @author fengin
 * @since 1.0.0
 */
public class HttpServerInitializer extends ChannelInitializer<SocketChannel> {
    private static final Logger logger = LoggerFactory.getLogger(HttpServerInitializer.class);
    private static final int MAX_CONTENT_LENGTH = 65536;
    private static final int IDLE_TIMEOUT_SECONDS = 300; // 5分钟超时

    private final ApplicationContext context = ApplicationContext.getInstance();

    /**
     * 入站事件（Inbound）的处理顺序是从前到后
     * 出站事件（Outbound）的处理顺序是从后到前
     */
    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();

        // 1. 添加基础HTTP编解码器
        pipeline.addLast(new HttpServerCodec());
        pipeline.addLast(new HttpObjectAggregator(MAX_CONTENT_LENGTH));
        pipeline.addLast(new ChunkedWriteHandler());
        
        // 2. 添加空闲连接检测
        pipeline.addLast(new IdleStateHandler(IDLE_TIMEOUT_SECONDS, 0, 0, TimeUnit.SECONDS));

        // 3. 添加安全处理器
        pipeline.addLast(new SecurityHandler(context));

        // 4. 添加业务处理器（包含静态资源处理和路由分发）
        pipeline.addLast(new HttpRequestHandler(context));

        // 5. 添加异常处理器（需要前面的Handler出现异常都fire出来），统一异常处理
        pipeline.addLast(new ExceptionHandler());

        // 6.记录连接信息，用于安全检查
        String remoteIp = ch.remoteAddress().getAddress().getHostAddress();

        context.getSecurityManager().onNewConnection(remoteIp);
        ch.closeFuture().addListener(future -> context.getSecurityManager().onConnectionClosed(remoteIp));

        logger.debug("Initialized HTTP channel pipeline for client: {}", remoteIp);
    }
} 