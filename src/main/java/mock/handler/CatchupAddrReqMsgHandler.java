package mock.handler;

import rapid.cloud.cdn.protocol.media.ShareMediaInfo;
import rapid.cloud.cdn.protocol.utils.TorrentUtils;
import rapid.cloud.cdn.protocol.model.tracker.TrackerMsgHead;
import rapid.cloud.cdn.protocol.model.tracker.TrackerServerAddrNotify;
import rapid.cloud.cdn.protocol.protocol.MsgCodeDef;
import rapid.cloud.cdn.protocol.protocol.PacketHeaderBuilder;
import rapid.cloud.cdn.protocol.protocol.TrackerPacketBuilder;
import rapid.cloud.cdn.protocol.servAddr.ServerInfo;

import java.nio.ByteBuffer;
import java.util.*;

import mock.model.BaseClient;
import mock.model.CatchupMediaPlayModel;
import mock.model.MorcCatupClient;
import mock.service.MediaRepositoryService;
import mock.util.MockUtil;


public class CatchupAddrReqMsgHandler extends BaseMsgHandler {
    private byte[] clientHash = MockUtil.getRandomClientStreamHash();

    public ByteBuffer preHandler(BaseClient client, long now){
        ByteBuffer msg = null;
        try {
            MorcCatupClient trkClient = (MorcCatupClient) client;
            if (client.isTrackerLoginSucc() &&
                    ((now - lastSendTime >= 1500 && (trkClient.getLiveServerAddressList() == null ||
                     trkClient.getLiveServerAddressList().isEmpty())) ||
                            now - lastSendTime >= 60000)) {
                curSeq = trkClient.curReqId.incrementAndGet();

                if(trkClient.catchupModel == null) {
                    String epgId = MediaRepositoryService.chooseResource();
                    if(epgId == null)
                        epgId = Long.toHexString(client.getClientId());
                        trkClient.catchupModel = new CatchupMediaPlayModel(epgId, epgId, 1650596742000L, 3600L);
                }

                TrackerMsgHead msgHead = new TrackerMsgHead(0, MsgCodeDef.PLAY_SERVER_ADDR_REQ, trkClient.getClientId(),
                        trkClient.getCertifyCode(), curSeq);
                msg = PacketHeaderBuilder.serializeTrackerPacketByteBuffer(msgHead,
                        TrackerPacketBuilder.serialCatchupPlayServerReq(
                                clientHash, trkClient.catchupModel.getEpgId(),
                                trkClient.catchupModel.getStartUTC(), trkClient.catchupModel.getRange(), 1));
            }
        }catch (Throwable e){
            lastSendTime = now;
            msg = null;
            logger.error("play server req pre handler err, clientIdx:{}, catchupModel:{}",
            client.getClientId(), ((MorcCatupClient)client).catchupModel, e);
        }
        return msg;
    }

    public void afterHandler(BaseClient clientInfo){
        logger.info("client[{}] send play server address req msg, reqId:{}", Long.toHexString(clientInfo.getClientId()), curSeq);
    }

    public ByteBuffer onRecv(BaseClient client, TrackerMsgHead msgHead, TrackerServerAddrNotify addrNotify, long recvTime){
        
        logger.info("client[{}] have recv addr notify msg from [{}]!", Long.toHexString(client.getClientId()), msgHead.getConnectId());

        MorcCatupClient trkClient = (MorcCatupClient) client;
        trkClient.setLiveServerAddressList(addrNotify.getServerAddrs());
        
        logger.info("{} recv server addr notify, addr list:{},", Long.toHexString(client.getClientId()), trkClient.getLiveServerAddressList());

        for(ServerInfo serverInfo : trkClient.getLiveServerAddressList()){
            String extra = "";
            Map<String, String> extraParams = null;

            if (serverInfo instanceof ServerInfo.CDNAddrInfo) {
                extra = ((ServerInfo.CDNAddrInfo)serverInfo).getInfo();
                extraParams = MockUtil.stringToMap(extra);
                trkClient.catchupModel.setMediaId(extraParams.get("mediaId"));
            } else if (serverInfo instanceof ServerInfo.PlayAddrInfo) {
                extra = ((ServerInfo.PlayAddrInfo)serverInfo).getExtra();
                extraParams = MockUtil.stringToMap(extra);
                trkClient.catchupModel.setMediaId(extraParams.get("mediaId"));
//                StressTestResult.putPmUsageIfAbsent(serverInfo.getConnectId(), -1, -1)
//                        .addUserSample(clientInfo.clientId, trkClientInfo.mediaCode);
            }

            //StressTestResult.addPlayServerRspMetric(recvTime - lastSendTime, serverInfo == null);

            
            logger.info("{} cur mediaId:{} server addr info:{} extras:{}", Long.toHexString(client.getClientId()),
            trkClient.catchupModel.getMediaId(), extraParams.get("mediaId"), extraParams);
        }

        //判断是否有新增的资源资源地址返回，有就新增一个shareMediaInfo
        boolean existMedia = false;
        for(ShareMediaInfo shareMediaInfo : trkClient.shareMediaInfos){
            if(trkClient.catchupModel != null && Arrays.equals(shareMediaInfo.getInfoHash(),
             trkClient.catchupModel.getInfoHash())) {
                existMedia = true;
             }
        }
        if(!existMedia) {
            byte[] mediaHash =
                    trkClient.catchupModel != null ? TorrentUtils.hexStringToByteArray(trkClient.catchupModel.getMediaId()) : "".getBytes();
            String mediaId = TorrentUtils.byteArrayToHexString_lowCase(mediaHash);
            trkClient.addMedia(mediaHash, true);
            
            logger.info("{} add share media info:{}", Long.toHexString(client.getClientId()), mediaId);
        }

        lastRecvTime = System.currentTimeMillis();
        return null;
    }
}
