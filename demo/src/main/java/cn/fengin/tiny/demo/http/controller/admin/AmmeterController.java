package cn.fengin.tiny.demo.http.controller.admin;

import cn.fengin.tiny.http.HttpRequest;
import cn.fengin.tiny.http.HttpResponseUtil;
import io.netty.channel.ChannelHandlerContext;
import cn.fengin.tiny.demo.http.model.ApiResponse;
import java.util.*;

/**
 * 电表管理接口，示例用
 */
public class AmmeterController {

    /**
     * 电表列表 /api/admin/ammeter/list
     */
    public void list(ChannelHandlerContext ctx, HttpRequest request) {
        try {
            // 模拟分页数据
            List<Map<String, Object>> ammeters = new ArrayList<>();
            for (int i = 0; i < 12; i++) {
                Map<String, Object> ammeter = new HashMap<>();
                ammeter.put("ammeterNo", "19152100" + (100000 + i));
                ammeter.put("type", "DDZY601");
                ammeter.put("status", i % 4 == 0 ? "offline" : "online");
                ammeter.put("valveStatus", i % 2 == 0 ? "开阀" : "关阀");
                ammeter.put("readTime", System.currentTimeMillis() - i * 60000);
                ammeters.add(ammeter);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("data", ammeters);
            response.put("total", 150);

            HttpResponseUtil.send(ctx, ApiResponse.success(response));
        } catch (Exception e) {
            HttpResponseUtil.send(ctx, ApiResponse.error(500, "获取电表列表失败"));
        }
    }

    /**
     * 电表详情 /api/admin/ammeter/detail
     */
    public void detail(ChannelHandlerContext ctx, HttpRequest request) {
        try {
            String ammeterNo = request.getParameter("ammeterNo");
            
            Map<String, Object> detail = new HashMap<>();
            detail.put("ammeterNo", ammeterNo);
            detail.put("cat", "单相费控智能表");
            detail.put("type", "DDZY601");
            detail.put("psw", "123456");
            detail.put("comPort", "COM1");
            detail.put("baudRate", "9600");
            detail.put("protocol", "DL/T645-2007");
            detail.put("status", "online");
            detail.put("valveStatus", "开阀");
            detail.put("readTime", System.currentTimeMillis() - 300000);
            detail.put("peek", "123.45");
            detail.put("sharp", "234.56");
            detail.put("flat", "345.67");
            detail.put("low", "456.78");

            HttpResponseUtil.send(ctx, ApiResponse.success(detail));
        } catch (Exception e) {
            HttpResponseUtil.send(ctx, ApiResponse.error(500, "获取电表详情失败"));
        }
    }

    /**
     * 批量采集 /api/admin/ammeter/read
     */
    public void read(ChannelHandlerContext ctx, HttpRequest request) {
        try {
            Map<String, Boolean> result = new HashMap<>();
            result.put("success", true);
            HttpResponseUtil.send(ctx, ApiResponse.success(result));
        } catch (Exception e) {
            HttpResponseUtil.send(ctx, ApiResponse.error(500, "批量采集请求失败"));
        }
    }
}
