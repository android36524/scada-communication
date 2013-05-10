package com.ht.scada.communication;

/**
 * Created with IntelliJ IDEA.
 * User: 薄成文
 * Date: 13-4-25
 * Time: 下午3:59
 * To change this template use File | Settings | File Templates.
 */
public interface IService {
    /**
     * 启动服务
     */
    void start();

    /**
     * 停止服务
     */
    void stop();

    boolean isRunning();
}
