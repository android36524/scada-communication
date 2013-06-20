package com.ht.scada.communication.dao.impl;

import com.ht.db.util.DbUtilsTemplate;
import com.ht.scada.communication.dao.BaseDao;

/**
 * Created with IntelliJ IDEA.
 * 作者: "薄成文"
 * 日期: 13-5-18 上午10:40
 * To change this template use File | Settings | File Templates.
 */
public abstract class BaseDaoImpl<T> implements BaseDao<T> {

    private DbUtilsTemplate dbUtilsTemplate;

    protected BaseDaoImpl() {
    }

    protected BaseDaoImpl(DbUtilsTemplate dbUtilsTemplate) {
        this.dbUtilsTemplate = dbUtilsTemplate;
    }

    @Override
    public void setDbUtilsTemplate(DbUtilsTemplate dbUtilsTemplate) {
        this.dbUtilsTemplate = dbUtilsTemplate;
    }

    @Override
    public DbUtilsTemplate getDbUtilsTemplate() {
        return dbUtilsTemplate;
    }
}
