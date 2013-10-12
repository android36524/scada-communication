package com.ht.scada.communication.web;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 *
 * @author: 薄成文 13-8-18 上午9:59
 * To change this template use File | Settings | File Templates.
 */
public class TymeleafRender {

    private final TemplateEngine templateEngine;

    @Inject
    public TymeleafRender(TemplateEngine templateEngine) {
        this.templateEngine = templateEngine;
    }

    public void rend(HttpServletRequest request, HttpServletResponse response, String templateFile) {

        response.setContentType("text/html; charset=UTF-8");
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Cache-Control", "no-cache");
        response.setDateHeader("Expires", 0);

        WebContext context = new WebContext(request, response, request.getServletContext(), request.getLocale());
        try {
            templateEngine.process(templateFile, context, response.getWriter());
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            //throw new FreemarkerViewException(templateFile, request, e);
        }
    }
}
