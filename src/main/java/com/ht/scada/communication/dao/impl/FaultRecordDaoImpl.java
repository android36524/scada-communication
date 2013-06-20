package com.ht.scada.communication.dao.impl;

import co.mewf.sqlwriter.Queries;
import com.ht.db.util.DbUtilsTemplate;
import com.ht.scada.communication.dao.FaultRecordDao;
import com.ht.scada.communication.entity.FaultRecord;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * 作者: 薄成文 13-5-20 下午9:37
 * To change this template use File | Settings | File Templates.
 */
public class FaultRecordDaoImpl extends BaseDaoImpl<FaultRecord> implements FaultRecordDao {
    private String insertSql;
    private String updateSql;

    public FaultRecordDaoImpl() {
        this(null);
    }

    public FaultRecordDaoImpl(DbUtilsTemplate dbUtilsTemplate) {
        super(dbUtilsTemplate);
        // INSERT INTO T_Fault_Record(id, code, name, info, value, action_time, resume_time) VALUES(?, ?, ?, ?, ?, ?, ?)
        insertSql = Queries.insert(FaultRecord.class).sql();
        // UPDATE T_Fault_Record SET T_Fault_Record.resume_time = ? WHERE T_Fault_Record.id = ?
        updateSql = Queries.update(FaultRecord.class).set("resumeTime").where().eq("id").sql();
    }

    @Override
    public void insertOrUpdateAll(List<FaultRecord> records) {
        List<FaultRecord> insertList = new ArrayList<>();
        List<FaultRecord> updateList = new ArrayList<>();
        for (FaultRecord record : records) {
            if (record.isPersisted()) {
                updateList.add(record);
            } else {
                record.setPersisted(true);
                insertList.add(record);
            }
        }
        if (!insertList.isEmpty()) {
            Object[][] params = new Object[insertList.size()][7];
            for (int i = 0; i < params.length; i++) {
                FaultRecord record = insertList.get(i);
                params[i] = new Object[]{record.getId(), record.getCode(), record.getName(),
                        record.getInfo(), record.getValue() ? 1 : 0, record.getActionTime(), record.getResumeTime()};
            }
            getDbUtilsTemplate().batchUpdate(insertSql, params);
        }

        if (!updateList.isEmpty()) {
            Object[][] params = new Object[updateList.size()][2];
            for (int i = 0; i < params.length; i++) {
                FaultRecord record = insertList.get(i);
                params[i] = new Object[]{record.getResumeTime(), record.getId()};
            }
            getDbUtilsTemplate().batchUpdate(updateSql, params);
        }
    }

    @Override
    public List<FaultRecord> getAll() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
