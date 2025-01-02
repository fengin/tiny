package cn.fengin.tiny.demo.tcp.message;

import cn.fengin.tiny.tcp.message.Message;

/**
 * 集中器消息示例
 *
 */
public class CenterMessage implements Message {
    private String deviceId;
    private int type;
    private byte[] payload;

    /**
     * 构造消息
     * @param deviceId 设备ID
     * @param type 消息类型
     * @param content 消息内容
     */
    public CenterMessage(String deviceId, int type, String content) {
        this.deviceId = deviceId;
        this.type = type;
        this.payload = content.getBytes();
    }

    @Override
    public String getDeviceId() {
        return deviceId;
    }

    @Override
    public int getMessageType() {
        return type;
    }

    @Override
    public byte[] getPayload() {
        return payload;
    }
}
