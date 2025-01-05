package cn.fengin.tiny.http.interceptor;

import cn.fengin.tiny.http.HttpRequest;
import cn.fengin.tiny.http.HttpResponse;
import io.netty.channel.ChannelHandlerContext;

/**
 * HTTP请求拦截器接口
 *
 * @author fengin
 * @since 1.0.0
 */
public interface Interceptor {
    /**
     * 前置处理
     * @return true继续处理，false中断请求
     */
    boolean preHandle(HttpRequest request,HttpResponse response);
    
    /**
     * 后置处理
     */
    void postHandle(HttpRequest request,HttpResponse response);
    
    /**
     * 完成处理，包括异常情况
     */
    default void afterCompletion(HttpRequest request,HttpResponse response, Exception ex) {}
    
    /**
     * 获取拦截器优先级，数字越小优先级越高
     */
    default int getOrder() {
        return 0;
    }
} 