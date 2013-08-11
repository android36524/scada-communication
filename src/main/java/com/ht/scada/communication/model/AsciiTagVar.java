package com.ht.scada.communication.model;

import com.ht.scada.communication.entity.TagVarTpl;

import java.util.Date;

public class AsciiTagVar extends TagVar {

    private byte[] rawValue;
    private String lastValue = "";

    public AsciiTagVar(EndTagWrapper endTagWrapper, TagVarTpl tpl) {
        super(endTagWrapper, tpl);
        rawValue = new byte[tpl.getByteLen()];
    }

    public String getLastValue() {
        return lastValue;
    }

    /**
     * 计算并更新变量值, 计算公式为：value * coefValue + baseValue
     * @param datetime
     */
    public void update(Date datetime) {
        // 实时数据更新
        lastValue = new String(rawValue);
        endTagWrapper.getRealtimeDataMap().put(getTpl().getVarName(), lastValue);
    }

    public byte[] getRawValue() {
        return rawValue;
    }

    public void setRawValue(byte[] rawValue) {
        this.rawValue = rawValue;
    }
}