package com.ht.scada.communication;

import com.ht.scada.communication.dao.ChannelInfoDao;
import com.ht.scada.communication.entity.ChannelInfo;
import com.ht.scada.communication.iec104.IEC104Channel;
import com.ht.scada.communication.modbus.ModbusTcpChannel;
import com.ht.scada.communication.model.EndTagWrapper;
import io.netty.channel.nio.NioEventLoopGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CommunicationManager implements IService {
	
	private static final Logger log = LoggerFactory.getLogger(CommunicationManager.class);

    private static CommunicationManager instance = new CommunicationManager();
    private List<ChannelInfo> channels;

    public static CommunicationManager getInstance() {
        return instance;
    }

    private ChannelInfoDao channelInfoDao;

    private NioEventLoopGroup nioEventLoopGroup;

	// 默认处理1000个采集通道
	//private final List<CommunicationChannel> commChannels = new ArrayList<>(1000);
    /**
     * 通道序号与采集通道之间的映射
     */
    private final Map<Integer, CommunicationChannel> channelMap = new ConcurrentHashMap<>(1000);

    /**
     * 服务运行状态
     */
    private volatile boolean running = false;

    /**
     * 采集通道是否启动
     */
    private volatile boolean channelsRunning = false;

    private final Object lock = new Object();

    private CommunicationManager() {
        //start();
    }

    /**
	 * 初始化通讯控制器
	 * @throws Exception
	 */
	public void init() throws Exception {
        channelInfoDao = DataBaseManager.getInstance().getChannelInfoDao();
        TagCfgManager.getInstance().init();
		initChannels();
	}
	
	//@PreDestroy
	public void destroy() {
        stopAllChannel();
		channelMap.clear();
        TagCfgManager.getInstance().destroy();
        nioEventLoopGroup.shutdownGracefully();
	}

    public void startAllChannel() {
        synchronized (lock) {
            if (!channelsRunning) {
                log.info("启动通道采集服务");
                // 更新Web服务地址
                Map<String, String> map = new HashMap<>(3);
                map.put("url.comm", Config.INSTANCE.getUrl() + "/comm");
                map.put("url.comm.yk", Config.INSTANCE.getUrl() + "/comm/yk");
                map.put("url.comm.yt", Config.INSTANCE.getUrl() + "/comm/yt");
                DataBaseManager.getInstance().getRealtimeDataService().putValus(map);
                for (CommunicationChannel channel : channelMap.values()) {
                    channel.start();
                }
                channelsRunning = true;
            }
        }
    }

    public void stopAllChannel() {
        synchronized (lock) {
            if (channelsRunning) {
                log.info("停止通道采集服务");
                for (CommunicationChannel channel : channelMap.values()) {
                    channel.stop();
                }
                channelsRunning = false;
            }
        }
    }

    @Override
    public void start() {
        if (running) {
            return;
        }
        log.info("启动服务");
        try {
            if (nioEventLoopGroup != null && !nioEventLoopGroup.isShutdown()) {
                nioEventLoopGroup.shutdownGracefully();
            }
            nioEventLoopGroup = new NioEventLoopGroup();
            init();
            running = true;
        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }


    @Override
    public void stop() {
        if (!running) {
            return;
        }
        log.info("停止服务");
        running = false;
        destroy();
    }
	
	/**
	 * 初始化采集通道
	 * @throws Exception 
	 */
	private void initChannels() throws Exception {
        channels = channelInfoDao.getAll();
        /*************** test ***************/
        // TODO: 测试用通道,正式发布时删除
        ChannelInfo channelInfo = channels.get(0);
        for (int i=6; i < 1000; i++) {
            ChannelInfo c = new ChannelInfo();
            c.setName("RTU-" + i);
            c.setFrames(channelInfo.getFrames());
            c.setIdx(i + 1);
            c.setIntvl(channelInfo.getIntvl());
            c.setOffline(channelInfo.getOffline());
            c.setPortInfo(channelInfo.getPortInfo());
            c.setProtocal(channelInfo.getProtocal());
            channels.add(c);
            //initChannel(c);
        }
        /*************** test end ***************/

		for (ChannelInfo channel : channels) {
			log.info("初始化采集通道:{}", channel.getName());
			initChannel(channel);
		}
	}
	
	private void initChannel(ChannelInfo channel) throws Exception {
		CommunicationChannel commChannel = null;
        //List<EndTagWrapper> endTagList = TagCfgManager.getInstance().getEndTagWrapperByChannelIdx(channel.getIdx());
        // TODO : 用于测试，只获取序号是1的通道对应的监控对象
        List<EndTagWrapper> endTagList = TagCfgManager.getInstance().getEndTagWrapperByChannelIdx(1);
		switch (channel.getProtocal()) {
		case ModbusTCP:
			commChannel = new ModbusTcpChannel(channel, endTagList);
			break;
		case IEC104:
			commChannel = new IEC104Channel(channel, endTagList);
			break;
		default:
            log.error("不支持的通讯规约：{}", channel.getProtocal());
			break;
		}
		
		if (commChannel != null) {
            channelMap.put(channel.getIdx(), commChannel);
		}
	}

    @Override
    public boolean isRunning() {
        return running;
    }

    /**
     * 执行遥控操作
     * @param channelIndex
     * @param endCode
     * @param varName
     * @param value
     * @return
     */
    public boolean exeYK(int channelIndex, String endCode, final String varName, boolean value) {
        CommunicationChannel channel = channelMap.get(channelIndex);
        if (channel != null) {
            return channel.exeYK(endCode, varName, value);
        } else {
            return false;
        }
    }

    /**
     * 执行遥调操作
     * @param channelIndex
     * @param endCode
     * @param varName
     * @param value
     * @return
     */
    public boolean exeYT(int channelIndex, String endCode, final String varName, int value) {
    //public void exeYT(int channelIndex, int deviceAddr, int dataID, int value) {
        CommunicationChannel channel = channelMap.get(channelIndex);
        if (channel != null) {
            return channel.exeYT(endCode, varName, value);
        } else {
            return false;
        }
    }

    public NioEventLoopGroup getNioEventLoopGroup() {
        return nioEventLoopGroup;
    }

    public List<ChannelInfo> getChannels() {
        return channels;
    }

    public Map<Integer, CommunicationChannel> getChannelMap() {
        return channelMap;
    }
}
