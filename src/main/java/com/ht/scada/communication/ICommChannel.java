package com.ht.scada.communication;

/**
 * 采集通道接口程序
 * @author 薄成文
 *
 */
public interface ICommChannel extends Runnable, IService {
	
	/**
	 * 执行一次采集操作，该操作可以在线程里循环执行
	 */
	void execute();

	/**
	 * 采集通道初始化
	 * @throws Exception
	 */
	void init() throws Exception;


	/**
	 * 执行遥控操作
	 * @param dataID
	 * @param value
	 * @return
	 */
	boolean exeYK(int dataID, boolean value);

	/**
	 * 执行遥调操作
	 * @param dataID
	 * @param value
	 * @return
	 */
	boolean exeYT(int dataID, int value);
}