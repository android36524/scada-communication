/*****************************************************************************
 * Copyright 2011 Zdenko Vrabel
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 *****************************************************************************/
package com.ht.scada.communication.web;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.templateresolver.ServletContextTemplateResolver;

/**
 * Use this Lime MVC Module if you want to use Freemarker
 * template engine for rendering views.
 * 
 * The module register and bind freemarker's Configuration
 * instance into Guice as a singleton. 
 * 
 * @see org.zdevra.guice.mvc.MvcModule
 */
public class ThymeleafModule extends AbstractModule {

// ------------------------------------------------------------------------
    private TemplateEngine templateEngine;
    private String encoding = "UTF-8";
    private String templateMode = "HTML5";
    private String prefix = "/WEB-INF/tpl/";
    private String suffix = ".html";

// ------------------------------------------------------------------------
    public ThymeleafModule() {
    }

    @Override
    protected void configure() {
        ServletContextTemplateResolver templateResolver = new ServletContextTemplateResolver();

        // XHTML is the default mode, but we will set it anyway for better understanding of code
        templateResolver.setTemplateMode(templateMode);
        templateResolver.setPrefix(prefix);
        templateResolver.setSuffix(suffix);
        //templateResolver.setCharacterEncoding("UTF-8");
        templateResolver.setCharacterEncoding(encoding);
        // Set template cache TTL to 1 hour. If not set, entries would live in cache until expelled by LRU
        templateResolver.setCacheTTLMs(Long.valueOf(3600000L));

        templateResolver.setCacheable(false);

        templateEngine = new TemplateEngine();
        templateEngine.setTemplateResolver(templateResolver);

        bind(TemplateEngine.class).toInstance(templateEngine);
        bind(TymeleafRender.class).in(Singleton.class);
    }

}
