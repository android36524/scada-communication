package com.ht.scada.communication.entity;

import javax.persistence.*;
import java.util.Date;
import java.util.UUID;

/**
 * 遥测越限记录
 * @author 薄成文
 *
 */
@Entity
@Table(name="T_OffLimits_Record")
public class OffLimitsRecord {

    /**
     * 如果设置了GeneratedValue, sqlwriter会作为是ID自增模式
     */
    //@GeneratedValue(strategy = GenerationType.AUTO)
    @Id
	private String id;	// 唯一主键

    @Column(name = "end_id")
    private int endId;
    @Column(name = "end_name")
    private String endName;
    @Column(name = "tag_name")
    private String tagName;// 中文名称
	private String code;// 计量点编号(回路号、井号等)
	private String name;// 变量名称
	private String info;// 报警信息
	private double value;// 动作值
	private double threshold;// 阈值
	private Boolean type;// 越限类型 true:越上限，false:越下限
    @Column(name = "action_time")
    @Temporal(TemporalType.TIMESTAMP)
	private Date actionTime;

    @Column(name = "resume_time", insertable = false)
    @Temporal(TemporalType.TIMESTAMP)
	private Date resumeTime;

    @Transient
    private boolean persisted;// 是否已经写入数据库

	public OffLimitsRecord() {
        this.id = UUID.randomUUID().toString().replace("-", "");
	}

	public OffLimitsRecord(int endId, String endName, String code, String name, String tagName, String info, double value,
			double threshold, boolean type, Date actionTime) {
        this.id = UUID.randomUUID().toString().replace("-", "");
        this.endId = endId;
        this.endName = endName;
        this.tagName = tagName;
		this.code = code;
		this.name = name;
		this.info = info;
		this.value = value;
		this.threshold = threshold;
		this.type = type;
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

    public int getEndId() {
        return endId;
    }

    public void setEndId(int endId) {
        this.endId = endId;
    }

    public String getEndName() {
        return endName;
    }

    public void setEndName(String endName) {
        this.endName = endName;
    }

    public String getTagName() {
        return tagName;
    }

    public void setTagName(String tagName) {
        this.tagName = tagName;
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

	public double getValue() {
		return value;
	}

	public void setValue(double value) {
		this.value = value;
	}

	public double getThreshold() {
		return threshold;
	}

	public void setThreshold(double threshold) {
		this.threshold = threshold;
	}

	public Boolean getType() {
		return type;
	}

	public void setType(Boolean type) {
		this.type = type;
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
