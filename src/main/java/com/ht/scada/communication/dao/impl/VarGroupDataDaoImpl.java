package com.ht.scada.communication.dao.impl;

import com.google.common.base.Joiner;
import com.ht.db.Database;
import com.ht.db.util.DbUtilsTemplate;
import com.ht.scada.common.tag.util.VarGroupEnum;
import com.ht.scada.communication.Config;
import com.ht.scada.communication.dao.VarGroupDataDao;
import com.ht.scada.communication.entity.VarGroupData;
import org.apache.commons.lang.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 *
 * @author: 薄成文 13-6-27 上午11:11
 * To change this template use File | Settings | File Templates.
 */
public class VarGroupDataDaoImpl extends BaseDaoImpl<VarGroupData> implements VarGroupDataDao {
    public static final Logger log = LoggerFactory.getLogger(VarGroupDataDaoImpl.class);

    public VarGroupDataDaoImpl(DbUtilsTemplate dbUtilsTemplate) {
        super(dbUtilsTemplate);
    }

    @Override
    public void insert(VarGroupData varGroupData) {
        //To change body of implemented methods use File | Settings | File Templates.
        String sql = createSqlString(varGroupData);
        log.debug("插入VarGroupData数据:{}", sql);
        getDbUtilsTemplate().update(sql, varGroupData.getCode(), varGroupData.getDatetime());
    }

    @Override
    public List<Map<String, Object>> findByCodeAndDatetime(String code, VarGroupEnum varGroup, Date start, Date end, int skip, int limit) {
        StringBuilder sqlBuilder = new StringBuilder();
        String tableName = "T_" + varGroup.toString();
        Database database = Config.INSTANCE.getDatabase();
        switch (database) {
            case SQL_SERVER:
                sqlBuilder.append("select t2.n, t1.* from ")
                        .append(tableName).append(" t1, ")
                        .append("(select top ")
                        .append(limit + skip)
                        .append(" row_number() over (order by datetime asc) n, id from ")
                        .append(tableName)
                        .append(" where code=? and datetime>=? and datetime<?) t2 where t1.id=t2.id and t2.n >")
                        .append(skip)
                        .append(" order by t2.n asc");
                break;
            case MYSQL:
                sqlBuilder.append("select * from ")
                        .append(tableName)
                        .append(" where code=? and datetime>=? and datetime<?")
                        .append(" order by datetime asc ")
                        .append("limit ").append(skip).append(",").append(limit);
                break;
            case ORACLE:
            case POSTGRESQL:
                sqlBuilder.append("select * from ")
                        .append("(select ROWNUM r, t1.* from ").append(tableName)
                        .append("t1 where code=? and datetime>=? and datetime<? and ROWNUM<")
                        .append(skip + limit)
                        .append(" order by datetime asc ")
                        .append(") t2")
                        .append("where t2.r>=")
                        .append(skip)
                        .append("order by t2.r asc");
                break;
        }
        List<Map<String, Object>> list = getDbUtilsTemplate().find(sqlBuilder.toString(), code, new Timestamp(start.getTime()), new Timestamp(end.getTime()));
        return list;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public long getCount(String code, VarGroupEnum varGroup, Date start, Date end) {
        String sql = "select count(id) from T_" + varGroup.toString()  + " where code=? and datetime>? and datetime<?";
        Long count = getDbUtilsTemplate().findBy(sql, null, code, new Timestamp(start.getTime()), new Timestamp(end.getTime()));
        return count == null ? 0 : count;
    }

    private String createSqlString(VarGroupData varGroupData) {
        StringBuilder sqlBuilder = new StringBuilder();
        sqlBuilder.append("insert into T_").append(varGroupData.getGroup().toString()).append(" (code,datetime,");
        //sqlBuilder.append("set ");
        List<String> keys = new ArrayList<>();
        List<String> values = new ArrayList<>();
        List<String> params  = new ArrayList<>();
        for (Map.Entry<String, Boolean> entry : varGroupData.getYxValueMap().entrySet()) {
            keys.add(entry.getKey());
            params.add(entry.getValue() ? "1" : "0");
            //params.add("?");
        }
        for (Map.Entry<String, Float> entry : varGroupData.getYcValueMap().entrySet()) {
            keys.add(entry.getKey());
            params.add(entry.getValue() + "");
            //params.add("?");
        }
        for (Map.Entry<String, Double> entry : varGroupData.getYmValueMap().entrySet()) {
            keys.add(entry.getKey());
            params.add(entry.getValue() + "");
            //params.add("?");
        }
        for (Map.Entry<String, float[]> entry : varGroupData.getArrayValueMap().entrySet()) {
            keys.add(entry.getKey());
            params.add("'" + Joiner.on(",").join(ArrayUtils.toObject(entry.getValue())) + "'");
            //params.add("?");
        }
        Joiner.on(",").appendTo(sqlBuilder, keys);
        sqlBuilder.append(") values (?,?,");
        //sqlBuilder.append(varGroupData.getDatetime());
        Joiner.on(",").appendTo(sqlBuilder, params);
        sqlBuilder.append(")");

        return sqlBuilder.toString();
    }

    @Override
    public List<VarGroupData> getAll() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
