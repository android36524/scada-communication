package com.ht.scada.communication.dao.impl;

import com.google.common.base.Joiner;
import com.ht.db.Database;
import com.ht.db.util.DbUtilsTemplate;
import com.ht.scada.common.tag.util.VarGroupEnum;
import com.ht.scada.communication.Config;
import com.ht.scada.communication.VarGroupTable;
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
    private static final String VAR_GROUP_TABLE_PREFIX = "T_Group_";

    public VarGroupDataDaoImpl(DbUtilsTemplate dbUtilsTemplate) {
        super(dbUtilsTemplate);
    }

    @Override
    public void insert(VarGroupData varGroupData) {
        String sql = generateInsertSqlString(varGroupData);
        log.debug("插入VarGroupData数据:{}", sql);
        getDbUtilsTemplate().update(sql, varGroupData.getCode(), varGroupData.getDatetime());
    }

    @Override
    public List<Map<String, Object>> findByCodeAndDatetime(String code, VarGroupEnum varGroup, Date start, Date end, int skip, int limit) {
        StringBuilder sqlBuilder = new StringBuilder();
        String tableName = VAR_GROUP_TABLE_PREFIX + varGroup.toString();
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
    public void createGroupTableIfNotExists(VarGroupEnum varGroup, VarGroupTable varGroupTable) {
        StringBuilder sqlBuilder = new StringBuilder();
        String tableName = VAR_GROUP_TABLE_PREFIX + varGroup.toString();
        sqlBuilder.append("CREATE TABLE IF NOT EXISTS `").append(tableName).append("` (");
        sqlBuilder.append("`id` INT(10) NOT NULL AUTO_INCREMENT, \n");

        for (String name : varGroupTable.getYcVarList()) {
            sqlBuilder.append("`").append(name).append("` FLOAT NULL,\n");
        }
        for (String name : varGroupTable.getYcArrayVarList()) {
            sqlBuilder.append("`").append(name).append("` TEXT NULL,\n");
        }
        for (String name : varGroupTable.getYxVarList()) {
            sqlBuilder.append("`").append(name).append("` TINYINT NULL,\n");
        }
        for (String name : varGroupTable.getYmVarList()) {
            sqlBuilder.append("`").append(name).append("` DOUBLE NULL,\n");
        }

//        sqlBuilder.append("`yc` FLOAT NULL,\n");
//        sqlBuilder.append("`ym` DOUBLE NULL,\n");
//        sqlBuilder.append("`yx` TINYINT NULL,\n");
//        sqlBuilder.append("`ycArray` TEXT NULL,\n");

//        sqlBuilder.append("`var_group` VARCHAR(50) NULL,\n");
        sqlBuilder.append("`code` VARCHAR(50) NULL,\n");
        sqlBuilder.append("`datetime` DATETIME NULL,\n");
        sqlBuilder.append("PRIMARY KEY (`id`)\n");
        sqlBuilder.append(")");

        String sql = sqlBuilder.toString();
        log.debug(sql);
        getDbUtilsTemplate().update(sql);
    }

    @Override
    public long getCount(String code, VarGroupEnum varGroup, Date start, Date end) {
        String tableName = VAR_GROUP_TABLE_PREFIX + varGroup.toString();
        String sql = "select count(id) from " + tableName + " where code=? and datetime>? and datetime<?";
        Long count = getDbUtilsTemplate().findBy(sql, null, code, new Timestamp(start.getTime()), new Timestamp(end.getTime()));
        return count == null ? 0 : count;
    }

    @Override
    public List<VarGroupData> getVarGroupData(String code, VarGroupEnum varGroup, Date start, Date end, int skip, int limit) {
        List<VarGroupData> list = new ArrayList<>();

        StringBuilder sqlBuilder = new StringBuilder();
        String tableName = VAR_GROUP_TABLE_PREFIX + varGroup.toString();
        sqlBuilder.append("select * from ")
                .append(tableName)
                .append(" where code=? and datetime>=? and datetime<?")
                .append(" order by datetime asc ")
                .append("limit ").append(skip).append(",").append(limit);

        List<Map<String, Object>> dataList = getDbUtilsTemplate().find(sqlBuilder.toString(), code,
                new Timestamp(start.getTime()), new Timestamp(end.getTime()));
        for (Map<String, Object> map : dataList) {
            VarGroupData data = new VarGroupData();
            data.setCode(code);
            data.setGroup(varGroup);

            Object obj = map.get("datetime");
            if (obj != null && obj instanceof Date) {
                data.setDatetime((Date) obj);
            }
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                obj = entry.getValue();
                if (obj != null && !entry.getKey().equals("code") && !entry.getKey().equals("var_group")) {
                    if (obj instanceof Float) {
                        data.getYcValueMap().put(entry.getKey(), (Float)obj);
                    } else if (obj instanceof Double) {
                        data.getYmValueMap().put(entry.getKey(), (Double)obj);
                    } else if (obj instanceof Integer) {
                        data.getYxValueMap().put(entry.getKey(), ((Integer)obj).intValue() == 1);
                    } else if (obj instanceof String) {
                        String[] array = ((String)obj).split(",");
                        float[] floatArray = new float[array.length];
                        for (int i = 0; i < floatArray.length; i++) {
                            floatArray[i] = Float.parseFloat(array[i]);
                        }
                        data.getArrayValueMap().put(entry.getKey(), floatArray);
                    }
                }
            }
            list.add(data);
        }

        return list;  //To change body of implemented methods use File | Settings | File Templates.
    }

    private String generateInsertSqlString(VarGroupData varGroupData) {
        StringBuilder sqlBuilder = new StringBuilder();
        String tableName = VAR_GROUP_TABLE_PREFIX + varGroupData.getGroup().toString();
        sqlBuilder.append("insert into ").append(tableName).append(" (code,datetime,");
        //sqlBuilder.append("set ");
        List<String> keys = new ArrayList<>();
        List<String> params  = new ArrayList<>();
        for (Map.Entry<String, Boolean> entry : varGroupData.getYxValueMap().entrySet()) {
            keys.add(entry.getKey());
            params.add(entry.getValue() ? "1" : "0");
        }
        for (Map.Entry<String, Float> entry : varGroupData.getYcValueMap().entrySet()) {
            keys.add(entry.getKey());
            params.add(entry.getValue() + "");
        }
        for (Map.Entry<String, Double> entry : varGroupData.getYmValueMap().entrySet()) {
            keys.add(entry.getKey());
            params.add(entry.getValue() + "");
        }
        for (Map.Entry<String, float[]> entry : varGroupData.getArrayValueMap().entrySet()) {
            keys.add(entry.getKey());
            params.add("'" + Joiner.on(",").join(ArrayUtils.toObject(entry.getValue())) + "'");
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
