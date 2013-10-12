package com.ht.scada.communication.web.controller;

import com.ht.scada.communication.Config;
import org.lime.guice.mvc.views.json.annotations.JsonView;
import org.lime.guice.mvc.views.thymeleaf.ThymeleafViewPoint;
import org.thymeleaf.TemplateEngine;
import org.zdevra.guice.mvc.ViewPoint;
import org.zdevra.guice.mvc.annotations.Controller;
import org.zdevra.guice.mvc.annotations.Model;
import org.zdevra.guice.mvc.annotations.Path;
import org.zdevra.guice.mvc.views.RedirectViewPoint;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

@Controller
@Singleton
public class EditController extends AbstractController {

    @Inject
    public EditController(TemplateEngine templateEngine) {
        super(templateEngine);
    }

    @Path("/json") @Model("json") @JsonView
    public Object jsonTest() {
        Map<String, String> map = new HashMap<>();
        map.put("a", "b");
        return map;
    }

    @Path("/edit")
    public ViewPoint process( HttpServletRequest request)
            throws Exception {
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

            return new ThymeleafViewPoint(templateEngine, "view");
        } else {// post
            // TODO:更新表单项
            //response.sendRedirect("/");
            return new RedirectViewPoint("/");
        }

    }

}