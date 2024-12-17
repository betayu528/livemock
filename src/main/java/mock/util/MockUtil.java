package mock.util;

import org.apache.commons.lang3.RandomUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.system.SystemProperties;

import rapid.cloud.cdn.common.constant.EnumTrackerServerType;
import rapid.cloud.cdn.common.mrttoken.EMRTTokenField;
import rapid.cloud.cdn.common.mrttoken.MRTToken;
import rapid.cloud.cdn.common.mrttoken.MRTTokenTools;
import rapid.cloud.cdn.common.security.Base64;
import rapid.cloud.cdn.protocol.p2p.Constants;
import rapid.cloud.cdn.protocol.servAddr.IPGroups;
import rapid.cloud.cdn.protocol.servAddr.ServerInfo;
import rapid.cloud.cdn.protocol.utils.P2PUtils;
import rapid.cloud.cdn.protocol.security.AESSecurity;
import rapid.cloud.server.common.pbtoken.ChannelBRTTokenTools;

import javax.crypto.Cipher;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class MockUtil {
    private final static Logger logger = LoggerFactory.getLogger(MockUtil.class);

    private static final int HASH_LEN = 20;
    static Cipher aesSecurity = null;

    public MockUtil () {
       
    }

    public static String buildClientKey(long sessKey1, long sessKey2) {
        String clientKey = sessKey1 + "-" + sessKey2;
        return clientKey;
    }

    public static IPGroups buildIPGroup(long connectId, String ... hosts ) {
        IPGroups iPs = new IPGroups(connectId,hosts.length, Constants.PEER_TYPE);
        int i = 0;
        for(String host:hosts){
            InetSocketAddress addr = P2PUtils.hostToSocketAddr(host);
            iPs.pushIP(i++, P2PUtils.getRawIPInt(addr), (short)addr.getPort());
        }
        return iPs;
    }

    public static String getRandomString(int strlen) {
        String buff = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
        char[] indexBuff = buff.toCharArray();
        char[] str = new char[strlen];
        for(int i = 0; i < strlen; i++){
            str[i] = indexBuff[RandomUtils.nextInt(0, indexBuff.length)];
        }
        return String.copyValueOf(str);
    }

    public static List<byte[]> buildHashPool(int hashPoolSize){
        List<byte[]> hashPool = new ArrayList<>(hashPoolSize > 0 ? hashPoolSize : 1);

        for(int i = 0; i < hashPoolSize; i++){
            byte[] hash = new byte[HASH_LEN];

            for(int j = 0; j < HASH_LEN - 4; j++)//前面填0
                hash[j] = 0x00;
            hash[HASH_LEN - 4] = (byte) ((i & 0xFF000000) >>> 24);
            hash[HASH_LEN - 3] = (byte) ((i & 0x00FF0000) >> 16);
            hash[HASH_LEN - 2] = (byte) ((i & 0x0000FF00) >> 8);
            hash[HASH_LEN - 1] = (byte) (i & 0x000000FF);
            hashPool.add(hash);
            hash[0] = (byte) 0xEE;
        }
        return hashPool;
    }
    
    public static HashMap<String, String> buildRandomAttrMap(String mac, String ip, String releaseId, int trackerType){
        HashMap<String, String> attrMap = new HashMap<>();
        attrMap.put("ADID", "266d6cad219e0304");
        //attrMap.put("CODE", streamPair.getKey());
        attrMap.put("DTYP", "1");
        attrMap.put("UID", mac);
        attrMap.put("ASID", "b01-btvkids-513511-0");
        //attrMap.put("CHNID", streamPair.getValue());
        attrMap.put("PRID", "null");
        attrMap.put("RANDSTRING1", getRandomString(RandomUtils.nextInt(16, 25)));
        attrMap.put("APPVER", "testVersion");
        
        if(trackerType == EnumTrackerServerType.eLiveTracker.ordinal()){
            attrMap.put("TOKEN", ChannelBRTTokenTools.cryptBRTToken(mac, ip, 10));
            attrMap.put("RELEASEID", releaseId);
            attrMap.put("APKSN", mac);
        } else {
            MRTToken mrtToken = new MRTToken();
            mrtToken.put(EMRTTokenField.ACCOUNT_TYPE, "123");
            mrtToken.put(EMRTTokenField.ACCOUNT_ID, mac);
            mrtToken.put(EMRTTokenField.DID, mac);
            mrtToken.put(EMRTTokenField.CLIENT_IP, ip);
            mrtToken.put(EMRTTokenField.VALID_MINUTES, "10");
            mrtToken.put(EMRTTokenField.CREATE_TS, String.valueOf(System.currentTimeMillis()/1000));
            attrMap.put("TOKEN", MRTTokenTools.encode(mrtToken, "gvqlgfwB6oufvfO4j=jb"));
            attrMap.put("ASN", mac);
            attrMap.put("PSN", mac + "-123");
            attrMap.put("VID", releaseId);
        }
        attrMap.put("RANDSTRING2", getRandomString(RandomUtils.nextInt(16, 25)));
        return attrMap;
    }

    public static String convertAttrMap2Array(Map<String, String> inMap){
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

    static {
        try{
            aesSecurity = AESSecurity.createFileCipher(P2PUtils.getDATAKey(), 0, Cipher.DECRYPT_MODE);
        }catch (Exception e){
            logger.error("create decryptCipher exception", e);
        }
    }
    
    public static String encryptToBase64String(byte[] data){
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

    public static String toConnectString(Map<String, String> mapObject) {
        StringBuilder strBuilder = new StringBuilder();
        boolean flag = true;
        for (Map.Entry<String, String> entry : mapObject.entrySet()) {
            if (!flag) {
                strBuilder.append("&");
            } else {
                flag = false;
            }
            strBuilder.append(entry.getKey()).append("=").append(entry.getValue());
        }

        // 转化的字符串须是 8 的倍数， 不足的位置用 & 补足
        int padd = 8 - strBuilder.length() % 8;
        for (int i = 0; i < padd; i++) {
            strBuilder.append("&");
        }
        return strBuilder.toString();
    }

    
    public static String bytesToHex(byte [] byteArray) {
        final char[] hexArray = "0123456789ABCDEF".toCharArray();
        char[] hexChars = new char[byteArray.length * 2];
        for ( int j = 0; j < byteArray.length; j++ ) {
            int v = byteArray[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    public static String getRandomDidString(int size) {
        final char [] availStr = "1234567890".toCharArray();
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < size; i++) {
            int index = RandomUtils.nextInt(0, 10);
            builder.append(availStr[index]);
        }
        return builder.toString();
    }

    public static String getRandomMac() {
        String buff = "0123456789abcdef";
        char[] indexBuff = buff.toCharArray();
        char[] mac = new char[12];
        for(int i = 0; i < mac.length; i++){
            mac[i] = indexBuff[RandomUtils.nextInt(0, indexBuff.length)];
        }
        return String.copyValueOf(mac);
    }

    public static byte[] getRandomClientStreamHash() {
        String buff = "0123456789abcdef";
        char[] indexBuff = buff.toCharArray();
        byte[] hash = new byte[HASH_LEN];
        for(int i = 0; i < hash.length; i++){
            hash[i] = (byte) indexBuff[RandomUtils.nextInt(0, indexBuff.length)];
        }
        return hash;
    }

    public static Map<String, String> stringToMap(String str){
        Map<String, String> dicts = new HashMap<>();
        String[] attrs = str.split("&");
        for (String attrPair : attrs) {
            String[] attrArray = attrPair.split("=");
            String key = attrArray[0];
            String value = null;
            if (attrArray.length > 1) {
                value = attrArray[1];
            }
            dicts.put(key, value);
        }
        return dicts;
    }

    public static IPGroups buildIPGroup(long connectId, int ip, int port) {
        IPGroups iPs = new IPGroups(connectId, 1, Constants.PEER_TYPE);
        iPs.pushIP(0, ip, (short) port);
        return iPs;
    }

    public static void main(String[] args) {
        // try {
        //     DatagramChannel channel = DatagramChannel.open();
        //     InetSocketAddress addr = new InetSocketAddress("147.135.44.184", 37000);
        //     ByteBuffer wantToSend = ByteBuffer.wrap("hello world".getBytes());
        //     channel.send(wantToSend, addr);
        // } catch (IOException e) {
        //     // TODO: handle exception
        //     e.printStackTrace();
        // }
        // System.out.println(MockUtil.getRandomDidString(12));
        // System.out.println(MockUtil.getRandomMac());

            ServerInfo info = new ServerInfo();

        ByteBuffer buff = ByteBuffer.allocate(4096);
        buff.put(new byte[100]);
        buff.position();
        System.out.println("buff cap: " + buff.capacity() + "data size: " + buff.position());

        System.out.println(Long.MAX_VALUE);
        System.out.println("xxxxxxx");


        HashSet<Integer> set = new HashSet<>();
        set.add(89);
        set.add(90);
        set.add(91);
        System.out.println(set.contains(91));
        set.add(91);
        set.remove(89);
        
        System.out.println(set.contains(new Integer(89)));
        // try {
        //     DatagramChannel channel = DatagramChannel.open();
        //     System.out.println(channel.getLocalAddress());
        //     String ip = "58.250.250.235";
        //     String localIP = "10.10.4.89";
        //     InetSocketAddress addr = new InetSocketAddress("0.0.0.0", 7000);

        //     channel.configureBlocking(false);
        //     channel.bind(addr);
        //     System.out.println(channel.getLocalAddress());
        //     Selector selector = Selector.open();
        //     channel.register(selector, SelectionKey.OP_READ);

        //     System.out.println("haha");
        //     while (selector.select() > 0) {
        //         Iterator<SelectionKey> it = selector.selectedKeys().iterator();
        //         while (it.hasNext()) {
        //             SelectionKey sk = it.next();
        //             if (sk.isReadable()) {
        //                 ByteBuffer buff = ByteBuffer.allocate(1024);
        //                 channel.receive(buff);
        //                 buff.flip();
        //                 System.out.println(new String(buff.array(), 0, buff.limit()));
        //             }
        //         }
        //         it.remove();
        //     }
        // } catch (IOException e) {
        //     e.printStackTrace();
        // }
    }

}
