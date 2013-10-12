package com.ht.scada.communication.web.servlet;

import com.ht.scada.communication.web.TymeleafRender;

import javax.inject.Inject;
import javax.servlet.http.HttpServlet;

/**
 * Created with IntelliJ IDEA.
 *
 * @author: 薄成文 13-8-18 上午10:04
 * To change this template use File | Settings | File Templates.
 */
public class TymeleafRenderServlet extends HttpServlet {

    protected TymeleafRender render;

    @Inject
    public void setRender(TymeleafRender render) {
        this.render = render;
    }
}
