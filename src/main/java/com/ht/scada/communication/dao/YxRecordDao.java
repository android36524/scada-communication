package com.ht.scada.communication.dao;

import com.ht.scada.communication.entity.YxRecord;

import java.util.Date;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * 作者: "薄成文"
 * 日期: 13-5-18 上午9:10
 * To change this template use File | Settings | File Templates. */
public interface YxRecordDao extends BaseDao<YxRecord> {
    void insertAll(List<YxRecord> records);
    long getCount(String code, Date start, Date end);
    public List<YxRecord> findByDateTime(String code, Date start, Date end, int skip, int limit);
}
