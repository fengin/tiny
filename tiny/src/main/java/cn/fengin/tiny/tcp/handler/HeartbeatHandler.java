package cn.fengin.tiny.tcp.handler;

import cn.fengin.tiny.tcp.ConnectionManager;
import cn.fengin.tiny.tcp.ConnectionManager.Connection;
import cn.fengin.tiny.config.ServerConfig;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 心跳处理器
 * 负责处理连接空闲检测和状态更新
 */
public class HeartbeatHandler extends ChannelInboundHandlerAdapter {
    private static final Logger logger = LoggerFactory.getLogger(HeartbeatHandler.class);

    /** 连接管理器 */
    private final ConnectionManager connectionManager = ConnectionManager.getInstance();
    
    private final ServerConfig.TcpServerConfig config;
    
    public HeartbeatHandler(ServerConfig.TcpServerConfig config) {
        this.config = config;
    }
    
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent event = (IdleStateEvent) evt;
            //60秒内没有收到设备的任何数据（包括心跳），就会触发 READER_IDLE 事件
            //通常在这记录设备可能离线的日志，更新设备连接状态为空闲，或者考虑断开连接
            if (event.state() == IdleState.READER_IDLE) {
                Channel channel = ctx.channel();
                Connection conn = connectionManager.getConnection(connectionManager.getDeviceId(channel));
                if (conn != null) {
                    long idleTime = System.currentTimeMillis() - conn.getLastActiveTime();
                    // 如果空闲时间超过配置的超时时间，则关闭连接
                    if (idleTime > config.getDeviceIdleTimeout() * 1000L) {
                        logger.warn("Device idle timeout({}s), closing connection: {}", idleTime/1000, channel);
                        connectionManager.updateConnectionStatus(channel, ConnectionManager.ConnectionStatus.DISCONNECTED);
                        channel.close();
                    } else {
                        //记录空闲状态
                        logger.warn("Channel idle detected: {}, time: {}s", channel, idleTime/1000);
                        connectionManager.updateConnectionStatus(channel, ConnectionManager.ConnectionStatus.IDLE);
                    }
                }
            //当在指定时间内没有向通道写入任何数据时触发,我们的代码中配置为0，表示不检测写空闲
            }else if(event.state() == IdleState.WRITER_IDLE){
                //一般不需要处理这个事件，因为：
                //心跳是由设备主动发起的
                //服务端是被动接收心跳
                //服务端只需要在收到心跳时响应即可
            }
        }
        ctx.fireUserEventTriggered(evt);
    }
    
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        Channel channel = ctx.channel();
         // 收到任何消息，更新连接状态为已连接状态
        connectionManager.updateConnectionStatus(channel, ConnectionManager.ConnectionStatus.CONNECTED);
        // 继续传递消息给TcpServerHandler处理业务逻辑
        ctx.fireChannelRead(msg);
    }
} 