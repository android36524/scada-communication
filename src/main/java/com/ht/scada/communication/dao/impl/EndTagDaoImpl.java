package com.ht.scada.communication.dao.impl;

import com.ht.scada.communication.dao.EndTagDao;
import com.ht.scada.communication.entity.EndTag;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * 作者: "薄成文"
 * 日期: 13-5-18 上午11:58
 * To change this template use File | Settings | File Templates.
 */
public class EndTagDaoImpl extends BaseDaoImpl<EndTag> implements EndTagDao {
    @Override
    public List<EndTag> getAll() {
        return getDbUtilsTemplate().find(EndTag.class, "SELECT * from T_End_Tag WHERE channel_idx IS NOT NULL AND device_addr IS NOT NULL");
    }
}
