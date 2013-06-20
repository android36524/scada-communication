package com.ht.scada.communication.dao;

import com.ht.db.util.DbUtilsTemplate;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * 作者: "薄成文"
 * 日期: 13-5-18 上午10:20
 * To change this template use File | Settings | File Templates.
 */
public interface BaseDao<T> {
    void setDbUtilsTemplate(DbUtilsTemplate dbUtilsTemplate);
    DbUtilsTemplate getDbUtilsTemplate();
    List<T> getAll();
}
