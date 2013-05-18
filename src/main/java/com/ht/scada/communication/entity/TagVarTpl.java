package com.ht.scada.communication.entity;

import com.ht.scada.communication.util.DataType;
import com.ht.scada.communication.util.VarGroup;
import com.ht.scada.communication.util.VarSubType;
import com.ht.scada.communication.util.VarType;

/**
 * 变量词典模板<br>
 * <p>
 * 大部分的监控末端都具有相同的变量类型，所有采用通用的变量词典模板来进行定义，开端节点只需要指定模板名称即可
 * </p>
 * 
 * @author 薄成文
 * @author 赵磊
 * 
 */
public class TagVarTpl {

    private int id;
	private String tplName;// 模板名称

	/* 变量属性 */
	private String varName; // 变量key,用于程序脚本
	private String tagName;	//变量名，中文
	private VarGroup varGroup; // 变量分组,可以为空
	private VarType varType; // 变量类型
	private VarSubType subType; // 变量子类型
	

	/* IO信息 */
	private int funCode = -1; // 功能码
	private int dataId = -1; // 数据ID
	private int byteOffset = 0; // 字节偏移量
	private int bitOffset = -1; // 位偏移量(-1表)
	private int byteLen = -1; // 字节长度
	private DataType dataType; // 值类型（bool, int32, int16, bcd, mod10000, float,
								// double）
	private Float baseValue;// 基值
	private Float coefValue;// 系数

	/* YM信息 */
	private Double maxValue;// 最大值
	private Double minValue;// 最小值
	private Integer unitValue;// 遥脉单位

	/* 变量扩展属性 */
	private String triggerName; // 触发采集帧名,如"soe", 需要与采集通道中定义的帧名称对应

	/**
	 * 存储器
	 */
	private String varStorage = "ym|0|599999999|1|0";

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTplName() {
        return tplName;
    }

    public void setTplName(String tplName) {
        this.tplName = tplName;
    }

    public String getVarName() {
        return varName;
    }

    public void setVarName(String varName) {
        this.varName = varName;
    }

    public String getTagName() {
        return tagName;
    }

    public void setTagName(String tagName) {
        this.tagName = tagName;
    }

    public VarGroup getVarGroup() {
        return varGroup;
    }

    public void setVarGroup(String varGroup) {
        this.varGroup = VarGroup.valueOf(varGroup);
    }

    public VarType getVarType() {
        return varType;
    }

    public void setVarType(String varType) {
        this.varType = VarType.valueOf(varType);
    }

    public VarSubType getSubType() {
        return subType;
    }

    public void setSubType(String subType) {
        this.subType = VarSubType.valueOf(subType);
    }

    public int getFunCode() {
        return funCode;
    }

    public void setFunCode(int funCode) {
        this.funCode = funCode;
    }

    public int getDataId() {
        return dataId;
    }

    public void setDataId(int dataId) {
        this.dataId = dataId;
    }

    public int getByteOffset() {
        return byteOffset;
    }

    public void setByteOffset(int byteOffset) {
        this.byteOffset = byteOffset;
    }

    public int getBitOffset() {
        return bitOffset;
    }

    public void setBitOffset(int bitOffset) {
        this.bitOffset = bitOffset;
    }

    public int getByteLen() {
        return byteLen;
    }

    public void setByteLen(int byteLen) {
        this.byteLen = byteLen;
    }

    public DataType getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = DataType.valueOf(dataType);
    }

    public Float getBaseValue() {
        return baseValue;
    }

    public void setBaseValue(Float baseValue) {
        this.baseValue = baseValue;
    }

    public Float getCoefValue() {
        return coefValue;
    }

    public void setCoefValue(Float coefValue) {
        this.coefValue = coefValue;
    }

    public Double getMaxValue() {
        return maxValue;
    }

    public void setMaxValue(Double maxValue) {
        this.maxValue = maxValue;
    }

    public Double getMinValue() {
        return minValue;
    }

    public void setMinValue(Double minValue) {
        this.minValue = minValue;
    }

    public Integer getUnitValue() {
        return unitValue;
    }

    public void setUnitValue(Integer unitValue) {
        this.unitValue = unitValue;
    }

    public String getTriggerName() {
        return triggerName;
    }

    public void setTriggerName(String triggerName) {
        this.triggerName = triggerName;
    }

    public String getVarStorage() {
        return varStorage;
    }

    public void setVarStorage(String varStorage) {
        this.varStorage = varStorage;
    }
}
