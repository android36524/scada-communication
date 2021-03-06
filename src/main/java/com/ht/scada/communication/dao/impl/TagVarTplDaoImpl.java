package com.ht.scada.communication.dao.impl;

import com.ht.scada.communication.dao.TagVarTplDao;
import com.ht.scada.communication.entity.TagVarTpl;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * 作者: "薄成文"
 * 日期: 13-5-18 下午12:41
 * To change this template use File | Settings | File Templates.
 */
public class TagVarTplDaoImpl extends BaseDaoImpl<TagVarTpl> implements TagVarTplDao {
    @Override
    public List<TagVarTpl> getAll() {
        return getDbUtilsTemplate().find(TagVarTpl.class, "select * from T_Tag_Cfg_Tpl");
    }
}
