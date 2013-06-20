package com.ht.scada.communication.dao.impl;

import com.ht.scada.communication.dao.VarIOInfoDao;
import com.ht.scada.communication.entity.VarIOInfo;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * 作者: "薄成文"
 * 日期: 13-5-18 下午12:55
 * To change this template use File | Settings | File Templates.
 */
public class VarIOInfoDaoImpl extends BaseDaoImpl<VarIOInfo> implements VarIOInfoDao {
    @Override
    public List<VarIOInfo> getAll() {
        return getDbUtilsTemplate().find(VarIOInfo.class, "SELECT * from T_Var_IO_Info");
    }
}
