/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.platform;

import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(PlatformTestRunner.class)
public class ApplicationsScopedTest {

  private static IBean<TestObject> m_bean01;
  private static IBean<Bean02> m_bean02;

  @BeforeClass
  public static void registerBeans() {
    m_bean01 = Platform.get().getBeanManager().registerClass(TestObject.class);
    m_bean02 = Platform.get().getBeanManager().registerClass(Bean02.class);
  }

  /**
   * A @ApplicationScoped bean is expected to be the same instance in case of multiple context lookups.
   */
  @Test
  public void testApplicationScopded() {
    TestObject i1 = BEANS.get(TestObject.class);
    Assert.assertNotNull(i1);
    ITestObject i2 = BEANS.get(TestObject.class);
    Assert.assertNotNull(i2);
    Assert.assertEquals(i1, i2);
  }

  /**
   * Tests if the @ApplicationScoped annotation is inherited from super classes.
   */
  @Test
  public void testInheritedApplicationScopded() {
    Bean02 i1 = BEANS.get(Bean02.class);
    Assert.assertNotNull(i1);
    Bean02 i2 = BEANS.get(Bean02.class);
    Assert.assertNotNull(i2);
    Assert.assertEquals(i1, i2);
  }

  @AfterClass
  public static void unregisterBeans() {
    Platform.get().getBeanManager().unregisterBean(m_bean01);
    Platform.get().getBeanManager().unregisterBean(m_bean02);

  }

  private static interface ITestObject {

  }

  @ApplicationScoped
  private static class TestObject implements ITestObject {

  }

  @ApplicationScoped
  private static abstract class AbstractBean02 {

  }

  private static class Bean02 extends AbstractBean02 {

  }

}
