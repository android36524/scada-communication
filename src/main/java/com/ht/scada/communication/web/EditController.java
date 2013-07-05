package com.ht.scada.communication.web;

import com.ht.scada.communication.Config;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Calendar;

public class EditController implements IGTVGController {


    public EditController() {
        super();
    }
    
    public void process(
            final HttpServletRequest request, final HttpServletResponse response,
            final ServletContext servletContext, final TemplateEngine templateEngine)
            throws Exception {
        if (request.getMethod().equals("GET")) {
            WebContext ctx = new WebContext(request, response, servletContext, request.getLocale());
            ctx.setVariable("today", Calendar.getInstance());

            ctx.setVariable("isSingle", false);
            ctx.setVariable("isMaster", false);
            ctx.setVariable("isSlaver", false);

            ctx.setVariable("isConnected", false);
            ctx.setVariable("masterHost", Config.INSTANCE.getMasterHost());
            ctx.setVariable("masterPort", Config.INSTANCE.getMasterPort());

            switch (Config.INSTANCE.getMode()) {
                case SINGLE:
                    ctx.setVariable("mode", "单机模式");
                    ctx.setVariable("isSingle", true);
                    break;
                case MASTER:
                    ctx.setVariable("mode", "主机模式");
                    ctx.setVariable("isMaster", true);
                    break;
                case SLAVER:
                    ctx.setVariable("mode", "备机模式");
                    ctx.setVariable("isSlaver", true);
                    break;
            }

            templateEngine.process("edit", ctx, response.getWriter());
        } else {// post
            // TODO:更新表单项
            response.sendRedirect("/");
        }

    }

}