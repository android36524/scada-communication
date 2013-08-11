package com.ht.scada.communication.model;

import com.ht.scada.communication.entity.OffLimitsRecord;
import com.ht.scada.common.tag.util.DataType;
import com.ht.scada.communication.util.StorageFactory.OffLimitsStorage;

import java.util.*;

public class YcTagVar extends TagVar {

	private final List<OffLimitsStorage> offLimitsStorages;

    /**
     * 未解除的遥测越限记录
     */
    public final List<OffLimitsRecord> unResumedRecords = new ArrayList<>(4);
    /**
     * 未解除的RTU历史数据越限记录
     */
    public final List<OffLimitsRecord> unResumedRtuRecords = new ArrayList<>(4);

	private float baseValue = 0;// 基值
	private float coefValue = 1;// 系数
	private float maxValue = Float.NaN;
	private float minValue = Float.NaN;

    private int rawValue = 0;// 遥测值原始值, 可用于计算INT32类型的数据
	private float lastYcValue = Float.NaN;// 当前遥测遥脉值
    private float[] lastArrayValue = null;// 数组数据

	public YcTagVar(EndTagWrapper endTag, TagVarTplWrapper tplInfo) {
        this(endTag, tplInfo, tplInfo.getTagVarTpl().getBaseValue(), tplInfo.getTagVarTpl().getCoefValue());
	}
    public YcTagVar(EndTagWrapper endTag, TagVarTplWrapper tplInfo, float baseValue, float coefValue) {
        super(endTag, tplInfo.getTagVarTpl());

        this.offLimitsStorages = tplInfo.getOffLimitsStorages();

        this.baseValue = baseValue;
        this.coefValue = coefValue;

        if (tplInfo.getTagVarTpl().getMaxValue() != null)
            maxValue = tplInfo.getTagVarTpl().getMaxValue().floatValue();
        if (tplInfo.getTagVarTpl().getMinValue() != null)
            minValue = tplInfo.getTagVarTpl().getMinValue().floatValue();

        if (tpl.getDataType() == DataType.INT16_ARRAY) {//示功图数据数组
            lastArrayValue = new float[tpl.getByteLen() / 2];
        }
    }

    public float getLastYcValue() {
        return lastYcValue;
    }

    public float[] getLastArrayValue() {
        return lastArrayValue;
    }

    /**
     * 计算并更新变量值, 计算公式为：value * coefValue + baseValue<br/>
     * 处理遥测变量值，包括加入实时数据库更新列表、处理遥测越限报警.<br/>
     * NOTE: 示功图数据需要在打包完成后才能加入实时数据更新列表，所以不在此处实现.<br/>
     *  SEE: EndTagWrapper.generateVarGroupHisData()<br/>
     * @param value
     * @param datetime
     */
    public void update(float value, Date datetime) {
        if (Float.isNaN(value)) {
            return;
        }
        value = calcValue(value);
        if (Float.isNaN(this.lastYcValue) || this.lastYcValue != value) {
            this.lastYcValue = value;
            // 处理遥测越限记录
            handleOffLimits(datetime, value);
            // 实时数据更新
            endTagWrapper.getRealtimeDataMap().put(getTpl().getVarName(), Float.toString(value));
        }
    }

    /**
     * 更新遥测数组值, 计算公式为：value * coefValue + baseValue
     * @param value
     * @param index
     */
    public void updateArrayValue(float value, int index) {
        if (lastArrayValue == null || Float.isNaN(value)) {
            return;
        }
        lastArrayValue[index] = calcValue(value);
    }

    /**
     * 计算并更新变量值, 计算公式为：value * coefValue + baseValue
     * @param value
     * @return
     */
    public float calcValue(float value) {
        return value * coefValue + baseValue;
    }

    public int getRawValue() {
        return rawValue;
    }

    public void setRawValue(int rawValue) {
        this.rawValue = rawValue;
    }

    private void handleOffLimits(Date datetime, double value) {
        if (this.offLimitsStorages != null
                && !this.offLimitsStorages.isEmpty()) {

            // 处理未恢复的越限报警
            List<OffLimitsRecord> resumedRecords = new ArrayList<>(4);
            for (OffLimitsRecord unResumedRecord : this.unResumedRecords) {
                if (unResumedRecord.getType()) {// 上一条记录为越上限
                    if (value < unResumedRecord.getThreshold() && datetime.getTime() >= unResumedRecord.getActionTime().getTime()) {// 解除越上限
                        // 越限恢复
                        unResumedRecord.setResumeTime(datetime);

                        // todo 还有更高效的办法来获取pushWnd吗？
                        boolean pushMessage = true;
                        for (OffLimitsStorage storage : offLimitsStorages) {
                            if (storage.threshold == unResumedRecord.getThreshold()) {
                                pushMessage = storage.pushWnd;
                                break;
                            }
                        }
                        endTagWrapper.addOffLimitsRecord(unResumedRecord, pushMessage);
                        resumedRecords.add(unResumedRecord);
                        //UUID.randomUUID()
                    }
                } else {// 上一条记录为越下限
                    if (value > unResumedRecord.getThreshold() && datetime.getTime() >= unResumedRecord.getActionTime().getTime()) {// 解除越下限
                        // 越限恢复
                        unResumedRecord.setResumeTime(datetime);
                        // todo 还有更高效的办法来获取pushWnd吗？
                        boolean pushMessage = true;
                        for (OffLimitsStorage storage : offLimitsStorages) {
                            if (storage.threshold == unResumedRecord.getThreshold()) {
                                pushMessage = storage.pushWnd;
                                break;
                            }
                        }
                        endTagWrapper.addOffLimitsRecord(unResumedRecord, pushMessage);
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
                                endTagWrapper.getEndTag().getCode(), tpl.getVarName(),
                                storage.info, value, storage.threshold,
                                false, datetime);
                        endTagWrapper.addOffLimitsRecord(record, storage.pushWnd);
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
                                endTagWrapper.getEndTag().getCode(), tpl.getVarName(),
                                storage.info, value, storage.threshold,
                                true, datetime);
                        endTagWrapper.addOffLimitsRecord(record, storage.pushWnd);
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
                        endTagWrapper.addOffLimitsRecord(unResumedRecord, false);
                        resumedRecords.add(unResumedRecord);
                    }
                } else {// 上一条记录为越下限
                    if (value > unResumedRecord.getThreshold() && datetime.getTime() >= unResumedRecord.getActionTime().getTime()) {// 解除越下限
                        unResumedRecord.setResumeTime(datetime);
                        endTagWrapper.addOffLimitsRecord(unResumedRecord, false);
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
                                endTagWrapper.getEndTag().getCode(), tpl.getVarName(),
                                storage.info, value, storage.threshold,
                                false, datetime);
                        endTagWrapper.addOffLimitsRecord(record, false);
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
                                endTagWrapper.getEndTag().getCode(), tpl.getVarName(),
                                storage.info, value, storage.threshold,
                                true, datetime);
                        endTagWrapper.addOffLimitsRecord(record, false);
                        this.unResumedRtuRecords.add(record);
                    }
                    break;
                }
            }
        }
    }
}