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

import java.util.Set;

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

  private static IBean<?> s_fixtureResourceBean;

  @IgnoreBean
  public static class FixtureResource implements IRestResource {
  }

  @BeforeClass
  public static void beforeClass() {
    s_fixtureResourceBean = TestingUtility.registerBean(new BeanMetaData(FixtureResource.class));
  }

  @AfterClass
  public static void afterClass() {
    TestingUtility.unregisterBean(s_fixtureResourceBean);
  }

  @Test
  public void testGetClasses() {
    RestApplication app = new RestApplication();
    Set<Class<?>> classes = app.getClasses();
    assertTrue(classes.contains(DefaultExceptionMapper.class));
    assertTrue(classes.contains(VetoExceptionMapper.class));
    assertTrue(classes.contains(WebApplicationExceptionMapper.class));
    assertTrue(classes.contains(ObjectMapperResolver.class));
    assertTrue(classes.contains(FixtureResource.class));
  }

}
