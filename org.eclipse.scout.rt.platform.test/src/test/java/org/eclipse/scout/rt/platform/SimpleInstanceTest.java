/*******************************************************************************
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.platform;

import org.eclipse.scout.rt.platform.internal.BeanManagerImplementor;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class SimpleInstanceTest {

  private static BeanManagerImplementor context;

  @BeforeClass
  public static void registerBeans() {
    context = new BeanManagerImplementor(new SimpleBeanDecorationFactory());
    context.registerClass(TestObject.class);
  }

  @AfterClass
  public static void unregisterBeans() {
    context = null;
  }

  @Test
  public void test() {
    TestObject i1 = context.getBean(TestObject.class).getInstance();
    Assert.assertNotNull(i1);
    ITestObject i2 = context.getBean(ITestObject.class).getInstance();
    Assert.assertNotNull(i2);
    Assert.assertNotEquals(i1, i2);
  }

  private static interface ITestObject {

  }

  private static class TestObject implements ITestObject {

  }
}
