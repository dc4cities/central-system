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

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.versly.rest.wsdoc.AnnotationProcessor;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import java.lang.annotation.Annotation;

public class SpringMVCRestImplementationSupport implements AnnotationProcessor.RestImplementationSupport {

    @Override
    public Class<? extends Annotation> getMappingAnnotationType() {
        return RequestMapping.class;
    }

    @Override
    public String[] getRequestPaths(ExecutableElement executableElement, TypeElement contextClass) {
        RequestMapping anno = executableElement.getAnnotation(RequestMapping.class);
        return requestPathsForAnnotation(anno);
    }

    @Override
    public String[] getRequestPaths(TypeElement cls) {
        RequestMapping clsAnno = cls.getAnnotation(RequestMapping.class);
        return requestPathsForAnnotation(clsAnno);
    }

    private String[] requestPathsForAnnotation(RequestMapping clsAnno) {
        if (clsAnno == null)
            return new String[0];
        else
            return clsAnno.value();
    }

    @Override
    public String getRequestMethod(ExecutableElement executableElement, TypeElement contextClass) {
        RequestMapping anno = executableElement.getAnnotation(RequestMapping.class);
        if (anno.method().length != 1)
            throw new IllegalStateException(String.format(
                    "The RequestMapping annotation for %s.%s is not parseable. Exactly one request method (GET/POST/etc) is required.",
                    contextClass.getQualifiedName(), executableElement.getSimpleName()));
        else
            return anno.method()[0].name();
    }

    @Override
    public String getPathVariable(VariableElement var) {
        PathVariable pathVar = var.getAnnotation(PathVariable.class);
        return pathVar == null ? null : pathVar.value();
    }

    @Override
    public String getRequestParam(VariableElement var) {
        RequestParam reqParam = var.getAnnotation(RequestParam.class);
        return reqParam == null ? null : reqParam.value();
    }

    @Override
    public boolean isRequestBody(VariableElement var) {
        return var.getAnnotation(org.springframework.web.bind.annotation.RequestBody.class) != null;
    }
}
