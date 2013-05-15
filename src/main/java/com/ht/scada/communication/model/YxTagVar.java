package com.ht.scada.communication.model;

import com.ht.scada.common.tag.util.StorageFactory.FaultStorage;
import com.ht.scada.common.tag.util.StorageFactory.YXStorage;
import com.ht.scada.data.kv.FaultRecord;
import com.ht.scada.data.kv.YXData;

import java.util.Date;
import java.util.Map;

public class YxTagVar extends TagVar {

	public final FaultStorage faultStorage;
	public final YXStorage yxStorage;

	public FaultRecord lastFaultRecord;
	public YXData lastYxRecord;

	public int lastYxValue = -1;// 当前遥信值

	public YxTagVar(EndTagWrapper endTag,VarTplInfo tplInfo) {
        super(endTag, tplInfo.getTagTpl());
		
		this.faultStorage = tplInfo.getFaultStorage();
		this.yxStorage = tplInfo.getYxStorage();
	}

    public void update(boolean status, Date date, Map<String, String> realtimeDataMap) {
        if (this.lastYxValue == -1 || this.lastYxValue != (status ? 1 : 0)) {
            this.lastYxValue = (status ? 1 : 0);
            // 处理状态类存储器
            this.handleStorage(date);
            // 加入实时数据更新队列
            String key = endTagWrapper.endTag.getCode() + "/" + this.tpl.getVarName();
            realtimeDataMap.put(key, Boolean.toString(status));
        }
    }

    private void handleStorage(Date date) {
        if (lastYxValue == -1) {
            return;
        }
        boolean status = lastYxValue == 1;

        if (faultStorage != null) {
            FaultStorage storage = faultStorage;
            FaultRecord lastRecord = lastFaultRecord;
            if (lastRecord == null) {// 第一次初始化变量值
                if ((storage.flag && status) || (!storage.flag && !status)) {// 报警
                    FaultRecord record = new FaultRecord(endTagWrapper.endTag.getCode(),
                            tpl.getVarName(), status ? storage.onInfo
                            : storage.offInfo, status, date);
                    endTagWrapper.addFaultRecord(record);// 加入存储列表
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
                    }
                    endTagWrapper.addFaultRecord(lastRecord);
                }
            }

        }
        if (yxStorage != null) {
            YXStorage storage = yxStorage;
            YXData lastRecord = lastYxRecord;
            if (lastRecord == null) {// 第一次初始化变量
                YXData record = new YXData(endTagWrapper.endTag.getCode(), tpl.getVarName(),
                        status ? storage.onInfo : storage.offInfo, status, date);
                endTagWrapper.addYxData(record);// 加入存储列表
                lastYxRecord = record;
            } else {
                if (lastRecord.getValue() != status) {// 变位
                    lastRecord.setValue(status);
                    lastRecord.setDatetime(date);
                    endTagWrapper.addYxData(lastRecord);// 加入存储列表
                }
            }

        }
    }
}