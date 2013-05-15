package com.ht.scada.communication.model;

import com.ht.scada.common.tag.util.DataType;
import com.ht.scada.common.tag.util.StorageFactory.OffLimitsStorage;
import com.ht.scada.data.kv.OffLimitsRecord;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class YcTagVar extends TagVar {

	public final List<OffLimitsStorage> offLimitsStorages;

    /**
     * 未解除的遥测越限记录
     */
    public final List<OffLimitsRecord> unResumedRecords = new ArrayList<>(4);
    /**
     * 未解除的RTU历史数据越限记录
     */
    public final List<OffLimitsRecord> unResumedRtuRecords = new ArrayList<>(4);

	public float baseValue = 0;// 基值
	public float coefValue = 1;// 系数
	public float maxValue = Float.NaN;
	public float minValue = Float.NaN;

	public float lastYcValue = Float.NaN;// 当前遥测遥脉值
    public float[] lastArrayValue = null;// 数组数据

	public YcTagVar(EndTagWrapper endTag, VarTplInfo tplInfo) {
        super(endTag, tplInfo.getTagTpl());
		
		this.offLimitsStorages = tplInfo.getOffLimitsStorages();
		
		baseValue = tplInfo.getTagTpl().getBaseValue();
		coefValue = tplInfo.getTagTpl().getCoefValue();
		
		if (tplInfo.getTagTpl().getMax() != null)
			maxValue = tplInfo.getTagTpl().getMax().floatValue();
		if (tplInfo.getTagTpl().getMin() != null)
			minValue = tplInfo.getTagTpl().getMin().floatValue();
		
		if (tpl.getDataType() == DataType.INT16_ARRAY) {//示功图数据数组
			lastArrayValue = new float[tpl.getByteLen() / 2];
		}
	}

    /**
     * 处理遥测变量值，包括加入实时数据库更新列表、处理遥测越限报警
     * @param value
     * @param datetime
     */
    public void update(float value, Date datetime, Map<String, String> realtimeDataMap) {
        if (Float.isNaN(lastYcValue)) {
            return;
        }
        if (Double.isNaN(this.lastYcValue) || this.lastYcValue != value) {
            this.lastYcValue = value;
            // 处理遥测越限记录
            handleOffLimits(datetime, value);
            // 实时数据更新
            String key = endTagWrapper.endTag.getCode() + "/" + this.tpl.getVarName();
            realtimeDataMap.put(key, Float.toString(value));
        }
    }

    private void handleOffLimits(Date datetime, double value) {
        if (this.offLimitsStorages != null
                && !this.offLimitsStorages.isEmpty()) {

            // 处理未恢复的越限报警
            List<OffLimitsRecord> resumedRecords = new ArrayList<>(4);
            for (OffLimitsRecord unResumedRecord : this.unResumedRecords) {
                if (unResumedRecord.getType()) {// 上一条记录为越上限
                    if (value < unResumedRecord.getThreshold() && datetime.getTime() >= unResumedRecord.getActionTime().getTime()) {// 解除越上限
                        unResumedRecord.setResumeTime(datetime);
                        endTagWrapper.addOffLimitsRecord(unResumedRecord);
                        resumedRecords.add(unResumedRecord);
                    }
                } else {// 上一条记录为越下限
                    if (value > unResumedRecord.getThreshold() && datetime.getTime() >= unResumedRecord.getActionTime().getTime()) {// 解除越下限
                        unResumedRecord.setResumeTime(datetime);
                        endTagWrapper.addOffLimitsRecord(unResumedRecord);
                        resumedRecords.add(unResumedRecord);
                    }
                }
            }
            this.unResumedRecords.removeAll(resumedRecords);

            // 越下限处理
            for (int i = 0; i < offLimitsStorages.size(); i++) {
                OffLimitsStorage storage = offLimitsStorages.get(i);
                if (!storage.type && value < storage.threshold) {// 越下限
                    boolean isRecorded = false;
                    for (OffLimitsRecord unResumedRecord : this.unResumedRecords) {
                        if (unResumedRecord.getThreshold() == storage.threshold) {
                            isRecorded = true;
                            break;
                        }
                    }
                    if (!isRecorded) {
                        OffLimitsRecord record = new OffLimitsRecord(
                                endTagWrapper.endTag.getCode(), tpl.getVarName(),
                                storage.info, value, storage.threshold,
                                false, datetime);
                        endTagWrapper.addOffLimitsRecord(record);
                        this.unResumedRecords.add(record);
                    }
                    break;
                }
            }
            // 越上限处理
            for (int i = offLimitsStorages.size() - 1; i >= 0; i--) {
                OffLimitsStorage storage = offLimitsStorages.get(i);
                if (storage.type && value > storage.threshold) {// 越上限
                    boolean isRecorded = false;
                    for (OffLimitsRecord unResumedRecord : this.unResumedRecords) {
                        if (unResumedRecord.getThreshold() == storage.threshold) {
                            isRecorded = true;
                            break;
                        }
                    }
                    if (!isRecorded) {
                        OffLimitsRecord record = new OffLimitsRecord(
                                endTagWrapper.endTag.getCode(), tpl.getVarName(),
                                storage.info, value, storage.threshold,
                                true, datetime);
                        endTagWrapper.addOffLimitsRecord(record);
                        this.unResumedRecords.add(record);
                    }
                    break;
                }
            }
        }
    }

    /**
     * 处理RTU历史数据中的遥测越限
     * @param datetime
     * @param value
     */
    public void handleRtuHisDataOffLimits(Date datetime, double value) {
        if (this.offLimitsStorages != null
                && !this.offLimitsStorages.isEmpty()) {

            // 处理未恢复的越限报警
            List<OffLimitsRecord> resumedRecords = new ArrayList<>(4);
            for (OffLimitsRecord unResumedRecord : this.unResumedRtuRecords) {
                if (unResumedRecord.getType()) {// 上一条记录为越上限
                    if (value < unResumedRecord.getThreshold() && datetime.getTime() >= unResumedRecord.getActionTime().getTime()) {// 解除越上限
                        unResumedRecord.setResumeTime(datetime);
                        endTagWrapper.addOffLimitsRecord(unResumedRecord);
                        resumedRecords.add(unResumedRecord);
                    }
                } else {// 上一条记录为越下限
                    if (value > unResumedRecord.getThreshold() && datetime.getTime() >= unResumedRecord.getActionTime().getTime()) {// 解除越下限
                        unResumedRecord.setResumeTime(datetime);
                        endTagWrapper.addOffLimitsRecord(unResumedRecord);
                        resumedRecords.add(unResumedRecord);
                    }
                }
            }
            this.unResumedRtuRecords.removeAll(resumedRecords);

            // 越下限处理
            for (int i = 0; i < offLimitsStorages.size(); i++) {
                OffLimitsStorage storage = offLimitsStorages.get(i);
                if (!storage.type && value < storage.threshold) {// 越下限
                    boolean isRecorded = false;
                    for (OffLimitsRecord unResumedRecord : this.unResumedRtuRecords) {
                        if (unResumedRecord.getThreshold() == storage.threshold) {
                            isRecorded = true;
                            break;
                        }
                    }
                    if (!isRecorded) {
                        OffLimitsRecord record = new OffLimitsRecord(
                                endTagWrapper.endTag.getCode(), tpl.getVarName(),
                                storage.info, value, storage.threshold,
                                false, datetime);
                        endTagWrapper.addOffLimitsRecord(record);
                        this.unResumedRtuRecords.add(record);
                    }
                    break;
                }
            }
            // 越上限处理
            for (int i = offLimitsStorages.size() - 1; i >= 0; i--) {
                OffLimitsStorage storage = offLimitsStorages.get(i);
                if (storage.type && value > storage.threshold) {// 越上限
                    boolean isRecorded = false;
                    for (OffLimitsRecord unResumedRecord : this.unResumedRtuRecords) {
                        if (unResumedRecord.getThreshold() == storage.threshold) {
                            isRecorded = true;
                            break;
                        }
                    }
                    if (!isRecorded) {
                        OffLimitsRecord record = new OffLimitsRecord(
                                endTagWrapper.endTag.getCode(), tpl.getVarName(),
                                storage.info, value, storage.threshold,
                                true, datetime);
                        endTagWrapper.addOffLimitsRecord(record);
                        this.unResumedRtuRecords.add(record);
                    }
                    break;
                }
            }
        }
    }
}