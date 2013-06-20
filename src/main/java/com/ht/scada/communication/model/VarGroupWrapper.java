package com.ht.scada.communication.model;


import com.ht.scada.communication.entity.VarGroupInfo;

import java.util.ArrayList;
import java.util.List;

public class VarGroupWrapper {
	private final VarGroupInfo varGroupInfo;
	private int lastMinute = -1;

    /**
     * TODO: 包括数组变量, 此种做法可能会造成错误的操作, 暂时没有更好的办法
     */
    private final List<YcTagVar> ycVarList = new ArrayList<>();
    private final List<YmTagVar> ymVarList = new ArrayList<>();
    private final List<YxTagVar> yxVarList = new ArrayList<>();

	public VarGroupWrapper(VarGroupInfo varGroupInfo) {
		this.varGroupInfo = varGroupInfo;
	}

    public VarGroupInfo getVarGroupInfo() {
        return varGroupInfo;
    }

    public int getLastMinute() {
        return lastMinute;
    }

    public void setLastMinute(int lastMinute) {
        this.lastMinute = lastMinute;
    }

    public List<YcTagVar> getYcVarList() {
        return ycVarList;
    }

    public List<YmTagVar> getYmVarList() {
        return ymVarList;
    }

    public List<YxTagVar> getYxVarList() {
        return yxVarList;
    }
}