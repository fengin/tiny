package cn.fengin.tiny.config;

import lombok.Data;

/**
 * 线程池配置类
 * 采用工厂模式创建线程池，支持不同类型线程池的创建
 *
 * @author fengin
 * @since 1.0.0
 */
@Data
public class ThreadPoolConfig {
    /**
     * 核心线程数，默认10
     * 建议设置为CPU核心数+1，保持CPU资源的最优利用
     */
    private int coreSize = Runtime.getRuntime().availableProcessors() + 1;
    
    /**
     * 最大线程数，默认20
     * 建议设置为核心线程数的2倍，在突发流量时能够及时处理
     */
    private int maxSize = coreSize * 2;
    
    /**
     * 任务队列容量，默认1000
     * 建议根据系统内存大小和任务特点调整
     */
    private int queueCapacity = 1000;
    
    /**
     * 线程存活时间（秒），默认60
     * 非核心线程的空闲存活时间
     */
    private int keepAliveSeconds = 60;
} 