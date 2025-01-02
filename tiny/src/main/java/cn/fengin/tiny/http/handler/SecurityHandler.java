package cn.fengin.tiny.http.handler;

import cn.fengin.tiny.context.ApplicationContext;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 安全处理器
 * 处理请求安全检查,处理请求资源限制
 *
 * @author fengin
 * @since 1.0.0
 */
public class SecurityHandler extends ChannelInboundHandlerAdapter {
    private static final Logger logger = LoggerFactory.getLogger(SecurityHandler.class);
    private final ApplicationContext context;
    private long currentRequestSize = 0;

    public SecurityHandler(ApplicationContext context){
        this.context = context;
    }
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        String remoteIp = "";
        try {
            if (msg instanceof HttpRequest) {
                // 重置请求大小计数器
                currentRequestSize = 0;
                HttpRequest request = (HttpRequest) msg;
                remoteIp = ctx.channel().remoteAddress().toString();
                // 执行安全检查
                context.getSecurityManager().checkRequest(request, remoteIp);
                logger.debug("Security check passed for IP: {}", remoteIp);
            }
            //处理请求资源限制
            if (msg instanceof HttpContent) {
                HttpContent content = (HttpContent) msg;
                ByteBuf buf = content.content();
                // 更新请求大小并检查内存使用
                int readable = buf.readableBytes();
                currentRequestSize += readable;
                context.getSecurityManager().allocateMemory(readable);
                logger.debug("Allocated {} bytes for request, total: {}", readable, currentRequestSize);
            }
            //继续往下个handler传播消息
            ctx.fireChannelRead(msg);
        } catch (Exception e) {
            logger.warn("Security check failed for IP: {}", remoteIp,e.getCause());
            //不再调用 ctx.fireChannelRead(msg)，阻止消息继续传播,直接传播异常
            ctx.fireExceptionCaught(e);
        }
    }
    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        // 释放资源
        if (currentRequestSize > 0) {
            context.getSecurityManager().freeMemory(currentRequestSize);
            logger.debug("Freed {} bytes on channel inactive", currentRequestSize);
        }
        ctx.fireChannelInactive();
    }
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        // 发生异常时释放资源
        if (currentRequestSize > 0) {
            context.getSecurityManager().freeMemory(currentRequestSize);
            logger.debug("Freed {} bytes on exception", currentRequestSize);
        }
        ctx.fireExceptionCaught(cause);
    }
} 