package mock.handler;

import rapid.cloud.cdn.protocol.model.tracker.mfc.MFCPlayServerAddrReqMsg;
import rapid.cloud.cdn.protocol.utils.TorrentUtils;

public class MFCMediaPlayModel extends MFCPlayServerAddrReqMsg {
    private String mediaId = null;
    private byte[] infoHash = null;

    public MFCMediaPlayModel(String fileId, String fileSize){
        super(fileId, fileSize);
        setPlayType("MPQ");
        setCloudSource("MEGA");
        setCloudUserName("admin");
        setCloudPwd("123456");
        setCloudLoginType("DEAFE");
        setFileKey("YHUJHOOF");
        setExtra("5r245423tt#2344");
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
