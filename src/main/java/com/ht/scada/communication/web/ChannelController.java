package com.ht.scada.communication.web;

import com.ht.scada.communication.CommunicationManager;
import com.ht.scada.communication.entity.ChannelInfo;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Calendar;
import java.util.List;

public class ChannelController implements IGTVGController {

    public ChannelController() {
        super();
    }

    public void process(
            final HttpServletRequest request, final HttpServletResponse response,
            final ServletContext servletContext, final TemplateEngine templateEngine)
            throws Exception {

        WebContext ctx = new WebContext(request, response, servletContext, request.getLocale());
        ctx.setVariable("today", Calendar.getInstance());

        //log.debug("显示采集通道");
        List<ChannelInfo> list = CommunicationManager.getInstance().getChannels();
        ctx.setVariable("channels", list);

        templateEngine.process("channels", ctx, response.getWriter());
    }

}