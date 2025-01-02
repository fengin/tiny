package cn.fengin.tiny.tcp.handler;

import cn.fengin.tiny.tcp.ConnectionManager;
import cn.fengin.tiny.tcp.message.MessageProcessor;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import cn.fengin.tiny.tcp.message.Message;

/**
 * TCP服务器业务处理器
 * 负责处理设备连接的建立、断开和业务消息处理
 */
public class TcpServerHandler extends ChannelInboundHandlerAdapter {
    private static final Logger logger = LoggerFactory.getLogger(TcpServerHandler.class);
    
    private final MessageProcessor processor;
    private final ConnectionManager connectionManager = ConnectionManager.getInstance();
    
    public TcpServerHandler(MessageProcessor processor) {
        this.processor = processor;
    }
    
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        // 不应该在连接建立时就注册设备ID，而是等待认证消息
        Channel channel = ctx.channel();
        logger.info("New channel active - Remote: {}, Local: {}", channel.remoteAddress(), channel.localAddress());
        super.channelActive(ctx);
    }
    
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        long startTime = System.currentTimeMillis();
        Channel channel = ctx.channel();
        String deviceId = connectionManager.getDeviceId(channel);
        Message message = (Message) msg;

        try {
            if (deviceId == null) {
                //未注册的设备，尝试从消息中获取设备ID进行注册（如果业务需要认证，也可以实现auth方法）
                if (message.getDeviceId() != null && processor.auth(message)) {
                    connectionManager.registerConnection(message.getDeviceId(), channel);
                    //通知业务处理连接事件
                    processor.onConnected(message.getDeviceId());
                    logger.info("Device registered - ID: {}, Remote: {}, MessageType: {}", 
                        message.getDeviceId(), channel.remoteAddress(), message.getMessageType());
                } else {
                    logger.error("Authentication failed - Remote: {}, AttemptedDeviceId: {}, MessageType: {}", 
                        channel.remoteAddress(), 
                        message.getDeviceId() != null ? message.getDeviceId() : "null",
                        message.getMessageType());
                    channel.close();
                    return;
                }
            }

            // 调用消息处理器处理业务消息
            processor.process(message, channel);
            
            // 记录消息处理性能指标
            long processingTime = System.currentTimeMillis() - startTime; // 转换为毫秒
            if (processingTime > 100) { // 如果处理时间超过100ms，使用警告级别
                logger.warn("Message processing slow - DeviceId: {}, MessageType: {}, ProcessingTime: {}ms", message.getDeviceId(), message.getMessageType(), processingTime);
                return;
            } 
            logger.debug("Message processed - DeviceId: {}, MessageType: {}, ProcessingTime: {}ms", message.getDeviceId(), message.getMessageType(), processingTime);
        } catch (Exception e) {
            logger.error("Error processing message - DeviceId: {}, Remote: {}, MessageType: {}", deviceId != null ? deviceId : "unknown", channel.remoteAddress(), message != null ? message.getMessageType() : "unknown",e);
            throw e;
        }
    }
    
    @Override //心跳检测超时，断开连接，会触发这个方法
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        Channel channel = ctx.channel();
        // 移除连接
        String deviceId = connectionManager.removeConnection(channel);
        //通知业务处理断线事件
        if (deviceId != null) {
            processor.onDisconnected(deviceId);
            logger.info("Device disconnected - ID: {}, Remote: {},", deviceId, channel.remoteAddress());
        } else {
            logger.info("Unregistered channel disconnected - Remote: {}", channel.remoteAddress());
        }
        super.channelInactive(ctx);
    }
    
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        Channel channel = ctx.channel();
        String deviceId = connectionManager.getDeviceId(channel);
        logger.info("Error in TcpServerHandler, remote:{},deviceId:{}",channel.remoteAddress(),deviceId,cause);
        ctx.close();
    }
} 