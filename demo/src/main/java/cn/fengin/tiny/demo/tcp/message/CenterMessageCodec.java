package cn.fengin.tiny.demo.tcp.message;

import cn.fengin.tiny.tcp.message.Message;
import cn.fengin.tiny.tcp.message.MessageCodec;
import io.netty.buffer.ByteBuf;

/**
 * 集中器的消息编解码器示例
 * 实现MessageCodec接口，处理消息的序列化和反序列化
 * 消息格式：
 * [总长度(4字节)][设备ID长度(4字节)][设备ID][消息类型(4字节)][消息内容]
 */
public class CenterMessageCodec implements MessageCodec {
    @Override
    public Message decode(ByteBuf in) throws Exception {
        // 检查是否有足够的数据可读
        if (in.readableBytes() < 8) return null;

        // 标记当前读取位置，以便需要时可以重置
        in.markReaderIndex();
        int totalLength = in.readInt();
        if (in.readableBytes() < totalLength - 4) {
            in.resetReaderIndex();
            return null;
        }

        // 读取设备ID
        int deviceIdLength = in.readInt();
        byte[] deviceIdBytes = new byte[deviceIdLength];
        in.readBytes(deviceIdBytes);
        String deviceId = new String(deviceIdBytes);

        // 读取消息类型和内容
        int type = in.readInt();
        int contentLength = totalLength - 12 - deviceIdLength;
        byte[] content = new byte[contentLength];
        in.readBytes(content);

        return new CenterMessage(deviceId, type, new String(content));
    }

    @Override
    public void encode(Message msg, ByteBuf out) throws Exception {
        CenterMessage message = (CenterMessage) msg;
        byte[] deviceIdBytes = message.getDeviceId().getBytes();
        byte[] content = message.getPayload();

        int totalLength = 12 + deviceIdBytes.length + content.length;

        out.writeInt(totalLength);
        out.writeInt(deviceIdBytes.length);
        out.writeBytes(deviceIdBytes);
        out.writeInt(message.getMessageType());
        out.writeBytes(content);
    }
}
