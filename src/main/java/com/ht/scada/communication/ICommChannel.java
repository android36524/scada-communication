package com.ht.scada.communication;

/**
 * 采集通道接口程序
 * @author 薄成文
 *
 */
public interface ICommChannel extends IService {
	
	/**
	 * 采集通道初始化
	 * @throws Exception
	 */
	void init() throws Exception;


	/**
	 * 执行遥控操作
     * @param deviceAddr
	 * @param dataID
	 * @param value
	 * @return
	 */
	boolean exeYK(int deviceAddr, int dataID, boolean value);

	/**
	 * 执行遥调操作
     *
     *
     * @param deviceAddr
	 * @param dataID
	 * @param value
	 * @return
	 */
	boolean exeYT(int deviceAddr, int dataID, int value);
}