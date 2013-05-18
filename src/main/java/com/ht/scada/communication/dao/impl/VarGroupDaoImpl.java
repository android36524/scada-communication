package com.ht.scada.communication.dao.impl;

import com.ht.scada.communication.dao.VarGroupDao;
import com.ht.scada.communication.entity.VarGroupInfo;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * 作者: "薄成文"
 * 日期: 13-5-18 下午12:52
 * To change this template use File | Settings | File Templates.
 */
public class VarGroupDaoImpl extends BaseDaoImpl<VarGroupInfo> implements VarGroupDao {
    @Override
    public List<VarGroupInfo> getAll() {
        List<VarGroupInfo> list = null;
        try {
            list = query(VarGroupInfo.class, "SELECT * from T_Var_Group_Cfg");
        } catch (SQLException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            list = new ArrayList<VarGroupInfo>();
        }
        return list;
    }
}
