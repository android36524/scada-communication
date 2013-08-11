package com.ht.scada.communication.dao;

import com.ht.scada.common.tag.util.VarGroupEnum;
import com.ht.scada.communication.VarGroupTable;
import com.ht.scada.communication.entity.VarGroupData;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 *
 * @author: 薄成文 13-6-27 上午11:09
 * To change this template use File | Settings | File Templates.
 */
public interface VarGroupDataDao extends BaseDao<VarGroupData> {
    void insert(VarGroupData varGroupData);

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
    List<Map<String, Object>> findByCodeAndDatetime(String code, VarGroupEnum varGroup, Date start, Date end, int skip, int limit);

    void createGroupTableIfNotExists(VarGroupEnum varGroup, VarGroupTable varGroupTable);

    /**
     * 查询某监控对象在指定时间范围内的记录总数，用于分页查询
     * @param code
     * @param varGroup
     * @param start
     * @param end
     * @return
     */
    long getCount(String code, VarGroupEnum varGroup, Date start, Date end);

    List<VarGroupData> getVarGroupData(String code, VarGroupEnum varGroup, Date start, Date end, int skip, int limit);
}
