package com.ht.scada.communication.dao.impl;

import com.ht.scada.communication.dao.VarGroupInfoDao;
import com.ht.scada.communication.entity.VarGroupInfo;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * 作者: "薄成文"
 * 日期: 13-5-18 下午12:52
 * To change this template use File | Settings | File Templates.
 */
public class VarGroupInfoDaoImpl extends BaseDaoImpl<VarGroupInfo> implements VarGroupInfoDao {
    @Override
    public List<VarGroupInfo> getAll() {
        return getDbUtilsTemplate().find(VarGroupInfo.class, "SELECT * from T_Var_Group_Cfg");
    }
}
