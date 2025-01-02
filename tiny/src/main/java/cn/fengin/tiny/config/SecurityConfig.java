package cn.fengin.tiny.config;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * 安全相关配置
 *
 * @author fengin
 * @since 1.0.0
 */
@Setter
@Getter
public class SecurityConfig {
    /**
     * 是否启用目录浏览
     */
    private boolean directoryBrowsing = false;

    /**
     * 是否启用ETag
     */
    private boolean etagEnabled = true;

    /**
     * 允许的文件扩展名
     */
    private List<String> allowedExtensions = new ArrayList<>();
    /**
     * 跨域配置
     */
    private SecurityConfig.CorsConfig cors = new SecurityConfig.CorsConfig();

    @Data
    public static class CorsConfig {
        private boolean enabled = false;
        private String allowOrigin = "*";
        private String allowMethods = "GET, POST, OPTIONS";
        private String allowHeaders = "*";
        private int maxAge = 3600;
    }

    private int maxRequestsPerSecond = 100;
    private int maxConnectionsPerIp = 50;
    private long maxRequestBodySize = 10 * 1024 * 1024; // 10MB
    private long maxMemoryUsage = Runtime.getRuntime().maxMemory() * 90 / 100;
    private long maxDiskSpace = 1024 * 1024 * 1024; // 1GB

}