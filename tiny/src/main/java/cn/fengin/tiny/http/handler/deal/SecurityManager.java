package cn.fengin.tiny.http.handler.deal;

import cn.fengin.tiny.config.SecurityConfig;
import cn.fengin.tiny.exception.SecurityException;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 安全管理器
 * 实现请求限流、IP黑名单等安全功能
 *
 * @author fengin
 * @since 1.0.0
 */
public class SecurityManager {
    private static final Logger logger = LoggerFactory.getLogger(SecurityManager.class);
    
    // IP访问计数器
    private final ConcurrentHashMap<String, AtomicInteger> ipConnectionCounter = new ConcurrentHashMap<>();
    
    // IP请求计数器
    private final ConcurrentHashMap<String, AtomicInteger> ipRequestCounter = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, AtomicLong> ipLastRequestTime = new ConcurrentHashMap<>();

    // 资源使用计数器
    private final AtomicLong currentMemoryUsage = new AtomicLong(0);

    // 配置
    private final SecurityConfig securityConfig;

    public SecurityManager(SecurityConfig securityConfig) {
        this.securityConfig = securityConfig;
    }
    
    /**
     * 检查请求是否允许
     */
    public void checkRequest(HttpRequest request, String remoteIp) {
        // 检查连接数
        checkConnectionLimit(remoteIp);
        
        // 检查请求频率
        checkRequestRate(remoteIp);
        
        // 检查Headers
        checkHeaders(request.headers());

        // 检查静态资源请求类型是否允许
        checkStaticResourceType(request.uri());

    }
    /**
     * 释放内存资源
     */
    public void freeMemory(long bytes) {
        currentMemoryUsage.addAndGet(-bytes);
    }
    /**
     * 获取当前内存使用情况
     */
    public long getCurrentMemoryUsage() {
        return currentMemoryUsage.get();
    }

    private void checkStaticResourceType(String uri) {
        if(null == uri || !uri.contains("."))return;
        try {
            URI parsedUri = new URI(uri);
            String path = parsedUri.getPath(); // 去除查询参数
            if(!path.contains("."))return;
            for (String extension : securityConfig.getAllowedExtensions()) {
                if (path.endsWith(extension)) {
                    return;
                }
            }
        } catch (Exception e) {
            logger.error("Invalid URI: " + uri, e);
        }
        throw new SecurityException("Request uri is not allowed");
    }
    /**
     * 申请内存资源
     */
    public void allocateMemory(long bytes) {
        if (currentMemoryUsage.addAndGet(bytes) > securityConfig.getMaxMemoryUsage()) {
            currentMemoryUsage.addAndGet(-bytes);
            logger.error("Memory limit exceeded, requested: {} bytes", bytes);
            throw new SecurityException("Memory limit exceeded");
        }
    }

    /**
     * 新建连接时调用
     */
    public void onNewConnection(String remoteIp) {
        ipConnectionCounter.computeIfAbsent(remoteIp, k -> new AtomicInteger(0)).incrementAndGet();
    }
    
    /**
     * 连接关闭时调用
     */
    public void onConnectionClosed(String remoteIp) {
        ipConnectionCounter.computeIfPresent(remoteIp, (k, v) -> {
            v.decrementAndGet();
            return v;
        });
    }
    
    private void checkConnectionLimit(String remoteIp) {
        AtomicInteger counter = ipConnectionCounter.get(remoteIp);
        if (counter != null && counter.get() > securityConfig.getMaxConnectionsPerIp()) {
            logger.warn("Too many connections from IP: {}", remoteIp);
            throw new SecurityException("Too many connections from your IP");
        }
    }
    
    private void checkRequestRate(String remoteIp) {
        long currentTime = System.currentTimeMillis();
        AtomicInteger counter = ipRequestCounter.computeIfAbsent(remoteIp, k -> new AtomicInteger(0));
        AtomicLong lastTime = ipLastRequestTime.computeIfAbsent(remoteIp, k -> new AtomicLong(currentTime));
        
        // 检查是否需要重置计数器
        if (currentTime - lastTime.get() > 1000) {
            counter.set(0);
            lastTime.set(currentTime);
        }
        
        // 检查请求频率
        if (counter.incrementAndGet() > securityConfig.getMaxConnectionsPerIp()) {
            logger.warn("Request rate limit exceeded for IP: {}", remoteIp);
            throw new SecurityException("Request rate limit exceeded");
        }
    }
    
    private void checkHeaders(HttpHeaders headers) {
        // 检查Content-Length
        String contentLength = headers.get("Content-Length");
        if (contentLength != null) {
            try {
                long length = Long.parseLong(contentLength);
                if (length > 10 * 1024 * 1024) { // 10MB
                    throw new SecurityException("Request body too large");
                }
            } catch (NumberFormatException e) {
                throw new SecurityException("Invalid Content-Length header");
            }
        }
    }
} 