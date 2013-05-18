package com.ht.scada.communication.web;

import com.ht.scada.communication.entity.ChannelInfo;
import com.ht.scada.communication.util.CommunicationProtocal;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
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
        List<ChannelInfo> list = new ArrayList<>();
        for (int i = 0; i < 1000; i++) {
            ChannelInfo channel = new ChannelInfo();
            channel.setName("通道"+i);
            channel.setIdx(i);
            channel.setProtocal(CommunicationProtocal.IEC104.toString());
            channel.setFrames("03034-dsa343");
            list.add(channel);
        }
        ctx.setVariable("channels", list);

        templateEngine.process("channels", ctx, response.getWriter());
    }

}