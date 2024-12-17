package mock.handler;

import org.apache.commons.lang3.RandomUtils;

import mock.config.MockConfigHandler;
import mock.enums.EnumClientStatus;
import mock.model.BaseClient;
import mock.util.MockUtil;
import rapid.cloud.cdn.protocol.model.tracker.TrackerAnnounceRsp;
import rapid.cloud.cdn.protocol.model.tracker.TrackerMsgHead;
import rapid.cloud.cdn.protocol.model.tracker.TrackerRequestEvent;
import rapid.cloud.cdn.protocol.protocol.MsgCodeDef;
import rapid.cloud.cdn.protocol.protocol.PacketHeaderBuilder;
import rapid.cloud.cdn.protocol.protocol.TrackerPacketBuilder;

import java.nio.ByteBuffer;

import static rapid.cloud.cdn.protocol.p2p.Constants.BTFTYPE_LIVETV;

public class LiveAnnounceMsgHandler extends BaseMsgHandler {
    private long lastNeighborTime = 0;
    private int loopIdx = 0;
    byte[] infoHash = new byte[20];
    byte[] peerId = new byte[20];

    @Override
    public ByteBuffer preHandler(BaseClient client){
        long now = System.currentTimeMillis();
        int numWant = 0;
        ByteBuffer message = null;
        try {
            logger.info("lastsendtime :{} last recv time", lastSendTime, lastRecvTime);
            long intervalTime = now - lastSendTime;
            //发送超时
            if (lastSendTime > 0 && intervalTime > 60000 &&
                    now - client.lastLoginTime >= 60000) {//超过60s没有收到响应,状态改为未登录
                logger.info("client[{}] send timeout:{}", Long.toHexString(client.getClientId()), intervalTime);
                client.logout();
            }

            intervalTime = now - lastRecvTime;
            //接收超时
            if (lastRecvTime > 0 && intervalTime >= 60000 &&
                    now - lastRecvTime >= 60000) {//超过60s没有收到响应,状态改为未登录
                logger.info("client[{}] recv timeout:{}", Long.toHexString(client.getClientId()), intervalTime);
                client.logout();
            }
//                client.needAddrCheck = ((loopIdx % 6) == 0);
            loopIdx++;

            if ((client.getStatus() == EnumClientStatus.eOnline || client.getStatus() == EnumClientStatus.eGetNeighborSucc) &&
                    now + MSG_PREPARE_TIME - lastSendTime > 6000) {
                curSeq = client.curReqId.incrementAndGet();
                if (now - lastNeighborTime >= 18000) {
                    lastNeighborTime = now;
                    numWant = 30;
                }

                if(client.getLiveServerAddressList() != null && client.getLiveServerAddressList().size() > 0) {
                    client.getLiveServerAddressList().get(0).setServerConnStatus(client.isPrtOk() ? (short) 0 : (short) 1);
                }
                TrackerMsgHead msgHead = new TrackerMsgHead(0, MsgCodeDef.ANNOUNCE_REQUEST, client.getClientId(),
                        client.certifyCode, curSeq);
                byte[] payload = null;
                if(!MockConfigHandler.getInstance().isShareRateV90())
                    payload = TrackerPacketBuilder.serialLiveAnnouncePkt(BTFTYPE_LIVETV, infoHash,
                            peerId, TrackerRequestEvent.COMPLETED, 0, numWant,
                            MockUtil.buildIPGroup(client.getClientId(), client.getClientIp(), client.getListenPort()),
                            0, 0, 0, 0, 0, 0, 0, 0,
                            (byte) 0, 1000, 0, 10240, 1024, (short) 1, client.getLiveServerAddressList());
                else
                    payload = TrackerPacketBuilder.serialLiveAnnounceV2Pkt((byte) 20,
                            ((loopIdx%3)==0)? (byte)0: (byte)1, (short)client.getListenPort(), client.getClientIp(),
                            (short)client.getListenPort(), client.getClientIp(), ((loopIdx%6)==0)? (byte)13: (byte)1,
                            (short)2200, (short) 1, client.getLiveServerAddressList());
                message = PacketHeaderBuilder.serializeTrackerPacketByteBuffer(msgHead, payload);
            }
        
        } catch (Throwable e){
            lastSendTime = now;
            message = null;
            logger.error("announce pre handler err, clientId:{}", client.getClientId(), e);
        }
        return message;
    }

    @Override
    public void afterHandler(BaseClient client){
        lastSendTime = System.currentTimeMillis();
        logger.info("client[{}]send announce msg, reqId:{}", Long.toHexString(client.getClientId()), curSeq);
    }

    public void onRecv(BaseClient client, TrackerMsgHead msgHead, TrackerAnnounceRsp annRsp, long recvTime){
        int neighborNum = 0;
        
        logger.info("client[{}] have received announce rsp! recv time {}", client.getClientId(), recvTime);
        long now = System.currentTimeMillis();
        long intervalTime = now - recvTime;
        if (intervalTime > 60000) {//超过60s没有收到响应,状态改为未登录
            logger.info("client[{}] receive timeout:{}", client.getClientId(), intervalTime);
            client.logout();
        }

        neighborNum = annRsp.getPeers() != null ? annRsp.getPeers().size() : 0;

        int seeder = 60;
        int random = seeder > 0 ? RandomUtils.nextInt(0, seeder) : -1;
        if(neighborNum > 5) {
            client.setStatus(EnumClientStatus.eGetNeighborSucc);
        }

        logger.info("rsp {}",  annRsp.toString());
        logger.info("client[{}] have receive announce Rsp, reqId:{}, peers[{}], announce send interval:{}, response time:{}",
                    client.getClientId(), msgHead.getReqSeq(), neighborNum, recvTime - lastSendTime, now);
        lastRecvTime = now;

        
        afterHandler(client);
    }
}
