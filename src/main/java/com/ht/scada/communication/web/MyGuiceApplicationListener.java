package com.ht.scada.communication.web;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.servlet.GuiceServletContextListener;
import com.google.inject.servlet.ServletModule;
import com.ht.scada.communication.CommunicationManager;
import com.ht.scada.communication.DataBaseManager;
import com.ht.scada.communication.cluser.CluserController;
import com.ht.scada.communication.guice.PersistModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContextEvent;

public class MyGuiceApplicationListener extends GuiceServletContextListener {
    public static final Logger log = LoggerFactory.getLogger(MyGuiceApplicationListener.class);
    public static Injector injector;

    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {
        super.contextInitialized(servletContextEvent);    //To change body of overridden methods use File | Settings | File Templates.

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

    @Override
    protected Injector getInjector() {
        injector = Guice.createInjector(new PersistModule(), new ServletModule() {
            @Override
            protected void configureServlets() {
                //serve("*.html").with(MyServlet.class);
            }
        });
        return injector;
    }
}