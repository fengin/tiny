package cn.fengin.tiny.context;

import cn.fengin.tiny.config.*;
import cn.fengin.tiny.http.handler.deal.SecurityManager;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Map;


/**
 * 应用上下文，把TCP HTTP服务配置全放一起了，没有拆开来了
 * 管理全局配置和共享资源
 *
 * @author fengin
 * @since 1.0.0
 */
@Getter
public class ApplicationContext {
    private static final Logger logger = LoggerFactory.getLogger(ApplicationContext.class);
    /**
     *  启动时间
     */
    public static final long startTime = System.currentTimeMillis();
    // 用于测试
    @Setter
    private static volatile ApplicationContext instance;

    // Getters for configs
    // 配置实例
    private final ServerConfig serverConfig;
    private final DatabaseConfig databaseConfig;
    private final SecurityConfig securityConfig;
    private final StaticResourceConfig staticResourceConfig;
    private final ThreadPoolConfig threadPoolConfig;
    private final AuthConfig authConfig;

    private final SecurityManager securityManager;
    
    private ApplicationContext() {
        logger.info("Begin Start Tiny FrameWork Application ...");

        // 从系统属性获取配置文件路径
        String configFile = System.getProperty("app.config");
        // 加载配置数据
        Map<String, Object> configData = ConfigLoader.load(configFile);
        
        // 初始化各个配置实例
        serverConfig = ConfigLoader.loadServerConfig(configData);
        databaseConfig = ConfigLoader.loadDatabaseConfig(configData);
        securityConfig = ConfigLoader.loadSecurityConfig(configData);
        staticResourceConfig = ConfigLoader.loadResourceConfig(configData);
        threadPoolConfig = ConfigLoader.loadThreadPoolConfig(configData);
        authConfig = ConfigLoader.loadAuthConfig(configData);

        // 初始化安全检查器
        this.securityManager = new SecurityManager(securityConfig);
    }
    
    public static ApplicationContext getInstance() {
        if (instance == null) {
            synchronized (ApplicationContext.class) {
                if (instance == null) {
                    instance = new ApplicationContext();
                }
            }
        }
        return instance;
    }

} 