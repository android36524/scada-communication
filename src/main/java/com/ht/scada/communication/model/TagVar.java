package com.ht.scada.communication.model;

import com.ht.scada.common.tag.entity.TagCfgTpl;

/**
 * Created with IntelliJ IDEA.
 * 作者: "薄成文"
 * 日期: 13-5-8 下午12:33
 * To change this template use File | Settings | File Templates.
 */
public class TagVar {
    protected final EndTagWrapper endTagWrapper;
    public final TagCfgTpl tpl;

    public TagVar(EndTagWrapper endTagWrapper, TagCfgTpl tpl) {
        this.endTagWrapper = endTagWrapper;
        this.tpl = tpl;
    }
}
