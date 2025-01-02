package cn.fengin.tiny.demo.http.controller.admin;

import cn.fengin.tiny.http.HttpRequest;
import cn.fengin.tiny.http.HttpResponseUtil;
import io.netty.channel.ChannelHandlerContext;
import cn.fengin.tiny.demo.http.model.ApiResponse;
import java.util.*;

/**
 * 集中器管理接口，示例用
 */
public class CenterController {

    /**
     * 集中器列表 /api/admin/center/list
     */
    public void list(ChannelHandlerContext ctx, HttpRequest request) {
        try {
            // 模拟分页数据
            List<Map<String, Object>> centers = new ArrayList<>();
            for (int i = 0; i < 12; i++) {
                Map<String, Object> center = new HashMap<>();
                center.put("name", "三楼号"+(i+1)+"层集中器");
                center.put("status", i % 3 == 0 ? "offline" : "online");
                center.put("type", "376国网集中器");
                center.put("addr", "10111" + (10000 + i));
                center.put("ammeterNum", 50);
                
                Map<String, Integer> ammeter = new HashMap<>();
                ammeter.put("online", 48);
                ammeter.put("offline", 2);
                center.put("ammeter", ammeter);
                
                centers.add(center);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("data", centers);
            response.put("total", 100);

            HttpResponseUtil.send(ctx, ApiResponse.success(response));
        } catch (Exception e) {
            HttpResponseUtil.send(ctx, ApiResponse.error(500, "获取集中器列表失败"));
        }
    }
}
