package cn.fengin.tiny.demo.tcp.processor;

import cn.fengin.tiny.demo.DemoApplication;
import cn.fengin.tiny.tcp.message.Message;
import cn.fengin.tiny.tcp.message.MessageProcessor;
import com.fasterxml.jackson.databind.util.JSONPObject;
import io.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 消息处理器DEMO, 收到相关消息事件后，在这里丢给你的业务逻辑去处理
 * 一种处理器对应一种消息编解码器CodeC
 */
public class CenterMessageProcessor implements MessageProcessor {
    private static final Logger logger = LoggerFactory.getLogger(CenterMessageProcessor.class);
    @Override
    public void process(Message msg, Channel channel) {
        logger.warn("Received message, deviceId: {}, messageType:{}, content:{}",msg.getDeviceId(), msg.getMessageType(), new String(msg.getPayload()));
        //收到消息后处理逻辑
    }

    @Override
    public void onConnected(String deviceId) {
        logger.warn("Device connected: {}", deviceId);
        //连接成功后处理逻辑
    }

    @Override
    public void onDisconnected(String deviceId) {
        logger.warn("Device disconnected: {}", deviceId);
        //断开连接后处理逻辑
    }
}
