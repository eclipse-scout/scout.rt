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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.eclipse.scout.rt.platform.internal.BeanManagerImplementor;
import org.junit.Assert;
import org.junit.Test;

public class BeanDoubleRegistrationTest {

  @Test
  public void testDoubleRegistration() {
    BeanManagerImplementor context = new BeanManagerImplementor(new SimpleBeanDecorationFactory());

    IBean<?> reg1 = context.registerClass(Bean01.class);
    IBean<?> reg2 = context.registerClass(Bean01.class);
    Assert.assertFalse(reg1 == reg2);
    assertNull(context.optBean(Object.class));
    assertNotNull(context.getBean(Bean01.class));
    assertEquals(1, context.getBeans(Object.class).size());
    assertEquals(1, context.getBeans(Bean01.class).size());
    assertEquals(2, context.getRegisteredBeans(Object.class).size());
    assertEquals(2, context.getRegisteredBeans(Bean01.class).size());

    context.unregisterBean(reg2);
    assertNull(context.optBean(Object.class));
    assertNotNull(context.getBean(Bean01.class));
    assertEquals(1, context.getBeans(Object.class).size());
    assertEquals(1, context.getBeans(Bean01.class).size());
    assertEquals(1, context.getRegisteredBeans(Object.class).size());
    assertEquals(1, context.getRegisteredBeans(Bean01.class).size());

    context.unregisterBean(reg1);
    assertNull(context.optBean(Object.class));
    assertNull(context.optBean(Bean01.class));
    assertEquals(0, context.getBeans(Object.class).size());
    assertEquals(0, context.getBeans(Bean01.class).size());
    assertEquals(0, context.getRegisteredBeans(Object.class).size());
    assertEquals(0, context.getRegisteredBeans(Bean01.class).size());
  }

  @Test
  public void testDoubleRegistrationWithOrder() {
    BeanManagerImplementor context = new BeanManagerImplementor(new SimpleBeanDecorationFactory());
    assertBeanCount(context, 0, 0, 0, 0, 0);

    IBean<?> reg1a = context.registerBean(new BeanMetaData(Bean01.class).withOrder(100));
    assertBeanCount(context, 1, 0, 1, 0, 1);

    IBean<?> reg2 = context.registerBean(new BeanMetaData(Bean02.class).withOrder(200));
    assertBeanCount(context, 2, 1, 2, 1, 2);

    IBean<?> reg1b = context.registerBean(new BeanMetaData(Bean01.class).withOrder(300));
    assertBeanCount(context, 2, 1, 3, 1, 3);

    context.unregisterBean(reg1a);
    assertBeanCount(context, 2, 1, 2, 1, 2);

    context.unregisterBean(reg1b);
    assertBeanCount(context, 1, 1, 1, 1, 1);

    context.unregisterBean(reg2);
    assertBeanCount(context, 0, 0, 0, 0, 0);
  }

  protected void assertBeanCount(BeanManagerImplementor context, int expectedBean01, int expectedBean02, int expectedRegBean01, int expectedRegBean02, int expectedObject) {
    assertEquals(expectedBean01, context.getBeans(Bean01.class).size());
    assertEquals(expectedBean02, context.getBeans(Bean02.class).size());
    assertEquals(expectedRegBean01, context.getRegisteredBeans(Bean01.class).size());
    assertEquals(expectedRegBean02, context.getRegisteredBeans(Bean02.class).size());
    assertEquals(expectedObject, context.getRegisteredBeans(Object.class).size());
  }

  private class Bean01 {
  }

  private class Bean02 extends Bean01 {
  }
}
