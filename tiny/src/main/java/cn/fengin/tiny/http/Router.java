package cn.fengin.tiny.http;

import cn.fengin.tiny.http.interceptor.Interceptor;
import cn.fengin.tiny.http.interceptor.InterceptorChain;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpMethod;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 路由处理器
 * 管理URL路由映射，支持不同HTTP方法的处理
 * 主要功能：
 * 1. 注册路由处理器
 * 2. 分发HTTP请求
 * 3. 支持RESTful风格API
 * 4. 线程安全的路由管理
 * 5. 管理请求拦截器
 *
 * @author fengin
 * @since 1.0.0
 */
public class Router {
    private static final Logger logger = LoggerFactory.getLogger(Router.class);
    
    /**
     * 路由映射表
     * key格式为 "METHOD:path"，例如 "GET:/api/users"
     * value为对应的路由处理器
     * 使用ConcurrentHashMap确保线程安全
     */
    private static final Map<String, RouteHandler> routes = new ConcurrentHashMap<>();
    
    /**
     * 拦截器链
     * -- GETTER --
     *  获取拦截器链

     */
    @Getter
    private static final InterceptorChain interceptorChain = new InterceptorChain();
    
    /**
     * 注册GET请求路由
     */
    public static void get(String path, RouteHandler handler) {
        addRoute(HttpMethod.GET, path, handler);
    }
    
    /**
     * 注册POST请求路由
     */
    public static void post(String path, RouteHandler handler) {
        addRoute(HttpMethod.POST, path, handler);
    }
    
    /**
     * 注册PUT请求路由
     */
    public static void put(String path, RouteHandler handler) {
        addRoute(HttpMethod.PUT, path, handler);
    }
    
    /**
     * 注册DELETE请求路由
     */
    public static void delete(String path, RouteHandler handler) {
        addRoute(HttpMethod.DELETE, path, handler);
    }
    
    /**
     * 添加拦截器
     */
    public static void addInterceptor(Interceptor interceptor) {
        interceptorChain.addInterceptor(interceptor);
        logger.info("Interceptor registered: {}", interceptor.getClass().getSimpleName());
    }

    /**
     * 添加路由
     */
    private static void addRoute(HttpMethod method, String path, RouteHandler handler) {
        String route = method.name() + ":" + path;
        routes.put(route, handler);
        logger.info("Route registered: {} {}", method, path);
    }
    
    /**
     * 处理HTTP请求
     */
    public static void handle(ChannelHandlerContext ctx, HttpRequest request) {
        String route = request.getMethod().name() + ":" + request.getUri().split("\\?")[0];
        RouteHandler handler = routes.get(route);
        
        if (handler != null) {
            // 执行路由处理器
            handler.handle(request,new HttpResponse(ctx));
        } else {
            // 未找到路由，返回404
            logger.warn("No route found for: {} {}", request.getMethod().name(), request.getUri());
            HttpResponseUtil.sendNotFound(ctx);
        }
    }
} 