package cn.fengin.tiny.http;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpResponseStatus;

import java.util.HashMap;
import java.util.Map;

public class HttpResponse {
    private ChannelHandlerContext ctx;
    private Map<String,String> headers = new HashMap<>();
    public HttpResponse(ChannelHandlerContext ctx){
        this.ctx = ctx;
    }

    /**
     * 设置响应头
     * @param key
     * @param value
     */
    public void setHeader(String key, String value){
        headers.put(key,value);
    }
    /**
     * 发送响应
     * @param str
     */
    public void write(String str){
        HttpResponseUtil.send(ctx, str,headers);
    }
    /**
     * 发送文件数据响应
     * @param file
     * @param contentType
     */
    public void writeFile(byte[] file, String contentType){
        HttpResponseUtil.sendFile(ctx, file, contentType,headers);
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
