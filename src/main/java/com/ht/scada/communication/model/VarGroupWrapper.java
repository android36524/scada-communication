package com.ht.scada.communication.model;


import com.ht.scada.communication.entity.VarGroupInfo;

import java.util.ArrayList;
import java.util.List;

public class VarGroupWrapper {
	private final VarGroupInfo varGroupInfo;
	private int lastMinute = -1;

    private final List<YcTagVar> ycVarList = new ArrayList<>();
    private final List<YcTagVar> ycArrayVarList = new ArrayList<>();
    private final List<YmTagVar> ymVarList = new ArrayList<>();
    private final List<YxTagVar> yxVarList = new ArrayList<>();
    private final List<AsciiTagVar> asciiTagVarList = new ArrayList<>();

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

    public List<YcTagVar> getYcArrayVarList() {
        return ycArrayVarList;
    }

    public List<YmTagVar> getYmVarList() {
        return ymVarList;
    }

    public List<YxTagVar> getYxVarList() {
        return yxVarList;
    }

    public List<AsciiTagVar> getAsciiTagVarList() {
        return asciiTagVarList;
    }
}