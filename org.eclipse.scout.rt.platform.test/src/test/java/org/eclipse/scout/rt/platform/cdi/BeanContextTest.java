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

import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.IBean;
import org.eclipse.scout.rt.platform.SimpleBeanInstanceFactory;
import org.eclipse.scout.rt.platform.internal.BeanContextImplementor;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 */
public class BeanContextTest {

  @Test
  public void testRegistration() {
    BeanContextImplementor context = new BeanContextImplementor(new SimpleBeanInstanceFactory());

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
    BeanContextImplementor context = new BeanContextImplementor(new SimpleBeanInstanceFactory());

    IBean reg1 = context.registerClass(Bean01.class);
    IBean reg2 = context.registerClass(Bean02.class);

    Assert.assertEquals(2, context.getBeans(Object.class).size());
    context.unregisterBean(reg1);
    Assert.assertEquals(1, context.getBeans(Object.class).size());
    context.unregisterBean(reg2);
    Assert.assertEquals(0, context.getBeans(Object.class).size());
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
