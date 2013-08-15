package com.ht.scada.communication.dao;

import com.ht.scada.communication.entity.OffLimitsRecord;

import java.util.Date;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * 作者: "薄成文"
 * 日期: 13-5-18 上午9:10
 * To change this template use File | Settings | File Templates.
 */
public interface OffLimitsRecordDao extends BaseDao<OffLimitsRecord> {

    void updateAll(List<OffLimitsRecord> offLimitsUpdateList);

    void insertAll(List<OffLimitsRecord> offLimitsInsertList);

    long getCount(String code, Date start, Date end);
    public List<OffLimitsRecord> findByActionTime(String code, Date start, Date end, int skip, int limit);
}
