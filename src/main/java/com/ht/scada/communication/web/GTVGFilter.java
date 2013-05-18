package com.ht.scada.communication.web;

import org.thymeleaf.TemplateEngine;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;


@WebFilter("/*")
public class GTVGFilter implements Filter {


    private ServletContext servletContext;


    public GTVGFilter() {
        super();
    }

    private static void addUserToSession(final HttpServletRequest request) {
        // Simulate a real user session by adding a user object
        //request.getSession(true).setAttribute("user", new User("John", "Apricot", "Antarctica", null));
    }

    public void init(final FilterConfig filterConfig) throws ServletException {
        this.servletContext = filterConfig.getServletContext();
    }

    public void doFilter(final ServletRequest request, final ServletResponse response,
                         final FilterChain chain) throws IOException, ServletException {
        addUserToSession((HttpServletRequest) request);
        if (!process((HttpServletRequest) request, (HttpServletResponse) response)) {
            chain.doFilter(request, response);
        }
    }

    public void destroy() {
        // nothing to do
    }

    private boolean process(HttpServletRequest request, HttpServletResponse response)
            throws ServletException {

        try {
            IGTVGController controller = GTVGApplication.resolveControllerForRequest(request);
            if (controller == null) {
                return false;
            }

            TemplateEngine templateEngine = GTVGApplication.getTemplateEngine();

            response.setContentType("text/html;charset=UTF-8");
            response.setHeader("Pragma", "no-cache");
            response.setHeader("Cache-Control", "no-cache");
            response.setDateHeader("Expires", 0);

            controller.process(request, response, this.servletContext, templateEngine);

            return true;

        } catch (Exception e) {
            throw new ServletException(e);
        }

    }


}