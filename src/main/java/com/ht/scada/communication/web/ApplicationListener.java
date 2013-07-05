package com.ht.scada.communication.web; /**
 * Created with IntelliJ IDEA.
 * @author: 薄成文 13-6-19 上午9:53
 * To change this template use File | Settings | File Templates.
 */

import com.ht.scada.communication.CommunicationManager;
import com.ht.scada.communication.DataBaseManager;
import com.ht.scada.communication.cluser.CluserController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.http.HttpSessionAttributeListener;
import javax.servlet.http.HttpSessionBindingEvent;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

public class ApplicationListener implements ServletContextListener,
        HttpSessionListener, HttpSessionAttributeListener {
    public static final Logger log = LoggerFactory.getLogger(ApplicationListener.class);

    // Public constructor is required by servlet spec
    public ApplicationListener() {
    }

    // -------------------------------------------------------
    // ServletContextListener implementation
    // -------------------------------------------------------
    public void contextInitialized(ServletContextEvent sce) {
        log.info("初始化参数配置");
        //必须先初始化参数配置
        //Config.INSTANCE.init(sce.getServletContext().getRealPath("/WEB-INF/config.properties"));
        DataBaseManager.getInstance().init();

        // 启动通讯采集服务
        log.info("启动通讯服务");
        try {
            CommunicationManager.getInstance().start();
        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        // 启动双机热备服务（权限不同的模式决定是否执行采集）
        log.info("启动运行模式管理服务");
        CluserController.getInstance().start();
    }

    public void contextDestroyed(ServletContextEvent sce) {
      /* This method is invoked when the Servlet Context
         (the Web application) is undeployed or
         Application Server shuts down.
      */
        CommunicationManager.getInstance().destroy();
        DataBaseManager.getInstance().destroy();
    }

    // -------------------------------------------------------
    // HttpSessionListener implementation
    // -------------------------------------------------------
    public void sessionCreated(HttpSessionEvent se) {
      /* Session is created. */
    }

    public void sessionDestroyed(HttpSessionEvent se) {
      /* Session is destroyed. */
    }

    // -------------------------------------------------------
    // HttpSessionAttributeListener implementation
    // -------------------------------------------------------

    public void attributeAdded(HttpSessionBindingEvent sbe) {
      /* This method is called when an attribute 
         is added to a session.
      */
    }

    public void attributeRemoved(HttpSessionBindingEvent sbe) {
      /* This method is called when an attribute
         is removed from a session.
      */
    }

    public void attributeReplaced(HttpSessionBindingEvent sbe) {
      /* This method is invoked when an attibute
         is replaced in a session.
      */
    }
}
