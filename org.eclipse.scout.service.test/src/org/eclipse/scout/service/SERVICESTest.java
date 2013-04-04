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

import java.util.Dictionary;
import java.util.Hashtable;

import org.eclipse.scout.service.internal.Activator;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceRegistration;

/**
 * JUnit tests for {@link SERVICES}
 * 
 * @since 3.9.0
 */
public class SERVICESTest {
  private ServiceRegistration m_testReg1;
  private ServiceRegistration m_testReg2;
  private ServiceRegistration m_testReg3;

  /**
   * Registers test services with different rankings.
   */
  @Before
  public void registerTestServices() {
    m_testReg1 = registerService(ITestService.class.getName(), new TestService1(), 1);
    m_testReg2 = registerService(ITestService.class.getName(), new TestService2(), 2);
    m_testReg3 = registerService(ITestService.class.getName(), new TestService3(), 3);
  }

  private ServiceRegistration registerService(String clazz, Object service, int ranking) {
    BundleContext bundleContext = Activator.getDefault().getBundle().getBundleContext();
    Dictionary<String, Object> map = new Hashtable<String, Object>();
    map.put(Constants.SERVICE_RANKING, ranking);
    return bundleContext.registerService(clazz, service, map);
  }

  /**
   * Unregisters test services
   */
  @After
  public void unRegisterTestServices() {
    m_testReg1.unregister();
    m_testReg2.unregister();
    m_testReg3.unregister();
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
    ITestService[] services = SERVICES.getServices(ITestService.class);
    assertEquals("3 services should be registered ", 3, services.length);
    assertEquals("Services should be ordered ", TestService3.class, services[0].getClass());
    assertEquals("Services should be ordered ", TestService2.class, services[1].getClass());
    assertEquals("Services should be ordered ", TestService1.class, services[2].getClass());
  }

  /**
   * Test for {@link SERVICES#getService(Class)} for a service that was not registered.
   */
  @Test
  public void testGetServiceNull() {
    IUnregisteredTestService service = SERVICES.getService(IUnregisteredTestService.class);
    assertNull("No service should be found. ", service);
  }

  /**
   * Test for {@link SERVICES#getServices(Class)} for a service that was not registered.
   */
  @Test
  public void testGetServicesNull() {
    IUnregisteredTestService[] services = SERVICES.getServices(IUnregisteredTestService.class);
    assertEquals("No services should be found. ", 0, services.length);
  }

  /**
   * Test for {@link SERVICES#getServices(Class)} with a filter
   */
  @Test
  public void testGetServicesWithFilter() {
    ITestService[] services = SERVICES.getServices(ITestService.class, "(" + Constants.SERVICE_RANKING + "=2)");
    assertEquals("Services should be filtered by service ranking ", TestService2.class, services[0].getClass());
    assertEquals("Service count should be 1 (filtered)", 1, services.length);
  }

  /* Test data*/

  /**
   * Service registered with ranking 1
   */
  class TestService1 implements ITestService {
  }

  /**
   * Service registered with ranking 2
   */
  class TestService2 implements ITestService {
  }

  /**
   * Service registered with ranking 3
   */
  class TestService3 implements ITestService {
  }

  /**
   * Test service interface with services registered
   */
  interface ITestService {
  }

  /**
   * Test service interface with no services registered
   */
  interface IUnregisteredTestService {
  }

}
