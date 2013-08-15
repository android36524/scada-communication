package com.ht.scada.communication.model;

import com.ht.scada.communication.entity.EndTag;
import com.ht.scada.communication.entity.FaultRecord;
import com.ht.scada.communication.entity.YxRecord;
import com.ht.scada.communication.util.StorageFactory.FaultStorage;
import com.ht.scada.communication.util.StorageFactory.YXStorage;

import java.util.Date;

public class YxTagVar extends TagVar {

	private final FaultStorage faultStorage;
	private final YXStorage yxStorage;

	private FaultRecord lastFaultRecord;
	private YxRecord lastYxRecord;

	private int lastYxValue = -1;// 当前遥信值

	public YxTagVar(EndTagWrapper endTag,TagVarTplWrapper tplInfo) {
        super(endTag, tplInfo.getTagVarTpl());
		
		this.faultStorage = tplInfo.getFaultStorage();
		this.yxStorage = tplInfo.getYxStorage();
    }

    public int getLastYxValue() {
        return lastYxValue;
    }

    public void update(boolean status, Date date) {
        if (this.lastYxValue == -1 || this.lastYxValue != (status ? 1 : 0)) {
            this.lastYxValue = (status ? 1 : 0);
            // 处理状态类存储器
            this.handleStorage(date);
            // 加入实时数据更新队列
            endTagWrapper.getRealtimeDataMap().put(getTpl().getVarName(), Boolean.toString(status));
        }
    }

    private void handleStorage(Date date) {
        if (lastYxValue == -1) {
            return;
        }
        boolean status = lastYxValue == 1;

        EndTag endTag = endTagWrapper.getEndTag();
        if (faultStorage != null) {
            FaultStorage storage = faultStorage;
            FaultRecord lastRecord = lastFaultRecord;
            if (lastRecord == null) {// 第一次初始化变量值
                if ((storage.flag && status) || (!storage.flag && !status)) {// 报警
                    FaultRecord record = new FaultRecord(endTag.getId(), endTag.getName(), endTag.getCode(),
                            tpl.getVarName(), tpl.getTagName(), status ? storage.onInfo : storage.offInfo, status, date);
                    endTagWrapper.addFaultRecord(record, storage.pushWnd);// 加入存储列表
                    lastFaultRecord = record;
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
                        lastFaultRecord = null;
                    }
                    endTagWrapper.addFaultRecord(lastRecord, storage.pushWnd);
                }
            }

        }
        if (yxStorage != null) {
            YXStorage storage = yxStorage;
            YxRecord lastRecord = lastYxRecord;
            // 是否推送消息
            boolean pushMessage = storage.pushWnd && (storage.alarmType == -1 || (storage.alarmType == lastYxValue));
            if (lastRecord == null || lastRecord.getValue() != status) {// 第一次初始化变量
                YxRecord record = new YxRecord(endTag.getId(), endTag.getName(), endTag.getCode(),
                        tpl.getVarName(), tpl.getTagName(), status ? storage.onInfo : storage.offInfo, status, date);
                endTagWrapper.addYxData(record, pushMessage);// 加入存储列表
                lastYxRecord = record;
            }

        }
    }
}