package cn.fengin.tiny.tcp.handler;

import cn.fengin.tiny.tcp.message.MessageCodec;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import cn.fengin.tiny.tcp.message.Message;

import java.util.List;

/**
 * TCP消息解码处理器
 */
public class MessageDecoderHandler extends ByteToMessageDecoder {
    private final MessageCodec codec;
    
    public MessageDecoderHandler(MessageCodec codec) {
        this.codec = codec;
    }
    
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        // 尝试解码消息
        Message msg = codec.decode(in);
        if (msg != null) {
            out.add(msg);
        }
        // 返回null表示需要更多数据，ByteToMessageDecoder会自动处理
    }
} 