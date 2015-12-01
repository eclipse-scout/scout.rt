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
public class SameBeanOrderTest {

  private static IBean<ABean> m_bean01;
  private static IBean<BBean> m_bean02;

  @BeforeClass
  public static void registerBeans() {
    m_bean01 = Platform.get().getBeanManager().registerClass(ABean.class);
    m_bean02 = Platform.get().getBeanManager().registerClass(BBean.class);
  }

  @Test
  public void testOrder() {
    List<ITestBean> all = BEANS.all(ITestBean.class);
    Assert.assertEquals(2, all.size());
    Assert.assertEquals(ABean.class, all.get(0).getClass());
    Assert.assertEquals(BBean.class, all.get(1).getClass());
  }

  @Test(expected = AssertionException.class)
  public void testMultipleException() {
    Assert.assertEquals(ABean.class, BEANS.get(ITestBean.class).getClass());
  }

  @AfterClass
  public static void removeBeans() {
    Platform.get().getBeanManager().unregisterBean(m_bean01);
    Platform.get().getBeanManager().unregisterBean(m_bean02);
  }

  private static interface ITestBean {

  }

  @Order(10)
  private static class ABean implements ITestBean {

  }

  @Order(10)
  private static class BBean implements ITestBean {

  }

}
