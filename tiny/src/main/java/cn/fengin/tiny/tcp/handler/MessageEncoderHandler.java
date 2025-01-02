package cn.fengin.tiny.tcp.handler;

import cn.fengin.tiny.tcp.message.Message;
import cn.fengin.tiny.tcp.message.MessageCodec;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * TCP消息编码处理器
 */
public class MessageEncoderHandler extends MessageToByteEncoder<Object> {
    private final MessageCodec codec;
    
    public MessageEncoderHandler(MessageCodec codec) {
        this.codec = codec;
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, Object msg, ByteBuf out) throws Exception {
        codec.encode((Message)msg, out);
    }
} 