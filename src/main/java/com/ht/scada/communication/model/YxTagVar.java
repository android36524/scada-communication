package com.ht.scada.communication.model;

import com.ht.scada.communication.data.kv.FaultRecord;
import com.ht.scada.communication.data.kv.YXData;
import com.ht.scada.communication.util.StorageFactory.FaultStorage;
import com.ht.scada.communication.util.StorageFactory.YXStorage;

import java.util.Date;
import java.util.Map;

public class YxTagVar extends TagVar {

	private final FaultStorage faultStorage;
	private final YXStorage yxStorage;

	private FaultRecord lastFaultRecord;
	private YXData lastYxRecord;

	private int lastYxValue = -1;// 当前遥信值

	public YxTagVar(EndTagWrapper endTag,TagVarTplWrapper tplInfo) {
        super(endTag, tplInfo.getTagVarTpl());
		
		this.faultStorage = tplInfo.getFaultStorage();
		this.yxStorage = tplInfo.getYxStorage();
	}

    public int getLastYxValue() {
        return lastYxValue;
    }

    public void update(boolean status, Date date, Map<String, String> realtimeDataMap) {
        if (this.lastYxValue == -1 || this.lastYxValue != (status ? 1 : 0)) {
            this.lastYxValue = (status ? 1 : 0);
            // 处理状态类存储器
            this.handleStorage(date);
            // 加入实时数据更新队列
            realtimeDataMap.put(getRTKey(), Boolean.toString(status));
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
                    FaultRecord record = new FaultRecord(endTagWrapper.getEndTag().getCode(),
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
                YXData record = new YXData(endTagWrapper.getEndTag().getCode(), tpl.getVarName(),
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