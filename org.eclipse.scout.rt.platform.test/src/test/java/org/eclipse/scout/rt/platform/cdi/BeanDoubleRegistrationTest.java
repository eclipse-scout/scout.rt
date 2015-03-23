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

import org.eclipse.scout.rt.platform.IBean;
import org.eclipse.scout.rt.platform.SimpleBeanInstanceFactory;
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
    Assert.assertTrue(reg1 == reg2);
    Assert.assertEquals(1, context.getBeans(Object.class).size());
    context.unregisterBean(reg2);
    Assert.assertEquals(0, context.getBeans(Object.class).size());
  }

  private class Bean01 {

  }
}
