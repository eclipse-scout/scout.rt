/*******************************************************************************
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.platform;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(PlatformTestRunner.class)
public class InjectInstancesTest {

  private static IBean<Bean01> m_bean01;
  private static IBean<Bean02> m_bean02;
  private static IBean<MyBean01> m_bean03;
  private static IBean<MyBean02> m_bean04;

  @BeforeClass
  public static void registerBeans() {
    m_bean01 = Platform.get().getBeanManager().registerClass(Bean01.class);
    m_bean02 = Platform.get().getBeanManager().registerClass(Bean02.class);
    m_bean03 = Platform.get().getBeanManager().registerClass(MyBean01.class);
    m_bean04 = Platform.get().getBeanManager().registerClass(MyBean02.class);

  }

  @Test
  public void testFieldInstanceInjection() {
    MyBean01 myBean = BEANS.get(MyBean01.class);
    Assert.assertNotNull(myBean);
    List<ITestBean> testBeans = myBean.getTestBeans();
    Assert.assertEquals(testBeans.size(), 2);
    testBeans.removeAll(CollectionUtility.arrayList(BEANS.get(Bean01.class), BEANS.get(Bean02.class)));
    Assert.assertEquals(0, testBeans.size());
  }

  @Test
  public void testMethodInstanceInjection() {
    MyBean02 myBean = BEANS.get(MyBean02.class);
    Assert.assertNotNull(myBean);
    List<ITestBean> testBeans = myBean.getTestBeans();
    Assert.assertEquals(testBeans.size(), 2);
    testBeans.removeAll(CollectionUtility.arrayList(BEANS.get(Bean01.class), BEANS.get(Bean02.class)));
    Assert.assertEquals(0, testBeans.size());
  }

  @AfterClass
  public static void removeBeans() {
    Platform.get().getBeanManager().unregisterBean(m_bean01);
    Platform.get().getBeanManager().unregisterBean(m_bean02);
    Platform.get().getBeanManager().unregisterBean(m_bean03);
    Platform.get().getBeanManager().unregisterBean(m_bean04);
  }

  private static interface ITestBean {

  }

  @ApplicationScoped
  private static class Bean01 implements ITestBean {

  }

  @ApplicationScoped
  private static class Bean02 implements ITestBean {

  }

  @ApplicationScoped
  private static class MyBean01 {

    protected List<ITestBean> getTestBeans() {
      List<ITestBean> result = new ArrayList<>();
      Iterator<ITestBean> it = BEANS.all(ITestBean.class).iterator();
      while (it.hasNext()) {
        result.add(it.next());
      }
      return result;
    }
  }

  @ApplicationScoped
  private static class MyBean02 {
    private List<ITestBean> testBeans = BEANS.all(ITestBean.class);

    public List<ITestBean> getTestBeans() {
      return testBeans;
    }
  }
}
