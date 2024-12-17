package mock.handler;

import java.nio.ByteBuffer;
import mock.model.BaseClient;
import rapid.cloud.cdn.protocol.model.tracker.TrackerMsgHead;
import rapid.cloud.cdn.protocol.protocol.MsgCodeDef;
import rapid.cloud.cdn.protocol.protocol.PacketHeaderBuilder;

public class TrackQuitMsgHandler extends BaseMsgHandler {
    
    @Override
    public ByteBuffer preHandler(BaseClient client) {
        if (client == null) {
            logger.error("client is null, return null buff msg");
            return null;
        }
        ByteBuffer message = null;
        long now = System.currentTimeMillis();
        try{
            if((now - lastSendTime) > 200){
                curSeq = client.getCurReqId().incrementAndGet();
                TrackerMsgHead msgHead = new TrackerMsgHead(0, MsgCodeDef.QUIT_NOTIFY, client.getClientId(),
                        client.getCertifyCode(), curSeq);
                message = PacketHeaderBuilder.serializeTrackerPacketByteBuffer(msgHead, null);
            }
        }catch (Throwable e){
            logger.error("quit msg pre handler error, clientId:{}", client.getClientId(), e);
            return null;
        }
        return message;
    }

    @Override
    public void afterHandler(BaseClient client) {
        lastSendTime = System.currentTimeMillis();
        logger.info("client[{}]send quit msg, lastSendTime:{}, reqId:{}", client.getClientId(), lastSendTime, curSeq);
        client.logout();
    }

    public void onRecv(BaseClient client){
        logger.info("client[{}] have received quit msg!", client.getClientId());
        client.logout();
        lastRecvTime = System.currentTimeMillis();
    }
}
