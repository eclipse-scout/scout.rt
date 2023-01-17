/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client;

import java.lang.reflect.Method;

import org.eclipse.scout.rt.platform.BeanMetaData;
import org.eclipse.scout.rt.platform.Replace;
import org.eclipse.scout.rt.platform.internal.BeanManagerImplementor;
import org.eclipse.scout.rt.platform.service.IService;
import org.eclipse.scout.rt.shared.TunnelToServer;
import org.eclipse.scout.rt.shared.servicetunnel.ServiceTunnelProxyProducer;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Test {@link TunnelToServer} with and without {@link Replace} annotations
 */
public class TunnelToServerTest {
  private BeanManagerImplementor m_beanManager;

  @Before
  public void before() {
    m_beanManager = new BeanManagerImplementor();
  }

  @After
  public void after() {
    m_beanManager = null;
  }

  @Test
  public void testBeanProxy() {
    m_beanManager.registerBean(new BeanMetaData(IFixtureTunnelToServer.class).withApplicationScoped(true).withProducer(new TestingServiceTunnelProxyProducer<>(IFixtureTunnelToServer.class)));
    IFixtureTunnelToServer obj1 = m_beanManager.getBean(IFixtureTunnelToServer.class).getInstance();
    IFixtureTunnelToServer obj2 = m_beanManager.getBean(IFixtureTunnelToServer.class).getInstance();
    m_beanManager.registerBean(new BeanMetaData(IFixtureTunnelToServerEx1.class).withApplicationScoped(true));
    IFixtureTunnelToServerEx1 obj3 = m_beanManager.getBean(IFixtureTunnelToServerEx1.class).getInstance();

    Assert.assertNull(obj3);
    Assert.assertNotNull(obj1);
    Assert.assertSame(obj1, obj2);
    Assert.assertTrue(java.lang.reflect.Proxy.isProxyClass(obj1.getClass()));
    Assert.assertEquals("return IFixtureTunnelToServer#ping", obj1.ping());
  }

  private static final class TestingServiceTunnelProxyProducer<T> extends ServiceTunnelProxyProducer<T> {
    /**
     * @param interfaceClass
     */
    public TestingServiceTunnelProxyProducer(Class<?> interfaceClass) {
      super(interfaceClass);
    }

    @Override
    public Object invoke(T instance, Method method, Object[] args) {
      return "return " + getInterfaceClass().getSimpleName() + "#" + method.getName();
    }
  }

  private static interface IFixtureTunnelToServer extends IService {
    String ping();
  }

  private static interface IFixtureTunnelToServerEx1 extends IFixtureTunnelToServer, IService {
    String ping1();
  }
}
