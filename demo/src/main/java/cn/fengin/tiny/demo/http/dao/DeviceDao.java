package cn.fengin.tiny.demo.http.dao;

import cn.fengin.tiny.demo.http.model.Device;
import cn.fengin.tiny.db.AbstractDao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 设备数据访问对象使用示例，目前只支持SQLite，其他数据库需要自行实现
 */
public class DeviceDao extends AbstractDao<Device, String> {
   
    
    public DeviceDao() {
        // 初始化数据库表
        initTable();
    }
    
    private void initTable() {
        String sql = "CREATE TABLE IF NOT EXISTS devices (" +
            "device_id VARCHAR(50) PRIMARY KEY," +
            "name VARCHAR(100) NOT NULL," +
            "status INT DEFAULT 0," +
            "last_online TIMESTAMP," +
            "tcp_address VARCHAR(50)," +
            "tcp_port INT" +
            ")";
        execute(sql);
    }
    
    @Override
    protected Device mapRow(ResultSet rs) throws SQLException {
        Device device = new Device();
        device.setDeviceId(rs.getString("device_id"));
        device.setName(rs.getString("name"));
        device.setStatus(rs.getInt("status"));
        device.setLastOnline(rs.getTimestamp("last_online").toLocalDateTime());
        device.setTcpAddress(rs.getString("tcp_address"));
        device.setTcpPort(rs.getInt("tcp_port"));
        return device;
    }
    
    public List<Device> findAll() {
        String sql = "SELECT * FROM devices ORDER BY last_online DESC";
        return queryForList(sql);
    }
    
    public Optional<Device> findByDeviceId(String deviceId) {
        String sql = "SELECT * FROM devices WHERE device_id = ?";
        return queryForObject(sql, deviceId);
    }
    
    public void save(Device device) {
        String sql = "INSERT INTO devices (device_id, name, status, last_online, tcp_address, tcp_port) " +
            "VALUES (?, ?, ?, ?, ?, ?) " +
            "ON CONFLICT (device_id) DO UPDATE SET " +
            "name = EXCLUDED.name, " +
            "status = EXCLUDED.status, " +
            "last_online = EXCLUDED.last_online, " +
            "tcp_address = EXCLUDED.tcp_address, " +
            "tcp_port = EXCLUDED.tcp_port";
        
        update(sql, 
            device.getDeviceId(),
            device.getName(),
            device.getStatus(),
            device.getLastOnline(),
            device.getTcpAddress(),
            device.getTcpPort()
        );
    }
    
    public void updateStatus(String deviceId, int status) {
        String sql = "UPDATE devices SET status = ?, last_online = ? WHERE device_id = ?";
        update(sql, status, LocalDateTime.now(), deviceId);
    }
    
    public void updateTcpInfo(String deviceId, String address, int port) {
        String sql = "UPDATE devices SET tcp_address = ?, tcp_port = ? WHERE device_id = ?";
        update(sql, address, port, deviceId);
    }
    
    public long count() {
        String sql = "SELECT COUNT(*) FROM devices";
        return queryForLong(sql);
    }
    
    public long countByStatus(int status) {
        String sql = "SELECT COUNT(*) FROM devices WHERE status = ?";
        return queryForLong(sql, status);
    }
} 