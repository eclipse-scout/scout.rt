/*******************************************************************************
 * Copyright (c) 2010-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.platform;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.eclipse.scout.rt.platform.internal.BeanManagerImplementor;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Test for {@link BeanManagerImplementor#isBean(Class)}
 */
@RunWith(PlatformTestRunner.class)
public class BeanManagerIsBeanTest {

  private static class TestObject {
  }

  @Test
  public void testIsBean() {
    BeanManagerImplementor context = new BeanManagerImplementor(new SimpleBeanDecorationFactory());

    // test object is a bean, as soon as it was registered within bean manager
    assertFalse(context.isBean(TestObject.class));
    IBean<?> reg = context.registerClass(TestObject.class);
    assertTrue(context.isBean(TestObject.class));
    context.unregisterBean(reg);
    assertFalse(context.isBean(TestObject.class));

    // JRE classes are no beans
    assertFalse(context.isBean(String.class));
  }
}
