package com.ht.scada.communication.dao.impl;

import com.alibaba.druid.pool.DruidDataSource;
import com.ht.db.util.HumpMatcher;
import com.ht.db.util.MyBeanProcessor;
import com.ht.scada.communication.dao.BaseDao;
import org.apache.commons.dbutils.BasicRowProcessor;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;

import java.sql.SQLException;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * 作者: "薄成文"
 * 日期: 13-5-18 上午10:40
 * To change this template use File | Settings | File Templates.
 */
public abstract class BaseDaoImpl<T> implements BaseDao<T> {
    private static DruidDataSource dataSource;

    static {
        dataSource = new DruidDataSource();
        dataSource.setMaxWait(60000);
        dataSource.setMaxActive(20);
        dataSource.setMinIdle(1);
        dataSource.setInitialSize(1);
        dataSource.setUrl("jdbc:mysql://localhost/scada?useUnicode=true&characterEncoding=utf-8");
        dataSource.setUsername("root");
        dataSource.setPassword("root");

        try {
            dataSource.setFilters("stat,slf4j");
            dataSource.getConnection();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public QueryRunner getQueryRunner() {
        return new QueryRunner(dataSource);  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<T> query(Class<T> clazz, String sql, Object... params) throws SQLException {
        ResultSetHandler<List<T>> h = new BeanListHandler(clazz, new BasicRowProcessor(new MyBeanProcessor(new HumpMatcher())));
        return getQueryRunner().query(sql, h, params);
        //return getQueryRunner().query(sql, new BeanListHandler<T>(clazz), params);
    }
}
