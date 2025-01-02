package cn.fengin.tiny.tcp.message;

import io.netty.channel.Channel;

/**
 * TCP通信消息处理器接口
 * 负责处理设备消息和连接事件
 */
public interface MessageProcessor {
    /**
     * 认证消息，如果需要认证，则实现此方法
     * @param msg 消息内容
     * @return 是否认证成功
     */
    default boolean auth(Message msg){
        //默认不认证
        return true;
    }
    /**
     * 处理设备消息
     * @param msg 消息内容
     * @param channel 设备连接通道,有时需要即时回消息用到，比如心跳消息
     */
    void process(Message msg, Channel channel);
    
    /**
     * 处理连接建立事件
     * @param deviceId 设备ID
     */
    default void onConnected(String deviceId) {
    }
    
    /**
     * 处理连接断开事件
     * @param deviceId 设备ID
     */
    default void onDisconnected(String deviceId) {
    }
} 