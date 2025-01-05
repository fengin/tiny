package cn.fengin.tiny.http;

import io.netty.channel.ChannelHandlerContext;


/**
 * 路由处理器接口
 * 定义HTTP请求处理的标准接口
 * 
 * 使用函数式接口设计，便于使用Lambda表达式注册路由处理器
 * 例如：
 * Router.get("/api/users", (ctx, req) -> {
 *     // 处理GET /api/users请求
 * });
 * 
 * 实现类需要处理以下职责：
 * 1. 解析请求参数
 * 2. 执行业务逻辑
 * 3. 返回响应结果
 * 4. 处理异常情况
 *
 * @author fengin
 * @since 1.0.0
 */
@FunctionalInterface
public interface RouteHandler {
    /**
     * 处理HTTP请求
     *
     * @param request HTTP请求对象，包含请求的所有信息
     * @param response HTTP响应对象，用于发送响应
     */
    void handle(HttpRequest request,HttpResponse response);
} 