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
import org.junit.Assert;
import org.junit.Test;

public class ReUseVsReplaceTest {

  @Test
  public void testReUse() {
    BeanManagerImplementor context = new BeanManagerImplementor(new SimpleBeanDecorationFactory());
    context.registerClass(FooParam.class);
    context.registerClass(SpecialFooParam.class);
    Assert.assertEquals(FooParam.class, context.getBean(FooParam.class).getBeanClazz());
    Assert.assertEquals(2, context.getBeans(FooParam.class).size());

    Assert.assertEquals(SpecialFooParam.class, context.getBean(SpecialFooParam.class).getBeanClazz());
    Assert.assertEquals(1, context.getBeans(SpecialFooParam.class).size());
  }

  @Test
  public void testReplace() {
    BeanManagerImplementor context = new BeanManagerImplementor(new SimpleBeanDecorationFactory());
    context.registerClass(FooParam.class);
    context.registerClass(FooParamEx1.class);

    Assert.assertEquals(FooParamEx1.class, context.getBean(FooParam.class).getBeanClazz());
    Assert.assertEquals(1, context.getBeans(FooParam.class).size());

    Assert.assertEquals(FooParamEx1.class, context.getBean(FooParamEx1.class).getBeanClazz());
    Assert.assertEquals(1, context.getBeans(FooParamEx1.class).size());
  }

  @Test
  public void testReplaceWithTwoParallelReplaces() {
    BeanManagerImplementor context = new BeanManagerImplementor(new SimpleBeanDecorationFactory());
    context.registerClass(FooParam.class);
    context.registerClass(FooParamEx1.class);
    context.registerClass(FooParamEx2.class);

    Assert.assertEquals(FooParamEx2.class, context.getBean(FooParam.class).getBeanClazz());
    Assert.assertEquals(2, context.getBeans(FooParam.class).size());

    Assert.assertEquals(FooParamEx1.class, context.getBean(FooParamEx1.class).getBeanClazz());
    Assert.assertEquals(FooParamEx2.class, context.getBean(FooParamEx2.class).getBeanClazz());
  }

  @Test
  public void testReplaceWithTwoLevelsOfReplace() {
    BeanManagerImplementor context = new BeanManagerImplementor(new SimpleBeanDecorationFactory());
    context.registerClass(FooParam.class);
    context.registerClass(FooParamEx1.class);
    context.registerClass(FooParamEx1Ex.class);

    Assert.assertEquals(FooParamEx1Ex.class, context.getBean(FooParam.class).getBeanClazz());
    Assert.assertEquals(1, context.getBeans(FooParam.class).size());

    Assert.assertEquals(FooParamEx1Ex.class, context.getBean(FooParamEx1.class).getBeanClazz());
    Assert.assertEquals(1, context.getBeans(FooParamEx1.class).size());

    Assert.assertEquals(FooParamEx1Ex.class, context.getBean(FooParamEx1Ex.class).getBeanClazz());
    Assert.assertEquals(1, context.getBeans(FooParamEx1Ex.class).size());
  }

  @Test
  public void testReplaceWithTwoLevelsOfReplaceMixedWithParallelReplace() {
    BeanManagerImplementor context = new BeanManagerImplementor(new SimpleBeanDecorationFactory());
    context.registerClass(FooParam.class);
    context.registerClass(FooParamEx1.class);
    context.registerClass(FooParamEx2.class);
    context.registerClass(FooParamEx1Ex.class);

    Assert.assertEquals(FooParamEx2.class, context.getBean(FooParam.class).getBeanClazz());
    Assert.assertEquals(2, context.getBeans(FooParam.class).size());
    Assert.assertEquals(FooParamEx2.class, context.getBeans(FooParam.class).get(0).getBeanClazz());
    Assert.assertEquals(FooParamEx1Ex.class, context.getBeans(FooParam.class).get(1).getBeanClazz());

    Assert.assertEquals(FooParamEx1Ex.class, context.getBean(FooParamEx1.class).getBeanClazz());
    Assert.assertEquals(1, context.getBeans(FooParamEx1.class).size());

    Assert.assertEquals(FooParamEx1Ex.class, context.getBean(FooParamEx1Ex.class).getBeanClazz());
    Assert.assertEquals(1, context.getBeans(FooParamEx1Ex.class).size());

    Assert.assertEquals(FooParamEx2.class, context.getBean(FooParamEx2.class).getBeanClazz());
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
