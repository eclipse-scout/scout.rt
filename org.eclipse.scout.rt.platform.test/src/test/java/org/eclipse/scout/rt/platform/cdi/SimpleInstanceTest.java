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

import org.eclipse.scout.rt.platform.Platform;
import org.eclipse.scout.rt.platform.cdi.internal.BeanContext;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(PlatformTestRunner.class)
public class SimpleInstanceTest {

  private static BeanContext context;

  @BeforeClass
  public static void registerBeans() {
    context = new BeanContext();
    context.initBeanInstanceFactory();

    context.registerClass(TestObject.class);
  }

  @Test
  public void test() {
    TestObject i1 = context.getInstance(TestObject.class);
    Assert.assertNotNull(i1);
    ITestObject i2 = context.getInstance(ITestObject.class);
    Assert.assertNotNull(i2);
    Assert.assertNotEquals(i1, i2);
  }

  @AfterClass
  public static void unregisterBeans() {
    Platform.get().getBeanContext().registerClass(TestObject.class);
  }

  private static interface ITestObject {

  }

  private static class TestObject implements ITestObject {

  }
}
