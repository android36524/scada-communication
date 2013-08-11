package com.ht.scada.communication.web.cxf;

import org.apache.cxf.Bus;
import org.apache.cxf.BusFactory;
import org.apache.cxf.jaxrs.JAXRSServerFactoryBean;
import org.apache.cxf.transport.servlet.CXFNonSpringServlet;

import javax.servlet.ServletConfig;

/**
 * Created with IntelliJ IDEA.
 *
 * @author: 薄成文 13-7-11 下午4:37
 * To change this template use File | Settings | File Templates.
 */
public class MyCXFServlet extends CXFNonSpringServlet {
    @Override
    protected void loadBus(ServletConfig servletConfig) {
        super.loadBus(servletConfig);    //To change body of overridden methods use File | Settings | File Templates.

        // You could add the endpoint publish codes here
        Bus bus = getBus();
        BusFactory.setDefaultBus(bus);
        //Endpoint.publish("/Greeter", new GreeterImpl());


        JAXRSServerFactoryBean rsFactory = new JAXRSServerFactoryBean();
        //rsFactory.setProvider(new org.codehaus.jackson.jaxrs.JacksonJaxbJsonProvider());
        rsFactory.setBus(bus);
        rsFactory.setServiceBean(new MyResource());
        rsFactory.setAddress("/rs");
        rsFactory.create();
    }
}
