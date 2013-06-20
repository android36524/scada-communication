package com.ht.scada.communication.dao.impl;

import co.mewf.sqlwriter.Queries;
import com.ht.scada.communication.dao.YxRecordDao;
import com.ht.scada.communication.entity.YxRecord;

import java.sql.Timestamp;
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
        Object[][] params = new Object[records.size()][6];
        for (int i = 0; i < records.size(); i++) {
            YxRecord record = records.get(i);
            params[i] = new Object[]{record.getId(), record.getCode(), record.getName(),
                        record.getInfo(), record.getValue() ? 1 : 0, new Timestamp(record.getDatetime().getTime())};
        }
        getDbUtilsTemplate().batchUpdate(insertSql, params);
    }

    @Override
    public List<YxRecord> getAll() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
