package com.ht.scada.communication;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;

/**
 * Created with IntelliJ IDEA.
 * User: bcw
 * Date: 13-4-26
 * Time: 下午8:40
 * To change this template use File | Settings | File Templates.
 */
public enum Config {
    INSTANCE;

    private ServiceMode mode;
    private String url;//http://127.0.0.1:80
    private String masterHost;
    private int masterPort;
    private int webPort;
    private PropertiesConfiguration config;

    private Config() {
        try {
            config = new PropertiesConfiguration("config.properties");
            config.setAutoSave(true);
            url = config.getString("url");
            mode = ServiceMode.valueOf(config.getString("mode", "single").toUpperCase());
            if (mode == ServiceMode.SLAVER) {
                masterHost = config.getString("master.host");
            }
            masterPort = config.getInt("master.port", 4660);
            webPort = config.getInt("web.port", 8080);
        } catch (ConfigurationException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    public ServiceMode getMode() {
        return mode;
    }

    public void setMode(ServiceMode mode) {
        this.mode = mode;
        config.setProperty("mode", mode.toString().toLowerCase());
    }

    public String getMasterHost() {
        return masterHost;
    }

    public void setMasterHost(String masterHost) {
        this.masterHost = masterHost;
        config.setProperty("master.host", masterHost);
    }

    public int getMasterPort() {
        return masterPort;
    }

    public void setMasterPort(int masterPort) {
        this.masterPort = masterPort;
        config.setProperty("master.port", masterPort);
    }

    public int getWebPort() {
        return webPort;
    }

    public void setWebPort(int webPort) {
        this.webPort = webPort;
        config.setProperty("web.port", webPort);
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
