package cn.fengin.tiny.cache;


import lombok.Getter;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.*;

/**
 * 本地缓存实现
 * 基于ConcurrentHashMap实现的本地缓存，支持过期机制
 *
 * @author fengin
 * @since 1.0.0
 */
public class LocalCache<K, V> implements Cache<K, V> {
    //private static final Logger logger = LoggerFactory.getLogger(LocalCache.class);
    
    private final Map<K, CacheEntry<V>> cache = new ConcurrentHashMap<>();

    public LocalCache() {
        ScheduledExecutorService cleanupExecutor = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread thread = new Thread(r, "cache-cleanup");
            thread.setDaemon(true);
            return thread;
        });
        
        // 启动定期清理任务
        cleanupExecutor.scheduleAtFixedRate(
            this::cleanup, 
            1, 1, TimeUnit.MINUTES
        );
    }
    
    @Override
    public void put(K key, V value) {
        cache.put(key, new CacheEntry<>(value));
    }
    
    @Override
    public void put(K key, V value, long timeout, TimeUnit unit) {
        cache.put(key, new CacheEntry<>(value, 
            System.currentTimeMillis() + unit.toMillis(timeout)));
    }
    
    @Override
    public Optional<V> get(K key) {
        CacheEntry<V> entry = cache.get(key);
        if (entry != null && !entry.isExpired()) {
            return Optional.of(entry.getValue());
        }
        cache.remove(key);
        return Optional.empty();
    }
    
    @Override
    public void remove(K key) {
        cache.remove(key);
    }
    
    @Override
    public void clear() {
        cache.clear();
    }
    
    @Override
    public int size() {
        cleanup();
        return cache.size();
    }
    
    private void cleanup() {
        cache.entrySet().removeIf(entry -> entry.getValue().isExpired());
    }
    
    /**
     * 缓存条目，包含值和过期时间
     */
    private static class CacheEntry<V> {
        @Getter
        private final V value;
        private final long expireTime;
        
        public CacheEntry(V value) {
            this.value = value;
            this.expireTime = -1; // 永不过期
        }
        
        public CacheEntry(V value, long expireTime) {
            this.value = value;
            this.expireTime = expireTime;
        }

        public boolean isExpired() {
            return expireTime > 0 && System.currentTimeMillis() > expireTime;
        }
    }
} 