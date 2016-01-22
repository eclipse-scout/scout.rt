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
package org.eclipse.scout.rt.client;

import java.lang.reflect.Method;

import org.eclipse.scout.rt.platform.BeanMetaData;
import org.eclipse.scout.rt.platform.Replace;
import org.eclipse.scout.rt.platform.internal.BeanManagerImplementor;
import org.eclipse.scout.rt.platform.service.IService;
import org.eclipse.scout.rt.shared.TunnelToServer;
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
    m_beanManager.registerBean(new BeanMetaData(IFixtureTunnelToServer.class).withApplicationScoped(true).withProducer(new TestingTunnelToServerProxyProducer<>(IFixtureTunnelToServer.class)));
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

  private static final class TestingTunnelToServerProxyProducer<T> extends TunnelToServerProxyProducer<T> {
    /**
     * @param interfaceClass
     */
    public TestingTunnelToServerProxyProducer(Class<?> interfaceClass) {
      super(interfaceClass);
    }

    @Override
    public Object invoke(T instance, Method method, Object[] args) throws Throwable {
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
