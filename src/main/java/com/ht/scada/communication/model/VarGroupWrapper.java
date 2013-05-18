package com.ht.scada.communication.model;


import com.ht.scada.communication.entity.VarGroupInfo;

public class VarGroupWrapper {
	private final VarGroupInfo varGroupInfo;
	private int lastMinute = -1;
	
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
}