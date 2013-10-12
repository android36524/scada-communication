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
package org.lime.guice.mvc.views.json;

import com.alibaba.fastjson.JSON;
import org.zdevra.guice.mvc.ModelMap;
import org.zdevra.guice.mvc.ViewPoint;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * The view provide rendering of outptu HTML via Freemarker template
 * engine
 */
public class JsonViewPoint implements ViewPoint {

    @Override
    public void render(ModelMap model, HttpServlet servlet, HttpServletRequest request, HttpServletResponse response) {

        response.setContentType("application/json; charset=UTF-8");
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Cache-Control", "no-cache");
        response.setDateHeader("Expires", 0);

        Object json = model.get("json");
        if (json != null) {
            try {
                response.getWriter().write(JSON.toJSONString(json));
            } catch (IOException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }
    }
// ------------------------------------------------------------------------
}
