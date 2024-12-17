package mock.model;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ArrayBlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;

import mock.enums.EnumClientStatus;
import mock.handler.BaseMsgHandler;
import mock.handler.LiveAddressReqMsgHandler;
import mock.handler.LiveAnnounceMsgHandler;
import mock.handler.PrtTsDownloadMsgHandler;
import mock.handler.TrackQuitMsgHandler;
import mock.handler.TrackerConnMsgHandler;
import rapid.cloud.cdn.protocol.model.PMBlockResponse;
import rapid.cloud.cdn.protocol.model.PMConnectResponse;
import rapid.cloud.cdn.protocol.model.tracker.TrackerAnnounceRsp;
import rapid.cloud.cdn.protocol.model.tracker.TrackerConnectRsp;
import rapid.cloud.cdn.protocol.model.tracker.TrackerExchangeSDPToBReq;
import rapid.cloud.cdn.protocol.model.tracker.TrackerServerAddrNotify;
import rapid.cloud.cdn.protocol.model.tracker.MorC.MorCTrackerResourceReportRsp;
import rapid.cloud.cdn.protocol.protocol.MsgCodeDef;
import rapid.cloud.cdn.socket.model.RapidPacket;


public class MockLiveClient extends BaseClient {
    private final Logger logger = LoggerFactory.getLogger(MockLiveClient.class);
    private ChannelModel channelModel;
    private HashMap<Class<?>, BaseMsgHandler> messageHandlers = null; // 每个直播客户端注册一组消息处理器，分别处理不同类型的消息
    private HashMap<Integer, Long> seqHistory = new HashMap<>(); // key: 序列号 value 首次接收的时间戳
    
    public MockLiveClient(String channelId, String streamId, Long clientId) {
        super(clientId);
        this.channelModel = new ChannelModel(channelId, streamId);
        initMsgHandlers();
    }

    private void initMsgHandlers() {
        // 初始化时增加基本的消息处理器
        messageHandlers = new HashMap<>();
        messageHandlers.put(TrackerConnMsgHandler.class, new TrackerConnMsgHandler());
        messageHandlers.put(TrackQuitMsgHandler.class, new TrackQuitMsgHandler());
        messageHandlers.put(PrtTsDownloadMsgHandler.class, new PrtTsDownloadMsgHandler());
        messageHandlers.put(LiveAnnounceMsgHandler.class, new LiveAnnounceMsgHandler());
        messageHandlers.put(LiveAddressReqMsgHandler.class, new LiveAddressReqMsgHandler());
    }

    public void registerHandler(BaseMsgHandler msgHandler) {
        if (msgHandler == null) {
            return;
        }
        messageHandlers.put(getClass(), msgHandler);
    }

    public void logout() {
        status = EnumClientStatus.eOffline;
        certifyCode = 0; 
        prtOk = true;
        logger.info("{} client is offline");
    }

    public ChannelModel getChannelModel() {
        return this.channelModel;
    }

    public void setChannelModel(ChannelModel channelModel) {
        this.channelModel = channelModel;
    }

    public ByteBuffer getLoginpacket() {
        logger.info("try to login");
        status = EnumClientStatus.eTryToLogin;
        TrackerConnMsgHandler handler = new TrackerConnMsgHandler();
        return handler.preHandler(this);
    }

    public ByteBuffer getLogoutPacket() {
        logger.info("try to logout");
        //status = EnumClientStatus.eTryLogout;
        TrackQuitMsgHandler handler = new TrackQuitMsgHandler();
        ByteBuffer message = handler.preHandler(this);
        return message;
    }

    public ByteBuffer getHeartBeatAnnoucePacket() {
        LiveAnnounceMsgHandler handler = (LiveAnnounceMsgHandler) messageHandlers.get(LiveAnnounceMsgHandler.class);
        if (handler == null) {
            logger.error("心跳消息处理器未注册");
            return null;
        }
        ByteBuffer message = handler.preHandler(this);
        return message;
    }

    public void flushHistorySeqData() {
        logger.info("fresh history seq key");
        long now = System.currentTimeMillis();
        int cnt = 0; 
        Set<Entry<Integer, Long> > entrySet = seqHistory.entrySet();
        for (Map.Entry<Integer, Long> entry: entrySet) {
            long time = entry.getValue();
            if (now - time > 1 * 60 * 60 * 1000) {
                seqHistory.remove(entry.getKey());
            }
        }
        logger.info("clear {} item, history set current size: {}", cnt, seqHistory.size());
    }

    public void processPacket(RapidPacket packet) {
        if (packet == null) {
            logger.error("packect is null");
            return;
        }
        try {
            int reqSeq = 0;
            if(packet.getTrackerMsgHead() != null)
                reqSeq = packet.getTrackerMsgHead().getReqSeq();
            else if(packet.getPeerMsgHead() != null)
                reqSeq = packet.getPeerMsgHead().getReqSeq();
            else if(packet.getPttMsgHead() != null)
                reqSeq = packet.getPttMsgHead().getReqSeq();
            logger.info("req seq {}", reqSeq);
            if (seqHistory.get(reqSeq) != null) {
                // 重复包丢弃
                logger.info("reqSeq: {} 重复数据，丢弃", reqSeq);
                return;
            }
            seqHistory.put(reqSeq, System.currentTimeMillis());
        } catch (Exception e) {
            logger.error("check and save msg seq error:", e);
        }

        try {
            if (packet.getMsgPayload() instanceof TrackerConnectRsp) {
                ((TrackerConnMsgHandler) messageHandlers.get(TrackerConnMsgHandler.class)).onRecv(this,
                        packet.getTrackerMsgHead(), packet.getReceiveTimestamp());
            } else if (packet.getMsgPayload() instanceof TrackerAnnounceRsp) {
                ((LiveAnnounceMsgHandler) messageHandlers.get(LiveAnnounceMsgHandler.class)).onRecv(this,
                packet.getTrackerMsgHead(), (TrackerAnnounceRsp) packet.getMsgPayload(), packet.getReceiveTimestamp());
                // TODO 这里只处理了 live 的心跳回包
            } else if (packet.getMsgPayload() instanceof TrackerExchangeSDPToBReq) {
                // TODO 
            } else if (packet.getMsgPayload() instanceof TrackerServerAddrNotify) {
                logger.info("tracker addr notify");
                LiveAddressReqMsgHandler handler = (LiveAddressReqMsgHandler)messageHandlers.get(LiveAddressReqMsgHandler.class);
                if (handler == null) {
                    logger.debug("LiveAddressReqMsgHandler 没有注册");
                    return;
                }
                TrackerServerAddrNotify notify = (TrackerServerAddrNotify)packet.getMsgPayload();
                handler.onRecv(this, packet.getTrackerMsgHead(), notify, packet.getReceiveTimestamp());
            } else if (packet.getMsgPayload() instanceof MorCTrackerResourceReportRsp) {
               // TODO 
            } else if (packet.getTrackerMsgHead().getMsgType() == MsgCodeDef.QUIT_NOTIFY) {
                ((TrackQuitMsgHandler) messageHandlers.get(TrackQuitMsgHandler.class)).onRecv(this);
            } else if (packet.getMsgPayload() instanceof PMConnectResponse) {
               // ((PrtLoginMsgHandler) msgHandlerItems.get(PrtLoginMsgHandler.class)).onRecv(clientInfo, recvPkt, packet.getReceiveTimestamp());
            } else if (packet.getMsgPayload() instanceof PMBlockResponse) {
                // TODO
                // ((PrtTsDownloadMsgHandler) messageHandlers.get(PrtTsDownloadMsgHandler.class)).onRecv(this, packet);
            } else {
                logger.error("unknown message type!");
            }

        } catch (Exception e) {
            logger.error("handle receive error:", e);
        }
    }
}
