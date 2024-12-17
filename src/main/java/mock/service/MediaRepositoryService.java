package mock.service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.PostConstruct;

import org.springframework.stereotype.Service;

import mock.model.CatchupMediaPlayModel;
import mock.model.MorcCatupClient;
import mock.util.MockUtil;

@Service
public class MediaRepositoryService {
    private static AtomicInteger totalClientNum = new AtomicInteger(0);
    private static Map<String, AtomicInteger> topPlayingMedias = new ConcurrentHashMap<>();
    private static Map<String, AtomicInteger> notTopPlayingMedias = new ConcurrentHashMap<>();
    private static int playingMediaNum = 5;
    //hash池子，随机生成的
    private static List<byte[]> notPlayingHashPool = null;

    private static List<CatchupMediaPlayModel> catchupModelPool = new ArrayList<>(10);

    private static final Integer POOL_SIZE = 10;
    @PostConstruct
    void init() {
        playingMediaNum = POOL_SIZE;
        if(playingMediaNum <= 0)
            playingMediaNum = 1;
    }

    public static void buildNotPlayingMediaHashPool(){
        //要排除掉正在播放的资源，正在播放的资源是一个用户一个
        int actualHashPoolSize = POOL_SIZE - playingMediaNum;
        notPlayingHashPool = MockUtil.buildHashPool(actualHashPoolSize);
    }

    //回看返回epgId,点播返回fileId
    private static String getRandomResourceId(){
        // TODO
        return "";
    }

    /**
     * @return 当热门组已经满的时候返回null
     */
    public static String chooseResource(){
        int topPlayingClientNum = totalClientNum.get() * 20 / 100;
        int topMediaNum = playingMediaNum*20/100;//topPlayingClientNum / 200;
        if(topMediaNum <= 0)
            topMediaNum = 1;

        int clientNumPerTopMedia = topPlayingClientNum / topMediaNum;
        for(Map.Entry<String, AtomicInteger> entry : topPlayingMedias.entrySet()){
            if(entry.getValue().get() < clientNumPerTopMedia){
                entry.getValue().incrementAndGet();
                return entry.getKey();
            }
        }
        if(topPlayingMedias.size() < topMediaNum) {
            String resId = getRandomResourceId();
            topPlayingMedias.put(resId, new AtomicInteger(0));
            return resId;
        }

        int notTopPlayingClientNum = totalClientNum.get()*80/100;
        int notTopMediaNum = playingMediaNum - topMediaNum;//topPlayingClientNum / 200;
        int clientNumPerNoTopMedia = notTopPlayingClientNum / notTopMediaNum;
        for(Map.Entry<String, AtomicInteger> entry : notTopPlayingMedias.entrySet()){
            if(entry.getValue().get() < clientNumPerNoTopMedia){
                entry.getValue().incrementAndGet();
                return entry.getKey();
            }
        }
        if((topPlayingMedias.size() + notTopPlayingMedias.size()) < playingMediaNum) {
            String resId = getRandomResourceId();
            notTopPlayingMedias.put(resId, new AtomicInteger(0));
            return resId;
        }
        return null;
    }

    /**填充一些非在播的,每一个用户会填充10个，这里只填充9个，留一个给在播的*/
    public static void addUserCacheMedias(MorcCatupClient clientInfo, int index){
        if(notPlayingHashPool == null || notPlayingHashPool.isEmpty())
            return;
        int hashIdx = 0, firstLoopBack = -199;
        boolean isHashPoolTooLess = false;

        // TODO
    }

    //加入正常的数据(stream manager拉取的数据)
    public static void addCatchupModel(String epgId, String streamId){
        long now = System.currentTimeMillis();
        catchupModelPool.add(new CatchupMediaPlayModel(epgId, streamId, now, 3600L));
        catchupModelPool.add(new CatchupMediaPlayModel(epgId, streamId, now, 3600L));
    }

    //加入测试的数据
    public static void addCatchupTestParam1(String epgId){
        catchupModelPool.add(new CatchupMediaPlayModel(epgId, epgId, 1650596742000L, 3600L));
    }

    public static void setTotalClientNum(int clientNum){
        totalClientNum.getAndSet(clientNum);
    }

    public static Map<String, AtomicInteger> getTopPlayingMedias() {
        return topPlayingMedias;
    }

}