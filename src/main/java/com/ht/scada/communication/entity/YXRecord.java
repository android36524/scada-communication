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
@Table(name="T_YX_Record")
public class YxRecord {

    @Id
	private String id;	// 唯一主键
	private String code;// 计量点编号(回路号、井号等)
	private String name;// 变量名称
	private String info;
	private Boolean value;
    @Temporal(TemporalType.TIMESTAMP)
	private Date datetime;

    @Transient
    private boolean persisted;// 是否已经写入数据库

	public YxRecord() {
        this.id = UUID.randomUUID().toString().replace("-", "");
	}

	public YxRecord(String code, String name, String info, boolean value, Date datetime) {
        this.id = UUID.randomUUID().toString().replace("-", "");
		this.code = code;
		this.name = name;
		this.info = info;
		this.value = value;
		this.datetime = datetime;
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

	public Date getDatetime() {
		return datetime;
	}

	public void setDatetime(Date datetime) {
		this.datetime = datetime;
	}

}
