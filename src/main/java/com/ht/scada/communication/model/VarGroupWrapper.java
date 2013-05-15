package com.ht.scada.communication.model;

import com.ht.scada.common.tag.entity.VarGroupCfg;

public class VarGroupWrapper {
	public final VarGroupCfg cfg;
	public int lastMinute = -1;
	
	public VarGroupWrapper(VarGroupCfg cfg) {
		this.cfg = cfg;
	}
	
}