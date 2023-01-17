/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.platform.interceptor;

import java.io.Serializable;
import java.lang.reflect.Proxy;
import java.util.concurrent.Callable;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author Matthias Villiger
 */
public class DecoratingProxyTest {

  private static Callable<ITestBean> m_targetInstanceProvider;
  private static Callable<ITestBean> m_nullInstanceProvider;
  private static IInstanceInvocationHandler<ITestBean> m_handler;

  @BeforeClass
  public static void setup() {
    final P_TestBean testBean = new P_TestBean();
    m_targetInstanceProvider = () -> testBean;
    m_nullInstanceProvider = () -> null;
    m_handler = (i, method, args) -> {
      if (i == null) {
        return null;
      }
      return method.invoke(i, args);
    };
  }

  @Test
  public void testProxyEqualityWithInstance() throws Exception {
    testEqualityWith(m_targetInstanceProvider);
  }

  @Test
  public void testProxyEqualityWithoutInstance() throws Exception {
    testEqualityWith(m_nullInstanceProvider); // e.g. client side tunnelToServer proxies have not real instance available
  }

  @Test
  public void testProxyEqualityWithMixedInstance() {
    ITestBean proxy1 = DecoratingProxy.newInstance(m_handler, m_targetInstanceProvider, ITestBean.class).getProxy();
    ITestBean proxy2 = DecoratingProxy.newInstance(m_handler, m_nullInstanceProvider, ITestBean.class).getProxy();

    Assert.assertFalse(proxy1.equals(proxy2));
    Assert.assertFalse(proxy2.equals(proxy1));
  }

  @Test
  public void testProxyHashCodeAndToStringWithInstance() throws Exception {
    testHashCodeAndToStringWith(m_targetInstanceProvider);
  }

  @Test
  public void testProxyHashCodeAndToStringWithoutInstance() throws Exception {
    testHashCodeAndToStringWith(m_nullInstanceProvider);
  }

  private static void testHashCodeAndToStringWith(Callable<ITestBean> provider) throws Exception {
    ITestBean instance = provider.call();
    ITestBean proxy1 = DecoratingProxy.newInstance(m_handler, provider, ITestBean.class).getProxy();
    ITestBean proxy2 = DecoratingProxy.newInstance(m_handler, provider, ITestBean.class).getProxy();
    Assert.assertEquals(proxy1.hashCode(), proxy2.hashCode());
    Assert.assertEquals(proxy1.toString(), proxy2.toString());
    if (instance != null) {
      Assert.assertEquals("{proxy} " + P_TestBean.TO_STRING_VAL, proxy1.toString());
      Assert.assertEquals(P_TestBean.RESULT_VAL, proxy1.getResult());
    }
  }

  private static void testEqualityWith(Callable<ITestBean> provider) throws Exception {
    ITestBean instance = provider.call();
    ITestBean proxy1 = DecoratingProxy.newInstance(m_handler, provider, ITestBean.class).getProxy();
    ITestBean proxy2 = DecoratingProxy.newInstance(m_handler, provider, ITestBean.class).getProxy();

    Object otherProxy = Proxy.newProxyInstance(DecoratingProxyTest.class.getClassLoader(), new Class[]{ITestBean.class}, (proxy, method, args) -> null);

    Assert.assertTrue(proxy1.equals(proxy1));
    Assert.assertTrue(proxy2.equals(proxy2));

    Assert.assertTrue(proxy1.equals(proxy2));
    Assert.assertFalse(proxy1.equals(null));
    Assert.assertFalse(proxy1.equals(otherProxy));
    Assert.assertEquals(instance != null, proxy1.equals(instance));

    ITestBean beanProxyWithDifferentInterfaces = DecoratingProxy.newInstance(m_handler, provider, ITestBean.class, Serializable.class).getProxy();
    Assert.assertEquals(instance != null, proxy1.equals(beanProxyWithDifferentInterfaces));
  }

  private interface ITestBean {
    int getResult();
  }

  private static final class P_TestBean implements ITestBean {
    private static final String TO_STRING_VAL = "tostring";
    private static final int RESULT_VAL = 11;

    @Override
    public String toString() {
      return TO_STRING_VAL;
    }

    @Override
    public int getResult() {
      return RESULT_VAL;
    }
  }
}
