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
package org.eclipse.scout.rt.platform.cdi;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.inject.Inject;

import org.eclipse.scout.commons.CollectionUtility;
import org.eclipse.scout.rt.testing.platform.ScoutPlatformTestRunner;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 *
 */
@RunWith(ScoutPlatformTestRunner.class)
public class InjectInstancesTest {

  private static IBean<Bean01> m_bean01;
  private static IBean<Bean02> m_bean02;
  private static IBean<MyBean01> m_bean03;
  private static IBean<MyBean02> m_bean04;

  @BeforeClass
  public static void registerBeans() {
    m_bean01 = OBJ.registerClass(Bean01.class);
    m_bean02 = OBJ.registerClass(Bean02.class);
    m_bean03 = OBJ.registerClass(MyBean01.class);
    m_bean04 = OBJ.registerClass(MyBean02.class);

  }

  @Test
  public void testFieldInstanceInjection() {
    MyBean01 myBean = OBJ.one(MyBean01.class);
    Assert.assertNotNull(myBean);
    List<ITestBean> testBeans = myBean.getTestBeans();
    Assert.assertEquals(testBeans.size(), 2);
    testBeans.removeAll(CollectionUtility.arrayList(OBJ.one(Bean01.class), OBJ.one(Bean02.class)));
    Assert.assertEquals(0, testBeans.size());
  }

  @Test
  public void testMethodInstanceInjection() {
    MyBean02 myBean = OBJ.one(MyBean02.class);
    Assert.assertNotNull(myBean);
    List<ITestBean> testBeans = myBean.getTestBeans();
    Assert.assertEquals(testBeans.size(), 2);
    testBeans.removeAll(CollectionUtility.arrayList(OBJ.one(Bean01.class), OBJ.one(Bean02.class)));
    Assert.assertEquals(0, testBeans.size());
  }

  @AfterClass
  public static void removeBeans() {
    OBJ.unregisterBean(m_bean01);
    OBJ.unregisterBean(m_bean02);
    OBJ.unregisterBean(m_bean03);
    OBJ.unregisterBean(m_bean04);
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
    @Inject
    private Instance<ITestBean> testbeans;

    protected List<ITestBean> getTestBeans() {
      List<ITestBean> result = new ArrayList<>();
      Iterator<ITestBean> it = testbeans.iterator();
      while (it.hasNext()) {
        result.add(it.next());
      }
      return result;
    }
  }

  @ApplicationScoped
  private static class MyBean02 {
    private List<ITestBean> testBeans;

    @Inject
    private void setBeans(Instance<ITestBean> instances) {
      testBeans = new ArrayList<>();
      Iterator<ITestBean> it = instances.iterator();
      while (it.hasNext()) {
        testBeans.add(it.next());
      }
    }

    public List<ITestBean> getTestBeans() {
      return testBeans;
    }
  }
}
