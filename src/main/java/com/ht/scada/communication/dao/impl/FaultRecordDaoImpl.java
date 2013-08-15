package com.ht.scada.communication.dao.impl;

import com.ht.db.util.DbUtilsTemplate;
import com.ht.scada.communication.dao.FaultRecordDao;
import com.ht.scada.communication.entity.FaultRecord;

import java.sql.Timestamp;
import java.util.Date;
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
        insertSql = "INSERT INTO T_Fault_Record(id, end_id, end_name, tag_name, code, name, info, value, action_time, resume_time) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        // INSERT INTO T_Fault_Record(id, endId, endName, tagName, code, name, info, value, action_time, resume_time) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        //insertSql = Queries.insert(FaultRecord.class).sql();
        // UPDATE T_Fault_Record SET T_Fault_Record.resume_time = ? WHERE T_Fault_Record.id = ?
        //updateSql = Queries.update(FaultRecord.class).set("resumeTime").where().eq("id").sql();
        updateSql = "UPDATE T_Fault_Record SET T_Fault_Record.resume_time = ? WHERE T_Fault_Record.id = ?";
    }

    @Override
    public void updateAll(List<FaultRecord> updateList) {
        Object[][] params = new Object[updateList.size()][2];
        for (int i = 0; i < params.length; i++) {
            FaultRecord record = updateList.get(i);
            params[i] = new Object[]{record.getResumeTime(), record.getId()};
        }
        getDbUtilsTemplate().batchUpdate(updateSql, params);
    }

    @Override
    public long getCount(String code, Date start, Date end) {
        String sql = "select count(id) from T_Fault_Record where code=? and action_time>=? and action_time<?";
        Long count = getDbUtilsTemplate().findBy(sql, null, code, new Timestamp(start.getTime()), new Timestamp(end.getTime()));
        return count == null ? 0 : count;
    }

    @Override
    public List<FaultRecord> findByActionTime(String code, Date start, Date end, int skip, int limit) {
        StringBuilder sqlBuilder = new StringBuilder();
        //sqlBuilder.append("select * from  where code=? and datetime>=? and datetime<? order by datetime asc limit ");
        sqlBuilder.append("select * from T_Fault_Record where code=? and action_time>=? and action_time<? order by action_time asc limit ");
        sqlBuilder.append(skip).append(",").append(limit);
        return getDbUtilsTemplate().find(FaultRecord.class, sqlBuilder.toString(), code,
                new Timestamp(start.getTime()), new Timestamp(end.getTime()));
    }

    @Override
    public void insertAll(List<FaultRecord> insertList) {
        Object[][] params = new Object[insertList.size()][7];
        for (int i = 0; i < params.length; i++) {
            FaultRecord record = insertList.get(i);
            record.setPersisted(true);
            params[i] = new Object[]{record.getId(), record.getEndId(), record.getEndName(), record.getTagName(),
                    record.getCode(), record.getName(), record.getInfo(),
                    record.getValue() ? 1 : 0, record.getActionTime(), record.getResumeTime()};
        }
        getDbUtilsTemplate().batchUpdate(insertSql, params);
    }

    @Override
    public List<FaultRecord> getAll() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
