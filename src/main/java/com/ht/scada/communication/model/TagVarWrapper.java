package com.ht.scada.communication.model;

import com.ht.scada.common.data.FaultRecord;
import com.ht.scada.common.data.OffLimitsRecord;
import com.ht.scada.common.data.YXData;
import com.ht.scada.common.tag.util.DataType;
import com.ht.scada.communication.util.VarTplInfo;

public class TagVarWrapper {
	//VarTplInfo tpl;
	public final VarTplInfo tplWrapper;
	
	public OffLimitsRecord lastOffLimitsRecord;
	public FaultRecord lastFaultRecord;
	public YXData lastYxRecord;
	
	public float baseValue = 0;// 基值
	public float coefValue = 1;// 系数
	public double maxValue = Double.NaN;
	public double minValue = Double.NaN;
	
	public float lastYcValue = Float.NaN;// 当前遥测遥脉值
	public double lastYmValue = Double.NaN;// 当前遥测遥脉值
	public int lastYxValue = -1;// 当前遥信值
	public float[] lastArrayValue = null;
	
	public TagVarWrapper(VarTplInfo tplInfo) {
		this.tplWrapper = tplInfo;
		
		baseValue = tplInfo.getTagTpl().getBaseValue();
		coefValue = tplInfo.getTagTpl().getCoefValue();
		
		if (tplInfo.getTagTpl().getMax() != null)
			maxValue = tplInfo.getTagTpl().getMax();
		if (tplInfo.getTagTpl().getMin() != null)
			minValue = tplInfo.getTagTpl().getMin();
		
		if (tplWrapper.getTagTpl().getDataType() == DataType.INT16_ARRAY) {//示功图数据数组
			lastArrayValue = new float[tplWrapper.tagTpl.getByteLen() / 2];
		}
	}
	
}