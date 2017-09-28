/*******************************************************************************
 * Copyright (c) 2010-2016 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.shared.servicetunnel;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.LinkedHashSet;
import java.util.Set;

import org.eclipse.scout.rt.platform.IBean;
import org.eclipse.scout.rt.platform.Replace;
import org.eclipse.scout.rt.platform.SimpleBeanDecorationFactory;
import org.eclipse.scout.rt.platform.interceptor.IBeanDecorator;
import org.eclipse.scout.rt.platform.interceptor.IBeanInvocationContext;
import org.eclipse.scout.rt.platform.internal.BeanManagerImplementor;
import org.eclipse.scout.rt.platform.inventory.IClassInfo;
import org.eclipse.scout.rt.platform.inventory.IClassInventory;
import org.eclipse.scout.rt.platform.inventory.internal.JandexClassInventory;
import org.eclipse.scout.rt.platform.service.IService;
import org.eclipse.scout.rt.platform.util.Assertions.AssertionException;
import org.eclipse.scout.rt.shared.TunnelToServer;
import org.jboss.jandex.Indexer;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Test {@link TunnelToServer} with and without {@link Replace} annotations
 */
public class RegisterTunnelToServerPlatformListenerTest {

  private static JandexClassInventory s_classInventory;

  private RegisterTunnelToServerPlatformListener m_registrator;
  private BeanManagerImplementor m_beanManager;

  @BeforeClass
  public static void beforeClass() throws Exception {
    Indexer indexer = new Indexer();
    indexClass(indexer, IFixtureTunnelToServer.class);
    indexClass(indexer, IFixtureTunnelToServerEx1.class);
    indexClass(indexer, IFixtureTunnelToServerEx2.class);
    indexClass(indexer, IFixtureTunnelToServerEx3.class);
    indexClass(indexer, FixtureTunnelToServerEx3Impl.class);
    s_classInventory = new JandexClassInventory(indexer.complete());
  }

  @AfterClass
  public static void afterClass() {
    s_classInventory = null;
  }

  protected static void indexClass(Indexer indexer, Class<?> clazz) throws IOException {
    indexer.index(clazz.getResourceAsStream(RegisterTunnelToServerPlatformListenerTest.class.getSimpleName() + "$" + clazz.getSimpleName() + ".class"));
  }

  @Before
  public void before() {
    m_beanManager = new BeanManagerImplementor(new FixtureClientBeanDecorationFactory());
    m_registrator = new RegisterTunnelToServerPlatformListener();
  }

  @After
  public void after() {
    m_beanManager = null;
    m_registrator = null;
  }

  @Test
  public void testBase() {
    registerTunnelToServerBeans(IFixtureTunnelToServer.class);
    assertPing("return IFixtureTunnelToServer#ping", IFixtureTunnelToServer.class);
  }

  @Test
  public void testReplaceEx1() {
    registerTunnelToServerBeans(IFixtureTunnelToServer.class, IFixtureTunnelToServerEx1.class);
    assertPing("return IFixtureTunnelToServerEx1#ping", IFixtureTunnelToServerEx1.class);
  }

  @Test(expected = AssertionException.class)
  public void testNoInterface() {
    registerTunnelToServerBeans(FixtureTunnelToServerEx3Impl.class);
    assertPing("whatever", FixtureTunnelToServerEx3Impl.class);
  }

  @Test
  public void testNoInheritance() {
    registerTunnelToServerBeans(IFixtureTunnelToServerEx3.class, IFixtureTunnelToServer.class);
    assertPing("return IFixtureTunnelToServer#ping", IFixtureTunnelToServer.class);
  }

  @Test
  public void testReplaceEx1ReverseOrderRegistration() {
    registerTunnelToServerBeans(IFixtureTunnelToServerEx1.class, IFixtureTunnelToServer.class);
    assertPing("return IFixtureTunnelToServerEx1#ping", IFixtureTunnelToServerEx1.class);
  }

  @Test
  public void testReplaceEx2_1() {
    registerTunnelToServerBeans(IFixtureTunnelToServer.class, IFixtureTunnelToServerEx1.class, IFixtureTunnelToServerEx2.class);
    assertReplaceEx2();
  }

  @Test
  public void testReplaceEx2_2() {
    registerTunnelToServerBeans(IFixtureTunnelToServer.class, IFixtureTunnelToServerEx2.class, IFixtureTunnelToServerEx1.class);
    assertReplaceEx2();
  }

  @Test
  public void testReplaceEx2_3() {
    registerTunnelToServerBeans(IFixtureTunnelToServerEx1.class, IFixtureTunnelToServer.class, IFixtureTunnelToServerEx2.class);
    assertReplaceEx2();
  }

  @Test
  public void testReplaceEx2_4() {
    registerTunnelToServerBeans(IFixtureTunnelToServerEx1.class, IFixtureTunnelToServerEx2.class, IFixtureTunnelToServer.class);
    assertReplaceEx2();
  }

  @Test
  public void testReplaceEx2_5() {
    registerTunnelToServerBeans(IFixtureTunnelToServerEx2.class, IFixtureTunnelToServer.class, IFixtureTunnelToServerEx1.class);
    assertReplaceEx2();
  }

  @Test
  public void testReplaceEx2_6() {
    registerTunnelToServerBeans(IFixtureTunnelToServerEx2.class, IFixtureTunnelToServerEx1.class, IFixtureTunnelToServer.class);
    assertReplaceEx2();
  }

  private void assertReplaceEx2() {
    assertPing("return IFixtureTunnelToServerEx2#ping", IFixtureTunnelToServerEx2.class);
  }

  private void registerTunnelToServerBeans(Class<?>... classes) {
    Set<IClassInfo> classInfos = new LinkedHashSet<>();
    for (Class<?> c : classes) {
      classInfos.add(s_classInventory.getClassInfo(c));
    }
    IClassInventory classInventory = mock(IClassInventory.class);
    when(classInventory.getKnownAnnotatedTypes(TunnelToServer.class)).thenReturn(classInfos);
    m_registrator.registerTunnelToServerProxies(m_beanManager, classInventory);
  }

  private void assertPing(String expectedPingResult, Class<?>... queryClasses) {
    for (Class<?> queryClass : queryClasses) {
      IBean<?> bean = m_beanManager.getBean(queryClass);
      assertNotNull(bean);
      Object obj = bean.getInstance();
      assertNotNull(obj);
      assertTrue(IFixtureTunnelToServer.class.isInstance(obj));
      assertEquals(expectedPingResult, ((IFixtureTunnelToServer) obj).ping());
    }
  }

  private static final class FixtureClientBeanDecorationFactory extends SimpleBeanDecorationFactory {
    @Override
    public <T> IBeanDecorator<T> decorate(final IBean<T> bean, Class<? extends T> queryType) {
      return new IBeanDecorator<T>() {
        @Override
        public Object invoke(IBeanInvocationContext<T> context) {
          Method method = context.getTargetMethod();
          return "return " + bean.getBeanClazz().getSimpleName() + "#" + method.getName();
        }
      };
    }
  }

  @TunnelToServer
  public static interface IFixtureTunnelToServer extends IService {
    String ping();
  }

  @TunnelToServer
  public static interface IFixtureTunnelToServerEx1 extends IFixtureTunnelToServer {
  }

  @TunnelToServer
  public static interface IFixtureTunnelToServerEx2 extends IFixtureTunnelToServerEx1 {
  }

  public static interface IFixtureTunnelToServerEx3 extends IFixtureTunnelToServerEx2 {
  }

  public static class FixtureTunnelToServerEx3Impl implements IFixtureTunnelToServerEx3 {

    @Override
    public String ping() {
      return "pong";
    }
  }
}
