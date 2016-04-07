/*
 * Copyright 2016 The DC4Cities author.
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
 */

package org.versly.rest.wsdoc.impl;

import org.versly.rest.wsdoc.AnnotationProcessor;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.ws.rs.*;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;

public class JaxRSRestImplementationSupport implements AnnotationProcessor.RestImplementationSupport {
    @Override
    public Class<? extends Annotation> getMappingAnnotationType() {
        return Path.class;
    }

    @Override
    public String[] getRequestPaths(ExecutableElement executableElement, TypeElement contextClass) {
        Path anno = executableElement.getAnnotation(Path.class);
        if (anno == null)
            throw new IllegalStateException(String.format(
                    "The Path annotation for %s.%s is not parseable. Exactly one value is required.",
                    contextClass.getQualifiedName(), executableElement.getSimpleName()));
        else
            return new String[]{anno.value()};
    }

    @Override
    public String[] getRequestPaths(TypeElement cls) {
        Path clsAnno = cls.getAnnotation(Path.class);
        if (clsAnno == null)
            return new String[0];
        else
            return new String[]{clsAnno.value()};
    }

    @Override
    public String getRequestMethod(ExecutableElement executableElement, TypeElement contextClass) {
        List<String> methods = new ArrayList<String>();

        gatherMethod(executableElement, methods, GET.class);
        gatherMethod(executableElement, methods, PUT.class);
        gatherMethod(executableElement, methods, POST.class);
        gatherMethod(executableElement, methods, DELETE.class);

        if (methods.size() != 1)
            throw new IllegalStateException(String.format(
                    "The method annotation for %s.%s is not parseable. Exactly one request method (GET/POST/PUT/DELETE) is required. Found: %s",
                    contextClass.getQualifiedName(), executableElement.getSimpleName(), methods));

        return methods.get(0);
    }

    private void gatherMethod(ExecutableElement executableElement, List<String> methods, Class<? extends Annotation> anno) {
        if (executableElement.getAnnotation(anno) != null) {
            methods.add(anno.getSimpleName());
        }
    }

    @Override
    public String getPathVariable(VariableElement var) {
        PathParam param = var.getAnnotation(PathParam.class);
        return param == null ? null : param.value();
    }

    @Override
    public String getRequestParam(VariableElement var) {
        QueryParam param = var.getAnnotation(QueryParam.class);
        return param == null ? null : param.value();
    }

    @Override
    public boolean isRequestBody(VariableElement var) {
        return var.getAnnotationMirrors().size() == 0;
    }
}
