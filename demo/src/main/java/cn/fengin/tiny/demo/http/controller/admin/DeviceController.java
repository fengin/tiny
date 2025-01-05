package cn.fengin.tiny.demo.http.controller.admin;

import cn.fengin.tiny.http.HttpRequest;
import cn.fengin.tiny.http.HttpResponse;
import cn.fengin.tiny.http.HttpResponseUtil;
import io.netty.channel.ChannelHandlerContext;
import cn.fengin.tiny.demo.http.model.ApiResponse;
import java.util.*;

/**
 * 设备管理接口，示例用
 */
public class DeviceController {

    /**
     * 设备总体信息 /api/admin/device/info
     */
    public void getInfo(HttpRequest request, HttpResponse response) {
        try {
            Map<String, Object> info = new HashMap<>();
            
            // 集中器状态
            Map<String, Integer> center = new HashMap<>();
            center.put("online", 8);
            center.put("offline", 2);
            info.put("center", center);
            
            // 电表状态
            Map<String, Integer> ammeter = new HashMap<>();
            ammeter.put("online", 150);
            ammeter.put("offline", 20);
            info.put("ammeter", ammeter);
            
            // 采集状态
            info.put("lastReadTime", System.currentTimeMillis() - 300000); // 5分钟前
            info.put("ok", 145);
            info.put("failed", 25);

            response.write(ApiResponse.success(info));
        } catch (Exception e) {
            response.write(ApiResponse.error(500, "获取设备信息失败"));
        }
    }

    /**
     * 拉取设备配置 /api/admin/device/config/pull
     */
    public void pullConfig(HttpRequest request, HttpResponse response) {
        try {
            Map<String, Boolean> result = new HashMap<>();
            result.put("success", true);
            response.write(ApiResponse.success(result));
        } catch (Exception e) {
            response.write(ApiResponse.error(500, "拉取设备配置失败"));
        }
    }
}
