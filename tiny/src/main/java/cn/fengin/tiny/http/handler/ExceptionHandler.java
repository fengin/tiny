package cn.fengin.tiny.http.handler;

import cn.fengin.tiny.exception.HttpException;
import cn.fengin.tiny.exception.TinyException;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_LENGTH;
import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_TYPE;

/**
 * 全局异常处理器
 *
 * @author fengin
 * @since 1.0.0
 */
@ChannelHandler.Sharable
public class ExceptionHandler extends ChannelInboundHandlerAdapter {
    private static final Logger logger = LoggerFactory.getLogger(ExceptionHandler.class);

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        logger.error("Exception caught: {}", cause.getMessage());

        // 构建错误响应
        String errorMessage;
        int httpCode;
        if (cause instanceof TinyException) {
            TinyException te = (TinyException) cause;
            errorMessage = String.format("{\"code\":%d,\"message\":\"%s\"}", te.getCode(), te.getMessage());
            httpCode = (cause instanceof HttpException)?te.getCode():400;
        } else {
            errorMessage = "{\"code\":500,\"message\":\"Internal Server Error\"}";
            httpCode = 500;
        }

        byte[] bytes = errorMessage.getBytes();
        FullHttpResponse response = new DefaultFullHttpResponse(
            HttpVersion.HTTP_1_1,
            HttpResponseStatus.valueOf(httpCode)
        );

        response.headers().set(CONTENT_TYPE, "application/json").set(CONTENT_LENGTH, bytes.length);
        response.content().writeBytes(bytes);

        // 发送响应并关闭连接
        ctx.writeAndFlush(response)
           .addListener(future -> ctx.close());
    }
} 