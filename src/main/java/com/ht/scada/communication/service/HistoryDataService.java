package com.ht.scada.communication.service;

import com.ht.scada.common.tag.util.VarGroupEnum;
import com.ht.scada.communication.entity.FaultRecord;
import com.ht.scada.communication.entity.OffLimitsRecord;
import com.ht.scada.communication.entity.VarGroupData;
import com.ht.scada.communication.entity.YxRecord;

import java.util.Collection;
import java.util.Date;
import java.util.List;

public interface HistoryDataService {

    /**
     * 保存遥信变位记录
     * @param record
     */
    public void saveYXData(YxRecord record);

    /**
     * 保存遥测越限记录或更新遥测越限恢复时间
     * @param record
     */
    public void saveOrUpdateOffLimitsRecord(OffLimitsRecord record);

    /**
     * 保存故障报警记录或更新故障记录恢复时间
     * @param record
     */
    public void saveOrUpdateFaultRecord(FaultRecord record);

	public void saveVarGroupData(Collection<VarGroupData> list);
    public void saveVarGroupData(VarGroupData data);

    /**
     * 查询某监控对象在指定时间范围内的分组历史数据
     * @param code 监控对象编辑
     * @param varGroup 变量分组
     * @param start 起始时间
     * @param end 结束时间
     * @param skip 用于分页查询
     * @param limit 返回的最大数量用于分页查询
     * @return
     */
    List<VarGroupData> getVarGroupData(String code, VarGroupEnum varGroup, Date start, Date end, int skip, int limit);

    /**
     * 查询某监控对象在指定时间范围内的记录总数，用于分页查询
     * @param code
     * @param varGroup
     * @param start
     * @param end
     * @return
     */
    long getVarGroupDataCount(String code, VarGroupEnum varGroup, Date start, Date end);

    long getYxRecordCount(String code, Date start, Date end);
    long getFaultRecordCount(String code, Date start, Date end);
    long getOfflimitsRecordCount(String code, Date start, Date end);

    List<YxRecord> getYxRecordByDatetime(String code, Date start, Date end, int skip, int limit);
    List<FaultRecord> getFaultRecordByActionTime(String code, Date start, Date end, int skip, int limit);
    List<OffLimitsRecord> getOffLimitsRecordByActionTime(String code, Date start, Date end, int skip, int limit);

}
