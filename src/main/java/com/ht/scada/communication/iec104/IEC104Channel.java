package com.ht.scada.communication.iec104;

import com.ht.iec104.IEC104Master;
import com.ht.iec104.IEC104Master.YKHandler;
import com.ht.iec104.IEC104Master.YTHandler;
import com.ht.iec104.MasterHandler;
import com.ht.iec104.frame.IEC104IFrame;
import com.ht.iec104.util.TiConst;
import com.ht.scada.common.data.VarGroupData;
import com.ht.scada.common.tag.entity.AcquisitionChannel;
import com.ht.scada.common.tag.util.ChannelFrameFactory;
import com.ht.scada.common.tag.util.ChannelFrameFactory.IEC104Frame;
import com.ht.scada.common.tag.util.PortInfoFactory;
import com.ht.scada.common.tag.util.PortInfoFactory.TcpIpInfo;
import com.ht.scada.common.tag.util.VarGroup;
import com.ht.scada.common.tag.util.VarType;
import com.ht.scada.communication.CommunicationChannel;
import com.ht.scada.communication.model.EndTagWrapper;
import com.ht.scada.communication.model.TagVar;
import com.ht.scada.communication.util.VarGroupWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * IEC104规约采集通道<br>
 * <pre>
 * 由于采集方式为异步方式，所以默认不在线程模式下执行, 需要在主程序中主动调用execute()函数执行采集。<br>
 * 如果设置为线程运行模式，则在启动采集后自动创建一个线程并在线程是循环执行采集。
 * </pre>
 * @author 薄成文
 *
 */
public class IEC104Channel extends CommunicationChannel {
	private static final Logger log = LoggerFactory.getLogger(IEC104Channel.class);

	private volatile boolean running = false;
	private Thread thread;
	
	private List<FrameWrapper> frameList;
	private IEC104Master master;
	private final int realtimeDataInterval = 1000;
	private final int historyDataInterval = 60000;

	private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
	private long nextRealtimeHandleTime;

	private long nextHistoryHandleTime;
	
	public IEC104Channel(AcquisitionChannel channel) throws Exception {
		super(channel);
	}
	
	@Override
	public void init() throws Exception {
		log.info("{}:初始化采集通道", channel.getName());
		
		String portInfo = channel.getPortInfo();
		if (portInfo.startsWith("tcp/ip")) {
			
			List<IEC104Frame> list = ChannelFrameFactory.parseIEC104Frames(channel.getFrames());
			if (!list.isEmpty()) {
				frameList = new ArrayList<>();
				for (IEC104Frame frame : list) {
					frameList.add(new FrameWrapper(frame));
				}
			}
			log.info("{} - 召唤帧共：{}", channel.getName(), frameList.size());
			
			
			TcpIpInfo tcpIpInfo = PortInfoFactory.parseTcpIpInfo(portInfo);
			master = new IEC104Master(tcpIpInfo.ip, tcpIpInfo.port, 1);
			
			master.setMasterHandler(new MyMasterHandler());
			
		} else {
			log.error("采集通道{}的物理端口信息配置错误：{}", portInfo);
			throw new RuntimeException("IEC104通讯规约只支持TCP/IP通讯方式。");
		}

	}

	@Override
	public void start() {
		log.info("{}:启动采集", channel.getName());
		
		if(master != null) {
			running = true;
			master.open();
			thread = new Thread(this);
			thread.start();
		}
	}

	@Override
	public void stop() {
		log.info("{}:停止采集", channel.getName());
		
		running = false;
		if (thread != null) {
			thread.interrupt();
		}
		if (master != null) {
			master.close();
		}
	}
	
	@Override
	public void run() {
		nextRealtimeHandleTime = System.currentTimeMillis() + realtimeDataInterval;
		nextHistoryHandleTime = System.currentTimeMillis() + historyDataInterval;
		
		while(running) {
			try {
				if (master.isConnected()) {
					execute();
				} else {
					nextRealtimeHandleTime = System.currentTimeMillis() + realtimeDataInterval;
					nextHistoryHandleTime = System.currentTimeMillis() + historyDataInterval;
				}
				Thread.sleep(100);
			} catch (Exception e) {
			}
			
		}
	}
	
	/**
	 * 处理RTU上送的遥测历史数据
	 * @param defaultInterval
	 */
	private void handleHisYcData(final int defaultInterval) {
		IEC104IFrame frame;
		while ((frame = master.getHisYcFrameQueue().poll()) != null) {
				final int[] infoID = frame.infoID; 
				final int[] value = frame.getYcValue();
				final Calendar[] datetime = frame.getTimes();
				final boolean sq = frame.sq;
				
				forEachTagVar(master.getSlaveID(), -1, null, Void.class, new DataHandler<Void>() {
					@Override
					public Void each(EndTagWrapper model, TagVar var) {
						
						VarGroup varGroup = var.tpl.getVarGroup();
						VarGroupWrapper varGroupWrapper = model.varGroupWrapperMap.get(varGroup);
						if (varGroupWrapper == null) {
							return null;
						}
						
						
						int interval = varGroupWrapper.cfg.getInterval();
						if (interval <= 0) {
							interval = defaultInterval;
						}
						
						if (var.tpl.getVarType() == VarType.QT && var.lastArrayValue != null) {// 功图历史数据
							
							int startID = var.tpl.getDataID();
							int len = var.lastArrayValue.length;
							if (startID > infoID[infoID.length - 1]
									|| startID + len <= infoID[0]) {
								return null;
							}
								
							Calendar cal = datetime[0];
							String key = createHistoryDataKey(varGroup, cal, interval);
							VarGroupData data = model.historyGroupDataMap.get(key);
							if (data == null) {
								data = new VarGroupData();
								data.setCode(model.endTag.getCode());
								data.setGroup(varGroup);
								data.setDatetime(cal.getTime());
								model.historyGroupDataMap.put(key, data);
								model.groupDataList.add(data);
							}
							float[] v = data.getArrayValueMap().get(var.tpl.getVarName());
							if (v == null) {
								v = new float[var.lastArrayValue.length];
								data.getArrayValueMap().put(var.tpl.getVarName(), v);
							}
							
							for (int i = 0; i < value.length; i++) {
								int offset = infoID[i] - startID;
								if (offset >= 0 && offset < len) {
									v[offset] = (var.coefValue) * value[i] + var.baseValue;
								}
							}
						} else if (var.tpl.getVarType() == VarType.YC) {
							int index = -1;
							if (sq) {// 连续的地址
								int i = var.tpl.getDataID() - infoID[0];
								if (i >= 0 && i < value.length) {
									index = i;
								}
							} else {// 非连续的地址
								for (int i = 0; i < infoID.length; i++) {
									if (infoID[i] == var.tpl.getDataID()) {
										index = i;
										break;
									}
								}
							}
							
							if (index >= 0) {
								float v = (var.coefValue) * value[index] + var.baseValue;
								Calendar cal = datetime[index];
								String key = createHistoryDataKey(varGroup, cal, interval);
								VarGroupData data = model.historyGroupDataMap.get(key);
								if (data == null) {
									data = new VarGroupData();
									data.setCode(model.endTag.getCode());
									data.setGroup(varGroup);
									data.setDatetime(cal.getTime());
									model.historyGroupDataMap.put(key, data);
									model.groupDataList.add(data);
								}
								data.getYcValueMap().put( var.tpl.getVarName(), v);
							}
						}
						
						return null;
					}
				});
		}
	}
	
	/**
	 * 处理RTU上送的遥脉历史数据
	 * @param defaultInterval
	 */
	private void handleHisYmData(final int defaultInterval) {
		IEC104IFrame frame;
		while ((frame = master.getHisYmFrameQueue().poll()) != null) {
				final int[] infoID = frame.infoID; 
				final long[] value = frame.getYmValue();
				final Calendar[] datetime = frame.getTimes();
				final boolean sq = frame.sq;
				
				forEachTagVar(master.getSlaveID(), -1, null, Void.class, new DataHandler<Void>() {
					@Override
					public Void each(EndTagWrapper model, TagVar var) {
						if (var.tpl.getVarType() != VarType.YM) {
							return null;
						}
						
						VarGroup varGroup = var.tpl.getVarGroup();
						VarGroupWrapper varGroupWrapper = model.varGroupWrapperMap.get(varGroup);
						if (varGroupWrapper == null) {
							return null;
						}
							
						int index = -1;
						if (sq) {// 连续的地址
							int i = var.tpl.getDataID() - infoID[0];
							if (i >= 0 && i < value.length) {
								index = i;
							}
						} else {// 非连续的地址
							for (int i = 0; i < infoID.length; i++) {
								if (infoID[i] == var.tpl.getDataID()) {
									index = i;
									break;
								}
							}
						}
						
						int interval = varGroupWrapper.cfg.getInterval();
						if (interval <= 0) {
							interval = defaultInterval;
						}
						
						if (index >= 0) {
							double v = (var.coefValue) * value[index] + var.baseValue;
							Calendar cal = datetime[index];
							String key = createHistoryDataKey(varGroup, cal, interval);
							VarGroupData data = model.historyGroupDataMap.get(key);
							if (data == null) {
								data = new VarGroupData();
								data.setCode(model.endTag.getCode());
								data.setGroup(varGroup);
								data.setDatetime(cal.getTime());
								model.historyGroupDataMap.put(key, data);
								model.groupDataList.add(data);
							}
							data.getYmValueMap().put( var.tpl.getVarName(), v);
						}
						
						return null;
					}
				});
		}
	}
	
	/**
	 * 处理RTU上送的遥信历史数据
	 * @param defaultInterval
	 */
	private void handleHisYxData(final int defaultInterval) {
		IEC104IFrame frame;
		while ((frame = master.getHisYxFrameQueue().poll()) != null) {
				final int[] infoID = frame.infoID; 
				final boolean[] value = frame.getYxValue();
				final Calendar[] datetime = frame.getTimes();
				final boolean sq = frame.sq;
				
				forEachTagVar(master.getSlaveID(), -1, null, Void.class, new DataHandler<Void>() {
					@Override
					public Void each(EndTagWrapper model, TagVar var) {
						if (var.tpl.getVarType() != VarType.YX) {
							return null;
						}
						
						VarGroup varGroup = var.tpl.getVarGroup();
						VarGroupWrapper varGroupWrapper = model.varGroupWrapperMap.get(varGroup);
						if (varGroupWrapper == null) {
							return null;
						}
							
						int index = -1;
						if (sq) {// 连续的地址
							int i = var.tpl.getDataID() - infoID[0];
							if (i >= 0 && i < value.length) {
								index = i;
							}
						} else {// 非连续的地址
							for (int i = 0; i < infoID.length; i++) {
								if (infoID[i] == var.tpl.getDataID()) {
									index = i;
									break;
								}
							}
						}
						
						int interval = varGroupWrapper.cfg.getInterval();
						if (interval <= 0) {
							interval = defaultInterval;
						}
						
						if (index >= 0) {
							Calendar cal = datetime[index];
							String key = createHistoryDataKey(varGroup, cal, interval);
							VarGroupData data = model.historyGroupDataMap.get(key);
							if (data == null) {
								data = new VarGroupData();
								data.setCode(model.endTag.getCode());
								data.setGroup(varGroup);
								data.setDatetime(cal.getTime());
								model.historyGroupDataMap.put(key, data);
								model.groupDataList.add(data);
							}
							data.getYxValueMap().put( var.tpl.getVarName(), value[index]);
						}
						
						return null;
					}
				});
		}
	}

	/**
	 * 
	 */
	private void handleYmData(Date date) {
		IEC104IFrame frame;
		while ((frame = master.getYmFrameQueue().poll()) != null) {
			
				final int[] infoID = frame.infoID; 
				final long[] value = frame.getYmValue();
				final boolean sq = frame.sq;
				
				forEachTagVar(master.getSlaveID(), -1, date, Double.class, new DataHandler<Double>() {
					@Override
					public Double each(EndTagWrapper model, TagVar var) {
						if (sq) {
							int i = var.tpl.getDataID() - infoID[0];
							if (i >= 0 && i < value.length) {
								return (double) (var.coefValue) * value[i] + var.baseValue;
							}
						} else {
							for (int i = 0; i < infoID.length; i++) {
								if (infoID[i] == var.tpl.getDataID()) {
									 return (double) (var.coefValue) * value[i] + var.baseValue;
								}
							}
						}
						return Double.NaN;
					}
				});
		}
	}

	/**
	 * @param date
	 */
	private void handleYcData(Date date) {
		IEC104IFrame frame;
		while ((frame = master.getYcFrameQueue().poll()) != null) {
			
			final int[] infoID = frame.infoID;
			final int[] value = frame.getYcValue();
			final boolean sq = frame.sq;

			forEachTagVar(master.getSlaveID(), -1, date, Float.class,
					new DataHandler<Float>() {
						@Override
						public Float each(EndTagWrapper model, TagVar var) {
							if (sq) {
								int i = var.tpl.getDataID() - infoID[0];
								if (i >= 0 && i < value.length) {
									return (var.coefValue) * value[i] + var.baseValue;
								}
							} else {
								for (int i = 0; i < infoID.length; i++) {
									if (infoID[i] == var.tpl.getDataID()) {
										return (var.coefValue) * value[i] + var.baseValue;
									}
								}
							}
							return Float.NaN;
						}
					});
			
			// 处理示功图、谐波等数组数据
			forEachTagVar(master.getSlaveID(), -1, date, float[].class,
					new DataHandler<float[]>() {
						@Override
						public float[] each(EndTagWrapper model, TagVar var) {
							if (sq && var.lastArrayValue != null) {// 数组(谐波、示功图等)
								int startID = var.tpl.getDataID();
								int len = var.lastArrayValue.length;
								if (startID > infoID[infoID.length - 1]
										|| startID + len <= infoID[0]) {
									return null;
								}
								for (int i = 0; i < value.length; i++) {
									int offset = infoID[i] - startID;
									if (offset >= 0 && offset < len) {
										var.lastArrayValue[offset] = (float) (var.coefValue) * value[i] + var.baseValue;
									}
								}
							}
							return null;
						}
					});
		}
	}

	/**
	 * @param date
	 */
	private void handleYxData(Date date) {
		IEC104IFrame frame = null;
		while ((frame = master.getYxFrameQueue().poll()) != null) {
				
				final int[] infoID = frame.infoID; 
				final boolean[] value = frame.getYxValue();
				final boolean sq = frame.sq;
				
				forEachTagVar(master.getSlaveID(), -1, date, Boolean.class, new DataHandler<Boolean>() {
					
					@Override
					public Boolean each(EndTagWrapper model, TagVar var) {
						if (sq) {// 连续的地址
							int index = var.tpl.getDataID() - infoID[0];
							if (index >= 0 && index < value.length) {
								return value[index];
							}
						} else {// 非连续的地址
							for (int i = 0; i < infoID.length; i++) {
								if (infoID[i] == var.tpl.getDataID()) {
									return value[i];
								}
							}
						}
						return null;
					}
				});
		}
	}
	
	@Override
	public void execute() {
		long currentTime = System.currentTimeMillis();
		
		// 遍历各个召唤帧
		for (FrameWrapper frameWrapper : frameList) {
			if (!running) {
				return;
			}
			
			if (frameWrapper.callEnd) {// 对应的召唤帧结束, 处理对应的历史数据
				if (frameWrapper.frame.ti == TiConst.CALL_HIS_DAT) {// 历史数据上传结束
					handleHisYxData(frameWrapper.frame.interval / 60);
					handleHisYcData(frameWrapper.frame.interval / 60);
					handleHisYmData(frameWrapper.frame.interval / 60);
				} else {// 处理当前的历史数据
					Date date = new Date();
					for (VarGroup group : getVarGroup(frameWrapper.frame.ti)) {
						handleHistoryData(group, date);
					}
				}
				frameWrapper.callEnd = false;
			}
			
			// 执行时间到且上次召唤已经结束
			currentTime = System.currentTimeMillis();
			if(currentTime >= frameWrapper.executeTime && !frameWrapper.callEnd) {
				master.call(frameWrapper.frame.ti);// 启动数据召唤
				frameWrapper.executeTime = currentTime + frameWrapper.frame.interval * 1000;
			}
		}
		
		// 处理并更新实时数据
		if (nextRealtimeHandleTime - currentTime < 0) {
			Date date = new Date();
			
			handleYxData(date);
			handleYcData(date);
			handleYmData(date);
			
			nextRealtimeHandleTime = currentTime + realtimeDataInterval;
			
			updateRealtimeData();
		}
		
		// 保存历史数据
		if (nextHistoryHandleTime - currentTime < 0) {
			persistHistoryData();
			nextHistoryHandleTime = currentTime + historyDataInterval;// 
		}
		
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
		}
		
	}

	@Override
	public boolean exeYK(final int dataID, final boolean status) {
		
		MyYKHandler ykHandler = new MyYKHandler(dataID, status);
		final long endTime = ykHandler.getEndTime();
		master.startYK(ykHandler);
		
		while(ykHandler.getRet() < 0 && endTime > System.currentTimeMillis()) {
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
			}
		}
		
		return ykHandler.getRet() == 1;
	}

	@Override
	public boolean exeYT(int dataID, int value) {
		MyYTHandler ytHandler = new MyYTHandler(dataID, value);
		final long endTime = ytHandler.getEndTime();
		master.startYT(ytHandler);
		
		while(ytHandler.getRet() < 0 && endTime > System.currentTimeMillis()) {
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
			}
		}
		
		return ytHandler.getRet() == 1;
	}
	
	private List<VarGroup> getVarGroup(int ti) {
			switch (ti) {
			case TiConst.CALL_ALL:// 总召唤结束
				return Arrays.asList(VarGroup.DIAN_YM, VarGroup.DIAN_YC, VarGroup.YOU_JING, VarGroup.SHUI_JING);
			case TiConst.CALL_YC_YX:
				return Arrays.asList(VarGroup.DIAN_YM, VarGroup.DIAN_YC, VarGroup.YOU_JING, VarGroup.SHUI_JING);
			case TiConst.CALL_HIS_DAT:// 历史数据
				// TODO:历史数据保存方式
				break;
			case TiConst.CALL_YM:// 电度
				return Arrays.asList(VarGroup.DIAN_YM);
			case TiConst.CALL_XB:// 谐波
				return Arrays.asList(VarGroup.DIAN_XB);
			case TiConst.CALL_DGT:// 电功图
				return Arrays.asList(VarGroup.YOU_JING_DGT);
			case TiConst.CALL_SGT:// 示功图
				return Arrays.asList(VarGroup.YOU_JING_SGT);
			case TiConst.CALL_JLC:// 计量间
				return Arrays.asList(VarGroup.JI_LIANG);
			case TiConst.CALL_ZC:// 注采
				return Arrays.asList(VarGroup.ZHU_CAI);
			case TiConst.CALL_RTU_CFG:// RTU参数
			case TiConst.CALL_SENSOR_CFG:// 传感器参数
			default:
				break;
			}
			
			return null;
	}
	
	
	/**
	 * @param varGroup
	 * @param cal
	 * @param interval
	 * @return
	 */
	private String createHistoryDataKey(VarGroup varGroup, Calendar cal,
			int interval) {
		int minute = cal.get(Calendar.MINUTE);
		minute = minute - (minute % interval);
		cal.set(Calendar.MINUTE, minute);
		String key = varGroup.toString() + dateFormat.format(cal);
		return key;
	}


	private static class FrameWrapper {
		private IEC104Frame frame;
		private long executeTime = 0;
		private boolean callEnd = false;
		private FrameWrapper(IEC104Frame frame) {
			this.frame = frame;
		}
	}
	
	private class MyMasterHandler implements MasterHandler {
		@Override
		public void callEnd(int ti) {
			
			for (FrameWrapper frameWrapper : frameList) {
				if (frameWrapper.frame.ti == ti) {
					frameWrapper.callEnd = true;
				}
			}
		}
	}
	
	private static class MyYTHandler implements YTHandler {
		private int ret = -1;
		private int infoID;
		private int value;
		private long endTime;

		private MyYTHandler(int infoID, int value) {
			this.infoID = infoID;
			this.value = value;
			endTime = System.currentTimeMillis() + 3000;
		}

		@Override
		public long getEndTime() {
			return endTime;
		}

		@Override
		public int getInfoID() {
			return infoID;
		}

		@Override
		public int getValue() {
			return value;
		}

		@Override
		public void onFailure(String message) {
			ret = 0;
		}

		@Override
		public void onSuccess() {
			ret = 1;
		}
		
		/**
		 * @return -1表示超时，1表示成功，0表示失败
		 */
		public int getRet() {
			return ret;
		}
	}

	
	private static class MyYKHandler implements YKHandler {
		private int ret = -1;
		private int infoID;
		private boolean value;
		private long endTime;

		private MyYKHandler(int infoID, boolean value) {
			this.infoID = infoID;
			this.value = value;
			endTime = System.currentTimeMillis() + 3000;
		}

		@Override
		public long getEndTime() {
			return endTime;
		}

		@Override
		public int getInfoID() {
			return infoID;
		}

		@Override
		public boolean getValue() {
			return value;
		}

		@Override
		public void onFailure(String msg) {
			ret = 0;
		}

		@Override
		public void onSuccess() {
			ret = 1;
		}
		
		/**
		 * @return -1表示超时，1表示成功，0表示失败
		 */
		public int getRet() {
			return ret;
		}
	}

}
