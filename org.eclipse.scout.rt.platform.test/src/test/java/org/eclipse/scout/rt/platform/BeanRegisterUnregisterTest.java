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
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(PlatformTestRunner.class)
public class BeanRegisterUnregisterTest {

  @Test
  public void testOneBean() {
    BeanManagerImplementor context = new BeanManagerImplementor(new SimpleBeanDecorationFactory());

    IBean<?> reg = context.registerClass(TestObject.class);
    Assert.assertEquals(1, context.getBeans(Object.class).size());
    context.unregisterBean(reg);
    Assert.assertEquals(0, context.getBeans(Object.class).size());
  }

  private static class TestObject {

  }
}
