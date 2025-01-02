package cn.fengin.tiny.demo.http.controller.admin;

import cn.fengin.tiny.config.AuthConfig;
import cn.fengin.tiny.context.ApplicationContext;
import cn.fengin.tiny.http.HttpRequest;
import cn.fengin.tiny.http.HttpResponseUtil;
import io.netty.channel.ChannelHandlerContext;
import cn.fengin.tiny.demo.http.model.ApiResponse;
import java.util.HashMap;
import java.util.Map;

/**
 * 用户管理接口，示例用
 */
public class UserController {

    /**
     * 登录接口 /api/admin/login
     */
    public void login(ChannelHandlerContext ctx, HttpRequest request) {
        try {
            // 解析登录参数
            // 解析登录数据
            String username = request.getParameter("username");
            String password = request.getParameter("password");
            AuthConfig.UserAuthConfig config = ApplicationContext.getInstance().getAuthConfig().getUserAuthConfig();

            // 验证用户名密码
            if (config.getUsername().equals(username) && config.getPassword().equals(password)) {
                Map<String, String> result = new HashMap<>();
                result.put("token", "mock-token-" + System.currentTimeMillis());
                HttpResponseUtil.send(ctx, ApiResponse.success(result));
            } else {
                HttpResponseUtil.send(ctx, ApiResponse.error(401, "用户名或密码错误"));
            }
        } catch (Exception e) {
            HttpResponseUtil.send(ctx, ApiResponse.error(500, "登录失败"));
        }
    }

    /**
     * 登出接口 /api/admin/logout
     */
    public void logout(ChannelHandlerContext ctx, HttpRequest request) {
        Map<String, Boolean> result = new HashMap<>();
        result.put("success", true);
        HttpResponseUtil.send(ctx, ApiResponse.success(result));
    }
}
