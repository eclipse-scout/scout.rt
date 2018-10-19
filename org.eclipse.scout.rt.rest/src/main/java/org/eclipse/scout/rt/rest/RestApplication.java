/*
 * Copyright (c) BSI Business Systems Integration AG. All rights reserved.
 * http://www.bsiag.com/
 */
package org.eclipse.scout.rt.rest;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.ws.rs.core.Application;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.ParamConverterProvider;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.IBean;
import org.eclipse.scout.rt.rest.container.IRestContainerRequestFilter;
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
    registerParamConverterProviders(classes);
    registerRestResources(classes);
    registerContainerRequestFilters(classes);
    return classes;
  }

  protected void registerContainerRequestFilters(Set<Class<?>> classes) {
    registerClasses(classes, IRestContainerRequestFilter.class);
  }

  protected void registerObjectMapperResolver(Set<Class<?>> classes) {
    registerClasses(classes, ObjectMapperResolver.class);
  }

  protected void registerExceptionMappers(Set<Class<?>> classes) {
    registerClasses(classes, ExceptionMapper.class);
  }

  protected void registerRestResources(Set<Class<?>> classes) {
    registerClasses(classes, IRestResource.class);
  }

  protected void registerParamConverterProviders(Set<Class<?>> classes) {
    registerClasses(classes, ParamConverterProvider.class);
  }

  protected <T> void registerClasses(Set<Class<?>> classes, Class<T> lookupBeanClazz) {
    List<IBean<T>> beans = BEANS.getBeanManager().getBeans(lookupBeanClazz);
    for (IBean<?> bean : beans) {
      classes.add(bean.getBeanClazz());
      LOG.debug("{} registered as REST {}", bean.getBeanClazz(), lookupBeanClazz.getSimpleName());
    }
    LOG.info("Registered {} classes as REST {}", beans.size(), lookupBeanClazz.getSimpleName());
  }

  @Override
  public Set<Class<?>> getClasses() {
    return m_classes;
  }
}
