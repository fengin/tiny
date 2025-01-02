package cn.fengin.tiny.tcp;

import cn.fengin.tiny.tcp.handler.HeartbeatHandler;
import cn.fengin.tiny.tcp.handler.MessageDecoderHandler;
import cn.fengin.tiny.tcp.handler.MessageEncoderHandler;
import cn.fengin.tiny.tcp.handler.TcpServerHandler;
import cn.fengin.tiny.tcp.message.MessageCodec;
import cn.fengin.tiny.tcp.message.MessageProcessor;
import cn.fengin.tiny.config.ServerConfig;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.timeout.IdleStateHandler;

import java.util.concurrent.TimeUnit;

/**
 * TCP服务器通道初始化器
 * 负责配置每个新建立的TCP连接的处理管道(Pipeline)
 * 按顺序添加：空闲检测、编解码、心跳处理、业务处理等处理器
 */
public class TcpServerInitializer extends ChannelInitializer<SocketChannel> {
    /** 消息编解码器，负责TCP消息的序列化和反序列化 */
    private final MessageCodec codec;
    
    /** 消息处理器，负责处理业务逻辑 */
    private final MessageProcessor processor;
    
    /** 服务器配置 */
    private final ServerConfig.TcpServerConfig config;
    
    /**
     * 构造通道初始化器
     * @param codec 消息编解码器
     * @param processor 消息处理器
     * @param config 服务器配置
     */
    public TcpServerInitializer(MessageCodec codec, MessageProcessor processor, ServerConfig.TcpServerConfig config) {
        this.codec = codec;
        this.processor = processor;
        this.config = config;
    }
    
    @Override
    protected void initChannel(SocketChannel ch) {
        ChannelPipeline pipeline = ch.pipeline();
        
        // 添加空闲检测处理器，60秒没有读取到数据则判定为空闲
        //readerIdleTime (60): 读空闲时间
        //writerIdleTime (0): 写空闲时间， 0表示不检测写空闲
        //allIdleTime (0): 所有空闲时间，0表示不检测所有空闲
        pipeline.addLast(new IdleStateHandler(config.getDeviceIdleTime(),  0, 0, TimeUnit.SECONDS));
        
        // 添加消息编解码处理器，处理TCP粘包/拆包
        //只处理上行消息解码，解码后的消息交给TcpServerHandler处理，一次解码一个消息，但是每个消息都会调用TcpServerHandler.channelRead方法
        pipeline.addLast(new MessageDecoderHandler(codec));
        //只处理下行消息封装
        pipeline.addLast(new MessageEncoderHandler(codec));
        
        // 添加心跳处理器，处理空闲检测事件
        pipeline.addLast(new HeartbeatHandler(config));
        
        // 添加业务处理器，处理解码后的消息
        pipeline.addLast(new TcpServerHandler(processor));
    }
} 