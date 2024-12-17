package mock.service;

import java.io.IOException;
import java.nio.channels.DatagramChannel;
import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mock.enums.EnumClientType;
import mock.model.*;


public class MockClientManager {
    private static Logger logger = LoggerFactory.getLogger(MockClientManager.class);

    private static class ManagerHoder{
        private static final MockClientManager instance = new MockClientManager();
    }

    private MockClientManager() {

    }

    public final static MockClientManager getInstance() {
        return ManagerHoder.instance;
    }
    private Map<Integer, LinkedBlockingQueue<MockRapidPacket> > client_queue_map; // 每个客户端端对应一个队列

    private Map<Long, BaseClient> clients_map = new HashMap<>(); // 创建的客户端列表 以clientId 为界限 
    
    void init() {
        logger.info("init {}", MockClientManager.class);
    }

    /*可能返回 null */
    public BaseClient getClient(Long clientId) {
        return clients_map.get(clientId);
    }

    public BaseClient getClient(String clientType) {
        EnumClientType type = EnumClientType.eUnkonwnCLient;
        String typeStr = StringUtils.lowerCase(clientType);

        if (StringUtils.equals(typeStr, "live")) {
            type = EnumClientType.eLiveClient;
        } else if (StringUtils.equals(typeStr, "ptt")) {
            type = EnumClientType.ePttClient;
        } else if (StringUtils.equals(typeStr, "vod")) {
            type = EnumClientType.eVodClient;
        }

        for (Map.Entry<Long, BaseClient> entry: clients_map.entrySet()) {
            BaseClient client = entry.getValue();
            if (type == EnumClientType.eLiveClient && client instanceof MockLiveClient) {
                return entry.getValue();
            }
            // TODO 加上其他类型的客户端处理代码
        }
        return null;
    }


    public Map<String, String> getListenServerAddress(Long clientId) {
        BaseClient client = clients_map.get(clientId);
        if (client == null) {
            return null;
        }
        DatagramChannel channel = client.getUdpChannel();
        if (channel == null) {
            return null;
        }
        Map<String, String> res = new HashMap<>();
        try {
            res.put("local", channel.getLocalAddress().toString());
            res.put("remote", channel.getRemoteAddress().toString());
        } catch (IOException e) {
            e.printStackTrace();
            logger.error("get addr error {} ", e);
            return null;
        }
        return res;
    }

    public List<BaseClient> getClients(List<Long> ids) {
        List<BaseClient> clients = new LinkedList<>();
        for (Long i : ids) {
            if (clients_map.containsKey(i)) {
                clients.add(clients_map.get(i));
            }
        } 
        return clients;
    }

    public List<BaseClient> getAllClients() {
        Collection<BaseClient> clients = clients_map.values();
        return new ArrayList<>(clients);
    }

    public void addClient(BaseClient client) {
        if (client == null) {
            return;
        }
        clients_map.put(client.getClientId(), client);
    }

//     // public void onLogoutUser(int percent){
//     //     int userNum = clientMap.size();
//     //     int needLogoutNum = Math.min(percent, 100)*userNum/100;
//     //     boolean headOrTail = flagMap.get(TimerTask.LogoutTask.class);
//     //     Set<ClientSimulator> clientSet = chooseClient(needLogoutNum, userNum, headOrTail);
//     //     flagMap.put(TimerTask.LogoutTask.class, !headOrTail);

//     //     for(ClientSimulator client : clientSet){
//     //         client.getClientInfo().sendLogout();
//     //     }
//     //     logger.info("behavior do logout percent:{} userNum:{}, totalUserNum:{}", percent, needLogoutNum, userNum);
//     // }

//     // public void onPrtNotOk(int percent){
//     //     int userNum = clientMap.size();
//     //     int needSwitchNum = Math.min(percent, 100)*userNum/100;
//     //     boolean headOrTail = flagMap.get(TimerTask.PrtNotOkTask.class);
//     //     Set<ClientSimulator> clientSet = chooseClient(needSwitchNum, userNum, headOrTail);
//     //     flagMap.put(TimerTask.PrtNotOkTask.class, !headOrTail);

//     //     for(ClientSimulator client : clientSet){
//     //         client.getClientInfo().prtOk = false;
//     //     }
//     //     logger.info("behavior do set prt not ok percent:{}, num:{}, totalUserNum:{}", percent, needSwitchNum, userNum);
//     // }

//     // public void onChangeBandwidth(int percent){
//     //     playServerSimulateService.setGlobalNormalBandwidthPercent(percent);
//     //     logger.info("behavior do change bandwidth percent:{}", percent);
//     // }



//     @Override
//     public void userSwitchChannel(int percent, int interval, int hasLogoutPercent){
//         if(StressingConfig.singleton().isEnableLoopRunCase()) {
//             tasks.put(TimerTask.SwitchChannelTask.class,
//                     new TimerTask.SwitchChannelTask(System.currentTimeMillis(), interval, percent, hasLogoutPercent));
//             flagMap.put(TimerTask.SwitchChannelTask.class, true);
//         }
//         else
//             onSwitchChannel(percent, hasLogoutPercent);
//         logger.info("behavior will switch channel, percent:{}, hasLogoutPercent:{}", percent, hasLogoutPercent);
//     }

//     @Override
//     public void SetChannelCount(byte distributeType, int channelCount,
//                                 Map<Integer, Integer> hotChannelMap, int channel4KPercent, int channel4KHotPercent){
//         setTestScope(channelCount);
//         List<StreamChannelItem> streamKeyPool = new LinkedList<>();
//         int realTestScope = Math.min(cfgService.getStreams().length, testScope);
//         for(int i = 0; i < realTestScope; i++)
//             streamKeyPool.add(cfgService.getStreams()[i]);
//         LinkedList<ClientSimulator> clients = new LinkedList<>(clientMap.values());
//         ConcurrentSkipListMap<Integer, Integer> hotChannelSortMap = (ConcurrentSkipListMap<Integer, Integer>) hotChannelMap;
//         Map.Entry<Integer, Integer> lastEntry = hotChannelSortMap.lastEntry();
//         int totalUserNum = clientMap.size();
//         hotChannelSet.clear();
//         channel4KSet.clear();
//         while(lastEntry != null){
//             for(int i = 0; i < lastEntry.getValue(); i++){
//                 StreamChannelItem item = streamKeyPool.remove(RandomUtils.nextInt(0, streamKeyPool.size()));
//                 for(int j = 0; j < totalUserNum*lastEntry.getKey()/100 && clients.size() > 0; j++) {
//                     BaseClientInfo clientInfo = clients.removeLast().getClientInfo();
//                     if(clientInfo instanceof LiveClientInfo) {
//                         ((LiveClientInfo) clientInfo).streamKey = item.getChannelId();
//                         ((LiveClientInfo) clientInfo).mediaCode = item.getStreamId();
//                     }
//                     clientInfo.sendLogout();
//                     hotChannelSet.put(item.getChannelId(), Boolean.TRUE);
//                 }
//             }
//             lastEntry = hotChannelSortMap.lowerEntry(lastEntry.getKey());
//         }

//         int channel4KNum = channel4KPercent*realTestScope/100;
//         int hot4KNum = channel4KNum*channel4KHotPercent/100;
//         int k = 0;
//         for(String streamKey: hotChannelSet.keySet()){
//             if(k < hot4KNum)
//                 channel4KSet.put(streamKey, Boolean.TRUE);
//             else
//                 break;
//             k++;
//         }
//         int remain4KNum = Math.min(Math.max(channel4KNum - hot4KNum, 0), streamKeyPool.size());
//         for(int j = 0; j < remain4KNum; j++)
//             channel4KSet.put(streamKeyPool.get(j).getChannelId(), Boolean.TRUE);

//         for(ClientSimulator clientSimulator : clients){
//             StreamChannelItem item = streamKeyPool.get(clientSimulator.getClientId()%streamKeyPool.size());
//             BaseClientInfo clientInfo = clientSimulator.getClientInfo();
//             if(clientInfo instanceof LiveClientInfo) {
//                 ((LiveClientInfo) clientInfo).streamKey = item.getChannelId();
//                 ((LiveClientInfo) clientInfo).mediaCode = item.getStreamId();
//             }
//             clientInfo.sendLogout();
//         }
// //        userSwitchChannel(100);
//         logger.info("behavior seted channel count:{}, distributeType:{}, hotMap:{}, 4kPercent:{}, 4kHotPercent:{}", channelCount, distributeType, hotChannelMap, channel4KNum, channel4KHotPercent);
//     }

//     @Override
//     public void prtNotOkLogin(int percent, int interval){
//         if(StressingConfig.singleton().isEnableLoopRunCase()) {
//             tasks.put(TimerTask.PrtNotOkTask.class, new TimerTask.PrtNotOkTask(System.currentTimeMillis(), interval, percent));
//             flagMap.put(TimerTask.PrtNotOkTask.class, true);
//         }
//         else
//             onPrtNotOk(percent);
//         logger.info("behavior will do prtNotOkLogin, percent:{}", percent);
//     }

//     @Override
//     public void prtLossRate(String prtName, int percent, int interval){
//         playServerSimulateService.setPlayServerLossRate(prtName, percent);
//         logger.info("behavior done change prt:{} loss rate:{}", prtName, percent);
//     }

//     @Override
//     public void changeUserBandwidth(Map<Long, BandwidthLine> bandwidthWave){
//         tasks.put(TimerTask.BandwidthWaveTask.class, new TimerTask.BandwidthWaveTask(System.currentTimeMillis(), 10000L, bandwidthWave));
//         logger.info("behavior will do bandwidth change :{}", bandwidthWave);
//     }

//     @Override
//     public void changeAllPrtBandwidth(int bandwidthPercentOfNormal){
//         onChangeBandwidth(bandwidthPercentOfNormal);
//         logger.info("behavior do change bandwidth percent:{}", bandwidthPercentOfNormal);
//     }

//     @Override
//     public void sleep(int time){}

//     public String chooseReleaseId(TreeMap<String, AtomicInteger> releaseMap){
//         Map.Entry<String, AtomicInteger> firstReleaseEntry = releaseMap.firstEntry();
//         if(releaseMap.size() > 0) {
//             if(firstReleaseEntry.getValue().decrementAndGet() <= 0)
//                 releaseMap.remove(firstReleaseEntry.getKey());
//             return firstReleaseEntry.getKey();
//         }
//         return "test";
//     }

//     public void registerClient(int clientId, ClientSimulator client){
//         clientMap.put(clientId, client);
//     }

//     public ClientSimulator removeClient(int clientId){
//         return clientMap.remove(clientId);
//     }

//     public Map<Integer, ClientSimulator> getClientMap(){
//         return clientMap;
//     }

//     public int getTestScope() {
//         return testScope;
//     }

//     public void setTestScope(int testScope) {
//         this.testScope = Math.min(testScope, cfgService.getStreams().length);
//     }

//     public boolean is4KChannel(String streamKey){
//         return channel4KSet.containsKey(streamKey);

}

