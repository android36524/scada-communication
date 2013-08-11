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

    public TagVar(EndTagWrapper endTagWrapper, TagVarTpl tpl) {
        this.endTagWrapper = endTagWrapper;
        this.tpl = tpl;
//        String[] array = new String[]{endTagWrapper.getEndTag().getCode(), tpl.getVarGroup().toString(), tpl.getVarType().toString(), this.tpl.getVarName()};
//        key = StringUtils.join(array, "/");
    }

    public TagVarTpl getTpl() {
        return tpl;
    }
}
