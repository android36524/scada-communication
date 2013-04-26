package com.ht.scada.communication.model;

import com.ht.scada.common.data.FaultRecord;
import com.ht.scada.common.data.OffLimitsRecord;
import com.ht.scada.common.data.VarGroupData;
import com.ht.scada.common.data.YXData;
import com.ht.scada.common.tag.entity.VarIOInfo;
import com.ht.scada.communication.util.VarGroupWrapper;
import com.ht.scada.communication.util.VarTplInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EndTagModel {
	public final int deviceAddr;
	public final int endTagID;
	public final String endTagCode;// 末端编号
	public final String endTagType;// 末端类型
	public final String tplName;
	
	// TODO:更新分组配置包装器
	public final List<VarGroupWrapper> varGroupWrappers = new ArrayList<>();
	
	public final Map<String, VarGroupData> historyGroupDataMap = new HashMap<>();
	
	/** KV存储暂存队列 **/
	public final List<VarGroupData> groupDataList = new ArrayList<>();
	public final List<YXData> yxDataList = new ArrayList<>();
	public final List<FaultRecord> faultRecordList = new ArrayList<>();
	public final List<OffLimitsRecord> offLimitsRecordList = new ArrayList<>();
	
	public final List<TagVar> varList;
	
	public EndTagModel(int deviceAddr, int endTagID, String endTagCode, String endTagType, String tplName, List<VarTplInfo> tplVarList, List<VarIOInfo> ioInfoList) {
		this.deviceAddr = deviceAddr;
		this.endTagID = endTagID;
		this.endTagCode = endTagCode;
		this.endTagType = endTagType;
		this.tplName = tplName;
		
		this.varList = new ArrayList<>(tplVarList.size());
		for (VarTplInfo tpl : tplVarList) {
			TagVar tagVar = new TagVar(tpl);
			if (ioInfoList != null) {
				for (VarIOInfo ioInfo : ioInfoList) {
					if (ioInfo.getVarName().equals(tpl.getTagTpl().getVarName())) {
						tagVar.baseValue = ioInfo.getBaseValue();
						tagVar.coefValue = ioInfo.getCoefValue();
						break;
					}
				}
			}
			
//			list.add(tagVar);
			varList.add(tagVar);
		}
	}

}