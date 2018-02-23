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

import static org.junit.Assert.assertTrue;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.ParamConverter;
import javax.ws.rs.ext.ParamConverterProvider;

import org.eclipse.scout.rt.platform.BeanMetaData;
import org.eclipse.scout.rt.platform.IBean;
import org.eclipse.scout.rt.platform.IgnoreBean;
import org.eclipse.scout.rt.rest.exception.DefaultExceptionMapper;
import org.eclipse.scout.rt.rest.exception.VetoExceptionMapper;
import org.eclipse.scout.rt.rest.exception.WebApplicationExceptionMapper;
import org.eclipse.scout.rt.testing.shared.TestingUtility;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Test for {@link RestApplication}
 */
public class RestApplicationTest {

  private static List<IBean<?>> s_registeredBeans = new ArrayList<>();

  @IgnoreBean
  public static class FixtureResource implements IRestResource {
  }

  @IgnoreBean
  public static class FixtureContextResolver implements ContextResolver<Object> {
    @Override
    public Object getContext(Class<?> type) {
      return null;
    }
  }

  @IgnoreBean
  public static class FixtureParamConverterProvider implements ParamConverterProvider {
    @Override
    public <T> ParamConverter<T> getConverter(Class<T> rawType, Type genericType, Annotation[] annotations) {
      return null;
    }
  }

  @BeforeClass
  public static void beforeClass() {
    s_registeredBeans.add(TestingUtility.registerBean(new BeanMetaData(FixtureResource.class)));
    s_registeredBeans.add(TestingUtility.registerBean(new BeanMetaData(FixtureContextResolver.class)));
    s_registeredBeans.add(TestingUtility.registerBean(new BeanMetaData(FixtureParamConverterProvider.class)));
  }

  @AfterClass
  public static void afterClass() {
    TestingUtility.unregisterBeans(s_registeredBeans);
  }

  @Test
  public void testGetClasses() {
    RestApplication app = new RestApplication();
    Set<Class<?>> classes = app.getClasses();
    assertTrue(classes.contains(DefaultExceptionMapper.class));
    assertTrue(classes.contains(VetoExceptionMapper.class));
    assertTrue(classes.contains(WebApplicationExceptionMapper.class));
    assertTrue(classes.contains(FixtureContextResolver.class));
    assertTrue(classes.contains(FixtureResource.class));
    assertTrue(classes.contains(FixtureParamConverterProvider.class));
  }
}
