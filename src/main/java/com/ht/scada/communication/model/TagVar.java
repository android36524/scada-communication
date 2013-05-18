package com.ht.scada.communication.model;

import com.ht.scada.communication.entity.TagVarTpl;

/**
 * Created with IntelliJ IDEA.
 * 作者: "薄成文"
 * 日期: 13-5-8 下午12:33
 * To change this template use File | Settings | File Templates.
 */
public class TagVar {
    protected final EndTagWrapper endTagWrapper;
    public final TagVarTpl tpl;
    private String key;

    public TagVar(EndTagWrapper endTagWrapper, TagVarTpl tpl) {
        this.endTagWrapper = endTagWrapper;
        this.tpl = tpl;
        key = endTagWrapper.getEndTag().getCode() + "/" + this.tpl.getVarGroup().toString() + "/" + this.tpl.getVarName();
    }

    /**
     * 获取实时数据库存储用key
     * @return
     */
    public String getRTKey() {
        return key;
    }

    public TagVarTpl getTpl() {
        return tpl;
    }
}
