package com.ht.scada.communication;

import com.ht.scada.communication.dao.ChannelInfoDao;
import com.ht.scada.communication.dao.impl.ChannelInfoDaoImpl;
import com.ht.scada.communication.entity.ChannelInfo;
import com.ht.scada.communication.iec104.IEC104Channel;
import com.ht.scada.communication.modbus.ModbusTcpChannel;
import com.ht.scada.communication.model.EndTagWrapper;
import com.ht.scada.communication.service.DataService;
import com.ht.scada.communication.service.RealtimeDataService;
import com.ht.scada.communication.service.impl.DataServiceKVImpl;
import com.ht.scada.communication.service.impl.RealtimeDataServiceImpl;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CommunicationManager implements IService {
	
	private static final Logger log = LoggerFactory.getLogger(CommunicationManager.class);

    private static CommunicationManager instance = new CommunicationManager();
    public static CommunicationManager getInstance() {
        return instance;
    }

    private DataService dataService;
    private RealtimeDataService realtimeDataService;

    private ChannelInfoDao channelInfoDao;

    private EventLoopGroup eventLoopGroup;

	// 默认处理1000个采集通道
	//private final List<CommunicationChannel> commChannels = new ArrayList<>(1000);
    /**
     * 通道序号与采集通道之间的映射
     */
    private final Map<Integer, CommunicationChannel> channelMap = new HashMap<>(1000);

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
        channelInfoDao = new ChannelInfoDaoImpl();
        realtimeDataService = new RealtimeDataServiceImpl();
        dataService = new DataServiceKVImpl();
    }

    public DataService getDataService() {
        return dataService;
    }

    public RealtimeDataService getRealtimeDataService() {
        return realtimeDataService;
    }

    /**
	 * 初始化通讯控制器
     * TODO:设置为自动启动
	 * @throws Exception 
	 */
	//@PostConstruct
	public void init() throws Exception {
        TagCfgManager.getInstance().init();
		initChannels();
	}
	
	//@PreDestroy
	public void destroy() {
        stopAllChannel();
		channelMap.clear();
        TagCfgManager.getInstance().destroy();
        eventLoopGroup.shutdown();
	}

    public void startAllChannel() {
        synchronized (lock) {
            if (!channelsRunning) {
                log.info("启动通道采集服务");
                // 更新Web服务地址
                realtimeDataService.putValue("url.comm", Config.INSTANCE.getUrl() + "/comm");
                realtimeDataService.putValue("url.comm.yk", Config.INSTANCE.getUrl() + "/comm/yk");
                realtimeDataService.putValue("url.comm.yt", Config.INSTANCE.getUrl() + "/comm/yt");
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
            //TODO: 用于测试，未初始化通道配置init();
            if (eventLoopGroup != null && !eventLoopGroup.isShutdown()) {
                eventLoopGroup.shutdown();
            }
            eventLoopGroup = new NioEventLoopGroup();
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
		List<ChannelInfo> channels = channelInfoDao.getAll();
		for (ChannelInfo channel : channels) {
			log.info("初始化采集通道:{}", channel.getName());
			initChannel(channel);

            // TODO: 测试用通道,正式发布时删除
            for (int i=2; i < 1000; i++) {
                channel.setIdx(i);
                initChannel(channel);
            }
		}
	}
	
	private void initChannel(ChannelInfo channel) throws Exception {
		CommunicationChannel commChannel = null;
		//List<EndTagWrapper> endTagList = tagCfgManager.getEndTagWrapperByChannelIdx(channel.getIdx());
        List<EndTagWrapper> endTagList = TagCfgManager.getInstance().getEndTagWrapperByChannelIdx(1);
		switch (channel.getProtocal()) {
		case ModbusTCP:
			commChannel = new ModbusTcpChannel(eventLoopGroup, channel, endTagList);
			break;
		case IEC104:
			commChannel = new IEC104Channel(eventLoopGroup, channel, endTagList);
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
     * @param deviceAddr
     * @param dataID
     * @param value
     * @return
     */
    public void exeYK(int channelIndex, int deviceAddr, int dataID, boolean value) {
        CommunicationChannel channel = channelMap.get(channelIndex);
        if (channel != null) {
            channel.exeYK(deviceAddr, dataID, value);
        }
    }

    /**
     * 执行遥调操作
     * @param deviceAddr
     * @param dataID
     * @param value
     * @return
     */
    public void exeYT(int channelIndex, int deviceAddr, int dataID, int value) {
        CommunicationChannel channel = channelMap.get(channelIndex);
        if (channel != null) {
            channel.exeYT(deviceAddr, dataID, value);
        }
    }
}
