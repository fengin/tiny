package cn.fengin.tiny.demo.http.model;

import java.time.LocalDateTime;

/**
 * 设备实体类，实际业务上用的实体对象
 */
public class Device {
    private String deviceId;      // 设备ID
    private String name;          // 设备名称
    private int status;          // 设备状态：0-离线，1-在线，2-异常
    private LocalDateTime lastOnline; // 最后在线时间
    private String tcpAddress;    // TCP连接地址
    private int tcpPort;         // TCP连接端口
    private String token;        // 设备认证令牌
    
    // Getters and setters
    public String getDeviceId() {
        return deviceId;
    }
    
    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public int getStatus() {
        return status;
    }
    
    public void setStatus(int status) {
        this.status = status;
    }
    
    public LocalDateTime getLastOnline() {
        return lastOnline;
    }
    
    public void setLastOnline(LocalDateTime lastOnline) {
        this.lastOnline = lastOnline;
    }
    
    public String getTcpAddress() {
        return tcpAddress;
    }
    
    public void setTcpAddress(String tcpAddress) {
        this.tcpAddress = tcpAddress;
    }
    
    public int getTcpPort() {
        return tcpPort;
    }
    
    public void setTcpPort(int tcpPort) {
        this.tcpPort = tcpPort;
    }
    
    public String getToken() {
        return token;
    }
    
    public void setToken(String token) {
        this.token = token;
    }
} 