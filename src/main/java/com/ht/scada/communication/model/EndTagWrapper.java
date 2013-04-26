package com.ht.scada.communication.model;

import com.ht.scada.common.data.FaultRecord;
import com.ht.scada.common.data.OffLimitsRecord;
import com.ht.scada.common.data.VarGroupData;
import com.ht.scada.common.data.YXData;
import com.ht.scada.common.tag.entity.EndTag;
import com.ht.scada.common.tag.entity.VarGroupCfg;
import com.ht.scada.common.tag.entity.VarIOInfo;
import com.ht.scada.common.tag.util.VarGroup;
import com.ht.scada.communication.util.VarGroupWrapper;
import com.ht.scada.communication.util.VarTplInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EndTagWrapper {
	public final EndTag endTag;
	
	public final Map<VarGroup, VarGroupWrapper> varGroupWrapperMap;
//	public final List<VarGroupWrapper> varGroupWrappers;
	
	/** RTU上送的历史数据暂存队列 **/
	public final Map<String, VarGroupData> historyGroupDataMap = new HashMap<>();
	
	/** KV存储暂存队列 **/
	public final List<VarGroupData> groupDataList = new ArrayList<>();
	public final List<YXData> yxDataList = new ArrayList<>();
	public final List<FaultRecord> faultRecordList = new ArrayList<>();
	public final List<OffLimitsRecord> offLimitsRecordList = new ArrayList<>();
	
	public final List<TagVar> varList;
	
	public EndTagWrapper(EndTag endTag, List<VarGroupCfg> varGroupCfgs, List<VarTplInfo> tplVarList, List<VarIOInfo> ioInfoList) {
		this.endTag = endTag;
		
		varGroupWrapperMap = new HashMap<>(varGroupCfgs.size());
		for (VarGroupCfg cfg : varGroupCfgs) {
			VarGroupWrapper wrapper = new VarGroupWrapper(cfg);
			varGroupWrapperMap.put(cfg.getVarGroup(), wrapper);
		}
		
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