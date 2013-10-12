package com.ht.scada.communication.web.servlet;

import com.ht.scada.communication.CommunicationManager;
import com.ht.scada.communication.entity.ChannelInfo;

import javax.inject.Singleton;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

@Singleton
public class ChannelServlet extends TymeleafRenderServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse resp) throws ServletException, IOException {
//    @Path("/channels") @ThymeleafView("channels")
//    public void process( HttpServletRequest request)
//            throws Exception {

        request.setAttribute("today", Calendar.getInstance());

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

        request.setAttribute("pageSize", pageSize);
        request.setAttribute("pageIndex", pageIndexParam);

        //log.debug("显示采集通道");
        List<ChannelInfo> list = CommunicationManager.getInstance().getChannels();

        int count = list.size();
        request.setAttribute("pageCount", (count + pageSize - 1) / pageSize);
//        if (count % pageSize == 0) {
//            request.setAttribute("pageCount", count / pageSize);
//        } else {
//            request.setAttribute("pageCount", count / pageSize + 1);
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
        request.setAttribute("channels", items);

        render.rend(request,resp, "channels");

    }

}