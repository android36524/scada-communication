package com.ht.scada.communication;

import com.ht.scada.common.middleware.service.UrlService;
import com.ht.scada.common.tag.entity.AcquisitionChannel;
import com.ht.scada.common.tag.service.AcquisitionService;
import com.ht.scada.communication.iec104.IEC104Channel;
import com.ht.scada.communication.modbus.ModbusTcpChannel;
import com.ht.scada.communication.model.EndTagWrapper;
import com.ht.scada.data.service.RealtimeDataService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Singleton
@Named
public class CommunicationManager implements IService {
	
	private static final Logger log = LoggerFactory.getLogger(CommunicationManager.class);
	
	@Inject
	private AcquisitionService acquisitionService;
    @Inject
    private TagCfgManager tagCfgManager;
    @Inject
    private RealtimeDataService realtimeDataService;

	// 默认处理1000个采集通道
	//private final List<CommunicationChannel> commChannels = new ArrayList<>(1000);
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

	/**
	 * 初始化通讯控制器
	 * @throws Exception 
	 */
	//@PostConstruct
	public void init() throws Exception {
        tagCfgManager.init();
		initChannels();
	}
	
	//@PreDestroy
	public void destroy() {
        stopAllChannel();
		channelMap.clear();
        tagCfgManager.destroy();
	}

    public void startAllChannel() {
        synchronized (lock) {
            if (!channelsRunning) {
                log.info("启动通道采集服务");
                // 更新Web服务地址
                realtimeDataService.putValue(UrlService.COMM_URL_KEY, Config.INSTANCE.getUrl() + "/comm");
                realtimeDataService.putValue(UrlService.COMM_YK_URL_KEY, Config.INSTANCE.getUrl() + "/comm/yk");
                realtimeDataService.putValue(UrlService.COMM_YT_URL_KEY, Config.INSTANCE.getUrl() + "/comm/yt");
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
        log.info("启动服务");
        try {
            //TODO: 用于测试，未初始化通道配置init();
            running = true;
        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }


    @Override
    public void stop() {
        log.info("停止服务");
        running = false;
        destroy();
    }
	
	/**
	 * 初始化采集通道
	 * @throws Exception 
	 */
	private void initChannels() throws Exception {
		List<AcquisitionChannel> channels = acquisitionService.getAllChannel();
		for (AcquisitionChannel channel : channels) {
			log.info("初始化采集通道:{}", channel.getName());
			initChannel(channel);
		}
	}
	
	private void initChannel(AcquisitionChannel channel) throws Exception {
		CommunicationChannel commChannel = null;
		List<EndTagWrapper> endTagList = tagCfgManager.getEndTagWrapperByChannelIdx(channel.getIdx());
		switch (channel.getProtocal()) {
		case ModbusTCP:
			commChannel = new ModbusTcpChannel(channel, endTagList);
			break;
		case IEC104:
			commChannel = new IEC104Channel(channel, endTagList);
			break;
		default:
			break;
		}
		
		if (commChannel != null) {
            channelMap.put(channel.getId(), commChannel);
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
