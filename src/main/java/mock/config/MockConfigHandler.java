package mock.config;

import java.io.FileReader;
import java.io.IOException;

import org.slf4j.*;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import mock.model.ConfigModel;;

public class MockConfigHandler {
    private final Logger logger = LoggerFactory.getLogger(MockConfigHandler.class);

    private final String CONFIG_PATH = "cfg/MockClientConfig.json";
    private final String APP_CONFIG_PATH = "cfg/MockProxy.properties";

    private ConfigModel configModel = new ConfigModel();

    /****************************************必填项***********************************/

    //消息超时时间，超过即认为消息超时，将重新登录
    private int validResponseTime = 60000; //ms
    //心跳消息发送间隔
    private int announceSendInterval = 6000; 
    //请求邻居的时间间隔，必须 >= 心跳间隔时间,一般为心跳间隔的整数倍
    private int requestNeighborInterval = 18000;
    //SDP消息发送间隔
    private int SDPExchangeInterval = 7000;
    //req play server消息发送间隔
    private int reqPlayServerInterval = 90000;

    //token过期时间，登录中校验的token过期时间
    private int tokenValidMin = 10; // 10s

    private int recvThreadNum = 10;
    private int procThreadNum = 10;
    private int sendThreadNum = 10;

    private long dumpUser = 0;
    private boolean isSendNewHeartBeat2SM = false;
    private boolean isShareRateV90 = false;

    /********************************仅 MFC 使用************************************/
    //hash池子大小,相当于资源数量
    private int hashPoolSize = 200000;
    //单个用户上报的资源数
    private int shareNumPerUser = 10;
    //token校验密钥
    private String authPassword = "gvqlgfwB6oufvfO4j=jb";
    /**是否pm地址*/
    private boolean enablePmAddrReq = false;

    private String trackerServers;

    private static class InstanceHolder {
        private static final MockConfigHandler instance = new MockConfigHandler();
    }

    private MockConfigHandler() {
        loadConfig();
    }

    public void loadConfig() {
        try {
            Gson gson = new Gson();
            
            configModel = gson.fromJson(new FileReader(CONFIG_PATH), new TypeToken<ConfigModel>(){}.getType());
            if (configModel == null) {
                logger.error("load config failed, {}", CONFIG_PATH);
                return;
            }
            System.out.println(configModel);
            logger.info("load config succ, tracker ip:{}!", configModel.getTrackerServer());
            this.authPassword = configModel.getAesAuthPasswd();
            this.trackerServers = configModel.getTrackerServer();
            this.recvThreadNum = configModel.getRecvThreadNum();
            this.sendThreadNum = configModel.getProcThreadNum();
            configModel.getRecvThreadNum();

        } catch (IOException e) {
            logger.error("load config failed {} not found ", CONFIG_PATH);
            logger.error(e.getStackTrace().toString());
        }
    }

    public final static MockConfigHandler getInstance() {
        return InstanceHolder.instance;
    }

    public ConfigModel getConfigModel() {
        return configModel;
    }


    public int getValidResponseTime() {
        return this.validResponseTime;
    }

    public void setValidResponseTime(int validResponseTime) {
        this.validResponseTime = validResponseTime;
    }

    public int getAnnounceSendInterval() {
        return this.announceSendInterval;
    }

    public void setAnnounceSendInterval(int announceSendInterval) {
        this.announceSendInterval = announceSendInterval;
    }

    public boolean isSendNewHeartBeat2SM() {
        return this.isSendNewHeartBeat2SM;
    }

    public void setSendNewHeartBeat2SM(boolean isSendNewHeartBeat2SM) {
        this.isSendNewHeartBeat2SM = isSendNewHeartBeat2SM;
    }

    public boolean isShareRateV90() {
        return this.isShareRateV90;
    }

    public void setShareRateV90(boolean isShareRateV90) {
        this.isShareRateV90 = isShareRateV90;
    }

    public static void main(String[] args) {
        String tracker = MockConfigHandler.getInstance().getConfigModel().getTrackerServer();
        System.out.println(tracker);

        Integer sendThreadNum = MockConfigHandler.getInstance().getConfigModel().getRecvThreadNum();
        System.out.println(sendThreadNum);
    }
}
