package cn.fengin.tiny.http.interceptor;

import cn.fengin.tiny.http.HttpRequest;
import cn.fengin.tiny.http.HttpResponse;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * 拦截器链
 * 管理和执行多个拦截器
 *
 * @author fengin
 * @since 1.0.0
 */
public class InterceptorChain {
    private static final Logger logger = LoggerFactory.getLogger(InterceptorChain.class);
    
    private final List<Interceptor> interceptors = new ArrayList<>();
    
    /**
     * 添加拦截器，getOrder值越小，执行顺序越靠前
     */
    public void addInterceptor(Interceptor interceptor) {
        interceptors.add(interceptor);
        interceptors.sort(Comparator.comparingInt(Interceptor::getOrder));
    }
    
    /**
     * 执行前置处理
     */
    public boolean applyPreHandle(ChannelHandlerContext ctx, HttpRequest request) {
        for (Interceptor interceptor : interceptors) {
            try {
                if (!interceptor.preHandle(request,new HttpResponse(ctx))) {
                    return false;
                }
            } catch (Exception e) {
                logger.error("Error in interceptor preHandle", e);
                return false;
            }
        }
        return true;
    }
    
    /**
     * 执行后置处理
     */
    public void applyPostHandle(ChannelHandlerContext ctx, HttpRequest request) {
        for (int i = interceptors.size() - 1; i >= 0; i--) {
            try {
                interceptors.get(i).postHandle(request,new HttpResponse(ctx));
            } catch (Exception e) {
                logger.error("Error in interceptor postHandle", e);
            }
        }
    }
    
    /**
     * 触发异常完成处理
     */
    public void triggerAfterCompletion(ChannelHandlerContext ctx, HttpRequest request, Exception ex) {
        for (int i = interceptors.size() - 1; i >= 0; i--) {
            try {
                interceptors.get(i).afterCompletion(request,new HttpResponse(ctx), ex);
            } catch (Exception e) {
                logger.error("Error in interceptor afterCompletion", e);
            }
        }
    }
} 