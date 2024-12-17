package mock.service;

import java.io.IOException;
import java.net.BindException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.PostConstruct;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import mock.config.MockConfigHandler;
import mock.enums.EnumClientStatus;
import mock.enums.EnumClientType;
import mock.model.BaseClient;
import mock.model.MockLiveClient;
import mock.model.MockRapidPacket;
import rapid.cloud.cdn.protocol.utils.P2PUtils;


@Service
public class MockClientOperateService {
    Logger logger = LoggerFactory.getLogger(MockClientOperateService.class);
   
    @Autowired
    MockerUdpSocketService mockerUdpSocketService;

    @Autowired
    TrackerMessageHandleService trackerMessageHandleService;

    private AtomicInteger port = new AtomicInteger(6199);
    private Map<Long, DatagramChannel> client_channel_map = new HashMap<>();

    @PostConstruct
    public void init() {
    }
    
    public BaseClient createClient(String clientType, String channelId, String streamId) {
        String typeStr = StringUtils.lowerCase(clientType);
        EnumClientType type = EnumClientType.eUnkonwnCLient;
        if (StringUtils.equals(typeStr, "live")) {
            type = EnumClientType.eLiveClient;
        } else if (StringUtils.equals(typeStr, "ptt")) {
            type = EnumClientType.ePttClient;
        } else if (StringUtils.equals(typeStr, "vod")) {
            type = EnumClientType.eVodClient;
        }
        if (type == EnumClientType.eLiveClient) {
            return this.createLiveMockClient(channelId, streamId);
        }
        return null;
    }

    private BaseClient createLiveMockClient(String channelId, String streamId) {
        Long clientId = this.generateClientId();
        MockLiveClient client = new MockLiveClient(channelId, streamId, clientId);
        DatagramChannel udpChannel = null;
    
        String localIp = MockConfigHandler.getInstance().getConfigModel().getLocalIp();
        if (localIp == null) {
            localIp = "10.10.4.89"; // 默认的调试地址 
        }
        //String ip = "58.250.250.235";
        //String ip = "147.135.44.192";
        while (true) {
            if (port.getAndIncrement() >= 65535) {
                logger.error("no avalable port can be use");
                return null;
            }
            try {
                udpChannel = mockerUdpSocketService.getUdpChannel(clientId);
                if (udpChannel == null) {
                    logger.error("获取通道错误");
                    return null;
                }

                if (udpChannel.getLocalAddress() != null) { // 通道已经被绑定过, 直接复用即可
                    client.setUdpChannel(udpChannel);
                    client.setListenAddress(udpChannel.getLocalAddress().toString());
                    MockClientManager.getInstance().addClient(client);
                    client_channel_map.put(clientId, udpChannel);
                    return client;
                }
                
                udpChannel.configureBlocking(false);
                InetSocketAddress iaddr = new InetSocketAddress(localIp, port.get());
                udpChannel.bind(iaddr);
                udpChannel.socket().setReceiveBufferSize(51200);
                udpChannel.socket().setSendBufferSize(51200);
                udpChannel.socket().setSoTimeout(1000);
                client.setUdpChannel(udpChannel);
                client.setListenAddress(udpChannel.getLocalAddress().toString());
                MockClientManager.getInstance().addClient(client);
                client_channel_map.put(clientId, udpChannel);
                return client;
            } catch (BindException e) {
                logger.info("port {} alreay bound, use new port {}", port.get(), port.incrementAndGet());
            } catch (IOException e) {
                logger.error(e.toString());
                return null;
            }
        }
    }
    
    private Long generateClientId() {
        //9223372036854775807 Long MAX 
        final Long low_bound = 100000000000000L; // 14个0
        long maybe = -1L;
        Random rand = new Random();
        
        while (MockClientManager.getInstance().getClient(maybe) != null|| maybe == -1) {
            maybe = rand.nextLong();
            maybe = maybe < 0 ? maybe * (-1L) : maybe; //保证是非负正数
        }
        return new Long(maybe) + low_bound;
    }

    public boolean loginUser(Long clientId) {
         // TODO 此处需要区分直播和点播回看各种类型
         MockLiveClient client = (MockLiveClient)MockClientManager.getInstance().getClient(clientId);
         if (client == null) {
             return false;
         }
         
         ByteBuffer message = client.getLoginpacket();
         InetSocketAddress [] addrs = new InetSocketAddress[1];
         String trackerServer = MockConfigHandler.getInstance().getConfigModel().getTrackerServer();
         client.setTrackerServer(trackerServer);
         addrs[0] = P2PUtils.hostToSocketAddr(trackerServer);
         client.setStatus(EnumClientStatus.eTryToLogin); 
         mockerUdpSocketService.sendPacket(new MockRapidPacket(message, client.getUdpChannel(), addrs));
         return true;
    }

    public boolean logoutUser(Long clientId) {
         MockLiveClient client = (MockLiveClient)MockClientManager.getInstance().getClient(clientId);
         if (client == null) {
             return false;
         }
         ByteBuffer message = client.getLogoutPacket();
         client.setStatus(EnumClientStatus.eTryLogout);
         InetSocketAddress [] addrs = new InetSocketAddress[1];
         String trackerServer = client.getTrackerServer();
         addrs[0] = P2PUtils.hostToSocketAddr(trackerServer);
         mockerUdpSocketService.sendPacket(new MockRapidPacket(message, client.getUdpChannel(), addrs));
         return true;
    }

    //6s发一次心跳 
    @Scheduled(fixedDelay = 6 * 1000) 
    public void sendHeartBeatAnnounce() {
        List<BaseClient> allClients = MockClientManager.getInstance().getAllClients();
        for (BaseClient client: allClients) {
            MockLiveClient liveClient = (MockLiveClient)client; // 目前只处理直播客户端
            if (liveClient == null) {
                continue;
            }
            if (client.getStatus() == EnumClientStatus.eOnline) {
                ByteBuffer message = liveClient.getHeartBeatAnnoucePacket();
                if (message == null) {
                    logger.error("message wrap fail");
                    continue;
                }
                InetSocketAddress [] addrs = new InetSocketAddress[1];
                String trackerServer = liveClient.getTrackerServer();
                addrs[0] = P2PUtils.hostToSocketAddr(trackerServer);
                logger.info("send heartbeat packt client: {}, to-> {}", liveClient.getClientId(), trackerServer);
                mockerUdpSocketService.sendPacket(new MockRapidPacket(message, client.getUdpChannel(), addrs));
            }
            logger.debug("client {}, is offline without sending annouce packet", client.getClientId());
        } 
    }
    
    // 30 分钟运行一次
    @Scheduled(fixedDelay = 30 * 60 * 1000) 
    public void flushHistorySeqData() {
        logger.info("scheduled task, fresh history seq key");
        List<BaseClient>  allClients = MockClientManager.getInstance().getAllClients();
        for (BaseClient client: allClients) {
            MockLiveClient liveClient = (MockLiveClient)client;
            if (liveClient == null) {
                continue;
            }
            liveClient.flushHistorySeqData();
        }
    }
}
