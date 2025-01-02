package cn.fengin.tiny.tcp;

import cn.fengin.tiny.tcp.message.Message;
import io.netty.channel.Channel;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * TCP连接管理器
 * 负责管理所有设备TCP连接的生命周期和状态
 */
public class ConnectionManager {
    private static final Logger logger = LoggerFactory.getLogger(ConnectionManager.class);
    
    private static volatile ConnectionManager instance;
    /**
     * 单例模式，全局设备连接管理器
     */
    private ConnectionManager() {}
    /**
     * 设备连接状态枚举
     */
    public enum ConnectionStatus {
        CONNECTED,      // 初始连接状态，可以通信
        IDLE,          // 空闲状态（可能离线），可以通信，但不保证能通信成功
        DISCONNECTED   // 已断开连接，无法通信，需要等待重连
    }
    
    /**
     * 设备连接信息类
     * 存储设备连接的详细信息
     */
    @Getter
    public static class Connection {
        private final Channel channel;
        private final String deviceId;
        private ConnectionStatus status;
        private long lastActiveTime;
        
        public Connection(Channel channel, String deviceId) {
            this.channel = channel;
            this.deviceId = deviceId;
            this.status = ConnectionStatus.CONNECTED;
            this.lastActiveTime = System.currentTimeMillis();
        }
        public boolean writeAndFlush(Message msg) {
            if(msg==null || null==msg.getDeviceId())return false;
            if(this.channel!=null && channel.isActive()){
                channel.writeAndFlush(msg);
                return true;
            }
            return false;
        }
        public void setLastActiveTime(long lastActiveTime) {
            this.lastActiveTime = lastActiveTime;
        }
    }
    
    /** 设备ID到连接信息的映射 */
    private final Map<String, Connection> deviceConnections = new ConcurrentHashMap<>();
    
    /** Channel到设备ID的映射 */
    private final Map<Channel, String> channelToDeviceId = new ConcurrentHashMap<>();
    /**
     * 注册新的设备连接
     * @param deviceId 设备ID
     * @param channel 设备连接的Channel
     */
    public void registerConnection(String deviceId, Channel channel) {
        Connection oldInfo = deviceConnections.get(deviceId);
        if (oldInfo != null) {
            // 如果存在旧连接，先移除映射关系，再关闭连接
            logger.warn("Device {} already connected, closing old connection", deviceId);
            channelToDeviceId.remove(oldInfo.channel);
            deviceConnections.remove(deviceId);
            try {
                oldInfo.channel.close();
            } catch (Exception e) {
                logger.error("Error closing old connection for device {}", deviceId, e);
            }
        }

        Connection info = new Connection(channel, deviceId);
        deviceConnections.put(deviceId, info);
        channelToDeviceId.put(channel, deviceId);
        logger.info("Device {} connected, total connected device number:{}", deviceId, deviceConnections.size());
    }
    
    /**
     * 更新连接状态
     * @param channel 设备Channel
     * @param status 新状态
     */
    public void updateConnectionStatus(Channel channel, ConnectionStatus status) {
        String deviceId = channelToDeviceId.get(channel);
        if (deviceId != null) {
            Connection info = deviceConnections.get(deviceId);
            if (info != null) {
                ConnectionStatus oldStatus = info.status;
                info.status = status;
                if(status==ConnectionStatus.CONNECTED){
                    info.lastActiveTime = System.currentTimeMillis();
                }
                if(oldStatus!=status){
                    logger.info("Device {} status changed from {} to {}", deviceId, oldStatus, status);
                }
            }
        }
    }
    
    /**
     * 移除连接
     * @param channel 要移除的Channel
     * @return 被移除连接的设备ID，如果没有找到对应的连接则返回null
     */
    public String removeConnection(Channel channel) {
        String deviceId = null;
        try {
            deviceId = channelToDeviceId.remove(channel);
            if (deviceId != null) {
                Connection connection = deviceConnections.remove(deviceId);
                if (connection != null && connection.getChannel().isOpen()) {
                    try {
                        connection.getChannel().close();
                    } catch (Exception e) {
                        logger.error("Error closing channel for device {}", deviceId, e);
                    }
                }
                logger.info("Device {} disconnected, total connected device number:{}", deviceId, deviceConnections.size());
            }
        } catch (Exception e) {
            logger.error("Error removing connection for channel {}", channel, e);
            // 确保在发生异常时也能清理资源
            if (deviceId != null) {
                deviceConnections.remove(deviceId);
            }
        }
        return deviceId;
    }
    
    /**
     * 获取设备的Channel
     * @param deviceId 设备ID
     * @return 设备的Channel，如果设备未连接则返回null
     */
    public Connection getConnection(String deviceId) {
        return deviceConnections.get(deviceId);
    }
    /**
     * 获取设备的连接状态
     * @param deviceId 设备ID
     * @return 连接状态，如果设备未连接则返回null
     */
    public ConnectionStatus getConnectionStatus(String deviceId) {
        Connection info = deviceConnections.get(deviceId);
        return info != null ? info.status : null;
    }
    
    /**
     * 获取Channel对应的设备ID
     * @param channel 设备Channel
     * @return 设备ID，如果未注册则返回null
     */
    public String getDeviceId(Channel channel) {
        return channelToDeviceId.get(channel);
    }
    
    /**
     * 判断设备是否已连接
     * @param deviceId 设备ID
     * @return 是否已连接
     */
    public boolean isConnected(String deviceId) {
        Connection info = deviceConnections.get(deviceId);
        return info != null && info.channel.isActive();
    }

    public static ConnectionManager getInstance() {
        if (instance == null) {
            synchronized (ConnectionManager.class) {
                if (instance == null) {
                    instance = new ConnectionManager();
                }
            }
        }
        return instance;
    }

} 