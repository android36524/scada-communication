package com.ht.scada.communication;

import com.ht.scada.common.tag.entity.AcquisitionChannel;
import com.ht.scada.common.tag.util.VarGroup;
import com.ht.scada.communication.model.*;
import com.ht.scada.communication.service.DataService;
import com.ht.scada.data.service.RealtimeDataService;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
	private final List<EndTagWrapper> endTagList;

	/** 实时数据暂存队列 **/
	protected Map<String, String> realtimeDataMap = new HashMap<>(256);

    protected CommunicationChannel(AcquisitionChannel channel, List<EndTagWrapper> endTagList) throws Exception {
        this.channel = channel;
        this.endTagList = endTagList;
        init();
    }

	public AcquisitionChannel getChannel() {
		return channel;
	}

    /**
     * 更新实时数据
     */
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
            model.persistHistoryData(dataService);
		}
	}

	/**
	 * 生成分组历史数据.<br>
	 * <p>
	 * 如果变量组配置中的存储间隔<=0,则直接将该分组数据加入存储列表。<br>
	 * 示功图数据等可以采用该方式进行配置。
	 * </p>
	 * <p>
	 * 如果变量组配置中的存储间隔>0,则先判断datetime的分钟数对应的时间段是否已经记录，如果没有则将该分组数据加入存储列表。<br>
	 * </p>
	 * @param varGroup
	 * @param datetime 数据对应的日期时间
	 */
	protected void generateHistoryData(VarGroup varGroup, Date datetime) {
		for (EndTagWrapper model : endTagList) {// 遍历所有末端
            model.generateVarGroupHisData(varGroup, datetime, realtimeDataMap);
		}
	}
	
    /**
     * 遍历采集设备（RTU）对应的末端的所有遥信变量
     * @param deviceAddr 设备地址过滤
     */
    protected void forEachYxTagVar(int deviceAddr, DataHandler<YxTagVar> dataHandler) {
        for (EndTagWrapper model : endTagList) {// 遍历所有节点并进行处理
            if (!isRunning()) {
                return;
            }
            if (model.endTag.getDeviceAddr() != deviceAddr) {
                continue;
            }

            for (YxTagVar var : model.yxVarList) {
                if (!isRunning()) {
                    return;
                }

                if (!dataHandler.each(model, var)) {
                    break;
                }
            }
        }
    }

    /**
     * 遍历采集设备（RTU）对应的末端的所有遥测变量
     * @param deviceAddr 设备地址过滤
     */
    protected void forEachYcTagVar(int deviceAddr, DataHandler<YcTagVar> dataHandler) {
        for (EndTagWrapper model : endTagList) {// 遍历所有节点并进行处理
            if (!isRunning()) {
                return;
            }
            if (model.endTag.getDeviceAddr() != deviceAddr) {
                continue;
            }

            for (YcTagVar var : model.ycVarList) {
                if (!isRunning()) {
                    return;
                }

                if (!dataHandler.each(model, var)) {
                    break;
                }
            }
        }
    }

    /**
     * 遍历采集设备（RTU）对应的末端的所有遥脉变量
     * @param deviceAddr 设备地址过滤
     */
    protected void forEachYmTagVar(int deviceAddr, DataHandler<YmTagVar> dataHandler) {
        for (EndTagWrapper model : endTagList) {// 遍历所有节点并进行处理
            if (!isRunning()) {
                return;
            }
            if (model.endTag.getDeviceAddr() != deviceAddr) {
                continue;
            }

            for (YmTagVar var : model.ymVarList) {
                if (!isRunning()) {
                    return;
                }

                if (!dataHandler.each(model, var)) {
                    break;
                }
            }
        }
    }

    /**
     * 遍历采集设备（RTU）对应的末端的所有其它变量
     * @param deviceAddr 设备地址过滤
     */
    protected void forEachQtTagVar(int deviceAddr, DataHandler<TagVar> dataHandler) {
        for (EndTagWrapper model : endTagList) {// 遍历所有节点并进行处理
            if (!isRunning()) {
                return;
            }
            if (model.endTag.getDeviceAddr() != deviceAddr) {
                continue;
            }

            for (TagVar var : model.qtVarList) {
                if (!isRunning()) {
                    return;
                }

                if (!dataHandler.each(model, var)) {

                    break;
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

	protected static interface DataHandler<T> {
		boolean each(EndTagWrapper model, T var);
	}

	/*
	 * protected static interface NumDataHandler { double each(EndTagWrapper
	 * model, TagVar var); }
	 */
}
