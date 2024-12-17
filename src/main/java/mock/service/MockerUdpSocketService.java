package mock.service;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import mock.enums.EnumClientStatus;
import mock.model.*;
import rapid.cloud.cdn.protocol.protocol.TrackerPacketBuilderV2;
import rapid.cloud.cdn.socket.model.RapidPacket;

@Service
public class MockerUdpSocketService {
    /**
     * 创建并管理发包队列，维护客户id和队列的信息 
    */
    private static final Logger logger = LoggerFactory.getLogger(MockerUdpSocketService.class);
    @Autowired
    TrackerMessageHandleService trackerMessageHandleService;

    private SocketChannel tcpChannel; // tcp channel 
    private AtomicBoolean bSendStop = new AtomicBoolean(false);
    private AtomicBoolean bRecvStop = new AtomicBoolean(false);
    private AtomicInteger recvNumber = new AtomicInteger(0);
    private AtomicInteger sendNumber = new AtomicInteger(0);

    private final Integer THREAD_CNT = 5; //与频道对应
    private List<Selector> selector_list = new ArrayList<>();
    private List<DatagramChannel> channelList = new ArrayList<>();
    //LinkedBlockingQueue<MockRapidPacket> sendQueue;
    private Selector selector;

    private Map<Integer, LinkedBlockingQueue<MockRapidPacket>> sendMsgQueueMap;
    private Map<Integer, LinkedBlockingQueue<MockRapidPacket>> recvMsgQueueMap;

    @PostConstruct
    public void init() {
        logger.info("{} init ... ", MockerUdpSocketService.class);
        
        sendMsgQueueMap = new ConcurrentHashMap<>();
        recvMsgQueueMap = new ConcurrentHashMap<>();
        for (int i = 0; i < THREAD_CNT; ++i) {
            //sendMsgQueueMap.put(i, new LinkedBlockingQueue<>(410000));
            LinkedBlockingQueue<MockRapidPacket> queue = new LinkedBlockingQueue<MockRapidPacket>(410000);
            sendMsgQueueMap.put((Integer)i, queue);

            LinkedBlockingQueue<MockRapidPacket> recv_queue = new LinkedBlockingQueue<MockRapidPacket>(410000);
            recvMsgQueueMap.put((Integer)i, recv_queue);
            final int index = i;
            new Thread(new Runnable() {
                @Override
                public void run() { 
                    logger.info("thead-{} {} started!", Thread.currentThread().getId(), Thread.currentThread().getName());
                    sendTaskLoop(index);
                }
            }, "send_thread-" + index).start();
            logger.info("thead-{} {} started!", Thread.currentThread().getId(), Thread.currentThread().getName());
            System.out.println(System.currentTimeMillis() + " :" + this.getClass());
            RapidPacket.setPBServer(false);
            RapidPacket.setProtocolBuilder(new TrackerPacketBuilderV2());
            initialSeletors();
            startSockets();
        }   
    }

    public DatagramChannel getUdpChannel(Long clientId) {
        int index = (int) (clientId % THREAD_CNT);
        if (index < 0|| index > channelList.size()) {
            logger.error("channel total number: {}, clientId is {}, index is {}", channelList.size(), clientId, index);
            return null;
        }
        return channelList.get(index);
    }

    private void initialSeletors() {
        try {
            for (int i = 0; i < THREAD_CNT; ++i) {
                Selector seletor = Selector.open();
                selector_list.add(seletor);

                DatagramChannel channel = DatagramChannel.open();
                channel.configureBlocking(false);
                channel.socket().setSendBufferSize(51200);
                channel.socket().setReceiveBufferSize(51200);
                channel.socket().setSoTimeout(1000);
                channel.register(seletor, SelectionKey.OP_READ);
                channelList.add(channel);
            }

        } catch (ClosedChannelException e) {
            logger.error("initial failed");
        } catch (IOException e) {
            logger.error("initial failed", e);
        }
        
    }

    public void registerChannels(DatagramChannel channel) {
        try {
            //DatagramChannel ch = channel.open();
            if (!channel.isRegistered())
                channel.register(selector, SelectionKey.OP_READ);
        } catch (ClosedChannelException e) {
            e.printStackTrace();
        }
    }

    public Selector getSelector() {
        return selector;
    }

    public void startSockets() {
        logger.info("start sockets"); 
        for (int i = 0; i < THREAD_CNT; ++i) {
            final int THREAD_ID = i;
            new Thread(new Runnable() {
                @Override
                public void run () {
                    recvTaskLoop(THREAD_ID);
                }
            }, "recive-" + THREAD_ID).start();
            logger.info("thread-{} {} started", Thread.currentThread().getId(), Thread.currentThread().getName());
        }
    }
    
    @Scheduled(fixedDelay = 10 * 60 * 1000) 
    public void runstatus() {
        logger.info("recv message count : {}, send message count : {}", recvNumber.get(), sendNumber.get());
    }

    public int getSendQueueNumber() {
        return sendMsgQueueMap.values().size();
    }

    public void sendPacket(MockRapidPacket packet){
        logger.debug("enter send packet method");
        ByteBuffer buffer = packet.getRapidPacket().getByteBuffer();
        buffer.rewind();
        
        DatagramChannel udpChannel = packet.getUdpChannel();
        if(udpChannel != null) {
            try {
                for (InetSocketAddress addr: packet.getSocketInetAddress()) {
                    udpChannel.send(buffer, addr);
                    sendNumber.incrementAndGet();
                    logger.debug("send packet success buffer size: {}, send to {}",
                       buffer.capacity(), addr.getAddress().getHostAddress());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            logger.error("chanel is null");
            try {
                DatagramChannel channel = getUdpChannel(null) ;
                for (InetSocketAddress addr: packet.getSocketInetAddress()) {
                    channel.send(buffer, addr);
                    logger.debug("send packet success buffer size: {}, send to {}",
                       buffer.capacity(), addr.getAddress().getHostAddress() + ":" + addr.getPort());
                }
                
            } catch (IOException e) {
                logger.error("{}", e.toString());
                e.printStackTrace();
            }
        }
    }

    public void sendTaskLoop(int index) {
        logger.info("enter {} sendTaskLoop", this.getClass());
        long startTime = 0,endTime = 0,subTime = 0, lastLogTime = 0;
        int i = 0, sendNum = 0;
        LinkedBlockingQueue<MockRapidPacket> q = this.sendMsgQueueMap.get(index);

        while(!bSendStop.get()) {
            try {
                startTime = System.currentTimeMillis();
                for(i = 1; i <= 100; i++) {
                    MockRapidPacket packet = q.take();
                    if (packet == null) {
                        logger.error("packect is null");
                        break;
                    }
                    logger.info("get message {}", packet.toString());
                    this.sendPacket(packet);
                }

                sendNum += i;
                endTime = System.currentTimeMillis();
                subTime = endTime - startTime;
                if (endTime - lastLogTime > 10000) {
                    logger.info("send-{}:one loop send time:{}, {}ms send num:{}",
                            Thread.currentThread().getName(), subTime, endTime - lastLogTime, sendNum);
                    lastLogTime = endTime;
                    sendNum = 0;
                }
                
                Thread.sleep(1);
            }catch (InterruptedException ie) {
                logger.error("sleep exception,", ie);
            }catch (Exception e){
                logger.error("send error,",e);
            }
            logger.error("thread send-{} exit", Thread.currentThread().getName());
        }
    }

    public void recvTaskLoop(int index) {
        logger.info("enter recv task loop thread-{} ", Thread.currentThread().getId());
        long startTime = 0,endTime = 0,subTime = 0;
        int readySize = 0;
        ByteBuffer buff = ByteBuffer.allocate(51200);
        while(!bRecvStop.get()) {
            try {
                Selector selector = selector_list.get(index);
                while (selector.select() > 0) {
                    startTime = System.currentTimeMillis();
                    Set<SelectionKey> readyKeys = selector.selectedKeys();
                    Iterator<SelectionKey> it = readyKeys.iterator();
                    readySize = readyKeys.size();
                    while (it.hasNext()) {
                        SelectionKey key = null;
                        try {
                            key = it.next();
                            it.remove();
                            DatagramChannel udpChannel = (DatagramChannel)key.channel();
                            if (key.isReadable()) {
                                receiveBuff(udpChannel, buff);
                            }
                        } catch (Exception e) {
                            logger.error("receive packet error", e);
                        }
                    }
                    try {
                        endTime = System.currentTimeMillis();
                        subTime = endTime - startTime;
                        if (subTime < 0)
                            Thread.sleep(5);
                        else if (subTime < 5)
                            Thread.sleep(5 - subTime);
                        else if (subTime <= 7)
                            Thread.sleep(3);
                        else if (subTime <= 20)
                            Thread.sleep(2);
                        else
                            Thread.sleep(1);
                    } catch (InterruptedException ie) {
                        logger.error("sleep exception,", ie);
                    }
                }
            } catch (Exception e) {
                logger.error("selector exception,", e);
            }

        }
        logger.error("thread receive-{} exit", Thread.currentThread().getName());
    }

    private void receiveBuff(DatagramChannel channel, ByteBuffer buff){
        logger.debug("Recv message !enter receiveBuff method");
        recvNumber.incrementAndGet();
        try {
            buff.clear();
            if(channel.receive(buff) == null) {
                return;
            }
            
            long now = System.currentTimeMillis();
            logger.info("recv message size: {}, now: {}", buff.position(), now);
            
            buff.flip();
            RapidPacket recvPkt = RapidPacket.parsePacket(buff);
            if(recvPkt != null){
                long clientId = recvPkt.getConnectIdFromHead(); // 这里保持connectId 和clientId 一致，因为tracker 是根据connectId 来辨识用户的
                BaseClient client = MockClientManager.getInstance().getClient(clientId);
                logger.info("clientId : {}. packet info -> mask:{}, msghead: {}, size: {}, peeraddr: {} , srcaddr: {}", 
                        clientId, recvPkt.getMask(), recvPkt.getMsgHead().getClass(),
                        recvPkt.getLength(), recvPkt.getPeerAddress(), recvPkt.getSourceAddress());

                recvPkt.setReceiveTimestamp(now);
                if (client == null) {
                    logger.error("get client error, clientId : {}", clientId);
                    return;
                }
                client.processPacket(recvPkt);
            }
        } catch (Exception e){
            logger.error("receive multi packet of client error", e);
        }
    }

    //调用 send packet 将包投递到发送队列中
    public void sendPacket(ByteBuffer byteBuffer, InetSocketAddress [] addrs, Long clientId, boolean isConnectMsg) {  
        if (addrs == null) {
            logger.error("param error, addr is null");
            return;
        }
        BaseClient client = MockClientManager.getInstance().getClient(clientId);
        if (client == null) {
            // 未绑定client 的消息将会特殊处理掉 
            logger.info("client Id is null, message will be transparently transmit");
            MockRapidPacket packet = new MockRapidPacket(byteBuffer, null, addrs);
            sendMsgQueueMap.get(0).offer(packet);
            return;
        }
        DatagramChannel channel = client.getUdpChannel();
        Long index = Thread.currentThread().getId() % THREAD_CNT;
        LinkedBlockingQueue<MockRapidPacket> q = sendMsgQueueMap.get(index.intValue());

        MockRapidPacket mpacket = new MockRapidPacket(byteBuffer, channel, addrs);
        q.offer(mpacket);

        logger.info("offer message to queue success");
    }

    public void switchChannel(BaseClient client, ChannelModel channelModel){
        MockLiveClient m_client = null;
        if (client instanceof MockLiveClient) {
            m_client = (MockLiveClient) client;
        } else {
            logger.error("mock client type is not live client");
            return;
        }

        if (m_client.getStatus() == EnumClientStatus.eOffline) {
            m_client.setChannelModel(channelModel);
        } else {
            m_client.logout();
        }
       
        m_client.login();
        logger.info("behavior do switch success");
    }

    public Boolean logout(BaseClient client) {
        return true;
    }
}
