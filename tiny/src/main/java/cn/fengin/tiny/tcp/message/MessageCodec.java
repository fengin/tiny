package cn.fengin.tiny.tcp.message;

import io.netty.buffer.ByteBuf;

/**
 * 消息编解码器接口
 * 负责TCP消息的序列化和反序列化
 * 实现类需要处理TCP粘包/拆包问题
 */
public interface MessageCodec {
    /**
     * 解码消息
     * 将TCP字节流解码为业务消息对象
     * 
     * @param in 输入的字节缓冲
     * @return 解码后的业务消息对象，如果数据不完整一定要返回null
     * @throws Exception 解码异常
     */
    Message decode(ByteBuf in) throws Exception;

    /**
     * 编码消息
     * 将业务消息对象编码为TCP字节流
     * 
     * @param msg 业务消息对象
     * @param out 输出的字节缓冲
     * @throws Exception 编码异常
     */
    void encode(Message msg, ByteBuf out) throws Exception;
} 