package org.eclipse.scout.rt.platform.interceptor.internal;

import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import org.eclipse.scout.rt.platform.BeanMetaData;
import org.eclipse.scout.rt.platform.internal.BeanImplementor;
import org.junit.Assert;
import org.junit.Test;

/**
 * <h3>{@link BeanProxyTest}</h3>
 *
 * @author Matthias Villiger
 */
public class BeanProxyTest {

  @Test
  public void testProxyEqualityWithInstance() {
    testWith(new P_TestBean());
  }

  @Test
  public void testProxyEqualityWithoutInstance() {
    testWith(null); // e.g. client side tunnelToServer proxies have not real instance available
  }

  private void testWith(P_TestBean instance) {
    BeanImplementor<?> bean = new BeanImplementor<>(new BeanMetaData(ITestBean.class).withApplicationScoped(true).withInitialInstance(instance));
    BeanProxyImplementor<?> beanProxy1 = new BeanProxyImplementor<>(bean, null, ITestBean.class);
    BeanProxyImplementor<?> beanProxy2 = new BeanProxyImplementor<>(bean, null, ITestBean.class);
    Object proxy1 = beanProxy1.getProxy();
    Object proxy2 = beanProxy2.getProxy();

    Object otherProxy = Proxy.newProxyInstance(BeanProxyTest.class.getClassLoader(), new Class[]{ITestBean.class}, new InvocationHandler() {
      @Override
      public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        return null;
      }
    });

    Assert.assertTrue(proxy1.equals(proxy1));
    Assert.assertTrue(proxy2.equals(proxy2));

    Assert.assertTrue(proxy1.equals(proxy2));
    Assert.assertFalse(proxy1.equals(null));
    Assert.assertFalse(proxy1.equals(otherProxy));
    if (instance != null) {
      Assert.assertTrue(proxy1.equals(instance));
    }

    Object beanProxyWithDifferentInterfaces = new BeanProxyImplementor<>(bean, null, ITestBean.class, Serializable.class).getProxy();
    Assert.assertEquals(instance != null, proxy1.equals(beanProxyWithDifferentInterfaces));
  }

  private interface ITestBean {
  }

  private static final class P_TestBean implements ITestBean {
  }
}
