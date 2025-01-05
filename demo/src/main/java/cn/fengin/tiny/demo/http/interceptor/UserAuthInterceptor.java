package cn.fengin.tiny.demo.http.interceptor;

import cn.fengin.tiny.http.HttpResponse;
import cn.fengin.tiny.http.interceptor.Interceptor;
import cn.fengin.tiny.http.HttpRequest;
import cn.fengin.tiny.http.HttpResponseUtil;
import io.netty.channel.ChannelHandlerContext;

import java.util.ArrayList;
import java.util.List;

/**
 * 用户认证拦截器
 * 处理需要用户登录的页面和API，一般用于本地页面访问接口
 */
public class UserAuthInterceptor implements Interceptor {


    private List<String> ignorePaths = new ArrayList<String>();

    public void setIgnorePaths(List<String> ignorePaths){
        this.ignorePaths = ignorePaths;
    }
    
    @Override
    public boolean preHandle(HttpRequest request,HttpResponse response) {
        // 登录页面和登录接口不需要验证
        if (isIgnore(request.getPath())) {
            return true;
        }
        
        // 验证token
        String token = request.getHeader("Authorization");
        if (token == null) {
            response.writeUnauthorized();
            return false;
        }
        
        // 验证token并解析用户信息
        try {
            String actualToken = token.substring(7);
            //TODO 业务处理验证用户权限
            request.setAttribute("user", actualToken);
            return true;
        } catch (Exception e) {
            response.writeUnauthorized();
            return false;
        }
    }
    
    @Override
    public void postHandle(HttpRequest request,HttpResponse response) {
        // 不需要后置处理
    }
    @Override
    public int getOrder() {
        return 100; // 用户鉴权优先级较低
    }

    public boolean isIgnore(String path){
        for (String pattern : ignorePaths) {
            if (isMatch(path, pattern)) {
                return true;
            }
        }
        return false;
    }
    private boolean isMatch(String requestPath, String pattern) {
        if (pattern.startsWith("**") && pattern.endsWith("**")) {
            // 匹配中间部分
            String middlePattern = pattern.substring(2, pattern.length() - 2);
            return requestPath.contains(middlePattern);
        } else if (pattern.startsWith("**")) {
            // 匹配结尾部分
            String endPattern = pattern.substring(2);
            return requestPath.endsWith(endPattern);
        } else if (pattern.endsWith("**")) {
            // 匹配开头部分
            String startPattern = pattern.substring(0, pattern.length() - 2);
            return requestPath.startsWith(startPattern);
        } else {
            // 全匹配
            return requestPath.equals(pattern);
        }
    }
} 