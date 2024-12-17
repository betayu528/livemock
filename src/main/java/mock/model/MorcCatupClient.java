package mock.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.RandomUtils;

import mock.handler.MFCMediaPlayModel;
import rapid.cloud.cdn.protocol.media.MFCMediaInfo;
import rapid.cloud.cdn.protocol.media.ShareMediaInfo;
import rapid.cloud.cdn.common.constant.EnumTrackerServerType;
import rapid.cloud.cdn.protocol.media.EMediaCacheType;


public class MorcCatupClient extends BaseClient {
    public byte[] hash = null;
    public CatchupMediaPlayModel catchupModel = null;
    public int playIndex = 0;
    public byte shareable = 1;
    public List<ShareMediaInfo> shareMediaInfos = new ArrayList<>(10);
    public List<MFCMediaInfo> mediaInfos = new ArrayList<>(10);
    public MFCMediaPlayModel MFCPlayModel = null;

    public EnumTrackerServerType trackerType = EnumTrackerServerType.eUnknownTrackerType;

    public MorcCatupClient(Long clientId) {
        super(clientId);
    }

    public void addMedia(byte[] mediaHash, boolean isPlaying){
        genShareMediaInfo(mediaHash, isPlaying);
        genMediaCacheInfo(mediaHash, isPlaying);
    }

    public EnumTrackerServerType getTrackerType() {
        return this.trackerType;
    }

    public void setTrackerType(EnumTrackerServerType trackerType) {
        this.trackerType = trackerType;
    }
    
    public void genShareMediaInfo(byte[] mediaHash, boolean isPlaying){
        ShareMediaInfo shareMediaInfo = new ShareMediaInfo();
        shareMediaInfo.setInfoHash(mediaHash);
        shareMediaInfo.setWantPeerGroups((byte) 1);
        shareMediaInfo.setStat(isPlaying ? (byte) 1 : (byte) 0);
        shareMediaInfo.setPlayCursor((byte) 0);
        shareMediaInfo.setFinishedBlocks((short) RandomUtils.nextInt(0, 32000));

        shareMediaInfos.add(shareMediaInfo);
        hash = shareMediaInfo.getInfoHash();
    }

    public void genMediaCacheInfo(byte[] mediaHash, boolean isPlaying){
        MFCMediaInfo mediaInfo = new MFCMediaInfo();
        mediaInfo.setMediaHash(mediaHash);
        mediaInfo.setCacheType(isPlaying ? EMediaCacheType.PLAYING : EMediaCacheType.PASSIVE);
        mediaInfo.setMediaSize(999999000L);
        mediaInfo.setCachedSize(88888L);
        mediaInfo.setDispersion(80);
        mediaInfo.setCurShareCnt(20);
        mediaInfo.setLastShareTime(System.currentTimeMillis());
        mediaInfos.add(mediaInfo);
    }

    public byte[] getPlayingInfoHash(){
        EnumTrackerServerType type = EnumTrackerServerType.eCatchupTracker;
        if (type == EnumTrackerServerType.eMFCTracker) {
            return MFCPlayModel != null ? MFCPlayModel.getInfoHash() : null;
        }

        if (type == EnumTrackerServerType.eCatchupTracker) {
            return catchupModel != null ? catchupModel.getInfoHash() : null;
        }
          
        return null;
    }


    public void updateServerAddressList(){
        if(this.liveServerAddrList != null && this.liveServerAddrList.size() > 0) {
            this.liveServerAddrList.get(0).setServerConnStatus(prtOk ? (short) 0 : (short) 1);
            this.liveServerAddrList.get(0).setChannelGrpId(-1);
        }

        //心跳里带的播放地址的channel group id要改成该mediaId在share media info里的index
        byte[] playingInfoHash = getPlayingInfoHash();
        if(playingInfoHash == null)
            return;

        for (int i = 0; i < shareMediaInfos.size(); i++) {
            if (Arrays.equals(shareMediaInfos.get(i).getInfoHash(), playingInfoHash))
                this.liveServerAddrList.get(0).setChannelGrpId(i);
        }
    }

    // public void logout(){
    //     super.logout();
    //     shareable = 1;
    //     shareMediaInfos.clear();
    //     mediaInfos.clear();
    // }

}
