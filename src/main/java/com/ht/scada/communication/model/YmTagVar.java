package com.ht.scada.communication.model;

import java.util.Date;
import java.util.Map;

public class YmTagVar extends TagVar {

	public float baseValue = 0;// 基值
	public float coefValue = 1;// 系数
	public double maxValue = Double.NaN;
	public double minValue = Double.NaN;

	public double lastYmValue = Double.NaN;// 当前遥脉值

	public YmTagVar(EndTagWrapper endTag, VarTplInfo tplInfo) {
        super(endTag, tplInfo.tagTpl);
		
		baseValue = tplInfo.getTagTpl().getBaseValue();
		coefValue = tplInfo.getTagTpl().getCoefValue();
		
		if (tplInfo.getTagTpl().getMax() != null)
			maxValue = tplInfo.getTagTpl().getMax();
		if (tplInfo.getTagTpl().getMin() != null)
			minValue = tplInfo.getTagTpl().getMin();
		
	}

    public void update(double value, Date datetime, Map<String, String> realtimeDataMap) {
        // 实时数据更新
        if (Double.isNaN(this.lastYmValue) || this.lastYmValue != value) {
            this.lastYmValue = value;
            String key = endTagWrapper.endTag.getCode() + "/" + this.tpl.getVarName();
            realtimeDataMap.put(key, Double.toString(value));
        }

    }
}