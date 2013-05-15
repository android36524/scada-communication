package com.ht.scada.communication.cluser;

import com.ht.scada.communication.CommunicationManager;
import com.ht.scada.communication.Config;
import com.ht.scada.communication.IService;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

/**
 * Created with IntelliJ IDEA.
 * User: bcw
 * Date: 13-4-28
 * Time: 下午2:56
 * To change this template use File | Settings | File Templates.
 */
@Singleton
@Named
public class CluserController implements IService {
    private volatile boolean running = false;
    private MasterServer masterServer;
    private SlaverClient slaverClient;

    @Inject
    private CommunicationManager communicationController;

    @Override
    public void start() {
        running = true;
        switch (Config.INSTANCE.getMode()) {
            case SINGLE:
                communicationController.startAllChannel();
                break;
            case MASTER:
                communicationController.startAllChannel();
                startMasterServer();
                break;
            case SLAVER:
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
