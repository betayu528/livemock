package mock.handler;

import org.apache.commons.lang3.StringUtils;

import mock.enums.EnumClientStatus;
import mock.model.BaseClient;
import mock.model.MockLiveClient;
import mock.util.MockUtil;

import rapid.cloud.cdn.common.security.Base64;
import rapid.cloud.cdn.common.constant.EnumTrackerServerType;
import rapid.cloud.cdn.protocol.model.tracker.TrackerMsgHead;
import rapid.cloud.cdn.protocol.protocol.MsgCodeDef;
import rapid.cloud.cdn.protocol.protocol.PacketHeaderBuilder;
import rapid.cloud.cdn.protocol.protocol.TrackerPacketBuilder;
import rapid.cloud.cdn.protocol.security.AESSecurity;
import rapid.cloud.cdn.protocol.utils.P2PUtils;

import javax.crypto.Cipher;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;


public class TrackerConnMsgHandler extends BaseMsgHandler {
    private static final int FIX_MAC_NUM = 3;
    public static ArrayBlockingQueue<String> fixedMacs = new ArrayBlockingQueue<>(FIX_MAC_NUM);
    protected Cipher aesSecurity = null;

    public TrackerConnMsgHandler(){
        setConnectMsg(true);
        //初始化aesSecurity的解密方法
        try{
            aesSecurity = AESSecurity.createFileCipher(P2PUtils.getDATAKey(), 0, Cipher.DECRYPT_MODE);
        }catch (Exception e){
            logger.error("create decryptCipher exception", e);
        }
    }

    @Override
    public ByteBuffer preHandler(BaseClient client){
        long now = System.currentTimeMillis();
        ByteBuffer message = null;
        try {
            //没有收到响应就20s发送一次
            if (!client.isTrackerLoginSucc() &&
                    (now - lastSendTime >= 10000 ||
                            (now - lastRecvTime >= 10000 && now - lastSendTime >= 20000L))) {
                curSeq = client.getCurReqId().incrementAndGet();
                client.setDid(MockUtil.getRandomMac());

                HashMap<String, String> attrMap =
                        MockUtil.buildRandomAttrMap(client.getDid(),
                                client.getListenAddress(), client.getReleaseId(),
                                EnumTrackerServerType.eLiveTracker.ordinal());
                if(client instanceof MockLiveClient) {
                    MockLiveClient liveClient = (MockLiveClient)client;
                    attrMap.put("CODE", liveClient.getChannelModel().getStreamId());
                    attrMap.put("CHNID", liveClient.getChannelModel().getChannelId());
                }

                String strAttr = convertAttrMap2Array(attrMap);
                String encryptAttr = encryptToBase64String(strAttr.getBytes());
                byte[] attrAttr = null;
                if(!StringUtils.isEmpty(encryptAttr)) {
                    attrAttr = new byte[encryptAttr.length()];
                    System.arraycopy(encryptAttr.getBytes(), 0, attrAttr, 0, encryptAttr.length());
                }

                // if(client.liveServerAddrLst != null && client.liveServerAddrLst.size() > 0)
                //     client.liveServerAddrLst.get(0).setServerConnStatus(client.prtOk ? (short) 0 : (short) 1);

                TrackerMsgHead msgHead = new TrackerMsgHead(0, MsgCodeDef.CONNECT_REQUEST, client.getClientId(),
                        client.getCertifyCode(), curSeq);
                byte[] payload = null;
                String strEngineVer = "2.2T07 Compile";
                if(client instanceof MockLiveClient) {
                    MockLiveClient live_client = (MockLiveClient)client;
                    payload = TrackerPacketBuilder.serialLiveConnPkt(0,
                            attrAttr, strEngineVer.getBytes(), (byte) 0, (short) 1, live_client.getLiveServerAddressList());
                }
                
                message = PacketHeaderBuilder.serializeTrackerPacketByteBuffer(msgHead, payload);
            }
        }catch (Throwable e){
            logger.error("connect msg pre handler error, clientId:{}", client.getClientId(), e);
        }
        return message;
    }

    @Override
    public void afterHandler(BaseClient client){
        lastSendTime = System.currentTimeMillis();
        logger.info("client[{}] send connect msg, reqId:{}", client.getClientId(), curSeq);
    }

    public void onRecv(BaseClient client, TrackerMsgHead msgHead, long recvTime){
        if(client.certifyCode == msgHead.getCertifyCode())//重复发送的请求，不管
            return;
        long now = System.currentTimeMillis();
        long intervalTime = now - lastSendTime;
        if (intervalTime > 60000 && lastSendTime > 0) {//超过60s没有收到响应,状态改为未登录
//            logger.info("client[{}] receive timeout:{}", client.getClientId(), intervalTime);
             client.setStatus(EnumClientStatus.eOffline);
        } else {
            client.setStatus(EnumClientStatus.eOnline);
        }
        
        logger.info("client[{}] have receive connect Rsp, reqId:{}", client.getClientId(), msgHead.getReqSeq());
        client.curReqId.getAndSet(msgHead.getReqSeq()); // 序列号在这里更新, 正常的流程需要做 
        client.certifyCode = msgHead.getCertifyCode();
        //client.needAddrCheck = true;
        client.lastLoginTime = now;
        afterHandler(client);
    }

    private String convertAttrMap2Array(Map<String, String> inMap){
        Iterator<Map.Entry<String, String>> it = inMap.entrySet().iterator();
        StringBuilder strBuff = new StringBuilder();
        while(it.hasNext()){
            Map.Entry<String, String> entry = it.next();

            strBuff.append(entry.getKey());
            strBuff.append("=");
            strBuff.append(entry.getValue());
            if(it.hasNext())
                strBuff.append("&");
        }
        return strBuff.toString();
    }

    /**
     * 通过这个方法包装aesSecurity进行效率节省
     * @param data
     * @return
     */
    private String encryptToBase64String(byte[] data){
        try{
            synchronized (aesSecurity) {
                byte[] bytes = AESSecurity.encryptByCipher(aesSecurity, data, data.length);
                return Base64.encodeToString(bytes, Base64.URL_SAFE | Base64.NO_WRAP);
            }
        }catch (Exception e){
            logger.error("decryptFromBase64 exception ", e);
        }
        return null;
    }

    static {
        int size = fixedMacs.size();
        String str = "0123456789";
        for (int i = size; i < FIX_MAC_NUM;i++){
            fixedMacs.offer(StringUtils.repeat(str.charAt(i%str.length()), 12));
        }
    }
}
