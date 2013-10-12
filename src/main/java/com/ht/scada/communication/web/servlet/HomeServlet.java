package com.ht.scada.communication.web.servlet;

import com.ht.scada.communication.CommunicationManager;
import com.ht.scada.communication.Config;

import javax.inject.Singleton;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Calendar;

@Singleton
public class HomeServlet extends TymeleafRenderServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse resp) throws ServletException, IOException {

        request.setAttribute("today", Calendar.getInstance());

        request.setAttribute("running", CommunicationManager.getInstance().isRunning());
        request.setAttribute("channelSize", CommunicationManager.getInstance().getChannels().size());

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

        //templateEngine.process("index", ctx, response.getWriter());

//        return ctx;
        //return new ModelAndView(ctx, new ThymeleafViewPoint("index"));
        render.rend(request, resp, "index");
    }

}