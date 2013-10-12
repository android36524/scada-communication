package com.ht.scada.communication.dao.impl;

import co.mewf.sqlwriter.Queries;
import com.ht.scada.communication.dao.YxRecordDao;
import com.ht.scada.communication.entity.YxRecord;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 *
 * @author: 薄成文 13-5-22 下午3:40
 * To change this template use File | Settings | File Templates.
 */
public class YxRecordDaoImpl extends BaseDaoImpl<YxRecord> implements YxRecordDao {
    private String insertSql;

    public YxRecordDaoImpl() {
        insertSql = Queries.insert(YxRecord.class).sql();
    }

    @Override
    public void insertAll(List<YxRecord> records) {
        if (records.isEmpty()) {
            return;
        }
        final Object[][] params = new Object[records.size()][];
        for (int i = 0; i < records.size(); i++) {
            YxRecord record = records.get(i);
            params[i] = new Object[]{
                    record.getId(), record.getEndId(), record.getEndName(), record.getTagName(),
                    record.getCode(), record.getName(),
                        record.getInfo(), record.getValue() ? 1 : 0, new Timestamp(record.getDatetime().getTime())};
        }
        dbUtilsTemplate.getDbExecutorService().execute(new Runnable() {
            @Override
            public void run() {
                getDbUtilsTemplate().batchUpdate(insertSql, params);
            }
        });
    }

    @Override
    public long getCount(String code, Date start, Date end) {
        String sql = "select count(id) from T_YX_Record where code=? and datetime>=? and datetime<?";
        Long count = getDbUtilsTemplate().findBy(sql, null, code, new Timestamp(start.getTime()), new Timestamp(end.getTime()));
        return count == null ? 0 : count;
    }

    @Override
    public List<YxRecord> findByDateTime(String code, Date start, Date end, int skip, int limit) {
        StringBuilder sqlBuilder = new StringBuilder();
        sqlBuilder.append("select * from T_YX_Record where code=? and datetime>=? and datetime<? order by datetime asc limit ");
        sqlBuilder.append(skip).append(",").append(limit);
        return getDbUtilsTemplate().find(YxRecord.class, sqlBuilder.toString(), code,
                new Timestamp(start.getTime()), new Timestamp(end.getTime()));
    }

    @Override
    public List<YxRecord> getAll() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
