package cn.fengin.tiny.demo.http.controller.admin;

import cn.fengin.tiny.http.HttpRequest;
import cn.fengin.tiny.http.HttpResponse;
import cn.fengin.tiny.http.HttpResponseUtil;
import io.netty.channel.ChannelHandlerContext;
import cn.fengin.tiny.demo.http.model.ApiResponse;
import java.util.HashMap;
import java.util.Map;

/**
 * 系统管理接口，示例用
 */
public class SystemController {
    /**
     * 系统状态数据接口 /api/admin/system/info
     */
    public void getInfo(HttpRequest request, HttpResponse response) {
        try {
            Map<String, Object> info = new HashMap<>();
            info.put("startTime", System.currentTimeMillis() - 86400000); // 1天前启动
            info.put("runTime", 86400000L);
            info.put("memory", 1024 * 1024 * 512L); // 512MB
            info.put("diskTotal", 1024 * 1024 * 1024 * 50L); // 50GB
            info.put("diskUsage", 1024 * 1024 * 1024 * 20L); // 20GB
            info.put("lastBeatTime", System.currentTimeMillis() - 60000); // 1分钟前
            info.put("ip", "243.22.19.83");
            info.put("httpPort", 8080);
            info.put("tcpPort", 8081);

            response.write(ApiResponse.success(info));
        } catch (Exception e) {
            response.write(ApiResponse.error(500, "获取系统信息失败"));
        }
    }

    /**
     * 读取前置机配置 /api/admin/system/config
     */
    public void getConfig(HttpRequest request,HttpResponse response) {
        try {
            Map<String, Object> config = new HashMap<>();
            config.put("remotAddr", "app.benzhitech.com");
            config.put("remotPort", 33221);
            config.put("remotSecret", "SLOIDKEFF0011FFeDEDSSEFD228FD");
            config.put("ip", "243.22.19.83");
            config.put("secret", "1D9eOIDKEFF0011FFeDEDS11FD228FD");

            response.write(ApiResponse.success(config));
        } catch (Exception e) {
            response.write(ApiResponse.error(500, "获取配置信息失败"));
        }
    }

    /**
     * 更新前置机配置 /api/admin/system/config/update
     */
    public void updateConfig(HttpRequest request,HttpResponse response) {
        try {
            // 模拟保存配置
            Map<String, Boolean> result = new HashMap<>();
            result.put("success", true);
            response.write(ApiResponse.success(result));
        } catch (Exception e) {
            response.write(ApiResponse.error(500, "更新配置失败"));
        }
    }

    /**
     * 健康检查接口 /api/admin/system/status
     */
    public void status(HttpRequest request,HttpResponse response) {
        Map<String, String> status = new HashMap<>();
        status.put("status", "ok");
        response.write(ApiResponse.success(status));
    }
}
