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
import org.junit.Assert;
import org.junit.Test;

/**
 * Test case for Class A which is replaced by two child classes
 */
public class DoubleReplaceTest {

  @Test
  public void testLeafA() {
    BeanManagerImplementor bm = createBeanManager(FixtureBase.class, FixtureA.class, FixtureB.class);
    Assert.assertEquals(FixtureA.class, bm.uniqueBean(FixtureA.class).getInstance().getClass());
  }

  @Test
  public void testLeafB() {
    BeanManagerImplementor bm = createBeanManager(FixtureBase.class, FixtureA.class, FixtureB.class);
    Assert.assertEquals(FixtureB.class, bm.uniqueBean(FixtureB.class).getInstance().getClass());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testBase() {
    BeanManagerImplementor bm = createBeanManager(FixtureBase.class, FixtureA.class, FixtureB.class);
    bm.uniqueBean(FixtureBase.class);
    Assert.fail();
  }

  @Test
  public void testLeafC() {
    BeanManagerImplementor bm = createBeanManager(FixtureBase.class, FixtureA.class, FixtureC.class);
    Assert.assertEquals(FixtureC.class, bm.uniqueBean(FixtureBase.class).getInstance().getClass());
  }

  @Test
  public void testLeafD() {
    BeanManagerImplementor bm = createBeanManager(FixtureBase.class, FixtureC.class, FixtureD.class);
    Assert.assertEquals(FixtureD.class, bm.uniqueBean(FixtureBase.class).getInstance().getClass());
  }

  private BeanManagerImplementor createBeanManager(Class... beans) {
    BeanManagerImplementor bm = new BeanManagerImplementor(new SimpleBeanDecorationFactory());
    for (Class<?> c : beans) {
      bm.registerClass(c);
    }
    return bm;
  }

  private static class FixtureBase {
  }

  @Replace
  private static class FixtureA extends FixtureBase {
  }

  @Replace
  private static class FixtureB extends FixtureBase {
  }

  @Replace
  @Order(100)
  private static class FixtureC extends FixtureBase {
  }

  @Replace
  @Order(10)
  private static class FixtureD extends FixtureBase {
  }
}
