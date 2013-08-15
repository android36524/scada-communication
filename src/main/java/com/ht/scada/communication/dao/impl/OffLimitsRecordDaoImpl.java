package com.ht.scada.communication.dao.impl;

import co.mewf.sqlwriter.Queries;
import com.ht.scada.communication.dao.OffLimitsRecordDao;
import com.ht.scada.communication.entity.OffLimitsRecord;

import java.sql.Timestamp;
import java.util.Date;
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
        // INSERT INTO T_OffLimits_Record(id, endId, endName, tagName, code, name, info, value, threshold, type, action_time, resume_time) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?)
        //insertSql = Queries.insert(OffLimitsRecord.class).sql();
        insertSql = "INSERT INTO T_OffLimits_Record(id, end_id, end_name, tag_name, code, name, info, value, action_time, resume_time) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        // UPDATE T_OffLimits_Record SET T_OffLimits_Record.resume_time = ? WHERE T_OffLimits_Record.id = ?
        updateSql = Queries.update(OffLimitsRecord.class).set("resumeTime").where().eq("id").sql();
    }

    @Override
    public void updateAll(List<OffLimitsRecord> list) {
        Object[][] params = new Object[list.size()][2];
        for (int i = 0; i < params.length; i++) {
            OffLimitsRecord record = list.get(i);
            params[i] = new Object[]{record.getResumeTime(), record.getId()};
        }
        getDbUtilsTemplate().batchUpdate(updateSql, params);
    }

    @Override
    public void insertAll(List<OffLimitsRecord> list) {
        Object[][] params = new Object[list.size()][9];
        for (int i = 0; i < params.length; i++) {
            OffLimitsRecord record = list.get(i);
            record.setPersisted(true);
            params[i] = new Object[]{
                    record.getId(), record.getEndId(), record.getEndName(), record.getTagName(),
                    record.getCode(), record.getName(),
                    record.getInfo(), record.getValue(), record.getThreshold(),
                    record.getType() ? 1 : 0, record.getActionTime(), record.getResumeTime()
            };
        }
        getDbUtilsTemplate().batchUpdate(insertSql, params);
    }

    @Override
    public long getCount(String code, Date start, Date end) {
        String sql = "select count(id) from T_OffLimits_Record where code=? and action_time>=? and action_time<?";
        Long count = getDbUtilsTemplate().findBy(sql, null, code, new Timestamp(start.getTime()), new Timestamp(end.getTime()));
        return count == null ? 0 : count;
    }

    @Override
    public List<OffLimitsRecord> findByActionTime(String code, Date start, Date end, int skip, int limit) {
        StringBuilder sqlBuilder = new StringBuilder();
        sqlBuilder.append("select * from T_OffLimits_Record where code=? and action_time>=? and action_time<? order by action_time asc limit ");
        sqlBuilder.append(skip).append(",").append(limit);
        return getDbUtilsTemplate().find(OffLimitsRecord.class, sqlBuilder.toString(), code,
                new Timestamp(start.getTime()), new Timestamp(end.getTime()));
    }

    @Override
    public List<OffLimitsRecord> getAll() {
        return null;
    }
}
