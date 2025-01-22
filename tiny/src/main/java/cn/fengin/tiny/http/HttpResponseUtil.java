package cn.fengin.tiny.http;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;

import java.util.Map;

/**
 * HTTP响应工具类
 * 提供常用的HTTP响应处理方法
 *
 * @author fengin
 * @since 1.0.0
 */
public class HttpResponseUtil {
    
    /**
     * 发送JSON响应
     */
    public static void send(ChannelHandlerContext ctx, String json, Map<String,String> headers) {
        FullHttpResponse response = new DefaultFullHttpResponse(
            HttpVersion.HTTP_1_1, 
            HttpResponseStatus.OK,
            Unpooled.copiedBuffer(json==null?"":json, CharsetUtil.UTF_8)
        );
        
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "application/json; charset=UTF-8");
        setHeaders(headers, response);
        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }

    private static void setHeaders(Map<String, String> headers, FullHttpResponse response) {
        if(headers!=null){
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                response.headers().set(entry.getKey(), entry.getValue());
            }
        }
        response.headers().set(HttpHeaderNames.CONTENT_LENGTH, response.content().readableBytes());
    }

    public static void sendFile(ChannelHandlerContext ctx, byte[] file, String contentType, Map<String,String> headers) {
        FullHttpResponse response = new DefaultFullHttpResponse(
            HttpVersion.HTTP_1_1,
            HttpResponseStatus.OK,
            Unpooled.wrappedBuffer(file)
        );

        response.headers().set(HttpHeaderNames.CONTENT_TYPE,contentType);
        setHeaders(headers, response);
        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }
    /**
     * 发送404响应
     */
    public static void sendNotFound(ChannelHandlerContext ctx) {
        sendError(ctx, HttpResponseStatus.NOT_FOUND);
    }
    
    /**
     * 发送500错误响应
     */
    public static void sendError(ChannelHandlerContext ctx, HttpResponseStatus status) {
        FullHttpResponse response = new DefaultFullHttpResponse(
            HttpVersion.HTTP_1_1, status,
            Unpooled.copiedBuffer(status.toString(), CharsetUtil.UTF_8)
        );
        
        response.headers()
            .set(HttpHeaderNames.CONTENT_TYPE, "text/plain; charset=UTF-8")
            .set(HttpHeaderNames.CONTENT_LENGTH, response.content().readableBytes());
        
        ctx.writeAndFlush(response).addListener(future -> ctx.close());
    }
    
    /**
     * 发送401响应
     */
    public static void sendUnauthorized(ChannelHandlerContext ctx) {
        sendError(ctx, HttpResponseStatus.UNAUTHORIZED);
    }
    
    /**
     * 发送400响应
     */
    public static void sendBadRequest(ChannelHandlerContext ctx, String message) {
        FullHttpResponse response = new DefaultFullHttpResponse(
            HttpVersion.HTTP_1_1, 
            HttpResponseStatus.BAD_REQUEST,
            Unpooled.copiedBuffer(message, CharsetUtil.UTF_8)
        );
        
        response.headers()
            .set(HttpHeaderNames.CONTENT_TYPE, "text/plain; charset=UTF-8")
            .set(HttpHeaderNames.CONTENT_LENGTH, response.content().readableBytes());
        
        ctx.writeAndFlush(response).addListener(future -> ctx.close());
    }
} 