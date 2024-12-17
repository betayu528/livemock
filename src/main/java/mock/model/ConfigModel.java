package mock.model;

public class ConfigModel {
    private String trackerServer;
    private String localIp;
    private String aesAuthPasswd;
    private Integer recvThreadNum;
    private Integer procThreadNum;
    private Integer sendThreadNum;
    private Integer clientMax;

    public ConfigModel() {
    }

    public ConfigModel(String trackerServer, String localIp, String aesAuthPasswd, Integer recvThreadNum,
     Integer procThreadNum, Integer sendThreadNum, Integer clientMax) {
        this.trackerServer = trackerServer;
        this.localIp = localIp;
        this.aesAuthPasswd = aesAuthPasswd;
        this.recvThreadNum = recvThreadNum;
        this.procThreadNum = procThreadNum;
        this.sendThreadNum = sendThreadNum;
        this.clientMax = clientMax;
    }

    public String getTrackerServer() {
        return this.trackerServer;
    }

    public void setTrackerServer(String trackerServer) {
        this.trackerServer = trackerServer;
    }

    public String getLocalIp() {
        return this.localIp;
    }

    public void setLocalIp(String localIp) {
        this.localIp = localIp;
    }

    public String getAesAuthPasswd() {
        return this.aesAuthPasswd;
    }

    public void setgetAesAuthPasswd(String aesAuthPasswd) {
        this.aesAuthPasswd = aesAuthPasswd;
    }

    public Integer getRecvThreadNum() {
        return this.recvThreadNum;
    }

    public void setRecvThreadNum(Integer recvThreadNum) {
        this.recvThreadNum = recvThreadNum;
    }

    public Integer getProcThreadNum() {
        return this.procThreadNum;
    }

    public void setProcThreadNum(Integer procThreadNum) {
        this.procThreadNum = procThreadNum;
    }

    public Integer getSendThreadNum() {
        return this.sendThreadNum;
    }

    public void setSendThreadNum(Integer sendThreadNum) {
        this.sendThreadNum = sendThreadNum;
    }

    public Integer getClientMax() {
        return this.clientMax;
    }

    public void setClientMax(Integer clientMax) {
        this.clientMax = clientMax;
    }
    
    @Override
    public String toString() {
        return "{" +
            " trackerServer='" + getTrackerServer() + "'" +
            ", localIp='" + getLocalIp() + "'" +
            ", aesAuthPasswd='" + getAesAuthPasswd() + "'" +
            ", recvThreadNum='" + getRecvThreadNum() + "'" +
            ", procThreadNum='" + getProcThreadNum() + "'" +
            ", sendThreadNum='" + getSendThreadNum() + "'" +
            ", clientMax='" + getClientMax() + "'" +
            "}";
    }
}

    
