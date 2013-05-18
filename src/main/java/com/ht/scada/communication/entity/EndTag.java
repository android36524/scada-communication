package com.ht.scada.communication.entity;

/**
 * 单井、回路等末端节点
 * 
 * @author 薄成文
 * 
 */
public class EndTag {

    private int id;
    private int channelIdx;
    private int deviceAddr;

	private String name;

	/**
	 * 末端编号(油井编号/回路编号)
	 */
	private String code;

	/**
	 * 节点类型<br>
	 * 不同的节点类型有对应的扩展信息表<br>
	 * varType=="配电回路" TagExtPower.class<br>
	 * varType=="油井A/B/C/D"
	 * TagExtOilA.class,TagExtOilB.class,TagExtOilC.class,TagExtOilD.class<br>
	 * varType=="水井" TagExtWater.class<br>
	 */
	private String varType;
	
	/**
	 * 节点子类型
	 * 油井：电滚筒、高原机、油梁式、螺杆泵、电潜泵
	 */
	private String subType;

	/**
	 * 变量模版名称,主要用于数据采集程序<br>
	 * <p>
	 * 当该标签节点是一个末端时（如油井）需要指定末端对应的变量模版，
	 * 变量模版中定义了末端所涉及到的所有变量，大部分的末端都具有相同的变量，所以采用模版来定义。
	 * </p>
	 */
	private String tplName;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getChannelIdx() {
        return channelIdx;
    }

    public void setChannelIdx(int channelIdx) {
        this.channelIdx = channelIdx;
    }

    public int getDeviceAddr() {
        return deviceAddr;
    }

    public void setDeviceAddr(int deviceAddr) {
        this.deviceAddr = deviceAddr;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getVarType() {
        return varType;
    }

    public void setVarType(String varType) {
        this.varType = varType;
    }

    public String getSubType() {
        return subType;
    }

    public void setSubType(String subType) {
        this.subType = subType;
    }

    public String getTplName() {
        return tplName;
    }

    public void setTplName(String tplName) {
        this.tplName = tplName;
    }
}
