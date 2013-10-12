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
package org.lime.guice.mvc.views.thymeleaf;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;
import org.zdevra.guice.mvc.ModelMap;
import org.zdevra.guice.mvc.ViewPoint;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

/**
 * The view provide rendering of outptu HTML via Freemarker template
 * engine
 */
public class ThymeleafViewPoint implements ViewPoint {

// ------------------------------------------------------------------------
    private TemplateEngine templateEngine;
    private ServletContext servletContext;
    private final String templateFile;

// ------------------------------------------------------------------------

    public ThymeleafViewPoint(String templateFile) {
        this.templateFile = templateFile;
    }

    public ThymeleafViewPoint(TemplateEngine templateEngine, String templateFile) {
        //To change body of created methods use File | Settings | File Templates.
        this.templateEngine = templateEngine;
        this.templateFile = templateFile;
    }

    // ------------------------------------------------------------------------
    @Override
    public void render(ModelMap model, HttpServlet servlet, HttpServletRequest request, HttpServletResponse response) {

        response.setContentType("text/html; charset=UTF-8");
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Cache-Control", "no-cache");
        response.setDateHeader("Expires", 0);

        WebContext context = new WebContext(request, response, request.getServletContext(), request.getLocale());
        for (Map.Entry<String, Object> entry : model.entrySet()) {
            context.setVariable(entry.getKey(), entry.getValue());
        }
        try {
            templateEngine.process(templateFile, context, response.getWriter());
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            //throw new FreemarkerViewException(templateFile, request, e);
        }
    }
// ------------------------------------------------------------------------
}
