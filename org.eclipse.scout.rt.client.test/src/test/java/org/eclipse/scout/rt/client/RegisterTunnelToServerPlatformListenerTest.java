package org.eclipse.scout.rt.client;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.LinkedHashSet;
import java.util.Set;

import org.eclipse.scout.commons.annotations.Replace;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.platform.IBean;
import org.eclipse.scout.rt.platform.interceptor.IBeanInterceptor;
import org.eclipse.scout.rt.platform.interceptor.IBeanInvocationContext;
import org.eclipse.scout.rt.platform.internal.BeanManagerImplementor;
import org.eclipse.scout.rt.platform.inventory.IClassInfo;
import org.eclipse.scout.rt.platform.inventory.IClassInventory;
import org.eclipse.scout.rt.platform.inventory.internal.JandexClassInventory;
import org.eclipse.scout.rt.platform.service.IService;
import org.eclipse.scout.rt.shared.TunnelToServer;
import org.jboss.jandex.Indexer;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Test {@link TunnelToServer} with and without {@link Replace} annotations
 */
public class RegisterTunnelToServerPlatformListenerTest {

  private static JandexClassInventory s_classInventory;

  private BeanManagerImplementor m_beanManager;
  private RegisterTunnelToServerPlatformListener m_registrator;

  @BeforeClass
  public static void beforeClass() throws Exception {
    Indexer indexer = new Indexer();
    indexClass(indexer, IFixtureTunnelToServer.class);
    indexClass(indexer, IFixtureTunnelToServerEx1.class);
    indexClass(indexer, IFixtureTunnelToServerEx2.class);
    s_classInventory = new JandexClassInventory(indexer.complete());
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
  }

  @Test
  public void testBase() {
    registerTunnelToServerBeans(IFixtureTunnelToServer.class);
    assertPings("return IFixtureTunnelToServer#ping", IFixtureTunnelToServer.class);
  }

  @Test
  public void testReplaceEx1() {
    registerTunnelToServerBeans(IFixtureTunnelToServer.class, IFixtureTunnelToServerEx1.class);
    assertPings("return IFixtureTunnelToServerEx1#ping", IFixtureTunnelToServer.class, IFixtureTunnelToServerEx1.class);
  }

  @Test
  public void testReplaceEx1ReverseOrderRegistration() {
    registerTunnelToServerBeans(IFixtureTunnelToServerEx1.class, IFixtureTunnelToServer.class);
    assertPings("return IFixtureTunnelToServerEx1#ping", IFixtureTunnelToServer.class, IFixtureTunnelToServerEx1.class);
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
    assertPings("return IFixtureTunnelToServerEx2#ping", IFixtureTunnelToServer.class, IFixtureTunnelToServerEx1.class, IFixtureTunnelToServerEx2.class);
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

  private void assertPings(String expectedPingResult, Class<?>... queryClasses) {
    for (Class<?> queryClass : queryClasses) {
      @SuppressWarnings("unchecked")
      Class<? extends IFixtureTunnelToServer> clazz = (Class<? extends IFixtureTunnelToServer>) queryClass;
      assertOnePing(expectedPingResult, clazz);
    }
  }

  private <T extends IFixtureTunnelToServer> void assertOnePing(String expectedPingResult, Class<T> queryClass) {
    assertTrue(IFixtureTunnelToServer.class.isAssignableFrom(queryClass));
    IBean<T> bean = m_beanManager.getBean(queryClass);
    assertNotNull(bean);
    T obj = bean.getInstance(queryClass);
    assertNotNull(obj);
    assertEquals(expectedPingResult, obj.ping());
  }

  private static final class FixtureClientBeanDecorationFactory extends ClientBeanDecorationFactory {
    @Override
    protected <T> IBeanInterceptor<T> decorateWithTunnelToServer(final IBean<T> bean, final Class<T> queryType) {
      return new IBeanInterceptor<T>() {
        @Override
        public Object invoke(IBeanInvocationContext<T> context) throws ProcessingException {
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

  @Replace
  @TunnelToServer
  public static interface IFixtureTunnelToServerEx1 extends IFixtureTunnelToServer {
  }

  @Replace
  @TunnelToServer
  public static interface IFixtureTunnelToServerEx2 extends IFixtureTunnelToServerEx1 {
  }
}
