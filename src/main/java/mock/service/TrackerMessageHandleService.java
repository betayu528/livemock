package mock.service;

import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import mock.config.MockConfigHandler;
import mock.model.BaseClient;
import mock.model.ChannelModel;
import mock.model.MockLiveClient;
import mock.util.MockUtil;
import rapid.cloud.cdn.protocol.model.tracker.TrackerMsgHead;
import rapid.cloud.cdn.protocol.protocol.MsgCodeDef;
import rapid.cloud.cdn.protocol.protocol.PMPacketBuilder;
import rapid.cloud.cdn.protocol.protocol.TrackerPacketBuilder;
import rapid.cloud.cdn.protocol.security.AESSecurity;
import rapid.cloud.cdn.protocol.protocol.PacketHeaderBuilder;
import rapid.cloud.cdn.protocol.servAddr.ServerInfo;
import rapid.cloud.server.common.pbtoken.ChannelBRTTokenTools;


@Service
public class TrackerMessageHandleService {
    private final Logger logger = LoggerFactory.getLogger(TrackerMessageHandleService.class);

    @PostConstruct
    public void init() {
        logger.info("init ...");
        
    }
    private byte [] buildAttr() {
        Map<String, String> extAttr = new HashMap<>();
        String mac = MockUtil.getRandomMac();
        extAttr.put("ADID", "266d6cad219e0304");
        //attrMap.put("CODE", streamPair.getKey());
        extAttr.put("DTYP", "1");
        extAttr.put("UID", mac);
        extAttr.put("ASID", "b01-btvkids-513511-0");
        //attrMap.put("CHNID", streamPair.getValue());
        extAttr.put("PRID", "null");
        extAttr.put("APPVER", "2.1.0");
        extAttr.put("RANDSTRING1", Long.toString(System.currentTimeMillis()));
        extAttr.put("RANDSTRING2", Long.toHexString(System.currentTimeMillis()));

        String localIp = MockConfigHandler.getInstance().getConfigModel().getLocalIp();
        if (localIp == null) {
            localIp = "0.0.0.0";
        }
        extAttr.put("TOKEN", ChannelBRTTokenTools.cryptBRTToken(mac, 
            localIp, 10));
        extAttr.put("RELEASEID", "4.4.5");
        extAttr.put("APKSN", mac);

        String streamKey = "368787171110289408";
        String channelId = "10043023";
        extAttr.put("CODE", streamKey);
        extAttr.put("CHNID", channelId);
        String extAttrString = MockUtil.toConnectString(extAttr);

        //AES 加密
        final String AES_KEY = PMPacketBuilder.DATA_AES_KEY;
        String attAesStr = AESSecurity.encryptToBase64(AES_KEY, extAttrString.getBytes());
        return attAesStr.getBytes();
    }

    public byte [] buildLoginTrackerPacket(Long connectId, int sessionId, int mask) {
        final String ENGINFO = "test-Engine";
        byte [] attributeAes = buildAttr();
        byte [] engineInfo = ENGINFO.getBytes();
        final String separator = "-";

        TrackerMsgHead msgHead = new TrackerMsgHead(0, MsgCodeDef.CONNECT_REQUEST, connectId, sessionId, 10086);
        byte[] payload = TrackerPacketBuilder.serialLiveConnPkt(mask, attributeAes, engineInfo, (byte) 0, (short) 1, null);
        byte[] buff = PacketHeaderBuilder.serializeTrackerPacketByteBuffer(msgHead, payload).array();
        System.out.println("buildTrackerLoginPacket connectId " + connectId + " sid " + sessionId + " length " + buff.length);
        System.out.println(MockUtil.bytesToHex(buff));
        System.out.println(Integer.toHexString(sessionId));
        try {
            FileOutputStream fo = new FileOutputStream("tracker"+ separator + (connectId)+separator+sessionId+".login");
            fo.write(buff);
            fo.flush();
            fo.close();
        } catch (Exception e) {
            System.out.println("FileOutputStream error "+ e.getMessage());
        }
        return buff;
    }

    public ByteBuffer buildTrackerMsgHeadByMessageCode(BaseClient client,  int code, byte [] payload) {
        /* 
    
        int msgCode [] = {
            MsgCodeDef.ANNOUNCE_REQUEST,
            MsgCodeDef.ADDR_NOTIFY,
            MsgCodeDef.ANNOUNCE_RESPONSE,
            MsgCodeDef.ADDR_NOTIFY_ACK,
            MsgCodeDef.CONNECT_REQUEST,
            MsgCodeDef.CONNECT_RESPONSE,
            MsgCodeDef.QUIT_NOTIFY
        };
        */
        AtomicInteger reqSeq = client.getCurReqId();
        TrackerMsgHead msgHead = new TrackerMsgHead(0, code, client.getClientId(), client.getCertifyCode(),
        reqSeq.incrementAndGet());
        return ByteBuffer.wrap(PacketHeaderBuilder.serializeTrackerPacket(msgHead, payload));
    }

    public ByteBuffer buildLoginPacketForLiveClient(MockLiveClient client) {
        Map<String, String> extAttr = new HashMap<>();
        String mac = MockUtil.getRandomMac();
        extAttr.put("ADID", "266d6cad219e0304");
        //attrMap.put("CODE", streamPair.getKey());
        extAttr.put("DTYP", "1");
        extAttr.put("UID", mac);
        extAttr.put("ASID", "b01-btvkids-513511-0");
        //attrMap.put("CHNID", streamPair.getValue());
        extAttr.put("PRID", "null");
        extAttr.put("APPVER", client.getReleaseId());
        extAttr.put("RANDSTRING1", Long.toString(System.currentTimeMillis()));
        extAttr.put("RANDSTRING2", Long.toHexString(System.currentTimeMillis()));
        
        String localIp = MockConfigHandler.getInstance().getConfigModel().getLocalIp();
        if (localIp == null) {
            localIp = "0.0.0.0";
        }
        extAttr.put("TOKEN", ChannelBRTTokenTools.cryptBRTToken(mac, 
            localIp, 10)); // ip from cient
        extAttr.put("RELEASEID", "4.4.5");
        extAttr.put("APKSN", mac);

        String streamKey = "368787171110289408";
        String channelId = "10043023";
        client.setChannelModel(new ChannelModel(channelId, streamKey));
        client.setDid(mac);
        extAttr.put("CODE", client.getChannelModel().getStreamId());
        extAttr.put("CHNID", client.getChannelModel().getChannelId());

        String extAttrString = MockUtil.toConnectString(extAttr);
        String encryptAttr = MockUtil.encryptToBase64String(extAttrString.getBytes());
        byte[] attrAttr = null;
        if(!StringUtils.isEmpty(encryptAttr)) {
            attrAttr = new byte[encryptAttr.length()];
            System.arraycopy(encryptAttr.getBytes(), 0, attrAttr, 0, encryptAttr.length());
        }
        Long connectId = client.getClientId(); // tracker 用connectId唯一辨识用户，所以把connectId 设置成和clientId一样 
        
        TrackerMsgHead head = new TrackerMsgHead(0, MsgCodeDef.CONNECT_REQUEST, connectId, 0, 0);
        byte [] payload = null;
        String strEngingeForTest = "Test Engine";
        
        payload = TrackerPacketBuilder.serialLiveConnPkt(0, attrAttr, strEngingeForTest.getBytes(),
            (byte)0,(short) 1, new LinkedList<ServerInfo>());

        ByteBuffer message = PacketHeaderBuilder.serializeTrackerPacketByteBuffer(head, payload);
        //mockUdpClientService.sendPacket(message); 
        System.out.println("message size: " + message.capacity());
        logger.info("message size: {}", message.capacity());
        return message;
    }

    public ByteBuffer makeEmptyPacket() {
        TrackerMsgHead header = new TrackerMsgHead(0, MsgCodeDef.CONNECT_REQUEST, 0x32423523,
                1, 1);

        int packetLen = TrackerMsgHead.HEADER_LENGTH_VER1;
        byte[] payload = new byte[1];
        payload[0] = ' ';

        if (payload != null) {
            packetLen += (4 + payload.length);
        }

        ByteBuffer byteBuffer = ByteBuffer.allocate(packetLen).order(ByteOrder.BIG_ENDIAN);

        byteBuffer.putInt(header.getMask());
        int position = byteBuffer.position();
        byteBuffer.putInt(header.getProtocolType());
        byteBuffer.putInt(header.getProtocolVer());
        byteBuffer.putInt(header.getMsgType());
        byteBuffer.putLong(header.getConnectId());
        byteBuffer.putInt(header.getCertifyCode());
        byteBuffer.putInt(header.getReqSeq());

        if (payload != null) {
            byteBuffer.putInt(payload.length+4);
        } else {
            byteBuffer.putInt(0);
        }
        byteBuffer.putInt(payload.length);
        byteBuffer.put(payload);

        for(byte byt : byteBuffer.array()){
            System.out.printf("%02x", byt);
        }
        return byteBuffer;
    }
}
