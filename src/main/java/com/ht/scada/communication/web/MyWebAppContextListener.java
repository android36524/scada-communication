package com.ht.scada.communication.web;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.servlet.GuiceServletContextListener;
import com.google.inject.servlet.ServletModule;
import com.ht.scada.communication.CommunicationManager;
import com.ht.scada.communication.cluser.CluserController;
import com.ht.scada.communication.guice.PersistModule;
import com.ht.scada.communication.web.servlet.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContextEvent;

public class MyWebAppContextListener extends GuiceServletContextListener {
    public static final Logger log = LoggerFactory.getLogger(MyWebAppContextListener.class);
    public static Injector injector;

    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {
        super.contextInitialized(servletContextEvent);    //To change body of overridden methods use File | Settings | File Templates.
        start();
    }

    private void start() {
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

//        injector = Guice.createInjector(new PersistModule(), new MvcModule() {
//
//            @Override
//            protected void configureControllers() {
//                //getServletContext()
//                install(new StreamModule());
//                install(new JsonModule());
//                install(new ThymeleafModule());
//
//                control("/").withController(HomeController.class);
//                control("/channels").withController(ChannelController.class);
//                control("/main/*").withController(EditController.class);
//                control("/endTag").withController(EndModelController.class);
//                control("/endTagHistory").withController(EndModelHistoryController.class);
//                control("/yxRecord").withController(RecordController.class);
//                control("/faultRecord").withController(RecordController.class);
//                control("/offLimitsRecord").withController(RecordController.class);
//
//                control("/gt").withController(SGTController.class);
//            }
//        });
        injector = Guice.createInjector(new PersistModule(), new ServletModule() {
            @Override
            protected void configureServlets() {
                //getServletContext()
                install(new ThymeleafModule());

//                serve("/services/*").with(MyCXFServlet.class);
//                serve("/gt").with(SGTServlet.class);
//                serve("/control/*").with(StartStopServlet.class);

                //To change body of implemented methods use File | Settings | File Templates.
                //serve("*.html").with(MyServlet.class);
                serve("/").with(HomeServlet.class);
                serve("/channels").with(ChannelServlet.class);
                serve("/main/*").with(EditServlet.class);
                serve("/endTag").with(EndModelServlet.class);
                serve("/endTagHistory").with(EndModelHistoryServlet.class);
//                control("/yxRecord").withController(RecordController.class);
//                control("/faultRecord").withController(RecordController.class);
//                control("/offLimitsRecord").withController(RecordController.class);

                serve("/gt").with(SGTServlet.class);
            }
        });
        return injector;
    }
}