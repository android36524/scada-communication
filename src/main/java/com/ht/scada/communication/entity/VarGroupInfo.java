package com.ht.scada.communication.entity;

import com.ht.scada.communication.util.VarGroup;

/**
 * 变量分组配置信息，包括分组名称，存储间隔
 * 
 * @author 薄成文
 * 
 */
public class VarGroupInfo {

    private int id;
	private VarGroup varGroup;
	private String name;// 分组名称
	
	private int intvl = 0;// 存储间隔

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public VarGroup getVarGroup() {
		return varGroup;
	}

	public void setVarGroup(String varGroup) {
		this.varGroup = VarGroup.valueOf(varGroup);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

    /**
     * 存储间隔(分钟)
     * @return
     */
	public int getIntvl() {
		return intvl;
	}

	public void setIntvl(int intvl) {
		this.intvl = intvl;
	}
	
}
