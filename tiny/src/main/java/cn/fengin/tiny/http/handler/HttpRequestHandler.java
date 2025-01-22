package cn.fengin.tiny.http.handler;

import cn.fengin.tiny.context.ApplicationContext;
import cn.fengin.tiny.http.Router;
import cn.fengin.tiny.http.HttpRequest;
import cn.fengin.tiny.http.interceptor.InterceptorChain;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;

import java.net.URI;
import java.util.Map;

/**
 * HTTP请求处理器
 * 处理HTTP请求，实现路由分发
 *
 * @author fengin
 * @since 1.0.0
 */
public class HttpRequestHandler extends SimpleChannelInboundHandler<FullHttpRequest> {
    private static final Logger logger = LoggerFactory.getLogger(HttpRequestHandler.class);
    private final ApplicationContext context;
    private final Map<String, String>  mimeTypes;
    public HttpRequestHandler(ApplicationContext context){
        this.context = context;
        this.mimeTypes = context.getStaticResourceConfig().getMimeTypes();
    }
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request) {
        // 记录请求信息
        logger.debug("Received HTTP request: {} {}", request.method(), request.uri());
        // 包装请求
        HttpRequest httpRequest = new HttpRequest(request);
        // 获取拦截器链
        InterceptorChain chain = Router.getInterceptorChain();
        
        try {
            // 如果是静态资源请求
            if (isStaticResource(httpRequest.getUri())) {
                new StaticResourceHandler(context).handle(ctx, httpRequest.getUri());
                return;
            }
            // 执行拦截器链
            if (chain.applyPreHandle(ctx, httpRequest)) {
                // 处理请求
                Router.handle(ctx, httpRequest);
                // 执行后置处理
                chain.applyPostHandle(ctx, httpRequest);
            }
        } catch (Exception e) {
            logger.warn("Error handling request: {}", e.getMessage());
            // 触发异常完成处理
            ctx.fireExceptionCaught(e);
            chain.triggerAfterCompletion(ctx, httpRequest, e);
        }
    }
    
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        logger.warn("Exception caught", cause.getCause());
        ctx.fireExceptionCaught(cause);
    }
    /**
     * 判断是否是静态资源请求
     */
    private boolean isStaticResource(String uri) {
        try {
            URI parsedUri = new URI(uri);
            String path = parsedUri.getPath(); // 去除查询参数
            for (String key : mimeTypes.keySet()) {
                if (path.endsWith(key)) {
                    return true;
                }
            }
        } catch (Exception e) {
            logger.error("Invalid URI: " + uri, e);
        }
        return false;
    }
} 