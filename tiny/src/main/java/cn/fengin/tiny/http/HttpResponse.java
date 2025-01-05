package cn.fengin.tiny.http;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpResponseStatus;

public class HttpResponse {
    private ChannelHandlerContext ctx;
    public HttpResponse(ChannelHandlerContext ctx){
        this.ctx = ctx;
    }
    /**
     * 发送响应
     * @param str
     */
    public void write(String str){
        HttpResponseUtil.send(ctx, str);
    }

    /**
     * 发送HTTP异常响应
     * @param status
     */
    public void writeHttpError(HttpResponseStatus status){
        HttpResponseUtil.sendError(ctx,status);
    }

    /**
     * 发送400错误响应
     * @param message
     */
    public void writeBadRequest(String message){
        HttpResponseUtil.sendBadRequest(ctx,message);
    }
    /**
     * 发送401错误响应
     */
    public void writeUnauthorized(){
        HttpResponseUtil.sendUnauthorized(ctx);
    }
}
