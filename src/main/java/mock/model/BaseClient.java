package mock.model;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SocketChannel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mock.enums.EnumClientStatus;
import rapid.cloud.cdn.protocol.servAddr.ServerInfo;
import rapid.cloud.cdn.socket.model.RapidPacket;

public class BaseClient implements IBaseClient {
    private static final Logger logger = LoggerFactory.getLogger(BaseClient.class);

    protected String did = null;
    protected Long clientId;
    protected String releaseId = "test_" + System.currentTimeMillis();
    public volatile long lastLoginTime = 0;
    public volatile int certifyCode = 0;
    public AtomicInteger curReqId = new AtomicInteger(0); 
    /*
     * 这个序列号很重要, 心跳包必须从登陆返回的序号包保持在一定范围 才能互送心跳
     */
    ChannelModel playChannel = null; // 频道和客户端 1对1 一个时间端，一个客户端对应一个频道 

    protected EnumClientStatus status;
    protected DatagramChannel udpChannel;
    protected SocketChannel tcpChannel = null;

    protected List<ServerInfo> relayServerList = new ArrayList<>();
    protected List<ServerInfo> prtServerList = new ArrayList<>();
    protected List<ServerInfo> liveServerAddrList = new ArrayList<>(10);
    protected String trackerServer = "";

    protected String listenAddress;
    protected int clientIp = 0;
    protected int listenPort;
    protected boolean prtOk;

    public BaseClient(Long clientId) {
        this.clientId = clientId;
        status = EnumClientStatus.eOffline;
    }

    public void setListenAddress(String listenAddress) {
        this.listenAddress = listenAddress;
    }

    public String getListenAddress() {
        return this.listenAddress;
    }

    public List<ServerInfo> getLiveServerAddressList() {
        return this.liveServerAddrList;
    }

    public void setLiveServerAddressList(List<ServerInfo> liveServerList) {
        this.liveServerAddrList = liveServerList;
    }

    public String getTrackerServer() {
        return this.trackerServer;
    }

    public void setTrackerServer(String trackerServer) {
        this.trackerServer = trackerServer;
    }

    public int getListenPort() {
        return this.listenPort;
    }

    public void setListenPort(int listenPort) {
        this.listenPort = listenPort;
    }

    public Long getClientId() {
        return clientId;
    }

    void setClientId(Long connId) {
        this.clientId = connId;
    }

    void setPlayChannel(ChannelModel channel) {
        this.playChannel = channel;
    }

    ChannelModel getPlayChannel() {
        return this.playChannel;
    }

    public void switchChannel(ChannelModel channel) {
        if (channel != this.playChannel) {
            setPlayChannel(channel);
        }
    }
    
    public void setReleaseId(String releaseId) {
        this.releaseId = releaseId;
    }

    String getRelString() {
        return this.releaseId;
    }

    public void setDid(String did) {
        this.did = did;
    }

    public String getDid() {
        return this.did;
    }

    public int getClientIp() {
        return this.clientIp;
    }

    public void setClientIp(int clientIp) {
        this.clientIp = clientIp;
    }

    
    public DatagramChannel getUdpChannel() {
        return udpChannel;
    }

    public void setUdpChannel(DatagramChannel channel) {
        this.udpChannel = channel;
    }

    public SocketChannel getTcpChannel() {
        return this.tcpChannel;
    }

    public void setTcpChannel(SocketChannel channel) {
        tcpChannel = channel;
    }

    public boolean isPrtOk() {
        return this.prtOk;
    }

    public void setPrtOk(boolean prtOk) {
        this.prtOk = prtOk;
    }

    @Override
    public void login() {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void logout() {
        // TODO Auto-generated method stub
        
    }

    // @Override
    // public void switchChannel() {
    //     // TODO Auto-generated method stub
        
    // }

    // @Override
    // public void changeChannel() {
    //     // TODO Auto-generated method stub
        
    // }
    @Override
    public void processPacket(RapidPacket packet) {
        logger.info("do nothing");
    }

    public void setStatus(EnumClientStatus eoffline) {
        this.status = eoffline;
    }

    public EnumClientStatus getStatus() {
        return this.status;
    }

    public int getCertifyCode() {
        return this.certifyCode;
    }

    public String getReleaseId() {
        return this.releaseId;
    }

    public boolean isTrackerLoginSucc() {
        return false;
    }

    public AtomicInteger getCurReqId() {
        return this.curReqId;
    }

    public void setRelayServerList(List<ServerInfo> serverList) {
        this.relayServerList = serverList;
    }

    public List<ServerInfo> getRelayServerList() {
        return relayServerList;
    }

    public void setPrtServerList(List<ServerInfo> prtServerList) {
        this.prtServerList = prtServerList;
    }

    public List<ServerInfo> getPrtServerList() {
        return this.prtServerList;
    }
}
