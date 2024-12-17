package mock.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mock.model.BaseClient;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.List;

public abstract class BaseMsgHandler {
    protected final static Logger logger = LoggerFactory.getLogger(BaseMsgHandler.class);
    protected static long MSG_PREPARE_TIME = 100;
    protected int curSeq = 0;
    protected long lastSendTime = 0;
    protected long lastRecvTime = 0;
    protected boolean isConnectMsg = false;
    private InetSocketAddress[] sendTo = null;
    //protected static PlayServerInteractInterface playServerInteractInterface;

    protected BaseMsgHandler(){}

    public ByteBuffer preHandler(BaseClient client){
        return null;
    }

    public void afterHandler(BaseClient client){}

    public void clear(){
        lastSendTime = 0;
        lastRecvTime = 0;
    }

    public boolean isConnectMsg() {
        return isConnectMsg;
    }

    public void setConnectMsg(boolean connectMsg) {
        isConnectMsg = connectMsg;
    }

    public InetSocketAddress[] getSendTo() {
        return sendTo;
    }

    public void setSendTo(List<InetSocketAddress> sendTo) {
        this.sendTo = (InetSocketAddress[]) sendTo.toArray();
    }

    // public static void setPlayServerInteractInterface(PlayServerInteractInterface playServerInteractInterface_) {
    //     playServerInteractInterface = playServerInteractInterface_;
    // }
}

