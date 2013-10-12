package com.ht.scada.communication;

import com.google.inject.servlet.GuiceFilter;
import com.ht.scada.communication.web.cxf.MyCXFServlet;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.webapp.WebAppContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created with IntelliJ IDEA.
 *
 * @author: 薄成文 13-7-31 上午10:47
 * To change this template use File | Settings | File Templates.
 */
public class JettyServer {
    private static final Logger log = LoggerFactory.getLogger(JettyServer.class);
    public static void main(String[] args) throws Exception {
        System.out.println("========== 初始化参数配置 ==========");
        //log.info("初始化参数配置");

        int webPort = Config.INSTANCE.getWebPort();
        System.out.println("======== 版本 V0.2, 运行端口:"+webPort+" ========");
        Server server = new Server(webPort);

        WebAppContext context = new WebAppContext("webapp", "/");
        //context.setDescriptor("webapp/WEB-INF/web.xml");
        //JettyServer.class.getResource("")

        //context.setResourceBase(JettyServer.class.getResource("/webapp").toExternalForm());
        context.setResourceBase("src/main/webapp");
        context.setParentLoaderPriority(true);
        context.getInitParams().put("org.eclipse.jetty.servlet.Default.useFileMappedBuffer", "false");
        server.setHandler(context);


        context.addFilter(GuiceFilter.class, "/*", null);
        context.addServlet(DefaultServlet.class, "/");
        context.addServlet(MyCXFServlet.class, "/services/*");

//		FileInputStream fis = new FileInputStream(new File("./etc/jetty.xml"));
//		XmlConfiguration configuration = new XmlConfiguration(fis);
//		Server server = (Server) configuration.configure();
        System.out.println("启动Web服务");
        server.start();
        server.join();
    }
}
