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

import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.commons.annotations.Replace;
import org.eclipse.scout.rt.platform.internal.BeanContextImplementor;
import org.junit.Assert;
import org.junit.Test;

public class ReUseVsReplaceTest {

  @Test
  public void testReUse() {
    BeanContextImplementor context = new BeanContextImplementor(new SimpleBeanInstanceFactory());
    context.registerClass(FooParam.class);
    context.registerClass(SpecialFooParam.class);
    Assert.assertEquals(FooParam.class, context.getInstance(FooParam.class).getClass());
    Assert.assertEquals(2, context.getBeans(FooParam.class).size());

    Assert.assertEquals(SpecialFooParam.class, context.getInstance(SpecialFooParam.class).getClass());
    Assert.assertEquals(1, context.getBeans(SpecialFooParam.class).size());
  }

  @Test
  public void testReplace() {
    BeanContextImplementor context = new BeanContextImplementor(new SimpleBeanInstanceFactory());
    context.registerClass(FooParam.class);
    context.registerClass(FooParamEx1.class);

    Assert.assertEquals(FooParamEx1.class, context.getInstance(FooParam.class).getClass());
    Assert.assertEquals(1, context.getBeans(FooParam.class).size());

    Assert.assertEquals(FooParamEx1.class, context.getInstance(FooParamEx1.class).getClass());
    Assert.assertEquals(1, context.getBeans(FooParamEx1.class).size());
  }

  @Test
  public void testReplaceWithTwoParallelReplaces() {
    BeanContextImplementor context = new BeanContextImplementor(new SimpleBeanInstanceFactory());
    context.registerClass(FooParam.class);
    context.registerClass(FooParamEx1.class);
    context.registerClass(FooParamEx2.class);

    Assert.assertEquals(FooParamEx2.class, context.getInstance(FooParam.class).getClass());
    Assert.assertEquals(2, context.getBeans(FooParam.class).size());

    Assert.assertEquals(FooParamEx1.class, context.getInstance(FooParamEx1.class).getClass());
    Assert.assertEquals(FooParamEx2.class, context.getInstance(FooParamEx2.class).getClass());
  }

  @Test
  public void testReplaceWithTwoLevelsOfReplace() {
    BeanContextImplementor context = new BeanContextImplementor(new SimpleBeanInstanceFactory());
    context.registerClass(FooParam.class);
    context.registerClass(FooParamEx1.class);
    context.registerClass(FooParamEx1Ex.class);

    Assert.assertEquals(FooParamEx1Ex.class, context.getInstance(FooParam.class).getClass());
    Assert.assertEquals(1, context.getBeans(FooParam.class).size());

    Assert.assertEquals(FooParamEx1Ex.class, context.getInstance(FooParamEx1.class).getClass());
    Assert.assertEquals(1, context.getBeans(FooParamEx1.class).size());

    Assert.assertEquals(FooParamEx1Ex.class, context.getInstance(FooParamEx1Ex.class).getClass());
    Assert.assertEquals(1, context.getBeans(FooParamEx1Ex.class).size());
  }

  @Test
  public void testReplaceWithTwoLevelsOfReplaceMixedWithParallelReplace() {
    BeanContextImplementor context = new BeanContextImplementor(new SimpleBeanInstanceFactory());
    context.registerClass(FooParam.class);
    context.registerClass(FooParamEx1.class);
    context.registerClass(FooParamEx2.class);
    context.registerClass(FooParamEx1Ex.class);

    Assert.assertEquals(FooParamEx2.class, context.getInstance(FooParam.class).getClass());
    Assert.assertEquals(2, context.getBeans(FooParam.class).size());
    Assert.assertEquals(FooParamEx2.class, context.getBeans(FooParam.class).get(0).getBeanClazz());
    Assert.assertEquals(FooParamEx1Ex.class, context.getBeans(FooParam.class).get(1).getBeanClazz());

    Assert.assertEquals(FooParamEx1Ex.class, context.getInstance(FooParamEx1.class).getClass());
    Assert.assertEquals(1, context.getBeans(FooParamEx1.class).size());

    Assert.assertEquals(FooParamEx1Ex.class, context.getInstance(FooParamEx1Ex.class).getClass());
    Assert.assertEquals(1, context.getBeans(FooParamEx1Ex.class).size());

    Assert.assertEquals(FooParamEx2.class, context.getInstance(FooParamEx2.class).getClass());
    Assert.assertEquals(1, context.getBeans(FooParamEx2.class).size());

  }

  private static class FooParam {
  }

  private static class SpecialFooParam extends FooParam {
  }

  @Replace
  private static class FooParamEx1 extends FooParam {
  }

  //wins over FooParamEx1
  @Order(-10)
  @Replace
  private static class FooParamEx2 extends FooParam {
  }

  @Replace
  private static class FooParamEx1Ex extends FooParamEx1 {
  }

}
