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

import org.eclipse.scout.rt.platform.internal.BeanManagerImplementor;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Test case for Class A which is replaced by two child classes
 */
public class DoubleReplaceTest {

  private static BeanManagerImplementor context;

  @BeforeClass
  public static void registerBeans() {
    context = new BeanManagerImplementor(new SimpleBeanDecorationFactory());
    context.registerClass(BaseClass.class);
    context.registerClass(ChildClassB.class);
    context.registerClass(ChildClassA.class);
  }

  @AfterClass
  public static void unregisterBeans() {
    context = null;
  }

  @Test
  public void testBothPresent() {
    IBean<BaseClass> bean = context.getBean(BaseClass.class);
    Assert.assertEquals(ChildClassA.class, bean.getBeanClazz());
  }

  @ApplicationScoped
  private static class BaseClass {

  }

  @Replace
  private static class ChildClassB extends BaseClass {
  }

  @Replace
  private static class ChildClassA extends BaseClass {
  }
}
