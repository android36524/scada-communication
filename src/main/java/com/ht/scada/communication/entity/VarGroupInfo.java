package com.ht.scada.communication.entity;

import com.ht.scada.common.tag.util.VarGroupEnum;

import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * 变量分组配置信息，包括分组名称，存储间隔
 * 
 * @author 薄成文
 * 
 */
@Entity
@Table(name="T_Var_Group_Cfg")
public class VarGroupInfo {

    private int id;
	private VarGroupEnum name;
    private String value;

	private int intvl = 0;// 存储间隔

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public VarGroupEnum getName() {
		return name;
	}

    public void setName(VarGroupEnum name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
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
