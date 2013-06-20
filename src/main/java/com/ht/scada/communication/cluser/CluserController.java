package com.ht.scada.communication.cluser;

import com.ht.scada.communication.CommunicationManager;
import com.ht.scada.communication.Config;
import com.ht.scada.communication.IService;

/**
 * Created with IntelliJ IDEA.
 * User: bcw
 * Date: 13-4-28
 * Time: 下午2:56
 * To change this template use File | Settings | File Templates.
 */
public class CluserController implements IService {
    private static CluserController instance = new CluserController();
    public static CluserController getInstance() {
        return instance;
    }

    private volatile boolean running = false;
    private MasterServer masterServer;
    private SlaverClient slaverClient;

    private CommunicationManager communicationController;

    private CluserController() {
        communicationController = CommunicationManager.getInstance();
    }

    @Override
    public void start() {
        running = true;
        switch (Config.INSTANCE.getMode()) {
            case SINGLE:// 单机模式直接启动采集
                communicationController.startAllChannel();
                break;
            case MASTER:// 主机模式直接启动采集
                communicationController.startAllChannel();
                startMasterServer();
                break;
            case SLAVER:// 从机模式需要判断主机状态后确定是否启动采集
                startSlaverClient();
                break;
        }
    }

    @Override
    public void stop() {
        running = false;
        if (masterServer != null) {
            masterServer.stop();
            masterServer = null;
        }
        if (slaverClient != null) {
            slaverClient.stop();
            slaverClient = null;
        }
    }

    @Override
    public boolean isRunning() {
        return running;  //To change body of implemented methods use File | Settings | File Templates.
    }

    private void startMasterServer() {
        masterServer = new MasterServer(Config.INSTANCE.getMasterPort());
        masterServer.start();
    }

    private void startSlaverClient() {
        slaverClient = new SlaverClient(Config.INSTANCE.getMasterHost(), Config.INSTANCE.getMasterPort(),
                communicationController);
        slaverClient.start();
    }

    /**
     * 是否已与主机（备机）建立连接
     * @return
     */
    public boolean isConnected() {
        if (slaverClient != null) {
            return slaverClient.isConnected();
        } else if(masterServer != null){
            return masterServer.isClientConnected();
        } else {
            return false;
        }
    }

    public MasterServer getMasterServer() {
        return masterServer;
    }

    public SlaverClient getSlaverClient() {
        return slaverClient;
    }
}
