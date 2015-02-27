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

import org.eclipse.scout.rt.platform.cdi.internal.BeanContext;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 */
public class BeanContextTest {

  @Test
  public void testRegistration() {
    BeanContext context = new BeanContext();
    context.registerClass(Bean01.class);
    Assert.assertEquals(1, context.getInstances(ITestBean.class).size());
    // register a second
    context.registerClass(Bean02.class);
    Assert.assertEquals(2, context.getInstances(ITestBean.class).size());
    Assert.assertNotNull(context.getInstance(ITestBean01.class));
    Assert.assertNotNull(context.getInstance(ITestBean.class));

    Assert.assertNotNull(context.getInstance(ITestBean02.class));
    Assert.assertNotNull(context.getInstance(ITestBean.class));
  }

  @Test
  public void testUnregisterByBean() {
    BeanContext context = new BeanContext();
    context.registerClass(Bean01.class);
    context.registerClass(Bean02.class);

    Assert.assertEquals(6, context.getAllRegisteredBeans().size());
    context.unregisterBean(new Bean<Bean01>(Bean01.class));
    Assert.assertEquals(3, context.getAllRegisteredBeans().size());
    context.unregisterBean(new Bean<Bean02>(Bean02.class));
    Assert.assertEquals(0, context.getAllRegisteredBeans().size());
  }

  private static interface ITestBean {

  }

  private static interface ITestBean01 {

  }

  private static interface ITestBean02 {

  }

  @ApplicationScoped
  private static class Bean01 implements ITestBean, ITestBean01 {

  }

  private static class Bean02 implements ITestBean, ITestBean02 {

  }
}
