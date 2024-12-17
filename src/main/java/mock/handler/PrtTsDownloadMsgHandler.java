package mock.handler;

import java.nio.ByteBuffer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mock.model.BaseClient;

public class PrtTsDownloadMsgHandler extends BaseMsgHandler{
    Logger logger = LoggerFactory.getLogger(PrtTsDownloadMsgHandler.class);

    @Override
    public ByteBuffer preHandler(BaseClient clientInfo) {
        return null;
    }

    @Override
    public void afterHandler(BaseClient clientInfo) {
        
    }

    public void onRecv(BaseClient client) {
        logger.info("client {}", client.getClientId());
    }
}
