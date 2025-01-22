package cn.fengin.tiny.http.handler;

import cn.fengin.tiny.config.StaticResourceConfig;
import cn.fengin.tiny.context.ApplicationContext;
import cn.fengin.tiny.exception.HttpException;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;



/**
 * 静态资源处理器
 * 处理静态资源请求，支持资源缓存
 *
 * @author fengin
 * @since 1.0.0
 */
public class StaticResourceHandler extends ChannelInboundHandlerAdapter {
    private static final Logger logger = LoggerFactory.getLogger(StaticResourceHandler.class);
    
    private static final Map<String, byte[]> resourceCache = new ConcurrentHashMap<>();
    private final ApplicationContext context;


    public StaticResourceHandler(ApplicationContext context){
        this.context = context;
    }
    /**
     * 处理静态资源请求
     */
    public void handle(ChannelHandlerContext ctx, String uri) throws Exception{
        StaticResourceConfig config = context.getStaticResourceConfig();
        URI parsedUri = new URI(uri);// 去除查询参数
        String path = config.getPath() + parsedUri.getPath();

        byte[] content = null;
        // 如果启用缓存，先从缓存获取
        if (config.isCacheEnabled()) {
            content = resourceCache.get(path);
        }
        // 缓存未命中，从文件系统加载
        if (content == null) {
            content = loadResource(path);
            if (content != null && config.isCacheEnabled()) {
                resourceCache.put(path, content);
            }
        }

        if (content != null) {
            sendResource(ctx, content, getContentType(path));
        } else {
            throw new HttpException(404,"Resource not found");
        }

    }
    
    /**
     * 加载资源文件
     */
    private static byte[] loadResource(String path) throws Exception {
        URL url = StaticResourceHandler.class.getResource(path);
        if (url == null) {
            return null;
        }
        
        try (InputStream in = url.openStream();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
            return out.toByteArray();
        }
    }
    
    /**
     * 发送资源响应
     */
    private void sendResource(ChannelHandlerContext ctx, byte[] content, String contentType) {
        StaticResourceConfig config = context.getStaticResourceConfig();
        FullHttpResponse response = new DefaultFullHttpResponse(
            HttpVersion.HTTP_1_1, 
            HttpResponseStatus.OK,
            Unpooled.wrappedBuffer(content)
        );
        
        response.headers()
            .set(HttpHeaderNames.CONTENT_TYPE, contentType)
            .set(HttpHeaderNames.CONTENT_LENGTH, content.length);
            
        if (config.isCacheEnabled()) {
            response.headers().set(
                HttpHeaderNames.CACHE_CONTROL, 
                "max-age=" + config.getCacheMaxAge()
            );
        }
        
        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }
    
    /**
     * 获取内容类型
     */
    private String getContentType(String path) {
        StaticResourceConfig config = context.getStaticResourceConfig();
        String extension = getFileExtension(path);
        String contentType = config.getMimeTypes().get(extension);
        return contentType != null ? contentType : "application/octet-stream";
    }
    
    /**
     * 获取文件扩展名
     */
    private static String getFileExtension(String path) {
        int dotIndex = path.lastIndexOf('.');
        return dotIndex > 0 ? path.substring(dotIndex) : "";
    }
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        logger.error("Exception caught", cause.getCause());
        ctx.fireExceptionCaught(cause);
    }
} 