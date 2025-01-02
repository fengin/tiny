package cn.fengin.tiny.context;

import cn.fengin.tiny.config.*;
import cn.fengin.tiny.tcp.message.MessageCodec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;
import cn.fengin.tiny.exception.ConfigException;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 配置加载器
 * 只负责从配置文件加载配置数据
 *
 * @author fengin
 * @since 1.0.0
 */
public class ConfigLoader {
    private static final Logger logger = LoggerFactory.getLogger(ConfigLoader.class);
    
    /**
     * 加载配置文件
     * @param configFileName 配置文件路径，如果为空则使用默认路径
     * @return 配置数据Map
     */
    public static Map<String, Object> load(String configFileName) {
        String path = configFileName;
        try{
            if (path == null || path.isEmpty()) {
                path = "application.yaml"; //框架本身启动服务可以这样使用，如果利用框架开发时，需要指定配置文件路径
            }
            InputStream is = ConfigLoader.class.getClassLoader().getResourceAsStream(path);
            if (is == null) {
                throw new ConfigException("Configuration file not found: " + path);
            }
            Map<String, Object> config = new Yaml().load(is);
            logger.info("Configuration loaded from: {}", path);
            return config;
        } catch (Exception e) {
            throw new ConfigException("Failed to load configuration: " + path, e);
        }
    }
    @SuppressWarnings("unchecked")
    public static ServerConfig loadServerConfig(Map<String, Object> configData) {
        Map<String, Object> serverData = (Map<String, Object>) configData.get("server");
        ServerConfig serverConfig = new ServerConfig();
        if (serverData != null) {
            // HTTP配置
            Map<String, Object> httpConfig = (Map<String, Object>) serverData.get("http");
            if (httpConfig != null) {
                ServerConfig.HttpConfig http = serverConfig.getHttp();
                http.setPort((Integer) httpConfig.getOrDefault("port", 8080));
                http.setBossThreads((Integer) httpConfig.getOrDefault("boss-threads", 1));
                http.setWorkerThreads((Integer) httpConfig.getOrDefault("worker-threads", 4));
                http.setBacklog((Integer) httpConfig.getOrDefault("backlog", 128));
                http.setKeepAlive((Boolean) httpConfig.getOrDefault("keepalive", true));
            }

            // TCP配置
            Map<String, Object> tcpConfig = (Map<String, Object>) serverData.get("tcp");
            if (tcpConfig != null) {
                List<Map<String, Object>> tcpServerConfigs = (List<Map<String, Object>>) tcpConfig.get("servers");
                if (tcpServerConfigs != null) {
                    List<ServerConfig.TcpServerConfig> tcpServers = new ArrayList<>();
                    for (Map<String, Object> tcpServerConfig : tcpServerConfigs) {
                        ServerConfig.TcpServerConfig config = new ServerConfig.TcpServerConfig();
                        config.setName((String) tcpServerConfig.get("name"));
                        config.setPort((Integer) tcpServerConfig.get("port"));
                        config.setBossThreads((Integer) tcpServerConfig.get("boss-threads"));
                        config.setWorkerThreads((Integer) tcpServerConfig.get("worker-threads"));
                        config.setBacklog((Integer) tcpServerConfig.get("backlog"));
                        config.setDeviceIdleTime((Integer) tcpServerConfig.get("device-idle-time"));
                        config.setDeviceIdleTimeout((Integer) tcpServerConfig.get("device-idle-timeout"));
                        tcpServers.add(config);
                    }
                    serverConfig.setTcpServers(tcpServers);
                }
            }
        }
        return serverConfig;
    }
    @SuppressWarnings("unchecked")
    public static DatabaseConfig loadDatabaseConfig(Map<String, Object> configData) {
        Map<String, Object> dbData = (Map<String, Object>) configData.get("database");
        DatabaseConfig databaseConfig = new DatabaseConfig();
        if (dbData != null) {
            Map<String, Object> sqliteConfig = (Map<String, Object>) dbData.get("sqlite");
            if (sqliteConfig != null) {
                DatabaseConfig.SqliteConfig sqlite = databaseConfig.getSqlite();
                sqlite.setPath((String) sqliteConfig.getOrDefault("path", "./data.db"));
                sqlite.setPoolSize((Integer) sqliteConfig.getOrDefault("maxPoolSize", 10));
            }
        }
        return databaseConfig;
    }
    @SuppressWarnings("unchecked")
    public static StaticResourceConfig loadResourceConfig(Map<String, Object> configData) {
        Map<String, Object> staticData = (Map<String, Object>) configData.get("static");
        StaticResourceConfig staticResourceConfig = new StaticResourceConfig();
        if (staticData != null) {
            staticResourceConfig.setPath((String) staticData.getOrDefault("path", "/static"));
            staticResourceConfig.setCacheEnabled((Boolean) staticData.getOrDefault("cache-enabled", true));
            staticResourceConfig.setCacheMaxAge((Integer) staticData.getOrDefault("cache-max-age", 3600));
            staticResourceConfig.setMaxCacheSize((Integer) staticData.getOrDefault("max-cache-size", 100));

            // MIME类型配置
            Map<String, String> mimeTypes = (Map<String, String>) staticData.get("mime-types");
            staticResourceConfig.setMimeTypes(mimeTypes==null?new HashMap<>():mimeTypes);
        }
        return staticResourceConfig;
    }
    @SuppressWarnings("unchecked")
    public static SecurityConfig loadSecurityConfig(Map<String, Object> configData) {
        Map<String, Object> securityData = (Map<String, Object>) configData.get("security");
        SecurityConfig securityConfig = new SecurityConfig();
        if (securityData != null) {
            securityConfig.setDirectoryBrowsing((Boolean) securityData.getOrDefault("directory-browsing", false));
            securityConfig.setEtagEnabled((Boolean) securityData.getOrDefault("etag-enabled", true));

            securityConfig.setAllowedExtensions((List<String>) securityData.getOrDefault("allowed-extensions", new ArrayList<>()));

            // CORS配置
            Map<String, Object> cors = (Map<String, Object>) securityData.get("cors");
            if (cors != null) {
                SecurityConfig.CorsConfig corsConfig = securityConfig.getCors();
                corsConfig.setEnabled((Boolean) cors.getOrDefault("enabled", false));
                corsConfig.setAllowOrigin((String) cors.getOrDefault("allow-origin", "*"));
                corsConfig.setAllowMethods((String) cors.getOrDefault("allow-methods", "GET, POST, OPTIONS"));
                corsConfig.setAllowHeaders((String) cors.getOrDefault("allow-headers", "*"));
                corsConfig.setMaxAge((Integer) cors.getOrDefault("max-age", 3600));
            }
            securityConfig.setMaxRequestsPerSecond((Integer) securityData.getOrDefault("maxRequestsPerSecond", 100));
            securityConfig.setMaxConnectionsPerIp((Integer) securityData.getOrDefault("maxConnectionsPerIp", 50));
            securityConfig.setMaxRequestBodySize((Integer) securityData.getOrDefault("maxRequestBodySize", 10485760));
            securityConfig.setMaxMemoryUsage(Long.parseLong(securityData.getOrDefault("maxMemoryUsage", 1073741824L).toString()));
            securityConfig.setMaxDiskSpace(Long.parseLong(securityData.getOrDefault("maxDiskSpace", 1073741824L).toString()));
        }
        return securityConfig;
    }
    @SuppressWarnings("unchecked")
    public static ThreadPoolConfig loadThreadPoolConfig(Map<String, Object> configData) {
        Map<String, Object> threadPoolData = (Map<String, Object>) configData.get("thread-pool");
        ThreadPoolConfig threadPoolConfig = new ThreadPoolConfig();
        if (threadPoolData != null) {
            threadPoolConfig.setCoreSize((Integer) threadPoolData.getOrDefault("core-size", 10));
            threadPoolConfig.setMaxSize((Integer) threadPoolData.getOrDefault("max-size", 20));
            threadPoolConfig.setQueueCapacity((Integer) threadPoolData.getOrDefault("queue-capacity", 1000));
            threadPoolConfig.setKeepAliveSeconds((Integer) threadPoolData.getOrDefault("keep-alive-seconds", 60));
        }
        return threadPoolConfig;
    }
    @SuppressWarnings("unchecked")
    public static AuthConfig loadAuthConfig(Map<String, Object> configData) {
        Map<String, Object> authData = (Map<String, Object>) configData.get("auth");
        AuthConfig authConfig = new AuthConfig();
        if (authData != null) {
            // 用户认证配置
            Map<String, Object> userData = (Map<String, Object>) authData.get("user");
            if (userData != null) {
                AuthConfig.UserAuthConfig user = authConfig.getUserAuthConfig();
                user.setUsername((String) userData.getOrDefault("username", "admin"));
                user.setPassword((String) userData.getOrDefault("password", "admin123"));
                user.setTokenExpireHours((Integer) userData.getOrDefault("token-expire-hours", 24));
            }
            // 系统认证配置
            Map<String, Object> systemData = (Map<String, Object>) authData.get("system");
            if (systemData != null) {
                AuthConfig.ApiAuthConfig system = authConfig.getApiAuthConfig();
                system.setKey((String) systemData.getOrDefault("key", "your-system-api-key"));
            }
        }
        return authConfig;
    }
} 