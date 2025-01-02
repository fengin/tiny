package cn.fengin.tiny.tcp.register;

import cn.fengin.tiny.tcp.message.MessageCodec;
import cn.fengin.tiny.tcp.message.MessageProcessor;
import lombok.Getter;


/**
 * TCP服务注册器
 * 负责管理TCP服务的名称、编解码器和消息处理器
 * 作为TCP服务器的配置中心
 */
@Getter
public class TcpRegistry {
    // Getters
    /** 服务名称，对应application.yaml中的配置 */
    private final String serverName;
    
    /** 消息编解码器 */
    private MessageCodec codec;
    
    /** 消息处理器 */
    private MessageProcessor processor;
    
    /**
     * 构造服务注册器
     * @param serverName 服务名称，必须与配置文件中的name匹配
     */
    public TcpRegistry(String serverName) {
        this.serverName = serverName;
    }
    
    /**
     * 注册消息编解码器
     * @param codec 消息编解码器实例
     * @return 当前注册器实例，支持链式调用
     */
    public TcpRegistry registerCodec(MessageCodec codec) {
        this.codec = codec;
        return this;
    }

    /**
     * 注册消息处理器
     * @param processor 消息处理器实例
     * @return 当前注册器实例，支持链式调用
     */
    public TcpRegistry registerProcessor(MessageProcessor processor) {
        this.processor = processor;
        return this;
    }
    
    /**
     * 验证注册器配置是否完整
     * 确保必要的组件都已注册
     * @throws IllegalStateException 如果配置不完整
     */
    public void validate() {
        if (codec == null) {
            throw new IllegalStateException("MessageCodec not configured for server: " + serverName);
        }
        if (processor == null) {
            throw new IllegalStateException("MessageProcessor not configured for server: " + serverName);
        }
    }

}
