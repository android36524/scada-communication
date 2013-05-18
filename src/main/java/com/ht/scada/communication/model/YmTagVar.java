package com.ht.scada.communication.model;

import com.ht.scada.communication.entity.TagVarTpl;

import java.util.Date;
import java.util.Map;

public class YmTagVar extends TagVar {

	private float baseValue = 0;// 基值
	private float coefValue = 1;// 系数
	private double maxValue = Double.NaN;
	private double minValue = Double.NaN;

	private double lastYmValue = Double.NaN;// 当前遥脉值

	public YmTagVar(EndTagWrapper endTag, TagVarTpl tpl) {
        this(endTag, tpl, tpl.getBaseValue(), tpl.getCoefValue());

//		baseValue = tplInfo.getTagVarTpl().getBaseValue();
//		coefValue = tplInfo.getTagVarTpl().getCoefValue();
//
//		if (tplInfo.getTagVarTpl().getMaxValue() != null)
//			maxValue = tplInfo.getTagVarTpl().getMaxValue();
//		if (tplInfo.getTagVarTpl().getMinValue() != null)
//			minValue = tplInfo.getTagVarTpl().getMinValue();
	}

    public YmTagVar(EndTagWrapper endTagWrapper, TagVarTpl tpl, float baseValue, float coefValue) {
        super(endTagWrapper, tpl);
        this.baseValue = baseValue;
        this.coefValue = coefValue;
        if (tpl.getMaxValue() != null)
            maxValue = tpl.getMaxValue();
        if (tpl.getMinValue() != null)
            minValue = tpl.getMinValue();
    }

    public double getLastYmValue() {
        return lastYmValue;
    }

    /**
     * 计算并更新变量值, 计算公式为：value * coefValue + baseValue
     * @param value
     * @param datetime
     * @param realtimeDataMap
     */
    public void update(double value, Date datetime, Map<String, String> realtimeDataMap) {
        // 实时数据更新
        if (Double.isNaN(value)) {
            return;
        }
        value = calcValue(value);
        if (Double.isNaN(this.lastYmValue) || this.lastYmValue != value) {
            this.lastYmValue = value;
            realtimeDataMap.put(getRTKey(), Double.toString(value));
        }

    }

    /**
     * 计算变量值, 计算公式为：value * coefValue + baseValue
     * @param value
     * @return
     */
    public double calcValue(double value) {
        return value * coefValue + baseValue;
    }
}