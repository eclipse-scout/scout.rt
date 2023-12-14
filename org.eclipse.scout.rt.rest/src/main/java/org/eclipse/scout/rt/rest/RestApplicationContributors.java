/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.rest;

import java.util.Set;
import java.util.stream.Collectors;

import jakarta.ws.rs.ext.ContextResolver;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.ParamConverterProvider;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.IBean;
import org.eclipse.scout.rt.rest.RestApplication.IRestApplicationClassesContributor;
import org.eclipse.scout.rt.rest.container.IRestContainerRequestFilter;
import org.eclipse.scout.rt.rest.container.IRestContainerResponseFilter;

/**
 * Collection of built-in contributors for {@link RestApplication} classes.
 */
public final class RestApplicationContributors {

  private RestApplicationContributors() {
  }

  public static class ContextResolverContributor implements IRestApplicationClassesContributor {
    @Override
    public Set<Class<?>> contribute() {
      return lookupBeanClasses(ContextResolver.class);
    }
  }

  public static class ExceptionMapperContributor implements IRestApplicationClassesContributor {
    @Override
    public Set<Class<?>> contribute() {
      return lookupBeanClasses(ExceptionMapper.class);
    }
  }

  public static class ParamConverterProviderContributor implements IRestApplicationClassesContributor {
    @Override
    public Set<Class<?>> contribute() {
      return lookupBeanClasses(ParamConverterProvider.class);
    }
  }

  public static class RestContainerRequestFilterContributor implements IRestApplicationClassesContributor {
    @Override
    public Set<Class<?>> contribute() {
      return lookupBeanClasses(IRestContainerRequestFilter.class);
    }
  }

  public static class RestContainerResponseFilterContributor implements IRestApplicationClassesContributor {
    @Override
    public Set<Class<?>> contribute() {
      return lookupBeanClasses(IRestContainerResponseFilter.class);
    }
  }

  public static class RestResourceContributor implements IRestApplicationClassesContributor {
    @Override
    public Set<Class<?>> contribute() {
      return lookupBeanClasses(IRestResource.class);
    }
  }

  /**
   * @return {@link Set} of bean classes matching the given {@code lookupBeanClazz}
   */
  public static Set<Class<?>> lookupBeanClasses(Class<?> lookupBeanClazz) {
    return BEANS.getBeanManager().getBeans(lookupBeanClazz).stream()
        .map(IBean::getBeanClazz)
        .collect(Collectors.toSet());
  }
}
