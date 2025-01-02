package cn.fengin.tiny.demo.http.interceptor;


import cn.fengin.tiny.http.interceptor.Interceptor;
import cn.fengin.tiny.http.HttpRequest;
import cn.fengin.tiny.http.HttpResponseUtil;
import io.netty.channel.ChannelHandlerContext;
import cn.fengin.tiny.config.AuthConfig;
import cn.fengin.tiny.context.ApplicationContext;

/**
 * API认证拦截器示例
 * 处理系统间调用的API鉴权，一般用于开放接口
 */
public class ApiAuthInterceptor implements Interceptor {
    
    @Override
    public boolean preHandle(ChannelHandlerContext ctx, HttpRequest request) {
        // 只处理系统间调用的API
        if (!isOpenApi(request.getPath())) {
            return true;
        }
        
        // 验证API密钥
        String apiKey = request.getHeader("X-API-Key");
        if (!isValidApiKey(apiKey)) {
            HttpResponseUtil.sendUnauthorized(ctx);
            return false;
        }
        
        return true;
    }
    
    @Override
    public void postHandle(ChannelHandlerContext ctx, HttpRequest request) {
        // 不需要后置处理
    }
    
    @Override
    public int getOrder() {
        return 50; // API鉴权优先级高于用户鉴权
    }
    
    private boolean isOpenApi(String path) {
        return path.startsWith("/api/open/");
    }
    
    private boolean isValidApiKey(String apiKey) {
        AuthConfig authConfig = ApplicationContext.getInstance().getAuthConfig();
        return apiKey != null && apiKey.equals(authConfig.getApiAuthConfig().getKey());
    }
} 