package com.ht.scada.communication.web;

import com.ht.scada.communication.CommunicationManager;
import com.ht.scada.communication.entity.ChannelInfo;
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

        String pageIndexParam = request.getParameter("pageIndex");
        if (pageIndexParam == null) {
            pageIndexParam = "1";
        }
        int pageIndex = Integer.parseInt(pageIndexParam) - 1;

        String pageSizeParam = request.getParameter("pageSize");
        if (pageSizeParam == null) {
            pageSizeParam = "20";
        }
        int pageSize = Integer.parseInt(pageSizeParam);

        ctx.setVariable("pageSize", pageSize);
        ctx.setVariable("pageIndex", pageIndexParam);

        //log.debug("显示采集通道");
        List<ChannelInfo> list = CommunicationManager.getInstance().getChannels();

        int count = list.size();
        ctx.setVariable("pageCount", (count + pageSize - 1) / pageSize);
//        if (count % pageSize == 0) {
//            ctx.setVariable("pageCount", count / pageSize);
//        } else {
//            ctx.setVariable("pageCount", count / pageSize + 1);
//        }
        List<ChannelInfo> items = new ArrayList<>(pageSize);
        int index;
        for (int i = 0; i < pageSize; i++) {
            index = i + pageSize * pageIndex;
            if (index >= list.size()) {
                break;
            }
            items.add(list.get(index));
        }
        ctx.setVariable("channels", items);

        templateEngine.process("channels", ctx, response.getWriter());
    }

}