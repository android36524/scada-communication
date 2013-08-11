package com.ht.scada.communication.web;

import com.ht.scada.communication.CommunicationChannel;
import com.ht.scada.communication.CommunicationManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
@WebServlet(urlPatterns = {"/yk"})
public class YkServlet extends HttpServlet {
    private static final Logger log = LoggerFactory.getLogger(YkServlet.class);

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/x-json");

        log.info("遥控操作");

        String pass = request.getParameter("password");
        if (pass != null && pass.equals("ht19928")) {
            String idxStr = request.getParameter("channelIdx");
            String endCode = request.getParameter("endCode");
            boolean value = Boolean.parseBoolean(request.getParameter("value"));
            String varName = request.getParameter("varName");

            log.info("EndCode: {}, VarName: {}, Value: {}", endCode, varName, value);

            int idx = Integer.parseInt(idxStr);
            CommunicationChannel communicationChannel = CommunicationManager.getInstance().getChannelMap().get(idx);
            //EndTagWrapper endTagWrapper = communicationChannel.getEndTagList().get(0);
            if (communicationChannel.exeYK(endCode, varName, value)) {
                response.getWriter().write("\"操作成功\"");
            } else {
                response.getWriter().write("\"操作失败\"");
            }
        } else {
            response.getWriter().write("\"密码错误\"");
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doPost(req, resp);    //To change body of overridden methods use File | Settings | File Templates.
    }
}
