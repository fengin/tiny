package cn.fengin.tiny.cache;

import lombok.Getter;

/**
 * 缓存管理器
 *
 * @author fengin
 * @since 1.0.0
 */
public class CacheManager {
    /**
     *  获取或创建缓存
     */
    @Getter
    private static final Cache<Object,Object> localCache = new LocalCache<>();

    /**
     * 清空所有缓存
     */
    public static void clearAll() {
        localCache.clear();
    }
} 