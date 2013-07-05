package com.ht.scada.communication.entity;

import javax.persistence.*;
import java.util.Date;
import java.util.UUID;

/**
 * 遥测数据记录
 * @author 薄成文
 *
 */
@Entity
@Table(name="T_Fault_Record")
public class FaultRecord {

    @Id
	private String id;	// 唯一主键
	private String code;// 计量点编号(回路号、井号等)
	private String name;// 变量名称
	private String info;// 故障信息
	private Boolean value;
    @Column(name = "action_time")
    @Temporal(TemporalType.TIMESTAMP)
	private Date actionTime;
    @Column(name = "resume_time")
    @Temporal(TemporalType.TIMESTAMP)
	private Date resumeTime;

    @Transient
    private boolean persisted;// 是否已经写入数据库

	public FaultRecord() {
        this.id = UUID.randomUUID().toString().replace("-", "");
	}
	
	public FaultRecord(String code, String name, String info, boolean value, Date actionTime) {
        this.id = UUID.randomUUID().toString().replace("-", "");
		this.code = code;
		this.name = name;
		this.info = info;
		this.value = value;
		this.actionTime = actionTime;
	}

    public boolean isPersisted() {
        return persisted;
    }

    public void setPersisted(boolean persisted) {
        this.persisted = persisted;
    }

    public String getId() {
		return id;
	}
	
	public void setId(String id) {
		this.id = id;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getInfo() {
		return info;
	}

	public void setInfo(String info) {
		this.info = info;
	}

	public Boolean getValue() {
		return value;
	}

	public void setValue(Boolean value) {
		this.value = value;
	}

	public Date getActionTime() {
		return actionTime;
	}

	public void setActionTime(Date actionTime) {
		this.actionTime = actionTime;
	}

	public Date getResumeTime() {
		return resumeTime;
	}

	public void setResumeTime(Date resumeTime) {
		this.resumeTime = resumeTime;
	}

}
