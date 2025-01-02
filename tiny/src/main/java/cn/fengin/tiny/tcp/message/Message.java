package cn.fengin.tiny.tcp.message;


/**
 * TCP消息定义，由业务实现消息内容
 * 包含消息类型和消息内容
 *
 * @author fengin
 * @since 1.0.0
 */
public interface Message {
    /**
     * 设备ID，每个连接上来的设备消息都应该带有设备ID
     * @return 设备ID
     */
    public String getDeviceId();

    /**
     * 消息类型，由业务/协议 自己来定义
     * @return 消息类型
     */
    public int getMessageType();
    /**
     * 消息内容，由业务/协议 自己来定义
     * @return 消息内容
     */
    public byte[] getPayload();
} 