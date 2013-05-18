package com.ht.scada.communication.dao.impl;

import com.ht.scada.communication.dao.ChannelInfoDao;
import com.ht.scada.communication.entity.ChannelInfo;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * 作者: "薄成文"
 * 日期: 13-5-18 上午10:24
 * To change this template use File | Settings | File Templates.
 */
public class ChannelInfoDaoImpl extends BaseDaoImpl<ChannelInfo> implements ChannelInfoDao {
    @Override
    public List<ChannelInfo> getAll() {
        List<ChannelInfo> list = null;
        try {
            list = query(ChannelInfo.class, "SELECT * from T_Acquisition_Channel WHERE idx IS NOT NULL");
        } catch (SQLException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            list = new ArrayList<ChannelInfo>();
        }
        return list;
    }
}
