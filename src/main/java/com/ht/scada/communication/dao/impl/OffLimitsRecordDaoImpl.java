package com.ht.scada.communication.dao.impl;

import co.mewf.sqlwriter.Queries;
import com.ht.db.util.DbUtilsTemplate;
import com.ht.scada.communication.dao.OffLimitsRecordDao;
import com.ht.scada.communication.entity.OffLimitsRecord;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * 作者: "薄成文"
 * 日期: 13-5-20 下午4:43
 * To change this template use File | Settings | File Templates.
 */
public class OffLimitsRecordDaoImpl extends BaseDaoImpl<OffLimitsRecord> implements OffLimitsRecordDao {

    private String insertSql;
    private String updateSql;

    public OffLimitsRecordDaoImpl() {
        this(null);
    }

    public OffLimitsRecordDaoImpl(DbUtilsTemplate dbUtilsTemplate) {
        super(dbUtilsTemplate);
        // INSERT INTO T_OffLimits_Record(id, code, name, info, value, threshold, type, action_time, resume_time) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?)
        insertSql = Queries.insert(OffLimitsRecord.class).sql();
        // UPDATE T_OffLimits_Record SET T_OffLimits_Record.resume_time = ? WHERE T_OffLimits_Record.id = ?
        updateSql = Queries.update(OffLimitsRecord.class).set("resumeTime").where().eq("id").sql();
    }

    @Override
    public void insertOrUpdateAll(List<OffLimitsRecord> records) {
        List<OffLimitsRecord> insertList = new ArrayList<>();
        List<OffLimitsRecord> updateList = new ArrayList<>();
        for (OffLimitsRecord record : records) {
            if (record.isPersisted()) {
                updateList.add(record);
            } else {
                record.setPersisted(true);
                insertList.add(record);
            }
        }
        if (!insertList.isEmpty()) {
            Object[][] params = new Object[insertList.size()][9];
            for (int i = 0; i < params.length; i++) {
                OffLimitsRecord record = insertList.get(i);
                params[i] = new Object[]{
                        record.getId(), record.getCode(), record.getName(),
                        record.getInfo(), record.getValue(), record.getThreshold(),
                        record.getType() ? 1 : 0, record.getActionTime(), record.getResumeTime()
                };
            }
            getDbUtilsTemplate().batchUpdate(insertSql, params);
        }

        if (!updateList.isEmpty()) {
            Object[][] params = new Object[updateList.size()][2];
            for (int i = 0; i < params.length; i++) {
                OffLimitsRecord record = insertList.get(i);
                params[i] = new Object[]{record.getResumeTime(), record.getId()};
            }
            getDbUtilsTemplate().batchUpdate(updateSql, params);
        }
    }

    @Override
    public List<OffLimitsRecord> getAll() {
        return null;
    }
}
