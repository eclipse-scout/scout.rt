package org.eclipse.scout.rt.client;

import java.lang.reflect.Method;

import org.eclipse.scout.commons.annotations.Replace;
import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.BeanMetaData;
import org.eclipse.scout.rt.platform.IBean;
import org.eclipse.scout.rt.platform.interceptor.IBeanDecorator;
import org.eclipse.scout.rt.platform.interceptor.IBeanInvocationContext;
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
    m_beanManager = new BeanManagerImplementor(new FixtureClientBeanDecorationFactory());
  }

  @After
  public void after() {
    m_beanManager = null;
  }

  @Test
  public void testBeanRegistration() {
    m_beanManager.registerBean(new BeanMetaData(IFixtureTunnelToServer.class).withApplicationScoped(true));
    IFixtureTunnelToServer obj = m_beanManager.getBean(IFixtureTunnelToServer.class).getInstance();
    Assert.assertNotNull(obj);
    Assert.assertEquals("return IFixtureTunnelToServer#ping", obj.ping());
  }

  @Test
  public void testReplace1WithoutTunnelAnnotation() {
    m_beanManager.registerBean(new BeanMetaData(IFixtureTunnelToServer.class).withApplicationScoped(true));
    m_beanManager.registerBean(new BeanMetaData(IFixtureTunnelToServerEx1.class).withApplicationScoped(true));
    IBean<IFixtureTunnelToServer> bean = m_beanManager.getBean(IFixtureTunnelToServer.class);
    Assert.assertNotNull(bean);
    IFixtureTunnelToServer obj = bean.getInstance();
    Assert.assertNull(obj);
  }

  @Test
  public void testReplace2WithTunnelAnnotation() {
    //add in reverse order to also test this
    m_beanManager.registerBean(new BeanMetaData(IFixtureTunnelToServerEx2.class).withApplicationScoped(true));
    m_beanManager.registerBean(new BeanMetaData(IFixtureTunnelToServerEx1.class).withApplicationScoped(true));
    m_beanManager.registerBean(new BeanMetaData(IFixtureTunnelToServer.class).withApplicationScoped(true));
    IBean<IFixtureTunnelToServer> bean = m_beanManager.getBean(IFixtureTunnelToServer.class);
    Assert.assertNotNull(bean);
    IFixtureTunnelToServer obj = bean.getInstance();
    Assert.assertNotNull(obj);
    Assert.assertEquals("return IFixtureTunnelToServerEx2#ping", obj.ping());

    IBean<IFixtureTunnelToServerEx1> bean1 = m_beanManager.getBean(IFixtureTunnelToServerEx1.class);
    Assert.assertNotNull(bean1);
    IFixtureTunnelToServerEx1 obj1 = bean1.getInstance();
    Assert.assertNotNull(obj1);
    Assert.assertEquals("return IFixtureTunnelToServerEx2#ping", obj1.ping());

    IBean<IFixtureTunnelToServerEx2> bean2 = m_beanManager.getBean(IFixtureTunnelToServerEx2.class);
    Assert.assertNotNull(bean2);
    IFixtureTunnelToServerEx2 obj2 = bean2.getInstance();
    Assert.assertNotNull(obj2);
    Assert.assertEquals("return IFixtureTunnelToServerEx2#ping", obj2.ping());
  }

  @Replace
  @ApplicationScoped
  private static final class FixtureClientBeanDecorationFactory extends ClientBeanDecorationFactory {
    @Override
    protected <T> IBeanDecorator<T> decorateWithTunnelToServer(final IBean<T> bean, final Class<? extends T> queryType) {
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
  private static interface IFixtureTunnelToServer extends IService {
    String ping();
  }

  @Replace
  //not set, annotations do not inherit on interfaces @TunnelToServer
  private static interface IFixtureTunnelToServerEx1 extends IFixtureTunnelToServer, IService {
    String ping1();
  }

  @Replace
  @TunnelToServer
  private static interface IFixtureTunnelToServerEx2 extends IFixtureTunnelToServerEx1, IService {
    String ping3();
  }
}
