/*
 * Copyright (c) 2019 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.rest;

import java.util.Set;
import java.util.stream.Collectors;

import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.ParamConverterProvider;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.IBean;
import org.eclipse.scout.rt.rest.RestApplication.IRestApplicationClassesContributor;
import org.eclipse.scout.rt.rest.container.IRestContainerRequestFilter;

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
