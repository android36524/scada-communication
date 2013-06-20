package com.ht.scada.communication.dao;

import com.ht.scada.communication.entity.FaultRecord;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * 作者: "薄成文"
 * 日期: 13-5-18 上午9:10
 * To change this template use File | Settings | File Templates.
 */
public interface FaultRecordDao extends BaseDao<FaultRecord> {
    void insertOrUpdateAll(List<FaultRecord> records);
}
