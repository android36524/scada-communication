package com.ht.scada.communication.model;

import com.ht.scada.communication.entity.VarGroupInfo;
import com.ht.scada.communication.util.DataType;
import com.ht.scada.communication.util.VarGroup;
import com.ht.scada.communication.entity.EndTag;
import com.ht.scada.communication.entity.VarIOInfo;
import com.ht.scada.communication.service.DataService;
import com.ht.scada.communication.data.kv.FaultRecord;
import com.ht.scada.communication.data.kv.OffLimitsRecord;
import com.ht.scada.communication.data.kv.VarGroupData;
import com.ht.scada.communication.data.kv.YXData;
import org.apache.commons.lang.StringUtils;
import org.joda.time.LocalDateTime;

import java.util.*;

public class EndTagWrapper {
	private final EndTag endTag;
	
	private final Map<VarGroup, VarGroupWrapper> varGroupWrapperMap;

	/** RTU上送的历史数据暂存队列 **/
	private final Map<String, VarGroupData> historyGroupDataMap = new HashMap<>();
	
	/** KV存储暂存队列 **/
	private final List<VarGroupData> groupDataList = new ArrayList<>();
	private final List<YXData> yxDataList = new ArrayList<>();
	private final List<FaultRecord> faultRecordList = new ArrayList<>();
	private final List<OffLimitsRecord> offLimitsRecordList = new ArrayList<>();
	
	//public final List<TagVar> varList;
    private final List<YxTagVar> yxVarList = new ArrayList<>();
    /**
     * TODO: 包括数组变量, 此种做法可能会造成错误的操作, 暂时没有更好的办法
     */
    private final List<YcTagVar> ycVarList = new ArrayList<>();
    private final List<YmTagVar> ymVarList = new ArrayList<>();
    private final List<TagVar> qtVarList = new ArrayList<>();
    private final List<TagVar> ykVarList = new ArrayList<>();
    private final List<TagVar> ytVarList = new ArrayList<>();

	public EndTagWrapper(EndTag endTag, List<VarGroupInfo> varGroupInfos, List<TagVarTplWrapper> tplVarList, List<VarIOInfo> ioInfoList) {
		this.endTag = endTag;
		
		varGroupWrapperMap = new HashMap<>(varGroupInfos.size());
		for (VarGroupInfo cfg : varGroupInfos) {
			VarGroupWrapper wrapper = new VarGroupWrapper(cfg);
			varGroupWrapperMap.put(cfg.getVarGroup(), wrapper);
		}
		
		//this.varList = new ArrayList<>(tplVarList.size());
		for (TagVarTplWrapper tplWrapper : tplVarList) {
			//varList.add(tagVar);
            switch (tplWrapper.getTagVarTpl().getVarType()) {
                case YX:
                    yxVarList.add(new YxTagVar(this, tplWrapper));
                    break;
                case YC:
                {
                    YcTagVar tagVar = createYcTagVar(ioInfoList, tplWrapper);
                    ycVarList.add(tagVar);
                }
                    break;
                case YM:
                {
                    YmTagVar tagVar = createYmTagVar(ioInfoList, tplWrapper);
                    ymVarList.add(tagVar);
                }
                    break;
                case YK:
                    ykVarList.add(new TagVar(this, tplWrapper.getTagVarTpl()));
                    break;
                case YT:
                    ytVarList.add(new TagVar(this, tplWrapper.getTagVarTpl()));
                    break;
                case QT:
                    if (tplWrapper.getTagVarTpl().getDataType() == DataType.INT16_ARRAY) {//示功图数据数组
                        YcTagVar tagVar = createYcTagVar(ioInfoList, tplWrapper);
                        ycVarList.add(tagVar);
                    }
                    qtVarList.add(new TagVar(this, tplWrapper.getTagVarTpl()));
                    break;
            }

		}
	}

    public Map<VarGroup, VarGroupWrapper> getVarGroupWrapperMap() {
        return varGroupWrapperMap;
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

    public void persistHistoryData(DataService dataService) {
        if (!this.groupDataList.isEmpty()) {
            dataService.saveVarGroupData(this.groupDataList);
            this.groupDataList.clear();
        }
        if (!this.yxDataList.isEmpty()) {
            dataService.saveYXData(this.yxDataList);
            this.yxDataList.clear();
        }
        if(!this.offLimitsRecordList.isEmpty()) {
            dataService.saveOffLimitsRecord(this.offLimitsRecordList);
            this.offLimitsRecordList.clear();
        }
        if (!this.faultRecordList.isEmpty()) {
            dataService.saveFaultRecord(this.faultRecordList);
            this.faultRecordList.clear();
        }
        this.historyGroupDataMap.clear();
    }

    public void addFaultRecord(FaultRecord record) {
        faultRecordList.add(record);
    }

    public void addOffLimitsRecord(OffLimitsRecord record) {
        offLimitsRecordList.add(record);
    }

    public void addYxData(YXData record) {
        yxDataList.add(record);
    }

    public void generateVarGroupHisData(VarGroup varGroup, Date datetime, Map<String, String> realtimeDataMap) {

        VarGroupWrapper wrapper = this.varGroupWrapperMap.get(varGroup);
        if (wrapper == null) {
            return;
        }

        int interval = wrapper.getVarGroupInfo().getIntvl();
        int minute = LocalDateTime.fromDateFields(datetime).getMinuteOfHour() / interval * interval;
        if (interval <= 0 || (wrapper.getLastMinute() != minute)) {
            wrapper.setLastMinute(minute);
            VarGroupData data = new VarGroupData();

            for (YxTagVar var : this.yxVarList) {// 遍历该节点下的所有变量，并进行处理
                if (var.tpl.getVarGroup() == varGroup && var.getLastYxValue() != -1) {
                    //realtimeDataMap.put(key, Boolean.toString(var.lastYxValue == 1));
                    data.getYxValueMap().put(var.tpl.getVarName(), var.getLastYxValue() > 0);
                }
            }

            for (YcTagVar var : this.ycVarList) {// 遍历该节点下的所有变量，并进行处理
                if (var.tpl.getVarGroup() == varGroup) {
                    if (!Float.isNaN(var.getLastYcValue())) {
                        data.getYcValueMap().put( var.tpl.getVarName(), var.getLastYcValue());
                    } else if (var.getLastArrayValue() != null) {
                        // TODO: 示功图数据打包完成后加入实时数据更新列表
                        realtimeDataMap.put(var.getRTKey(), StringUtils.join(Arrays.asList(var.getLastArrayValue()), ","));
                        data.getArrayValueMap().put( var.tpl.getVarName(), var.getLastArrayValue());
                    }
                }
            }

            for (YmTagVar var : this.ymVarList) {// 遍历该节点下的所有变量，并进行处理
                if (var.tpl.getVarGroup() == varGroup && !Double.isNaN(var.getLastYmValue())) {
                    data.getYmValueMap().put( var.tpl.getVarName(), var.getLastYmValue());
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