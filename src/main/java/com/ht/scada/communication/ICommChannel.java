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
     * @param endCode
	 * @param varName
	 * @param value
	 * @return
	 */
	boolean exeYK(String endCode, String varName, boolean value);

	/**
	 * 执行遥调操作
     * @param endCode
	 * @param varName
	 * @param value
	 * @return
	 */
	boolean exeYT(String endCode, String varName, int value);
}