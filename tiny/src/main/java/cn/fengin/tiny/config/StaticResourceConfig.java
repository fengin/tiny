package cn.fengin.tiny.config;

import lombok.Data;
import java.util.Map;
import java.util.HashMap;

/**
 * 静态资源配置类
 * 采用策略模式设计资源处理策略，支持不同的资源处理方式
 *
 * @author fengin
 * @since 1.0.0
 */
@Data
public class StaticResourceConfig {
    /**
     * 静态资源根路径，默认为/static
     */
    private String path = "/static";
    
    /**
     * 是否启用缓存
     */
    private boolean cacheEnabled = true;
    
    /**
     * 缓存最大存活时间（秒）
     */
    private int cacheMaxAge = 3600;
    
    /**
     * 最大缓存大小（MB）
     */
    private int maxCacheSize = 100;
    
    /**
     * 文件类型映射
     * key: 文件扩展名
     * value: Content-Type
     */
    private Map<String, String> mimeTypes = new HashMap<>();

} 