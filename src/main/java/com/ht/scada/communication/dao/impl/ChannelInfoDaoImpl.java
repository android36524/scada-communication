package com.ht.scada.communication.dao.impl;

import com.ht.scada.communication.dao.ChannelInfoDao;
import com.ht.scada.communication.entity.ChannelInfo;

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
        return getDbUtilsTemplate().find(ChannelInfo.class, "SELECT * from T_Acquisition_Channel WHERE idx IS NOT NULL");
    }
}
