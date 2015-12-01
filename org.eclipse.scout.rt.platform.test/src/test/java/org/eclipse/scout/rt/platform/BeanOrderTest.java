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

import java.util.List;

import org.eclipse.scout.rt.platform.util.Assertions.AssertionException;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(PlatformTestRunner.class)
public class BeanOrderTest {

  private static IBean<TestBean01> m_bean01;
  private static IBean<TestBean02> m_bean02;

  @BeforeClass
  public static void registerBeans() {
    m_bean01 = Platform.get().getBeanManager().registerClass(TestBean01.class);
    m_bean02 = Platform.get().getBeanManager().registerClass(TestBean02.class);
  }

  /**
   * Tests if two beans registered to the bean context are ordered on behaviour of their priortity.
   */
  @Test
  public void testOrder() {
    List<ITestBean> all = BEANS.all(ITestBean.class);
    Assert.assertEquals(2, all.size());
    Assert.assertEquals(TestBean01.class, all.get(0).getClass());
    Assert.assertEquals(TestBean02.class, all.get(1).getClass());
  }

  @Test(expected = AssertionException.class)
  public void testMutlipleException() {
    Assert.assertEquals(TestBean01.class, BEANS.get(ITestBean.class).getClass());
  }

  @AfterClass
  public static void removeBeans() {
    Platform.get().getBeanManager().unregisterBean(m_bean01);
    Platform.get().getBeanManager().unregisterBean(m_bean02);
  }

  private static interface ITestBean {

  }

  @Order(20)
  private static class TestBean01 implements ITestBean {

  }

  @Order(20)
  private static class TestBean02 implements ITestBean {

  }

}
