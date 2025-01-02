package cn.fengin.tiny.demo.http.service;


import cn.fengin.tiny.context.ApplicationContext;
import cn.fengin.tiny.demo.http.dao.DeviceDao;
import cn.fengin.tiny.demo.http.model.Device;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * 设备服务示例，实际根据业务情况定义
 * 处理设备相关的业务逻辑
 */
public class DeviceService {
    private static final Logger logger = LoggerFactory.getLogger(DeviceService.class);
    
    private final DeviceDao deviceDao;
    
    public DeviceService() {
        this.deviceDao = new DeviceDao();
    }
    
    /**
     * 验证用户登录
     */
    public boolean validateUser(String username, String password) {
        return username != null && 
               password != null && 
               username.equals(ApplicationContext.getInstance().getAuthConfig().getUserAuthConfig().getUsername()) &&
               password.equals(ApplicationContext.getInstance().getAuthConfig().getUserAuthConfig().getPassword());
    }
    

    /**
     * 更新设备状态
     */
    public void updateDeviceStatus(String deviceId, int status) {
        deviceDao.updateStatus(deviceId, status);
        logger.info("Device {} status updated to {}", deviceId, status);
    }
    
    /**
     * 更新设备TCP连接信息
     */
    public void updateDeviceTcpInfo(String deviceId, String address, int port) {
        deviceDao.updateTcpInfo(deviceId, address, port);
        logger.info("Device {} TCP info updated: {}:{}", deviceId, address, port);
    }
    
    /**
     * 验证设备token
     */
    public boolean validateDeviceToken(String deviceId, String token) {
        Optional<Device> device = deviceDao.findByDeviceId(deviceId);
        return device.isPresent() && device.get().getToken().equals(token);
    }
    
    /**
     * 处理设备数据
     */
    public void handleDeviceData(String deviceId, Object data) {
        // TODO: 根据业务需求处理设备数据
        logger.info("Received data from device {}: {}", deviceId, data);
    }
    
    /**
     * 获取系统状态
     * 包含设备总数、在线设备数等统计信息
     */
    public Map<String, Object> getSystemStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("totalDevices", deviceDao.count());
        status.put("onlineDevices", deviceDao.countByStatus(1));
        status.put("offlineDevices", deviceDao.countByStatus(0));
        return status;
    }
    
} 