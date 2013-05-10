package com.ht.scada.communication.model;

import com.ht.scada.common.tag.entity.EndTag;
import com.ht.scada.common.tag.entity.VarGroupCfg;
import com.ht.scada.common.tag.entity.VarIOInfo;
import com.ht.scada.common.tag.util.DataType;
import com.ht.scada.common.tag.util.VarGroup;
import com.ht.scada.communication.service.DataService;
import com.ht.scada.data.kv.FaultRecord;
import com.ht.scada.data.kv.OffLimitsRecord;
import com.ht.scada.data.kv.VarGroupData;
import com.ht.scada.data.kv.YXData;
import org.apache.commons.lang.StringUtils;
import org.joda.time.LocalDateTime;

import java.util.*;

public class EndTagWrapper {
	public final EndTag endTag;
	
	public final Map<VarGroup, VarGroupWrapper> varGroupWrapperMap;

	/** RTU上送的历史数据暂存队列 **/
	private final Map<String, VarGroupData> historyGroupDataMap = new HashMap<>();
	
	/** KV存储暂存队列 **/
	private final List<VarGroupData> groupDataList = new ArrayList<>();
	private final List<YXData> yxDataList = new ArrayList<>();
	private final List<FaultRecord> faultRecordList = new ArrayList<>();
	private final List<OffLimitsRecord> offLimitsRecordList = new ArrayList<>();
	
	//public final List<TagVar> varList;
    public final List<YxTagVar> yxVarList = new ArrayList<>();
    /**
     * TODO: 包括数组变量, 此种做法可能会造成错误的操作, 暂时没有更好的办法
     */
    public final List<YcTagVar> ycVarList = new ArrayList<>();
    public final List<YmTagVar> ymVarList = new ArrayList<>();
    public final List<TagVar> qtVarList = new ArrayList<>();
    public final List<TagVar> ykVarList = new ArrayList<>();
    public final List<TagVar> ytVarList = new ArrayList<>();

	public EndTagWrapper(EndTag endTag, List<VarGroupCfg> varGroupCfgs, List<VarTplInfo> tplVarList, List<VarIOInfo> ioInfoList) {
		this.endTag = endTag;
		
		varGroupWrapperMap = new HashMap<>(varGroupCfgs.size());
		for (VarGroupCfg cfg : varGroupCfgs) {
			VarGroupWrapper wrapper = new VarGroupWrapper(cfg);
			varGroupWrapperMap.put(cfg.getVarGroup(), wrapper);
		}
		
		//this.varList = new ArrayList<>(tplVarList.size());
		for (VarTplInfo tpl : tplVarList) {
			//varList.add(tagVar);
            switch (tpl.tagTpl.getVarType()) {
                case YX:
                    yxVarList.add(new YxTagVar(this, tpl));
                    break;
                case YC:
                {
                    YcTagVar tagVar = createYcTagVar(ioInfoList, tpl);
                    ycVarList.add(tagVar);
                }
                    break;
                case YM:
                {
                    YmTagVar tagVar = createYmTagVar(ioInfoList, tpl);
                    ymVarList.add(tagVar);
                }
                    break;
                case YK:
                    ykVarList.add(new TagVar(this, tpl.tagTpl));
                    break;
                case YT:
                    ytVarList.add(new TagVar(this, tpl.tagTpl));
                    break;
                case QT:
                    if (tpl.tagTpl.getDataType() == DataType.INT16_ARRAY) {//示功图数据数组
                        YcTagVar tagVar = createYcTagVar(ioInfoList, tpl);
                        ycVarList.add(tagVar);
                    }
                    qtVarList.add(new TagVar(this, tpl.tagTpl));
                    break;
            }

		}
	}

    private YmTagVar createYmTagVar(List<VarIOInfo> ioInfoList, VarTplInfo tpl) {
        YmTagVar tagVar = new YmTagVar(this, tpl);
        if (ioInfoList != null) {
            for (VarIOInfo ioInfo : ioInfoList) {
                if (ioInfo.getVarName().equals(tpl.getTagTpl().getVarName())) {
                    tagVar.baseValue = ioInfo.getBaseValue();
                    tagVar.coefValue = ioInfo.getCoefValue();
                    break;
                }
            }
        }
        return tagVar;
    }

    private YcTagVar createYcTagVar(List<VarIOInfo> ioInfoList, VarTplInfo tpl) {
        YcTagVar tagVar = new YcTagVar(this, tpl);
        if (ioInfoList != null) {
            for (VarIOInfo ioInfo : ioInfoList) {
                if (ioInfo.getVarName().equals(tpl.getTagTpl().getVarName())) {
                    tagVar.baseValue = ioInfo.getBaseValue();
                    tagVar.coefValue = ioInfo.getCoefValue();
                    break;
                }
            }
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

        int interval = wrapper.cfg.getInterval();
        int minute = LocalDateTime.fromDateFields(datetime).getMinuteOfHour() / interval * interval;
        if (interval <= 0 || (wrapper.lastMinute != minute)) {
            wrapper.lastMinute = minute;
            VarGroupData data = new VarGroupData();

            for (YxTagVar var : this.yxVarList) {// 遍历该节点下的所有变量，并进行处理
                if (var.tpl.getVarGroup() == varGroup && var.lastYxValue != -1) {
                    //realtimeDataMap.put(key, Boolean.toString(var.lastYxValue == 1));
                    data.getYxValueMap().put(var.tpl.getVarName(), var.lastYxValue > 0);
                }
            }

            for (YcTagVar var : this.ycVarList) {// 遍历该节点下的所有变量，并进行处理
                if (var.tpl.getVarGroup() == varGroup) {
                    if (!Float.isNaN(var.lastYcValue)) {
                        data.getYcValueMap().put( var.tpl.getVarName(), var.lastYcValue);
                    } else if (var.lastArrayValue != null) {
                        // TODO: 示功图数据打包完成后加入实时数据更新列表
                        String key = this.endTag.getCode() + "/" + var.tpl.getVarName();
                        realtimeDataMap.put(key, StringUtils.join(Arrays.asList(var.lastArrayValue), ","));
                        data.getArrayValueMap().put( var.tpl.getVarName(), var.lastArrayValue);
                    }
                }
            }

            for (YmTagVar var : this.ymVarList) {// 遍历该节点下的所有变量，并进行处理
                if (var.tpl.getVarGroup() == varGroup && !Double.isNaN(var.lastYmValue)) {
                    data.getYmValueMap().put( var.tpl.getVarName(), var.lastYmValue);
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
            v = new float[var.lastArrayValue.length];
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