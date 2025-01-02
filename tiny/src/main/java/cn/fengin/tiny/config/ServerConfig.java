package cn.fengin.tiny.config;

import lombok.Data;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

/**
 * 服务器配置类
 * 采用构建者模式设计配置结构，便于配置的扩展和维护
 *
 * @author fengin
 * @since 1.0.0
 */
@Data
public class ServerConfig {
    private HttpConfig http = new HttpConfig();
    @Setter
    @Getter
    private List<TcpServerConfig> tcpServers;

    /**
     * HTTP服务器配置
     * 参数说明：
     * - port: HTTP服务端口，默认8080
     * - bossThreads: Netty boss线程数，用于接收连接，默认1（对于接收连接来说通常1个线程就够了）
     * - workerThreads: Netty worker线程数，用于处理IO，默认为CPU核心数*2
     * - backlog: TCP连接队列大小，默认1024（根据JDK推荐值设置）
     */
    @Data
    public static class HttpConfig {
        private int port = 8080;
        private int bossThreads = 1;
        private int workerThreads = Runtime.getRuntime().availableProcessors() * 2;
        private int backlog = 1024;
        private boolean keepAlive = true;
    }

    /**
     * TCP服务器配置
     * 参数说明：
     * - port: TCP服务端口，默认8081
     * - bossThreads: Netty boss线程数，用于接收连接，默认1
     * - workerThreads: Netty worker线程数，用于处理IO，默认为CPU核心数*2
     * - backlog: TCP连接队列大小，默认1024
     * - deviceIdleTime: 设备心跳检测周期（秒），默认60秒
     * - deviceIdleTimeout: 设备空闲超时时间（秒），默认1800秒
     */
    @Setter
    @Getter
    public static class TcpServerConfig {
        private String name;
        private int port;
        private int bossThreads = 1;
        private int workerThreads = 4;
        private int backlog = 1024;
        private int deviceIdleTime = 60;    // 设备心跳检测周期（秒）
        private int deviceIdleTimeout = 1800; // 设备空闲超时时间（秒）
    }
} 