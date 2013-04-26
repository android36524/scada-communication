package com.ht.scada.communication;

import com.ht.scada.common.tag.entity.AcquisitionChannel;
import com.ht.scada.common.tag.service.AcquisitionService;
import com.ht.scada.communication.iec104.IEC104Channel;
import com.ht.scada.communication.modbus.ModbusTcpChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

@Singleton
@Named
public class CommunicationController implements IService {
	
	private static final Logger log = LoggerFactory.getLogger(CommunicationController.class);
	
	@Inject
	private AcquisitionService acquisitionService;
	
	// 默认处理1000个采集通道
	private final List<CommunicationChannel> commChannels = new ArrayList<>(1000);

    /**
     * 运行模式<br/>
     */
    private ServiceMode mode = ServiceMode.MASTER;

    /**
     * 运行状态
     */
    private boolean running = false;

	/**
	 * 初始化通讯控制器
	 * @throws Exception 
	 */
	//@PostConstruct
	public void init() throws Exception {
		initChannels();
        //Properties properties = new Properties();

	}
	
	//@PreDestroy
	public void destroy() {
		for (CommunicationChannel channel : commChannels) {
			channel.stop();
		}
		commChannels.clear();
	}

    @Override
    public void start() {
        switch (mode) {
            case SINGLE:
                break;
            case MASTER:
                startMasterServer();
                break;
            case SLAVER:
                startSlaverClient();
                break;
        }
        running = true;
    }

    private void startMasterServer() {
        MasterServer masterServer = new MasterServer(4660);
        masterServer.start();
    }

    private void startSlaverClient() {
       SlaverClient slaverClient = new SlaverClient("127.0.0.1", 4660);
       slaverClient.start();
    }

    @Override
    public void stop() {

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
		
		switch (channel.getProtocal()) {
		case ModbusTCP:
			commChannel = new ModbusTcpChannel(channel);
			break;
		case IEC104:
			commChannel = new IEC104Channel(channel);
			break;

		default:
			break;
		}
		
		if (commChannel != null) {
			synchronized (commChannels) {
				commChannels.add(commChannel);
			}
		}
	}

    public List<CommunicationChannel> getCommChannels() {
        return commChannels;
    }

    public boolean isRunning() {
        return running;
    }

    public ServiceMode getMode() {
        return mode;
    }

    public void setMode(ServiceMode mode) {
        this.mode = mode;
    }
}
