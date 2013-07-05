package com.ht.scada.communication.service;

import com.ht.scada.common.tag.util.VarGroupEnum;
import com.ht.scada.communication.entity.FaultRecord;
import com.ht.scada.communication.entity.OffLimitsRecord;
import com.ht.scada.communication.entity.VarGroupData;
import com.ht.scada.communication.entity.YxRecord;

import java.util.Date;
import java.util.List;

public interface HistoryDataService {
	
	public void saveYXData(List<YxRecord> list);
	public void saveOffLimitsRecord(List<OffLimitsRecord> list);
	public void saveFaultRecord(List<FaultRecord> list);
	
	public void saveVarGroupData(List<VarGroupData> list);

    public List<VarGroupData> getVarGroupDataByDatetimeRange(String code, VarGroupEnum varGroup, Date start, Date end);

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

    /**
     * 查询该时间点最新的一条数据记录
     * @param code
     * @param varGroup
     * @param start
     * @return 大于起始时间的第一条数据记录, 返回结果可以为空
     */
    VarGroupData getVarGroupData(String code, VarGroupEnum varGroup, Date start);
}
