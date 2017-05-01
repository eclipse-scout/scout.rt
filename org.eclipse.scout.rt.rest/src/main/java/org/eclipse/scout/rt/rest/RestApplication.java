/*
 * Copyright (c) BSI Business Systems Integration AG. All rights reserved.
 * http://www.bsiag.com/
 */
package org.eclipse.scout.rt.rest;

import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.Path;
import javax.ws.rs.core.Application;
import javax.ws.rs.ext.ExceptionMapper;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.IBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JAX-RS REST application registering all {@link IRestResource} and {@link ExceptionMapper} to JAX-RS context.
 */
public class RestApplication extends Application {

  private static final Logger LOG = LoggerFactory.getLogger(RestApplication.class);

  private Set<Class<?>> m_classes = new HashSet<>();

  public RestApplication() {
    m_classes = initClasses();
  }

  protected Set<Class<?>> initClasses() {
    Set<Class<?>> classes = new HashSet<>();
    registerObjectMapperResolver(classes);
    registerExceptionMappers(classes);
    registerRestResources(classes);
    return classes;
  }

  protected void registerObjectMapperResolver(Set<Class<?>> classes) {
    classes.add(ObjectMapperResolver.class);
  }

  protected void registerExceptionMappers(Set<Class<?>> classes) {
    for (@SuppressWarnings("rawtypes")
    IBean<ExceptionMapper> bean : BEANS.getBeanManager().getBeans(ExceptionMapper.class)) {
      classes.add(bean.getBeanClazz());
      LOG.info("{} registered as REST exception mapper", bean.getBeanClazz());
    }
  }

  protected void registerRestResources(Set<Class<?>> classes) {
    for (IBean<IRestResource> bean : BEANS.getBeanManager().getBeans(IRestResource.class)) {
      classes.add(bean.getBeanClazz());
      LOG.info("{} registered as REST resource on path=/{}", bean.getBeanClazz(), getContextPath(bean));
    }
  }

  /**
   * @return context path based on {@link Path} annotation.
   */
  protected String getContextPath(IBean<IRestResource> bean) {
    Path path = bean.getBeanAnnotation(Path.class);
    if (path != null) {
      return path.value();
    }
    return null;
  }

  @Override
  public Set<Class<?>> getClasses() {
    return m_classes;
  }
}
