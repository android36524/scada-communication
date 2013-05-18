package com.ht.scada.communication.entity;

/**
 * 末端节点各变量的IO信息设定，如果没有相应记录则采用变量模板中的默认值
 * 
 * @author 薄成文
 * 
 */
public class VarIOInfo {

    private int id;

	private int endTagId;
	
	private String varName;
	
	private float baseValue = 0;// 基值
	
	private float coefValue = 1;// 系数

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getEndTagId() {
        return endTagId;
    }

    public void setEndTagId(int endTagId) {
        this.endTagId = endTagId;
    }

    public String getVarName() {
		return varName;
	}

	public void setVarName(String varName) {
		this.varName = varName;
	}

	public float getBaseValue() {
		return baseValue;
	}

	public void setBaseValue(float baseValue) {
		this.baseValue = baseValue;
	}

	public float getCoefValue() {
		return coefValue;
	}

	public void setCoefValue(float coefValue) {
		this.coefValue = coefValue;
	}

}
