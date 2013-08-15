package com.ht.scada.communication;

import com.ht.scada.common.tag.util.VarGroupEnum;
import com.ht.scada.communication.entity.ChannelInfo;
import com.ht.scada.communication.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * @author 薄成文
 * 
 */
public abstract class CommunicationChannel implements ICommChannel {

    public static final Logger log = LoggerFactory.getLogger(CommunicationChannel.class);

	protected final ChannelInfo channel;

//	private List<EndTagWrapper> endTagList = Collections
//			.synchronizedList(new LinkedList<EndTagWrapper>());
	private final List<EndTagWrapper> endTagList;

	/** 实时数据暂存队列 **/
	//protected Map<String, String> realtimeDataMap = new HashMap<>(256);

    protected CommunicationChannel(ChannelInfo channel, List<EndTagWrapper> endTagList) throws Exception {

        this.channel = channel;
        this.endTagList = endTagList;
        if (endTagList == null || endTagList.isEmpty()) {
            log.warn("采集通道[{}]未关联监控对象, 请检查配置", channel.getName());
        } else {
            log.info("采集通道[{}]监控对象数量：{}", channel.getName(), endTagList.size());
            init();
        }
    }

	public ChannelInfo getChannel() {
		return channel;
	}

    /**
     * 更新实时数据
     */
	protected void updateRealtimeData() {
        for (EndTagWrapper wrapper : endTagList) {
            wrapper.updateRealtimeData();
        }
	}

	/**
	 * 保存历史数据
	 */
	protected void persistRtuHistoryData() {
		for (EndTagWrapper model : endTagList) {// 遍历所有节点并进行处理
            model.persistRtuHistoryData();
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
	protected void generateHistoryData(VarGroupEnum varGroup, Date datetime) {
		for (EndTagWrapper model : endTagList) {// 遍历所有末端
            model.generateVarGroupHisData(varGroup, datetime);
		}
	}

    public void forEachEndTag(EndTagHandler handler) {
        for (EndTagWrapper model : endTagList) {// 遍历所有节点并进行处理
            if (!handler.each(model)) {
                break;
            }
        }
    }

    /**
     * 遍历采集设备（RTU）对应的末端的所有遥信变量
     * @param deviceAddr 设备地址过滤
     */
    public void forEachYc2YxTagVar(int deviceAddr, DataHandler<YxTagVar > dataHandler) {
        for (EndTagWrapper model : endTagList) {// 遍历所有节点并进行处理
            if (!isRunning()) {
                return;
            }
            if (model.getEndTag().getDeviceAddr() != deviceAddr) {
                continue;
            }

            for (YxTagVar var : model.getYc2YxVarList()) {
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
     * 遍历采集设备（RTU）对应的末端的所有遥信变量
     * @param deviceAddr 设备地址过滤
     */
    public void forEachYxTagVar(int deviceAddr, DataHandler<YxTagVar > dataHandler) {
        for (EndTagWrapper model : endTagList) {// 遍历所有节点并进行处理
            if (!isRunning()) {
                return;
            }
            if (model.getEndTag().getDeviceAddr() != deviceAddr) {
                continue;
            }

            for (YxTagVar var : model.getYxVarList()) {
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
            if (model.getEndTag().getDeviceAddr() != deviceAddr) {
                continue;
            }

            for (YcTagVar var : model.getYcVarList()) {
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
     * 遍历采集设备（RTU）对应的末端的所有遥测数组变量
     * @param deviceAddr 设备地址过滤
     */
    protected void forEachYcArrayTagVar(int deviceAddr, DataHandler<YcTagVar> dataHandler) {
        for (EndTagWrapper model : endTagList) {// 遍历所有节点并进行处理
            if (!isRunning()) {
                return;
            }
            if (model.getEndTag().getDeviceAddr() != deviceAddr) {
                continue;
            }

            for (YcTagVar var : model.getYcArrayVarList()) {
                if (!isRunning()) {
                    return;
                }

                if (var.getLastArrayValue() != null) {// 遥测数组
                    if (!dataHandler.each(model, var)) {
                        break;
                    }
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
            if (model.getEndTag().getDeviceAddr() != deviceAddr) {
                continue;
            }

            for (YmTagVar var : model.getYmVarList()) {
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
            if (model.getEndTag().getDeviceAddr() != deviceAddr) {
                continue;
            }

            for (TagVar var : model.getQtVarList()) {
                if (!isRunning()) {
                    return;
                }

                if (!dataHandler.each(model, var)) {

                    break;
                }
            }
        }
    }

    protected void forEachAsciiTagVar(int deviceAddr, DataHandler<AsciiTagVar> dataHandler) {
        for (EndTagWrapper model : endTagList) {// 遍历所有节点并进行处理
            if (!isRunning()) {
                return;
            }
            if (model.getEndTag().getDeviceAddr() != deviceAddr) {
                continue;
            }

            for (AsciiTagVar var : model.getAsciiTagVarList()) {
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
     * 遍历采集设备（RTU）对应的末端的所有遥控变量
     * @param deviceAddr 设备地址过滤
     */
    protected void forEachYkTagVar(int deviceAddr, DataHandler<TagVar> dataHandler) {
        for (EndTagWrapper model : endTagList) {// 遍历所有节点并进行处理
            if (!isRunning()) {
                return;
            }
            if (deviceAddr > 0 && model.getEndTag().getDeviceAddr() != deviceAddr) {
                continue;
            }

            for (TagVar var : model.getYkVarList()) {
                if (!isRunning()) {
                    return;
                }

                if (!dataHandler.each(model, var)) {

                    break;
                }
            }
        }
    }

    public TagVar getYkVar(final String endCode, final String varName) {
        for (EndTagWrapper model : endTagList) {// 遍历所有节点并进行处理
            if (!isRunning()) {
                return null;
            }

            if (!model.getEndTag().getCode().equals(endCode)) {
                continue;
            }

            for (TagVar var : model.getYkVarList()) {
                if (!isRunning()) {
                    return null;
                }
                if (var.getTpl().getVarName().equals(varName)) {
                    return var;
                }
            }
        }

        return null;
    }

    /**
     * 遍历采集设备（RTU）对应的末端的所有遥控变量
     * @param deviceAddr 设备地址过滤
     */
    protected void forEachYtTagVar(int deviceAddr, DataHandler<TagVar> dataHandler) {
        for (EndTagWrapper model : endTagList) {// 遍历所有节点并进行处理
            if (!isRunning()) {
                return;
            }
            if (model.getEndTag().getDeviceAddr() != deviceAddr) {
                continue;
            }

            for (TagVar var : model.getYtVarList()) {
                if (!isRunning()) {
                    return;
                }

                if (!dataHandler.each(model, var)) {

                    break;
                }
            }
        }
    }

    public TagVar getYtVar(final String endCode, final String varName) {
        for (EndTagWrapper model : endTagList) {// 遍历所有节点并进行处理
            if (!isRunning()) {
                return null;
            }

            if (!model.getEndTag().getCode().equals(endCode)) {
                continue;
            }

            for (TagVar var : model.getYtVarList()) {
                if (!isRunning()) {
                    return null;
                }
                if (var.getTpl().getVarName().equals(varName)) {
                    return var;
                }
            }
        }

        return null;
    }

    public List<EndTagWrapper> getEndTagList() {
        return endTagList;
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
//			String endTagType, String tplName, List<TagVarTplWrapper> tplList,
//			List<VarIOInfo> ioInfoList) {
//		synchronized (lock) {
//			final EndTagWrapper model = new EndTagWrapper(deviceAddr, endTagID,
//					endTagCode, endTagType, tplName, tplList, ioInfoList);
//			endTagList.add(model);
//
//			for (TagVar tagVar : model.varList) {// 遍历该节点下的所有变量，并进行处理
//				if (tagVar.tpl.getType().equals(VarTypeConst.TYPE_YK)) {// 遥控
//				} else if (tagVar.tpl.getType().equals(VarTypeConst.TYPE_YT)) {// 遥调
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

    protected static interface DataHandler2<T, V> extends DataHandler<T>, Callable<V> {
    }

    public static interface EndTagHandler {
        boolean each(EndTagWrapper model);
    }

	/*
	 * protected static interface NumDataHandler { double each(EndTagWrapper
	 * model, TagVar var); }
	 */
}
