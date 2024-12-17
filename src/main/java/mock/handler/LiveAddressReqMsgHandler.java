package mock.handler;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import mock.model.BaseClient;
import mock.model.MockLiveClient;
import rapid.cloud.cdn.protocol.model.tracker.TrackerMsgHead;
import rapid.cloud.cdn.protocol.model.tracker.TrackerServerAddrNotify;
import rapid.cloud.cdn.protocol.protocol.MsgCodeDef;
import rapid.cloud.cdn.protocol.protocol.PacketHeaderBuilder;
import rapid.cloud.cdn.protocol.servAddr.ServerInfo;

public class LiveAddressReqMsgHandler extends BaseMsgHandler {
    
    @Override
    public ByteBuffer preHandler(BaseClient client) {
        AtomicInteger reqSeq = client.getCurReqId();
        reqSeq.incrementAndGet();
        TrackerMsgHead trackerMsgHead = new TrackerMsgHead(0, MsgCodeDef.ADDR_NOTIFY, client.getClientId(),
        client.getCertifyCode(), reqSeq.get());
        //TrackerPacketBuilder.serialServerAddressNotifyPkt(null, null, 0)
        byte [] array = PacketHeaderBuilder.serializeTrackerPacket(trackerMsgHead, null);

        /* PRT 地址一般不需要请求，客户端换台 播放都会直接下发 */ 
        return ByteBuffer.wrap(array);
    }

    @Override
    public void afterHandler(BaseClient client) {
        return;
    }

    public void onRecv(MockLiveClient client, TrackerMsgHead msgHead, TrackerServerAddrNotify addrNotify, long recvTime) {
        logger.info("client[{}] recv addr notify msg from [{}]!", client.getClientId(), msgHead.getConnectId());
        List<ServerInfo> infos = addrNotify.getServerAddrs();
        logger.info("addrs: {}", infos);
        if (client instanceof MockLiveClient) {
           MockLiveClient m_client = (MockLiveClient)client;
           m_client.setLiveServerAddressList(addrNotify.getServerAddrs());
        }
        lastRecvTime = System.currentTimeMillis();
    }
}
