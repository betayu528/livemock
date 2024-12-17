package mock.model;

import rapid.cloud.cdn.protocol.model.tracker.MorC.CatchupPlayServerAddrReqMsg;
import rapid.cloud.cdn.protocol.utils.TorrentUtils;

public class CatchupMediaPlayModel extends CatchupPlayServerAddrReqMsg {
    private String streamId = null;
    private String mediaId = null;
    private byte[] infoHash = null;

    public CatchupMediaPlayModel(String epgId, String streamId, long startTime, long duration){
        setEpgId(epgId);
        this.streamId = streamId;
        setStartUTC(startTime);
        setRange(duration);
    }

    public String getStreamId() {
        return streamId;
    }

    public void setStreamId(String streamId) {
        this.streamId = streamId;
    }


    public String getMediaId() {
        return mediaId;
    }

    public void setMediaId(String mediaId) {
        this.mediaId = mediaId;
        this.infoHash = TorrentUtils.hexStringToByteArray(mediaId);
    }

    public byte[] getInfoHash() {
        return infoHash;
    }

    public void setInfoHash(byte[] infoHash) {
        this.infoHash = infoHash;
    }

}

