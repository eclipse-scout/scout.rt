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

import static org.junit.Assert.*;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ContextResolver;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.ParamConverter;
import jakarta.ws.rs.ext.ParamConverterProvider;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.BeanMetaData;
import org.eclipse.scout.rt.platform.IBean;
import org.eclipse.scout.rt.platform.IgnoreBean;
import org.eclipse.scout.rt.rest.RestApplication.IRestApplicationClassesContributor;
import org.eclipse.scout.rt.rest.RestApplication.IRestApplicationPropertiesContributor;
import org.eclipse.scout.rt.rest.RestApplication.IRestApplicationSingletonsContributor;
import org.eclipse.scout.rt.rest.container.AntiCsrfContainerFilter;
import org.eclipse.scout.rt.rest.container.IRestContainerRequestFilter;
import org.eclipse.scout.rt.rest.container.IRestContainerResponseFilter;
import org.eclipse.scout.rt.rest.container.PathValidationFilter;
import org.eclipse.scout.rt.rest.exception.DefaultExceptionMapper;
import org.eclipse.scout.rt.rest.exception.VetoExceptionMapper;
import org.eclipse.scout.rt.rest.exception.WebApplicationExceptionMapper;
import org.eclipse.scout.rt.testing.platform.BeanTestingHelper;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Test for {@link RestApplication}
 */
public class RestApplicationTest {

  private static List<IBean<?>> s_registeredBeans = new ArrayList<>();

  public static class FixtureContextResolver implements ContextResolver<Object> {
    @Override
    public Object getContext(Class<?> type) {
      return null;
    }
  }

  public static class FixtureExceptionMapper implements ExceptionMapper<Exception> {
    @Override
    public Response toResponse(Exception exception) {
      return null;
    }
  }

  public static class FixtureParamConverterProvider implements ParamConverterProvider {
    @Override
    public <T> ParamConverter<T> getConverter(Class<T> rawType, Type genericType, Annotation[] annotations) {
      return null;
    }
  }

  @IgnoreBean
  public static class FixtureRestContainerRequestFilter implements IRestContainerRequestFilter {
    @Override
    public void filter(ContainerRequestContext requestContext) {
    }
  }

  @IgnoreBean
  public static class FixtureRestContainerResponseFilter implements IRestContainerResponseFilter {
    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) {
    }
  }

  @IgnoreBean
  public static class FixtureResource implements IRestResource {
  }

  @IgnoreBean
  public static class FixtureClassesContributor implements IRestApplicationClassesContributor {

    protected static final Class<?> CLASS = Object.class;

    @Override
    public Set<Class<?>> contribute() {
      return Collections.singleton(CLASS);
    }
  }

  @IgnoreBean
  public static class FixtureSingletonsContributor implements IRestApplicationSingletonsContributor {
    protected static final Object OBJECT = new Object();

    @Override
    public Set<Object> contribute() {
      return Collections.singleton(OBJECT);
    }
  }

  @IgnoreBean
  public static class FixturePropertiesContributor implements IRestApplicationPropertiesContributor {

    protected static final String KEY = "mocked-properties-key";
    protected static final Object VALUE = new Object();

    @Override
    public Map<String, Object> contribute() {
      return Collections.singletonMap(KEY, VALUE);
    }
  }

  @IgnoreBean
  public static class FixturePropertiesExContributor implements IRestApplicationPropertiesContributor {

    // provide a property with same key as FixturePropertiesContributor
    protected static final String KEY = FixturePropertiesContributor.KEY;
    protected static final Object VALUE = new Object();

    protected static final String KEY_2 = "mocked-properties-key-2";
    protected static final Object VALUE_2 = new Object();

    @Override
    public Map<String, Object> contribute() {
      Map<String, Object> properties = new HashMap<>();
      properties.put(KEY, VALUE);
      properties.put(KEY_2, VALUE_2);
      return properties;
    }
  }

  @BeforeClass
  public static void beforeClass() {
    s_registeredBeans.add(BEANS.get(BeanTestingHelper.class).registerBean(new BeanMetaData(FixtureContextResolver.class)));
    s_registeredBeans.add(BEANS.get(BeanTestingHelper.class).registerBean(new BeanMetaData(FixtureExceptionMapper.class)));
    s_registeredBeans.add(BEANS.get(BeanTestingHelper.class).registerBean(new BeanMetaData(FixtureParamConverterProvider.class)));
    s_registeredBeans.add(BEANS.get(BeanTestingHelper.class).registerBean(new BeanMetaData(FixtureRestContainerRequestFilter.class)));
    s_registeredBeans.add(BEANS.get(BeanTestingHelper.class).registerBean(new BeanMetaData(FixtureRestContainerResponseFilter.class)));
    s_registeredBeans.add(BEANS.get(BeanTestingHelper.class).registerBean(new BeanMetaData(FixtureResource.class)));

    s_registeredBeans.add(BEANS.get(BeanTestingHelper.class).registerBean(new BeanMetaData(FixtureClassesContributor.class)));
    s_registeredBeans.add(BEANS.get(BeanTestingHelper.class).registerBean(new BeanMetaData(FixtureSingletonsContributor.class)));
    s_registeredBeans.add(BEANS.get(BeanTestingHelper.class).registerBean(new BeanMetaData(FixturePropertiesContributor.class).withOrder(1000)));
    s_registeredBeans.add(BEANS.get(BeanTestingHelper.class).registerBean(new BeanMetaData(FixturePropertiesExContributor.class).withOrder(2000)));
  }

  @AfterClass
  public static void afterClass() {
    BEANS.get(BeanTestingHelper.class).unregisterBeans(s_registeredBeans);
  }

  @Test
  public void testGetClasses() {
    RestApplication app = new RestApplication();
    Set<Class<?>> classes = app.getClasses();
    assertTrue(classes.contains(FixtureContextResolver.class));
    assertTrue(classes.contains(FixtureExceptionMapper.class));
    assertTrue(classes.contains(FixtureParamConverterProvider.class));
    assertTrue(classes.contains(FixtureRestContainerRequestFilter.class));
    assertTrue(classes.contains(FixtureRestContainerResponseFilter.class));
    assertTrue(classes.contains(FixtureResource.class));
    assertTrue(classes.contains(FixtureClassesContributor.CLASS));

    // default scout exception mappers
    assertTrue(classes.contains(DefaultExceptionMapper.class));
    assertTrue(classes.contains(VetoExceptionMapper.class));
    assertTrue(classes.contains(WebApplicationExceptionMapper.class));
    assertTrue(classes.contains(AntiCsrfContainerFilter.class));
    assertTrue(classes.contains(PathValidationFilter.class));

    Set<Object> singletons = app.getSingletons();
    assertTrue(singletons.contains(FixtureSingletonsContributor.OBJECT));

    Map<String, Object> properties = app.getProperties();
    assertEquals(FixturePropertiesContributor.VALUE, properties.get(FixturePropertiesContributor.KEY));
    assertEquals(FixturePropertiesContributor.VALUE, properties.get(FixturePropertiesExContributor.KEY));
    assertEquals(FixturePropertiesExContributor.VALUE_2, properties.get(FixturePropertiesExContributor.KEY_2));
  }
}
