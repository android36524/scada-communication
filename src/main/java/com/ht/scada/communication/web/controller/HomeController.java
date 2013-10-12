package com.ht.scada.communication.web.controller;

import com.ht.scada.communication.CommunicationManager;
import com.ht.scada.communication.Config;
import org.lime.guice.mvc.views.thymeleaf.annotations.ThymeleafView;
import org.thymeleaf.TemplateEngine;
import org.zdevra.guice.mvc.annotations.Controller;
import org.zdevra.guice.mvc.annotations.Path;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;
import java.util.Calendar;

@Controller
@Singleton
public class HomeController extends AbstractController {

    @Inject
    public HomeController(TemplateEngine templateEngine) {
        super(templateEngine);
    }

    @Path("/") @ThymeleafView("index")
    public void process(HttpServletRequest request) throws Exception {

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
    }

}