package com.ht.scada.communication.dao.impl;

import com.ht.scada.communication.dao.VarIOInfoDao;
import com.ht.scada.communication.entity.VarIOInfo;

import java.sql.SQLException;
import java.util.ArrayList;
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
        List<VarIOInfo> list = null;
        try {
            list = query(VarIOInfo.class, "SELECT id, endTag as endTagId, varName, " +
                    "base_value as baseValue, coef_value as coefValue from T_Var_IO_Info");
        } catch (SQLException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            list = new ArrayList<VarIOInfo>();
        }
        return list;
    }
}
