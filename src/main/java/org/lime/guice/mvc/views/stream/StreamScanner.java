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
package org.lime.guice.mvc.views.stream;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.lime.guice.mvc.views.stream.annotations.StreamView;
import org.zdevra.guice.mvc.Utils;
import org.zdevra.guice.mvc.ViewPoint;
import org.zdevra.guice.mvc.ViewScanner;

import java.lang.annotation.Annotation;

/**
 * The view scanner is looking for {@literal @}ThymeleafView annotation
 * in controller or in controller's method and creates the 
 * {@link org.lime.guice.mvc.views.json.JsonViewPoint}r instance
 * 
 * This is internal class which is invisible for normal usage.
 */
@Singleton
class StreamScanner implements ViewScanner {

// ------------------------------------------------------------------------
    private final StreamViewPoint jsonViewPoint;

    @Inject
    public StreamScanner(StreamViewPoint viewPoint) {
        this.jsonViewPoint = viewPoint;
    }

    @Override
    public ViewPoint scan(Annotation[] anots) {
        StreamView anot = Utils.getAnnotation(StreamView.class, anots);
        if (anot == null) {
            return ViewPoint.NULL_VIEW;
        }

        return jsonViewPoint;
    }
// ------------------------------------------------------------------------
}
