package mock.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ChannelModel {
    /*频道 */
    @JsonProperty
    private String channelId;

    @JsonProperty
    private String streamId;

    @JsonProperty
    private String channelName = "default_test"; // mapp goose 
    
    public ChannelModel(String channelId, String streamId) {
        this.channelId = channelId;
        this.streamId = streamId;
    }

    public void setChannelId(String channelId) {
        this.channelId = channelId;
    }

    public String getChannelId() {
        return channelId;
    }

    public void setStreamKey(String streamId) {
        this.streamId = streamId;
    }

    public String getStreamId() {
        return streamId;
    }

    public void setChannealName(String name) {
        this.channelName = name;
    }

    public String getChannelName() {
        return channelName;
    }
}
