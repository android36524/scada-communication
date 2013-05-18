package com.ht.scada.communication.web;

import com.ht.scada.communication.CommunicationManager;
import com.ht.scada.communication.cluser.CluserController;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 * 作者: "薄成文"
 * 日期: 13-5-18 上午8:49
 * To change this template use File | Settings | File Templates.
 */
@WebServlet(urlPatterns = {"/main/start", "/main/stop"})
public class StartStopServlet extends HttpServlet {

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        System.out.println(request.getRequestURI());
        if (request.getRequestURI().endsWith("/main/start")) {
            try {
                CommunicationManager.getInstance().start();
                CluserController.getInstance().start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            try {
                CommunicationManager.getInstance().stop();
                CluserController.getInstance().stop();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        response.sendRedirect("/");
    }
}
