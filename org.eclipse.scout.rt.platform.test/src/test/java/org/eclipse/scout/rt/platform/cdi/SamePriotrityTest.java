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

import java.util.List;

import org.eclipse.scout.commons.annotations.Priority;
import org.eclipse.scout.rt.testing.platform.PlatformTestRunner;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 *
 */
@RunWith(PlatformTestRunner.class)
public class SamePriotrityTest {

  private static IBean<ABean> m_bean01;
  private static IBean<BBean> m_bean02;

  @BeforeClass
  public static void registerBeans() {
    m_bean01 = OBJ.registerClass(ABean.class);
    m_bean02 = OBJ.registerClass(BBean.class);
  }

  /**
   * Tests if two beans with the same priority registered to the bean context are ordered on behaviour of their class name.
   */
  @Test
  public void testPriority() {
    Assert.assertEquals(ABean.class, OBJ.one(ITestBean.class).getClass());
    List<ITestBean> all = OBJ.all(ITestBean.class);
    Assert.assertEquals(2, all.size());
    Assert.assertEquals(ABean.class, all.get(0).getClass());
    Assert.assertEquals(BBean.class, all.get(1).getClass());
  }

  @AfterClass
  public static void removeBeans() {
    OBJ.unregisterBean(m_bean01);
    OBJ.unregisterBean(m_bean02);
  }

  private static interface ITestBean {

  }

  @Priority(10)
  private static class ABean implements ITestBean {

  }

  @Priority(10)
  private static class BBean implements ITestBean {

  }

}
