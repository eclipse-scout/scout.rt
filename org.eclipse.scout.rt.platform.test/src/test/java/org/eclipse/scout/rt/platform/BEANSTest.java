/*******************************************************************************
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.platform;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.List;

import org.eclipse.scout.rt.platform.service.IService;
import org.eclipse.scout.rt.platform.util.Assertions.AssertionException;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * JUnit tests for {@link SERVICES}
 *
 * @since 3.9.0
 */
@RunWith(PlatformTestRunner.class)
public class BEANSTest {
  private IBean<?> m_ref1;
  private IBean<?> m_ref2;
  private IBean<?> m_ref3;
  private IBean<?> m_ref4;
  private IBean<?> m_ref5;

  /**
   * Registers test services with different rankings.
   */
  @Before
  public void registerTestServices() {
    m_ref1 = registerBean(TestService1.class, 2);
    m_ref2 = registerBean(TestService2.class, 1);
    m_ref3 = registerBean(TestService3.class, 0);
    m_ref4 = registerBean(Service1BelowAbstractTestService.class, -3);
    m_ref5 = registerBean(Service2BelowAbstractTestService.class, -2);
  }

  private IBean<?> registerBean(Class<?> serviceClazz, double order) {
    BeanMetaData bean = new BeanMetaData(serviceClazz);
    bean.withAnnotation(AnnotationFactory.createOrder(order));
    return Platform.get().getBeanManager().registerBean(bean);
  }

  /**
   * Unregisters test services
   */
  @After
  public void unRegisterTestServices() {
    Platform.get().getBeanManager().unregisterBean(m_ref1);
    Platform.get().getBeanManager().unregisterBean(m_ref2);
    Platform.get().getBeanManager().unregisterBean(m_ref3);
    Platform.get().getBeanManager().unregisterBean(m_ref4);
    Platform.get().getBeanManager().unregisterBean(m_ref5);
  }

  /**
   * Test for {@link SERVICES#getService(Class)}.
   */
  @Test
  public void testGetServiceHighestRanking() {
    ITestService service = BEANS.get(ITestService.class);
    assertEquals("Service with highest ranking expected.", TestService3.class, service.getClass());
  }

  /**
   * Test for {@link SERVICES#getServices(Class)}. The received services should be ordered by ranking.
   */
  @Test
  public void testGetServicesOrderedByRanking() {
    List<ITestService> services = BEANS.all(ITestService.class);
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
    IUnregisteredTestService service = BEANS.get(IUnregisteredTestService.class);
    assertNull("No service should be found. ", service);
  }

  /**
   * Test for {@link SERVICES#getServices(Class)} for a service that was not registered.
   */
  @Test
  public void testGetServicesNull() {
    List<IUnregisteredTestService> services = BEANS.all(IUnregisteredTestService.class);
    assertEquals("No services should be found. ", 0, services.size());
  }

  @Test
  public void testAbstractService() {
    List<AbstractTestService> all = BEANS.all(AbstractTestService.class);
    assertEquals(2, all.size());
    assertEquals(Service1BelowAbstractTestService.class, all.get(0).getClass());
    assertEquals(Service2BelowAbstractTestService.class, all.get(1).getClass());
    assertEquals(Service1BelowAbstractTestService.class, BEANS.get(AbstractTestService.class).getClass());
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

  public interface ITestService2 extends IService {
  }

  @ApplicationScoped
  public class TestService implements ITestService2 {
  }

  private static abstract class AbstractTestService {
  }

  private static class Service1BelowAbstractTestService extends AbstractTestService {
  }

  private static class Service2BelowAbstractTestService extends AbstractTestService {
  }
}
