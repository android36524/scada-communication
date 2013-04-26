package com.ht.scada.communication;

import com.ht.scada.common.data.FaultRecord;
import com.ht.scada.common.data.OffLimitsRecord;
import com.ht.scada.common.data.VarGroupData;
import com.ht.scada.common.data.YXData;
import com.ht.scada.common.data.service.RealtimeDataService;
import com.ht.scada.common.tag.entity.AcquisitionChannel;
import com.ht.scada.common.tag.entity.TagCfgTpl;
import com.ht.scada.common.tag.util.StorageFactory.FaultStorage;
import com.ht.scada.common.tag.util.StorageFactory.OffLimitsStorage;
import com.ht.scada.common.tag.util.StorageFactory.YXStorage;
import com.ht.scada.common.tag.util.VarGroup;
import com.ht.scada.common.tag.util.VarType;
import com.ht.scada.communication.kv.KeyDefinition;
import com.ht.scada.communication.model.EndTagWrapper;
import com.ht.scada.communication.model.TagVar;
import com.ht.scada.communication.service.DataService;
import com.ht.scada.communication.util.VarGroupWrapper;
import org.joda.time.LocalDateTime;

import javax.jms.JMSException;
import javax.jms.Session;
import java.util.*;

/**
 * @author 薄成文
 * 
 */
public abstract class CommunicationChannel implements ICommChannel {

	private DataService dataService;
	private RealtimeDataService realtimeDataService;

	protected final AcquisitionChannel channel;

//	private List<EndTagWrapper> endTagList = Collections
//			.synchronizedList(new LinkedList<EndTagWrapper>());
	private List<EndTagWrapper> endTagList;

	/** 实时数据暂存队列 **/
	private List<TagVar> realtimeDataList = new ArrayList<>(48);
	private Map<String, String> realtimeDataMap = new HashMap<>(128);

	public CommunicationChannel(AcquisitionChannel channel) throws Exception {
		this.channel = channel;
		TagVarController tagVarController = null;// TODO: 获取变量标签控制器实例
		this.endTagList = tagVarController.getEndTagWrapperByChannelIdx(channel.getIdx());
		init();
	}
	
	public AcquisitionChannel getChannel() {
		return channel;
	}

	protected void updateRealtimeData() {
		if (!realtimeDataMap.isEmpty()) {
			realtimeDataService.putBatchValue(realtimeDataMap);
			realtimeDataMap.clear();
		}
	}

	/**
	 * 保存历史数据
	 */
	protected void persistHistoryData() {
		for (EndTagWrapper model : endTagList) {// 遍历所有节点并进行处理
			if (!model.groupDataList.isEmpty()) {
				dataService.saveVarGroupData(model.groupDataList);
				model.groupDataList.clear();
			}
			if (!model.yxDataList.isEmpty()) {
				dataService.saveYXData(model.yxDataList);
				model.yxDataList.clear();
			}
			if(!model.offLimitsRecordList.isEmpty()) {
				dataService.saveOffLimitsRecord(model.offLimitsRecordList);
				model.offLimitsRecordList.clear();
			}
			if (!model.faultRecordList.isEmpty()) {
				dataService.saveFaultRecord(model.faultRecordList);
				model.faultRecordList.clear();
			}
			model.historyGroupDataMap.clear();
		}
	}

	/**
	 * 处理分组历史数据.<br>
	 * <p>
	 * 如果变量组配置中的存储间隔<=0,则直接将该分组数据加入存储列表。<br>
	 * 示功图数据等可以采用该方式进行配置。
	 * </p>
	 * <p>
	 * 如果变量组配置中的存储间隔>0,则用datetime的分钟数整除存储间隔的分钟数，如果结果为0，则将该分组数据加入存储列表。<br>
	 * 需要注意的是：如果采用该配置方式，要保证至少每隔1分钟调用1次该方法，否则可能会漏掉某个时间点。
	 * </p>
	 * @param varGroup
	 * @param datetime 数据对应的日期时间
	 */
	protected void handleHistoryData(VarGroup varGroup, Date datetime) {
		int minute = LocalDateTime.fromDateFields(datetime).getMinuteOfHour();
		for (EndTagWrapper model : endTagList) {// 遍历所有末端
			VarGroupWrapper wrapper = model.varGroupWrapperMap.get(varGroup);
			if (wrapper == null) {
				continue;
			}
			
			int interval = wrapper.cfg.getInterval();
			if (interval <= 0 || (wrapper.lastMinute != minute && minute % interval == 0)) {
				wrapper.lastMinute = minute;
				VarGroupData data = new VarGroupData();

				for (TagVar var : model.varList) {// 遍历该节点下的所有变量，并进行处理
					if (var.tpl.getVarGroup() == varGroup) {

						if (!Double.isNaN(var.lastYmValue)) {
							data.getYmValueMap().put(
									var.tpl.getVarName(),
									var.lastYmValue);
						} else if (!Float.isNaN(var.lastYcValue)) {
							data.getYcValueMap().put(
									var.tpl.getVarName(),
									var.lastYcValue);
						} else if (var.lastYxValue != -1) {
							data.getYxValueMap().put(
									var.tpl.getVarName(),
									var.lastYxValue > 0);
						} else if (var.lastArrayValue != null) {
							data.getArrayValueMap().put(
									var.tpl.getVarName(),
									var.lastArrayValue);
						}
					}
				}

				if (!data.getYcValueMap().isEmpty()
						|| !data.getYmValueMap().isEmpty()
						|| !data.getArrayValueMap().isEmpty()
						|| !data.getYxValueMap().isEmpty()) {
					data.setCode(model.endTag.getCode());
					data.setGroup(varGroup);
					data.setDatetime(datetime);
					model.groupDataList.add(data);
				}
			}
		}
	}
	
	/**
	 * 遍历采集设备（RTU）对应的末端的所有变量
	 * 
	 * @param deviceAddr
	 * @param funCode
	 *            当功能码为-1时不需要验证功能码
	 */
	protected <T> void forEachTagVar(int deviceAddr, int funCode, Date datetime, Class<T> clazz, DataHandler<T> dataHandler) {
		// 获取设备地址对应的所有末端节点
		for (EndTagWrapper model : endTagList) {// 遍历所有节点并进行处理
			if (model.endTag.getDeviceAddr() != deviceAddr) {
				continue;
			}

			realtimeDataList.clear();

			// for (TagVar var: varList) {// 遍历该节点下的所有变量，关进行处理
			for (TagVar var : model.varList) {// 遍历该节点下的所有变量，关进行处理
				if (funCode >= 0 && var.tpl.getFunCode() != funCode) {
					continue;
				}
				
				if (clazz.equals(Boolean.class) && var.tpl.getVarType() == VarType.YX) {
					Boolean status = (Boolean) dataHandler.each(model, var);
					if (status != null) {
						handleBoolValue(model, var, status, datetime);
					}
				} else if (clazz.equals(Float.class) && var.tpl.getVarType() == VarType.YC) {
					Float value = (Float) dataHandler.each(model, var);
					if (value != null && !Float.isNaN(value)) {
						handleYcValue(model, var, value, datetime);
					}
				} else if (clazz.equals(Double.class) && var.tpl.getVarType() == VarType.YM) {
					Double value = (Double) dataHandler.each(model, var);
					if (value != null && !Double.isNaN(value)) {
						handleYmValue(model, var, value, datetime);
					}
				} else if (clazz.isArray() && var.tpl.getVarType() == VarType.QT) {// 功图等数组数据处理
					dataHandler.each(model, var);
					realtimeDataList.add(var);
				} else if (clazz.equals(Void.class)) {// 历史数据处理
					dataHandler.each(model, var);
				}
			}

			if (!realtimeDataList.isEmpty()) {
				for (TagVar var : realtimeDataList) {

					if (var.lastYxValue >= 0) {
						String key = KeyDefinition.getKey(
								KeyDefinition.REAL_BOOL, model.endTag.getCode(),
								var.tpl.getVarName()).toString();
						realtimeDataMap.put(key,
								(var.lastYxValue == 0) ? "false"
										: "true");
					} else if (!Float.isNaN(var.lastYcValue)) {
						String key = KeyDefinition.getKey(
								KeyDefinition.REAL_NUM, model.endTag.getCode(),
								var.tpl.getVarName()).toString();
						realtimeDataMap.put(key, Integer.toHexString(Float
								.floatToIntBits(var.lastYcValue)));
					} else if (!Double.isNaN(var.lastYmValue)) {
						String key = KeyDefinition.getKey(
								KeyDefinition.REAL_NUM, model.endTag.getCode(),
								var.tpl.getVarName()).toString();
						realtimeDataMap.put(key, Long.toHexString(Double
								.doubleToLongBits(var.lastYmValue)));
					} else if (var.lastArrayValue != null) {
						String key = KeyDefinition.getKey(
								KeyDefinition.REAL_NUM, model.endTag.getCode(),
								var.tpl.getVarName()).toString();
						realtimeDataMap.put(key, Arrays.toString(var.lastArrayValue));
					}
				}
			}
		}
	}

	private void handleYcValue(EndTagWrapper model, TagVar tagVar, float value,
			Date datetime) {
		// 实时数据更新
		if (Double.isNaN(tagVar.lastYcValue) || tagVar.lastYcValue != value) {
			realtimeDataList.add(tagVar);
		}
		tagVar.lastYcValue = value;

		// 处理遥测越限记录
		handleYcStorage(model, tagVar, value, datetime);
	}
	
	private void handleYmValue(EndTagWrapper model, TagVar tagVar, double value,
			Date datetime) {
		// 实时数据更新
		if (Double.isNaN(tagVar.lastYmValue) || tagVar.lastYmValue != value) {
			realtimeDataList.add(tagVar);
		}
		tagVar.lastYmValue = value;

	}

	/**
	 * @param model
	 * @param tagVar
	 * @param value
	 * @param datetime
	 */
	private void handleYcStorage(EndTagWrapper model, TagVar tagVar,
			double value, Date datetime) {
		TagCfgTpl tpl = tagVar.tpl;
		// if (tagVar.ycStorage != null) {
		// YCStorage storage = tagVar.ycStorage;
		// YCData lastRecord = model.lastYCMap.get(tagVar);
		//
		// if (lastRecord == null) {
		// YCData record = new YCData(model.endTag.getCode(), tpl.getVarName(),
		// tpl.getVarGroup(), value, datetime);
		// ycDataList.add(record);// 加入存储列表
		// model.lastYCMap.put(tagVar, record);
		// } else {
		// if (storage.threshold > 0) {// 按变化范围存储
		// if (Math.abs(lastRecord.getValue() - value) > storage.threshold) {
		// lastRecord.setDatetime(datetime);
		// lastRecord.setValue(value);
		// ycDataList.add(lastRecord);// 加入存储列表
		// }
		// }
		// if (storage.interval > 0) {// 按存储间隔存储
		// if (datetime.getTime() - lastRecord.getDatetime().getTime() >
		// storage.interval * 60_000) {
		// lastRecord.setDatetime(datetime);
		// lastRecord.setValue(value);
		// ycDataList.add(lastRecord);// 加入存储列表
		// }
		// }
		// }
		// }
		// if (tagVar.ymStorage != null) {
		// YMStorage storage = tagVar.ymStorage;
		// YMData lastRecord = model.lastYMMap.get(tagVar);
		//
		// if (lastRecord == null) {
		// YMData record = new YMData(model.endTag.getCode(), tpl.getVarName(),
		// tpl.getVarGroup(), value, 0, datetime);
		// ymDataList.add(record);
		// model.lastYMMap.put(tagVar, record);
		// } else {
		// if (datetime.getTime() - lastRecord.getDatetime().getTime() >
		// storage.interval * 60_000) {
		// double changeValue = value - lastRecord.getValue();
		// if (changeValue >= 0) {
		// lastRecord.setValue(value);
		// lastRecord.setChange(changeValue);
		// lastRecord.setDatetime(datetime);
		// ymDataList.add(lastRecord);
		// }
		// }
		// }
		// }

		if (tagVar.offLimitsStorages != null
				&& !tagVar.offLimitsStorages.isEmpty()) {
			OffLimitsRecord lastRecord = tagVar.lastOffLimitsRecord;
			if (lastRecord != null) {
				if (lastRecord.getType()) {// 上一条记录为越上限
					if (value < lastRecord.getValue()) {
						lastRecord.setResumeTime(datetime);
						model.offLimitsRecordList.add(lastRecord);
					}
				} else {// 上一条记录为越下限
					if (value > lastRecord.getValue()) {
						lastRecord.setResumeTime(datetime);
						model.offLimitsRecordList.add(lastRecord);
					}
				}
			}

			List<OffLimitsStorage> list = tagVar.offLimitsStorages;// 限值升序排序
			// 越下限处理
			for (int i = 0; i < list.size(); i++) {
				OffLimitsStorage storage = list.get(i);
				if (!storage.type) {// 越下限
					if (value < storage.threshold) {
						if (lastRecord == null
								|| lastRecord.getThreshold() != storage.threshold) {
							OffLimitsRecord record = new OffLimitsRecord(
									model.endTag.getCode(), tpl.getVarName(),
									storage.info, value, storage.threshold,
									false, datetime);
							model.offLimitsRecordList.add(record);
							tagVar.lastOffLimitsRecord = record;
						}
					}
					break;
				}
			}
			// 越上限处理
			for (int i = list.size() - 1; i >= 0; i--) {
				OffLimitsStorage storage = list.get(i);
				if (storage.type) {// 越上限
					if (value > storage.threshold) {
						if (lastRecord == null
								|| lastRecord.getThreshold() != storage.threshold) {
							OffLimitsRecord record = new OffLimitsRecord(
									model.endTag.getCode(), tpl.getVarName(),
									storage.info, value, storage.threshold,
									true, datetime);
							model.offLimitsRecordList.add(record);
							tagVar.lastOffLimitsRecord = record;
						}
					}
					break;
				}
			}
		}
	}

	/**
	 * 处理状态值
	 * 
	 * @param model
	 * @param tagVar
	 * @param status
	 * @param date
	 */
	private void handleBoolValue(EndTagWrapper model, TagVar tagVar,
			boolean status, Date date) {
		// 加入实时数据更新队列
		if (tagVar.lastYxValue == -1
				|| tagVar.lastYxValue != (status ? 1 : 0)) {
			realtimeDataList.add(tagVar);
		}
		tagVar.lastYxValue = (status ? 1 : 0);

		// 处理状态类存储器
		handleBoolStorage(model, tagVar, status, date);
	}

	/**
	 * @param model
	 * @param tagVar
	 * @param status
	 * @param date
	 */
	private void handleBoolStorage(EndTagWrapper model, TagVar tagVar,
			boolean status, Date date) {
		TagCfgTpl tpl = tagVar.tpl;
		if (tagVar.faultStorage != null) {
			FaultStorage storage = tagVar.faultStorage;
			FaultRecord lastRecord = tagVar.lastFaultRecord;
			if (lastRecord == null) {// 第一次初始化变量值
				if ((storage.flag && status) || (!storage.flag && !status)) {// 报警
					FaultRecord record = new FaultRecord(model.endTag.getCode(),
							tpl.getVarName(), status ? storage.onInfo
									: storage.offInfo, status, date);
					model.faultRecordList.add(record);// 加入存储列表
					tagVar.lastFaultRecord = record;
				}
			} else {
				if (lastRecord.getValue() != status) {// 变位
					lastRecord.setValue(status);
					if ((storage.flag && status) || (!storage.flag && !status)) {// 报警
						lastRecord.setInfo(status ? storage.onInfo
								: storage.offInfo);
						lastRecord.setActionTime(date);
						lastRecord.setResumeTime(null);
					} else {// 报警解除
						lastRecord.setResumeTime(date);
					}
					model.faultRecordList.add(lastRecord);
				}
			}

		}
		if (tagVar.yxStorage != null) {
			YXStorage storage = tagVar.yxStorage;
			YXData lastRecord = tagVar.lastYxRecord;
			if (lastRecord == null) {// 第一次初始化变量
				YXData record = new YXData(model.endTag.getCode(), tpl.getVarName(),
						status ? storage.onInfo : storage.offInfo, status, date);
				model.yxDataList.add(record);// 加入存储列表
				tagVar.lastYxRecord = record;
			} else {
				if (lastRecord.getValue() != status) {// 变位
					lastRecord.setValue(status);
					lastRecord.setDatetime(date);
					model.yxDataList.add(lastRecord);// 加入存储列表
				}
			}

		}
	}

//	/*
//	 * (non-Javadoc)
//	 * 
//	 * @see com.ht.scada.communication.ICommChannel#endTagAttached(int, int,
//	 * java.lang.String, java.lang.String, java.lang.String, java.util.List,
//	 * java.util.List)
//	 */
//	@Override
//	public void endTagAttached(int deviceAddr, int endTagID, String endTagCode,
//			String endTagType, String tplName, List<VarTplInfo> tplList,
//			List<VarIOInfo> ioInfoList) {
//		synchronized (lock) {
//			final EndTagWrapper model = new EndTagWrapper(deviceAddr, endTagID,
//					endTagCode, endTagType, tplName, tplList, ioInfoList);
//			endTagList.add(model);
//
//			for (TagVar tagVar : model.varList) {// 遍历该节点下的所有变量，并进行处理
//				if (tagVar.tpl.getVarType().equals(VarTypeConst.TYPE_YK)) {// 遥控
//				} else if (tagVar.tpl.getVarType().equals(VarTypeConst.TYPE_YT)) {// 遥调
//				}
//			}
//		}
//	}
//
//	/*
//	 * (non-Javadoc)
//	 * 
//	 * @see com.ht.scada.communication.ICommChannel#endTagDetached(int, int)
//	 */
//	@Override
//	public void endTagDetached(int deviceAddr, int endTagID) {
//		synchronized (lock) {
//			List<EndTagWrapper> deletedList = new LinkedList<>();
//			for (EndTagWrapper model : endTagList) {
//				if (model.deviceAddr == deviceAddr
//						&& model.endTagID == endTagID) {
//					deletedList.add(model);
//				}
//			}
//			if (!deletedList.isEmpty()) {
//				endTagList.removeAll(deletedList);
//			}
//		}
//	}

	/**
	 * @param model
	 * @param tagVar
	 * @return
	 * @throws JMSException
	 * @throws Exception
	 */
	private Session handleYTVar(EndTagWrapper model, final TagVar tagVar)
			throws JMSException, Exception {
		// final Session session = jmsService.createSession();
		// Queue queue = jmsService.getQueue(model.endTag.getCode());
		// MessageConsumer messageConsumer = session.createConsumer(queue,
		// "varName='" + tagVar.tpl.getVarName() + "'");
		// messageConsumer.setMessageListener(new MessageListener() {
		// private MessageProducer replyProducer = session.createProducer(null);
		// @Override
		// public void onMessage(Message message) {
		// try {
		// long endTime = message.getLongProperty(RTUService.TIME_MILLIS);
		// if (endTime < System.currentTimeMillis()) {// 有效时间超时
		// return;
		// }
		//
		// int dataID = tagVar.tpl.getDataID();
		// double value = message.getDoubleProperty("value");
		//
		// Destination replyDestination = message.getJMSReplyTo();
		// if (replyDestination != null) {
		// boolean success = exeYT(dataID, (int) ((value - tagVar.baseValue) /
		// tagVar.coefValue));
		// Message replyMessage = session.createMessage();
		// replyMessage.setJMSCorrelationID(message.getJMSMessageID());
		// replyMessage.setBooleanProperty(RTUService.OP_REPLY_KEY, success);
		// replyProducer.send(replyDestination, replyMessage);
		// }
		//
		// } catch (JMSException e) {
		// e.printStackTrace();
		// }
		// }
		// });
		// return session;
		return null;
	}

	/**
	 * @param model
	 * @param tpl
	 * @return
	 * @throws JMSException
	 * @throws Exception
	 */
	private Session handleYKVar(EndTagWrapper model, final TagVar tagVar)
			throws JMSException, Exception {
		// final Session session = jmsService.createSession();
		// Queue queue = jmsService.getQueue(model.endTag.getCode());
		// MessageConsumer messageConsumer = session.createConsumer(queue,
		// "varName='" + tagVar.tpl.getVarName() + "'");
		// messageConsumer.setMessageListener(new MessageListener() {
		// private MessageProducer replyProducer = session.createProducer(null);
		// @Override
		// public void onMessage(Message message) {// 收到遥控请求
		// try {
		// long endTime = message.getLongProperty(RTUService.TIME_MILLIS);
		// if (endTime < System.currentTimeMillis()) {// 有效时间超时
		// return;
		// }
		//
		// int dataID = tagVar.tpl.getDataID();
		// boolean value = message.getBooleanProperty("value");
		//
		//
		// Destination replyDestination = message.getJMSReplyTo();
		// if (replyDestination != null) {
		// boolean success = exeYK(dataID, value);
		// Message replyMessage = session.createMessage();
		// replyMessage.setJMSCorrelationID(message.getJMSMessageID());
		// replyMessage.setBooleanProperty(RTUService.OP_REPLY_KEY, success);
		// replyProducer.send(replyDestination, replyMessage);
		// }
		//
		// } catch (JMSException e) {
		// e.printStackTrace();
		// }
		// }
		// });
		// return session;
		return null;
	}

	protected static interface DataHandler<T> {
		T each(EndTagWrapper model, TagVar var);
	}

	/*
	 * protected static interface NumDataHandler { double each(EndTagWrapper
	 * model, TagVar var); }
	 */
}
