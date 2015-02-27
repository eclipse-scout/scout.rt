/*******************************************************************************
 * Copyright (c) 2013 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.List;

import org.eclipse.scout.commons.Assertions.AssertionException;
import org.eclipse.scout.rt.platform.cdi.Bean;
import org.eclipse.scout.rt.platform.cdi.DynamicAnnotations;
import org.eclipse.scout.rt.platform.cdi.IBean;
import org.eclipse.scout.rt.platform.cdi.OBJ;
import org.eclipse.scout.rt.testing.platform.ScoutPlatformTestRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * JUnit tests for {@link SERVICES}
 *
 * @since 3.9.0
 */
@RunWith(ScoutPlatformTestRunner.class)
public class SERVICESTest {
  private IBean<?> m_ref1;
  private IBean<?> m_ref2;
  private IBean<?> m_ref3;

  /**
   * Registers test services with different rankings.
   */
  @Before
  public void registerTestServices() {
    m_ref1 = registerService(TestService1.class, 1);
    m_ref2 = registerService(TestService2.class, 2);
    m_ref3 = registerService(TestService3.class, 3);
  }

  private IBean<?> registerService(Class<? extends ITestService> serviceClazz, float priority) {

    Bean<? extends ITestService> bean = new Bean<ITestService>(serviceClazz);
    bean.addAnnotation(DynamicAnnotations.createPriority(priority));
    OBJ.registerBean(bean);
    return bean;
  }

  /**
   * Unregisters test services
   */
  @After
  public void unRegisterTestServices() {
    OBJ.unregisterBean(m_ref1);
    OBJ.unregisterBean(m_ref2);
    OBJ.unregisterBean(m_ref3);
  }

  /**
   * Test for {@link SERVICES#getService(Class)}.
   */
  @Test
  public void testGetServiceHighestRanking() {
    ITestService service = SERVICES.getService(ITestService.class);
    assertEquals("Service with highest ranking expected.", TestService3.class, service.getClass());
  }

  /**
   * Test for {@link SERVICES#getServices(Class)}. The received services should be ordered by ranking.
   */
  @Test
  public void testGetServicesOrderedByRanking() {
    List<ITestService> services = SERVICES.getServices(ITestService.class);
    assertEquals("3 services should be registered ", 3, services.size());
    assertEquals("Services should be ordered ", TestService3.class, services.get(0).getClass());
    assertEquals("Services should be ordered ", TestService2.class, services.get(1).getClass());
    assertEquals("Services should be ordered ", TestService1.class, services.get(2).getClass());
  }

  /**
   * Test for {@link SERVICES#getService(Class)} for a service that was not registered.
   */
  @Test(expected = AssertionException.class)
  public void testGetServiceNull() {
    IUnregisteredTestService service = SERVICES.getService(IUnregisteredTestService.class);
    assertNull("No service should be found. ", service);
  }

  /**
   * Test for {@link SERVICES#getServices(Class)} for a service that was not registered.
   */
  @Test
  public void testGetServicesNull() {
    List<IUnregisteredTestService> services = SERVICES.getServices(IUnregisteredTestService.class);
    assertEquals("No services should be found. ", 0, services.size());
  }

  /* Test data*/

  /**
   * Service registered with ranking 1
   */
  private static class TestService1 implements ITestService {
  }

  /**
   * Service registered with ranking 2
   */
  private static class TestService2 implements ITestService {
  }

  /**
   * Service registered with ranking 3
   */
  private static class TestService3 implements ITestService {
  }

  /**
   * Test service interface with services registered
   */
  private static interface ITestService {
  }

  /**
   * Test service interface with no services registered
   */
  private static interface IUnregisteredTestService {
  }

}
