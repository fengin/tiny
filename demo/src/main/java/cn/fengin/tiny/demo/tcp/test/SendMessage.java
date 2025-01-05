package cn.fengin.tiny.demo.tcp.test;

import cn.fengin.tiny.tcp.ConnectionManager;
import cn.fengin.tiny.tcp.message.Message;

public class SendMessage {

    public void testSendMessage(){
        ConnectionManager manager = ConnectionManager.getInstance();
        ConnectionManager.Connection conn = manager.getConnection("deviceId");
        if(conn==null)return;
        conn.writeAndFlush(new Message(){
            @Override
            public int getMessageType() {
                return 0;
            }
            @Override
            public byte[] getPayload() {
                return new byte[0];
            }
            @Override
            public String getDeviceId() {
                return "deviceId";
            }
        });
    }
}
