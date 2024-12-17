package mock.model;

import rapid.cloud.cdn.socket.model.RapidPacket;

public interface IBaseClient {
    public void login();
    public void logout();

    // @Override
    // public void switchChannel();

    // @Override
    // public void changeChannel(ChannelModel channel);
    public void processPacket(RapidPacket packet);
}
