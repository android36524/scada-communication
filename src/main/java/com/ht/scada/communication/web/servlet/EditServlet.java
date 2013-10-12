package com.ht.scada.communication.web.servlet;

import com.ht.scada.communication.Config;

import javax.inject.Singleton;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Calendar;

@Singleton
public class EditServlet extends TymeleafRenderServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        super.doGet(req, resp);    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse resp) throws ServletException, IOException {
        doGet(request, resp);    //To change body of overridden methods use File | Settings | File Templates.
        if (request.getMethod().equals("GET")) {
            request.setAttribute("today", Calendar.getInstance());

            request.setAttribute("isSingle", false);
            request.setAttribute("isMaster", false);
            request.setAttribute("isSlaver", false);

            request.setAttribute("isConnected", false);
            request.setAttribute("masterHost", Config.INSTANCE.getMasterHost());
            request.setAttribute("masterPort", Config.INSTANCE.getMasterPort());

            switch (Config.INSTANCE.getMode()) {
                case SINGLE:
                    request.setAttribute("mode", "单机模式");
                    request.setAttribute("isSingle", true);
                    break;
                case MASTER:
                    request.setAttribute("mode", "主机模式");
                    request.setAttribute("isMaster", true);
                    break;
                case SLAVER:
                    request.setAttribute("mode", "备机模式");
                    request.setAttribute("isSlaver", true);
                    break;
            }

            render.rend(request, resp, "view");
        } else {// post
            // TODO:更新表单项
            resp.sendRedirect("/");
        }

    }

}