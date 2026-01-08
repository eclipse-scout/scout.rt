/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.rest.resource;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HEAD;
import jakarta.ws.rs.HttpMethod;
import jakarta.ws.rs.OPTIONS;
import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;

import org.eclipse.scout.rt.api.data.ApiExposed;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.rest.IRestResource;
import org.eclipse.scout.rt.rest.RestApplicationScope;
import org.eclipse.scout.rt.rest.RestApplicationScopes;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.hamcrest.CoreMatchers;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ErrorCollector;
import org.junit.runner.RunWith;

@RunWith(PlatformTestRunner.class)
public abstract class AbstractApiExposedAnnotationTest {

  @Rule
  public ErrorCollector m_errorCollector = new ErrorCollector();

  @Test
  public void testAllOperationsAnnotated() {
    List<IRestResource> resources = BEANS.all(IRestResource.class);
    resources.stream()
        .map(IRestResource::getClass)
        .filter(this::acceptResource)
        .forEach(resourceClass -> Stream.of(resourceClass.getMethods())
            .filter(this::acceptMethod)
            .forEach(this::testMethod));
  }

  protected void testMethod(Method m) {
    m_errorCollector.checkThat(m.getDeclaringClass().getName() + "#" + m.getName() + " is missing an " + ApiExposed.class.getSimpleName() + " annotation", m.getAnnotation(ApiExposed.class) != null, CoreMatchers.is(true));
  }

  protected boolean acceptResource(Class<? extends IRestResource> resource) {
    RestApplicationScope applicationScope = resource.getAnnotation(RestApplicationScope.class);
    return (applicationScope == null || Arrays.asList(applicationScope.value()).contains(RestApplicationScopes.API)) && resource.getName().startsWith(getPackageNamePrefix());
  }

  protected abstract String getPackageNamePrefix();

  protected boolean acceptMethod(Method m) {
    if (!Modifier.isPublic(m.getModifiers())) {
      return false;
    }

    if (getHttpMethodAnnotations().anyMatch(m::isAnnotationPresent)) {
      return true;
    }

    return false;
  }

  protected Stream<Class<? extends Annotation>> getHttpMethodAnnotations() {
    return Stream.of(HttpMethod.class, GET.class, POST.class, PUT.class, DELETE.class, OPTIONS.class, HEAD.class, PATCH.class);
  }
}
