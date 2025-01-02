package cn.fengin.tiny.cache;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * 缓存接口
 * 定义缓存的基本操作
 *
 * @author fengin
 * @since 1.0.0
 */
public interface Cache<K, V> {
    /**
     * 存储数据
     */
    void put(K key, V value);
    
    /**
     * 存储数据并设置过期时间
     */
    void put(K key, V value, long timeout, TimeUnit unit);
    
    /**
     * 获取数据
     */
    Optional<V> get(K key);
    
    /**
     * 删除数据
     */
    void remove(K key);
    
    /**
     * 清空缓存
     */
    void clear();
    
    /**
     * 获取缓存大小
     */
    int size();
} 