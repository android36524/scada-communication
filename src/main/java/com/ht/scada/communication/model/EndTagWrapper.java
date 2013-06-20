package com.ht.scada.communication.model;

import com.ht.scada.common.tag.util.VarGroupEnum;
import com.ht.scada.common.tag.util.VarSubTypeEnum;
import com.ht.scada.common.tag.util.VarTypeEnum;
import com.ht.scada.communication.DataBaseManager;
import com.ht.scada.communication.data.kv.VarGroupData;
import com.ht.scada.communication.entity.YxRecord;
import com.ht.scada.communication.entity.*;
import com.ht.scada.communication.service.HistoryDataService;
import com.ht.scada.communication.service.RealtimeDataService;
import com.ht.scada.common.tag.util.DataType;
import org.apache.commons.lang.StringUtils;
import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class EndTagWrapper {
    public static final Logger log = LoggerFactory.getLogger(EndTagWrapper.class);

    private HistoryDataService historyDataService;
    private RealtimeDataService realtimeDataService;

	private final EndTag endTag;

    /**
     * 变量分组
     */
	private final Map<VarGroupEnum, VarGroupWrapper> varGroupWrapperMap;

	/**
     * RTU上送的历史数据暂存队列
     */
	private final Map<String, VarGroupData> historyGroupDataMap = new HashMap<>();

    /**
     * 历史数据暂存
     */
	private final List<VarGroupData> groupDataList = new ArrayList<>();
	private final List<YxRecord> yxRecordList = new ArrayList<>();
	private final List<FaultRecord> faultRecordList = new ArrayList<>();
	private final List<OffLimitsRecord> offLimitsRecordList = new ArrayList<>();

    /**
     * 遥信、遥测、遥脉实时数据暂存
     */
    private Map<String, String> realtimeDataMap = new HashMap<>(256);
    /**
     *遥测数组数据暂存
     */
    private Map<String, String> realtimeYcArrayDataMap = new HashMap<>(16);

    /**
     * TODO: 包括数组变量, 此种做法可能会造成错误的操作, 暂时没有更好的办法
     */
    private final List<YcTagVar> ycVarList = new ArrayList<>();
    private final List<YmTagVar> ymVarList = new ArrayList<>();
    private final List<YxTagVar> yxVarList = new ArrayList<>();

    private final List<TagVar> qtVarList = new ArrayList<>();
    private final List<TagVar> ykVarList = new ArrayList<>();
    private final List<TagVar> ytVarList = new ArrayList<>();

    private YxTagVar rtuStatusVar;

	public EndTagWrapper(EndTag endTag, List<VarGroupInfo> varGroupInfos,
                         List<TagVarTplWrapper> tplVarList, List<VarIOInfo> ioInfoList) {

        this.historyDataService = DataBaseManager.getInstance().getHistoryDataService();
        this.realtimeDataService = DataBaseManager.getInstance().getRealtimeDataService();
		this.endTag = endTag;
		
		varGroupWrapperMap = new HashMap<>(varGroupInfos.size());
		for (VarGroupInfo cfg : varGroupInfos) {
			VarGroupWrapper wrapper = new VarGroupWrapper(cfg);
			varGroupWrapperMap.put(cfg.getName(), wrapper);
		}

		//this.varList = new ArrayList<>(tplVarList.size());
		for (TagVarTplWrapper tplWrapper : tplVarList) {
			//varList.add(tagVar);
            VarGroupWrapper varGroupWrapper = varGroupWrapperMap.get(tplWrapper.getTagVarTpl().getVarGroup());
            if (varGroupWrapper == null) {
                throw new RuntimeException("未找到变量分组" + tplWrapper.getTagVarTpl().getVarGroup() + "的配置信息");
            }

            switch (tplWrapper.getTagVarTpl().getVarType()) {
                case YX:
                {
                    YxTagVar yxTagVar = new YxTagVar(this, tplWrapper);
                    yxVarList.add(yxTagVar);
                    if (varGroupWrapper != null) {
                        varGroupWrapper.getYxVarList().add(yxTagVar);
                    }
                    if (tplWrapper.getTagVarTpl().getSubType() == VarSubTypeEnum.RTU_RJ45_STATUS) {
                        // 网络通讯状态
                        rtuStatusVar = yxTagVar;
                    }
                    break;
                }
                case YC:
                {
                    YcTagVar tagVar = createYcTagVar(ioInfoList, tplWrapper);
                    ycVarList.add(tagVar);
                    if (varGroupWrapper != null) {
                        varGroupWrapper.getYcVarList().add(tagVar);
                    }
                }
                    break;
                case YM:
                {
                    YmTagVar tagVar = createYmTagVar(ioInfoList, tplWrapper);
                    ymVarList.add(tagVar);
                    if (varGroupWrapper != null) {
                        varGroupWrapper.getYmVarList().add(tagVar);
                    }
                }
                    break;
                case YK:
                    ykVarList.add(new TagVar(this, tplWrapper.getTagVarTpl()));
                    break;
                case YT:
                    ytVarList.add(new TagVar(this, tplWrapper.getTagVarTpl()));
                    break;
                case QT:
                    if (tplWrapper.getTagVarTpl().getDataType() == DataType.INT16_ARRAY) {//遥测数组
                        YcTagVar tagVar = createYcTagVar(ioInfoList, tplWrapper);
                        ycVarList.add(tagVar);
                    }
                    qtVarList.add(new TagVar(this, tplWrapper.getTagVarTpl()));
                    break;
            }
		}

        Map<String, String> groupVarMap = new HashMap<>();
        // 移除没用到的变量分组
        Set<VarGroupEnum> varGroupToRemove = new HashSet<>();
        for (VarGroupEnum varGroup : varGroupWrapperMap.keySet()) {
            VarGroupWrapper varGroupWrapper = varGroupWrapperMap.get(varGroup);
            if (varGroupWrapper.getYmVarList().isEmpty()
                    && varGroupWrapper.getYxVarList().isEmpty()
                    && varGroupWrapper.getYcVarList().isEmpty()) {
                varGroupToRemove.add(varGroup);
            } else {
                List<String> list = new ArrayList<>();
                for (YxTagVar yx : varGroupWrapper.getYxVarList()) {
                    list.add(yx.getTpl().getVarName());
                }
                for (YmTagVar ym : varGroupWrapper.getYmVarList()) {
                    list.add(ym.getTpl().getVarName());
                }
                for (YcTagVar yc : varGroupWrapper.getYcVarList()) {
                    if (yc.getTpl().getVarType() == VarTypeEnum.YC) {
                        list.add(yc.getTpl().getVarName());
                    }
                }
                groupVarMap.put(varGroup.toString(), StringUtils.join(list, ","));
            }
        }
        for (VarGroupEnum varGroup : varGroupToRemove) {
            varGroupWrapperMap.remove(varGroup);
        }
	}

    public void updateRtuStatus(boolean status, Date date) {
        if (rtuStatusVar != null) {
            rtuStatusVar.update(status, date);
        }
    }

    public Map<VarGroupEnum, VarGroupWrapper> getVarGroupWrapperMap() {
        return varGroupWrapperMap;
    }

    public Map<String, String> getRealtimeDataMap() {
        return realtimeDataMap;
    }

    /**
     * 获取指定变量组的存储间隔
     * @param varGroup
     * @return
     */
    public int getSaveIntvl4VarGroup(VarGroupEnum varGroup) {
        VarGroupWrapper varGroupWrapper = varGroupWrapperMap.get(varGroup);
        if (varGroupWrapper == null) {
            return -1;
        }

        return varGroupWrapper.getVarGroupInfo().getIntvl();
    }

    public EndTag getEndTag() {
        return endTag;
    }

    public List<YxTagVar> getYxVarList() {
        return yxVarList;
    }

    public List<YcTagVar> getYcVarList() {
        return ycVarList;
    }

    public List<YmTagVar> getYmVarList() {
        return ymVarList;
    }

    public List<TagVar> getQtVarList() {
        return qtVarList;
    }

    public List<TagVar> getYkVarList() {
        return ykVarList;
    }

    public List<TagVar> getYtVarList() {
        return ytVarList;
    }

    private YmTagVar createYmTagVar(List<VarIOInfo> ioInfoList, TagVarTplWrapper tpl) {
        YmTagVar tagVar = null;
        if (ioInfoList != null) {
            for (VarIOInfo ioInfo : ioInfoList) {
                if (ioInfo.getVarName().equals(tpl.getTagVarTpl().getVarName())) {
                    tagVar = new YmTagVar(this, tpl.getTagVarTpl(), ioInfo.getBaseValue(), ioInfo.getCoefValue());
                    break;
                }
            }
        }
        if (tagVar == null) {
            tagVar = new YmTagVar(this, tpl.getTagVarTpl());
        }
        return tagVar;
    }

    private YcTagVar createYcTagVar(List<VarIOInfo> ioInfoList, TagVarTplWrapper tpl) {
        YcTagVar tagVar = null;
        if (ioInfoList != null) {
            for (VarIOInfo ioInfo : ioInfoList) {
                if (ioInfo.getVarName().equals(tpl.getTagVarTpl().getVarName())) {
                    tagVar = new YcTagVar(this, tpl, ioInfo.getBaseValue(), ioInfo.getCoefValue());
                    break;
                }
            }
        }
        if (tagVar == null) {
            tagVar = new YcTagVar(this, tpl);
        }
        return tagVar;
    }

    /**
     * 更新实时数据
     */
    public void updateRealtimeData() {
        if (!realtimeDataMap.isEmpty()) {
            realtimeDataService.updateEndModel(endTag.getCode(), realtimeDataMap);
        }
        if (!realtimeYcArrayDataMap.isEmpty()) {
            realtimeDataService.updateEndModelYcArray(endTag.getCode(), realtimeYcArrayDataMap);
        }
    }

    /**
     * 保存历史数据
     */
    public void persistHistoryData() {
        if (!this.groupDataList.isEmpty()) {
            log.debug("{} - 保存分组历史数据共{}个", endTag.getName(), this.groupDataList.size());
            historyDataService.saveVarGroupData(this.groupDataList);
            this.groupDataList.clear();
        }
        if (!this.yxRecordList.isEmpty()) {
            historyDataService.saveYXData(this.yxRecordList);
            this.yxRecordList.clear();
        }
        if(!this.offLimitsRecordList.isEmpty()) {
            historyDataService.saveOffLimitsRecord(this.offLimitsRecordList);
            this.offLimitsRecordList.clear();
        }
        if (!this.faultRecordList.isEmpty()) {
            historyDataService.saveFaultRecord(this.faultRecordList);
            this.faultRecordList.clear();
        }
        this.historyGroupDataMap.clear();
    }

    /**
     * 增加或更新故障报警记录
     * @param record
     */
    public void addFaultRecord(FaultRecord record, boolean pushMessage) {
        faultRecordList.add(record);
        if (pushMessage) {
            if (record.getResumeTime() == null) {// 故障报警
                realtimeDataService.faultOccured(record);
            } else {// 解除故障报警
                realtimeDataService.faultResumed(record);
            }
        }
    }

    public void addOffLimitsRecord(OffLimitsRecord record, boolean pushMessage) {
        offLimitsRecordList.add(record);
        if (pushMessage) {
            if (record.getResumeTime() == null) {// 越限报警
                realtimeDataService.offLimitsOccured(record);
            } else {// 解除越限报警
                realtimeDataService.offLimitsResumed(record);
            }
        }
    }

    public void addYxData(YxRecord record, boolean pushMessage) {
        yxRecordList.add(record);
        if (pushMessage) {//推送消息
            realtimeDataService.yxChanged(record);
        }
    }

    public void generateVarGroupHisData(VarGroupEnum varGroup, Date datetime) {

        VarGroupWrapper wrapper = this.varGroupWrapperMap.get(varGroup);
        if (wrapper == null) {
            return;
        }

        int interval = wrapper.getVarGroupInfo().getIntvl();
        int minute = LocalDateTime.fromDateFields(datetime).getMinuteOfHour() / interval * interval;
        if (interval <= 0 || (wrapper.getLastMinute() != minute)) {
            log.debug("生成分组历史数据:{}-{}", endTag.getName(), varGroup.getValue());
            wrapper.setLastMinute(minute);
            VarGroupData data = new VarGroupData();

            for (YxTagVar var : wrapper.getYxVarList()) {// 遍历该节点下的所有变量，并进行处理
                if (var.getLastYxValue() != -1) {
                    data.getYxValueMap().put(var.getTpl().getVarName(), var.getLastYxValue() > 0);
                }
            }

            for (YcTagVar var : wrapper.getYcVarList()) {// 遍历该节点下的所有变量，并进行处理
                if (!Float.isNaN(var.getLastYcValue())) {
                    data.getYcValueMap().put( var.getTpl().getVarName(), var.getLastYcValue());
                } else if (var.getLastArrayValue() != null) {
                    // TODO: 示功图数据打包完成后加入实时数据更新列表
                    realtimeYcArrayDataMap.put(var.getTpl().getVarName(), StringUtils.join(Arrays.asList(var.getLastArrayValue()), ","));
                    data.getArrayValueMap().put( var.getTpl().getVarName(), var.getLastArrayValue());
                }
            }

            for (YmTagVar var : wrapper.getYmVarList()) {// 遍历该节点下的所有变量，并进行处理
                if (!Double.isNaN(var.getLastYmValue())) {
                    data.getYmValueMap().put( var.getTpl().getVarName(), var.getLastYmValue());
                }
            }

            if (!data.getYcValueMap().isEmpty()
                    || !data.getYmValueMap().isEmpty()
                    || !data.getArrayValueMap().isEmpty()
                    || !data.getYxValueMap().isEmpty()) {
                data.setCode(endTag.getCode());
                data.setGroup(varGroup);
                data.setDatetime(datetime);
                groupDataList.add(data);
                log.debug("加入分组存储队列：{}({})-{}", endTag.getName(), endTag.getCode(), varGroup);
            }
        }
    }

    public void addRTUYmHisData(TagVar var, String key, Date datetime, double value) {
        VarGroupData data = historyGroupDataMap.get(key);
        if (data == null) {
            data = new VarGroupData();
            data.setCode(endTag.getCode());
            data.setGroup(var.tpl.getVarGroup());
            data.setDatetime(datetime);
            historyGroupDataMap.put(key, data);
            groupDataList.add(data);
        }
        data.getYmValueMap().put(var.tpl.getVarName(), value);
    }

    public void addRTUYcHisData(TagVar var, String key, Date datetime, float value) {
        VarGroupData data = historyGroupDataMap.get(key);
        if (data == null) {
            data = new VarGroupData();
            data.setCode(endTag.getCode());
            data.setGroup(var.tpl.getVarGroup());
            data.setDatetime(datetime);
            historyGroupDataMap.put(key, data);
            groupDataList.add(data);
        }
        data.getYcValueMap().put(var.tpl.getVarName(), value);
    }

    public float[] addRTUYcArrayHisData(YcTagVar var, String key, Date datetime) {
        VarGroupData data = historyGroupDataMap.get(key);
        if (data == null) {
            data = new VarGroupData();
            data.setCode(endTag.getCode());
            data.setGroup(var.tpl.getVarGroup());
            data.setDatetime(datetime);
            historyGroupDataMap.put(key, data);
            groupDataList.add(data);
        }
        float[] v = data.getArrayValueMap().get(var.tpl.getVarName());
        if (v == null) {
            v = new float[var.getLastArrayValue().length];
            data.getArrayValueMap().put(var.tpl.getVarName(), v);
        }
        return v;
    }

    public void addRTUYxHisData(TagVar var, String key, Date datetime, boolean value) {
        VarGroupData data = historyGroupDataMap.get(key);
        if (data == null) {
            data = new VarGroupData();
            data.setCode(endTag.getCode());
            data.setGroup(var.tpl.getVarGroup());
            data.setDatetime(datetime);
            historyGroupDataMap.put(key, data);
            groupDataList.add(data);
        }
        data.getYxValueMap().put(var.tpl.getVarName(), value);
    }
}