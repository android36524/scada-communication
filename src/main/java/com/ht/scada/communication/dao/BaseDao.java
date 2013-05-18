package com.ht.scada.communication.dao;

import org.apache.commons.dbutils.QueryRunner;

import java.sql.SQLException;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * 作者: "薄成文"
 * 日期: 13-5-18 上午10:20
 * To change this template use File | Settings | File Templates.
 */
public interface BaseDao<T> {
    QueryRunner getQueryRunner();
    List<T> getAll();
    List<T> query(Class<T> clazz, String sql, Object ...params) throws SQLException;
}
