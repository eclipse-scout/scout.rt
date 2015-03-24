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

import org.eclipse.scout.rt.platform.internal.BeanContextImplementor;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 */
public class BeanDoubleRegistrationTest {

  @Test
  public void testDoubleRegistration() {
    BeanContextImplementor context = new BeanContextImplementor(new SimpleBeanInstanceFactory());

    IBean<?> reg1 = context.registerClass(Bean01.class);
    IBean<?> reg2 = context.registerClass(Bean01.class);
    Assert.assertFalse(reg1 == reg2);
    Assert.assertEquals(0, context.getBean(Object.class).size());
    Assert.assertEquals(1, context.getBean(Bean01.class).size());
    Assert.assertEquals(1, context.getBeans(Object.class).size());
    Assert.assertEquals(1, context.getBeans(Bean01.class).size());
    Assert.assertEquals(2, context.getRegisteredBeans(Object.class).size());
    Assert.assertEquals(2, context.getRegisteredBeans(Bean01.class).size());

    context.unregisterBean(reg2);
    Assert.assertEquals(0, context.getBean(Object.class).size());
    Assert.assertEquals(1, context.getBean(Bean01.class).size());
    Assert.assertEquals(1, context.getBeans(Object.class).size());
    Assert.assertEquals(1, context.getBeans(Bean01.class).size());
    Assert.assertEquals(1, context.getRegisteredBeans(Object.class).size());
    Assert.assertEquals(1, context.getRegisteredBeans(Bean01.class).size());

    context.unregisterBean(reg1);
    Assert.assertEquals(0, context.getBean(Object.class).size());
    Assert.assertEquals(0, context.getBean(Bean01.class).size());
    Assert.assertEquals(0, context.getBeans(Object.class).size());
    Assert.assertEquals(0, context.getBeans(Bean01.class).size());
    Assert.assertEquals(0, context.getRegisteredBeans(Object.class).size());
    Assert.assertEquals(0, context.getRegisteredBeans(Bean01.class).size());
  }

  private class Bean01 {

  }
}
