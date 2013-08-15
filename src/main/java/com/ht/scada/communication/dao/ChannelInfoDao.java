package com.ht.scada.communication.dao;

import com.google.inject.ImplementedBy;
import com.ht.scada.communication.dao.impl.ChannelInfoDaoImpl;
import com.ht.scada.communication.entity.ChannelInfo;

/**
 * Created with IntelliJ IDEA.
 * 作者: "薄成文"
 * 日期: 13-5-18 上午9:26
 * To change this template use File | Settings | File Templates.
 */
@ImplementedBy(ChannelInfoDaoImpl.class)
public interface ChannelInfoDao extends BaseDao<ChannelInfo> {
}
