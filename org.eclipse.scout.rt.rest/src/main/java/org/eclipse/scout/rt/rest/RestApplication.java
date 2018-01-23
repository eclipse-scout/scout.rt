/*******************************************************************************
 * Copyright (c) 2010-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.rest;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.ws.rs.core.Application;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.ExceptionMapper;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.IBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JAX-RS REST application registering all {@link IRestResource}, {@link ExceptionMapper} and {@link ContextResolver} to
 * the JAX-RS context.
 */
public class RestApplication extends Application {

  private static final Logger LOG = LoggerFactory.getLogger(RestApplication.class);

  private Set<Class<?>> m_classes = new HashSet<>();

  public RestApplication() {
    m_classes = initClasses();
  }

  protected Set<Class<?>> initClasses() {
    Set<Class<?>> classes = new HashSet<>();
    registerContextResolver(classes);
    registerExceptionMappers(classes);
    registerRestResources(classes);
    return classes;
  }

  protected void registerContextResolver(Set<Class<?>> classes) {
    registerClasses(classes, ContextResolver.class);
  }

  protected void registerExceptionMappers(Set<Class<?>> classes) {
    registerClasses(classes, ExceptionMapper.class);
  }

  protected void registerRestResources(Set<Class<?>> classes) {
    registerClasses(classes, IRestResource.class);
  }

  protected <T> void registerClasses(Set<Class<?>> classes, Class<T> lookupBeanClazz) {
    List<IBean<T>> beans = BEANS.getBeanManager().getBeans(lookupBeanClazz);
    for (IBean<?> bean : beans) {
      classes.add(bean.getBeanClazz());
      LOG.debug("{} registered as REST {}", bean.getBeanClazz(), lookupBeanClazz.getSimpleName());
    }
    LOG.info("Registered {} classe(s) as REST {}", beans.size(), lookupBeanClazz.getSimpleName());
  }

  @Override
  public Set<Class<?>> getClasses() {
    return m_classes;
  }
}
