package com.ht.scada.communication.web;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.templateresolver.ServletContextTemplateResolver;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;


public class GTVGApplication {

    
    private static Map<String, IGTVGController> controllersByURL;
    private static TemplateEngine templateEngine;
    
    
    
    static {
        initializeControllersByURL();
        initializeTemplateEngine();
    }
    
    
    private static void initializeTemplateEngine() {
        
        ServletContextTemplateResolver templateResolver = new ServletContextTemplateResolver();
        
        // XHTML is the default mode, but we will set it anyway for better understanding of code
        //templateResolver.setTemplateMode("XHTML");
        templateResolver.setTemplateMode("HTML5");
        // This will convert "home" to "/WEB-INF/templates/home.html"
        templateResolver.setPrefix("/WEB-INF/tpl/");
        templateResolver.setSuffix(".html");
        templateResolver.setCharacterEncoding("UTF-8");
        // Set template cache TTL to 1 hour. If not set, entries would live in cache until expelled by LRU
        templateResolver.setCacheTTLMs(Long.valueOf(3600000L));
        
        // Cache is set to true by default. Set to false if you want templates to
        // be automatically updated when modified.
        templateResolver.setCacheable(false);
        
        templateEngine = new TemplateEngine();
        templateEngine.setTemplateResolver(templateResolver);
        
    }
    
    
    private static Map<String, IGTVGController> initializeControllersByURL() {

        controllersByURL = new HashMap<String, IGTVGController>();
        controllersByURL.put("/", new HomeController());
        controllersByURL.put("/main/edit", new EditController());
        controllersByURL.put("/channels", new ChannelController());
        controllersByURL.put("/endTag", new EndModelController());
        controllersByURL.put("/endTagHistory", new EndModelHistoryController());
        controllersByURL.put("/yxRecord", new EndModelYxRecordController());
        controllersByURL.put("/faultRecord", new EndModelFaultRecordController());
        controllersByURL.put("/offLimitsRecord", new EndModelOffLimitsRecordController());
//        controllersByURL.put("/product/list", new ProductListController());
//        controllersByURL.put("/product/comments", new ProductCommentsController());
//        controllersByURL.put("/order/list", new OrderListController());
//        controllersByURL.put("/order/details", new OrderDetailsController());
//        controllersByURL.put("/subscribe", new SubscribeController());
//        controllersByURL.put("/userprofile", new UserProfileController());
        
        return controllersByURL;
        
    }

    
    public static IGTVGController resolveControllerForRequest(final HttpServletRequest request) {
        final String path = getRequestPath(request);
        return controllersByURL.get(path);
    }
    
    
    public static TemplateEngine getTemplateEngine() {
        return templateEngine;
    }

    
    
    private static String getRequestPath(final HttpServletRequest request) {
        
        String requestURI = request.getRequestURI();
        final String contextPath = request.getContextPath();
        
        final int fragmentIndex = requestURI.indexOf(';');
        if (fragmentIndex != -1) {
            requestURI = requestURI.substring(0, fragmentIndex);
        }
        
        if (requestURI.startsWith(contextPath)) {
            return requestURI.substring(contextPath.length());
        }
        return requestURI;
    }
    
    
    
    private GTVGApplication() {
        super();
    }
    
    
}