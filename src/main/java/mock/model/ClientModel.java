package mock.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnore;

import mock.enums.EnumClientStatus;
import rapid.cloud.cdn.protocol.servAddr.ServerInfo;

public class ClientModel {
    private Long clientId;
    private String did = null;
    private String name;
    private String releaseId = "";
    private EnumClientStatus status = EnumClientStatus.eOffline;

    public String listenAddress = "";
    public int clientIp = 0;
    private String trackerServer;
    private List<ServerInfo> liveServerList;
    
    public int listenPort = 0;
    public List<ServerInfo> relayServerList = new ArrayList<>();
    
    @JsonIgnore
    public volatile boolean prtOk = true;

    @JsonIgnore
    public boolean toDumpConnectMsg = true;

    public ClientModel() {
    }

    public ClientModel(Long clientId, String did, String name, String releaseId, String listenAddress, int clientIp,
     int listenPort, List<ServerInfo> liveAddrList, List<ServerInfo> relayServerList, String trackerServer) {
        this.clientId = clientId;
        this.did = did;
        this.name = name;
        this.releaseId = releaseId;
        this.listenAddress = listenAddress;
        this.clientIp = clientIp;
        this.listenPort = listenPort;
        this.trackerServer = trackerServer;
        this.relayServerList = relayServerList;
        this.liveServerList = liveAddrList;
    }

    public Long getClientId() {
        return this.clientId;
    }

    public void setClientId(Long clientId) {
        this.clientId = clientId;
    }

    public String getDid() {
        return this.did;
    }

    public void setDid(String did) {
        this.did = did;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getReleaseId() {
        return this.releaseId;
    }

    public void setReleaseId(String releaseId) {
        this.releaseId = releaseId;
    }

    public String getListenAddress() {
        return this.listenAddress;
    }

    public void setListenAddress(String listenAddress) {
        this.listenAddress = listenAddress;
    }

    public int getClientIp() {
        return this.clientIp;
    }

    public void setClientIp(int clientIp) {
        this.clientIp = clientIp;
    }

    public int getListenPort() {
        return this.listenPort;
    }

    public void setListenPort(int listenPort) {
        this.listenPort = listenPort;
    }

    public List<ServerInfo> getRelayServerList() {
        return this.relayServerList;
    }

    public void setRelayServerList(List<ServerInfo> relayServerList) {
        this.relayServerList = relayServerList;
    }

    public List<ServerInfo> getLiveServerList() {
        return this.liveServerList;
    }

    public void setLiveServerList(List<ServerInfo> liveServerList) {
        this.liveServerList = liveServerList;
    }

    public void setStatus(EnumClientStatus status) {
        this.status = status;
    }

    public EnumClientStatus getStatus() {
        return this.status;
    }

}
