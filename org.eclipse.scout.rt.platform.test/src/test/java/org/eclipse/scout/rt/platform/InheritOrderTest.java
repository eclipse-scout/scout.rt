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
 * Tests that @Order annotations are inherited if a Bean is replacing another and has no own order defined.
 */
public class InheritOrderTest {
  @Test
  public void testInheritOrderOnReplace() {
    BeanManagerImplementor context = new BeanManagerImplementor(new SimpleBeanDecorationFactory());

    context.registerClass(OrigBean.class);
    IBean<ReplacingBeanWithourOrder> bean = context.registerClass(ReplacingBeanWithourOrder.class);

    Assert.assertNotNull(bean.getBeanAnnotation(Replace.class));
    Assert.assertNotNull(bean.getBeanAnnotation(Order.class));
    Assert.assertEquals(11.0, bean.getBeanAnnotation(Order.class).value(), 0.001);

    OrigBean bean2 = context.getBean(OrigBean.class).getInstance();
    Assert.assertTrue(bean2 instanceof ReplacingBeanWithourOrder);
  }

  @Test
  public void testNotInheritedOrderOnReplaceWithOwnOrder() {
    BeanManagerImplementor context = new BeanManagerImplementor(new SimpleBeanDecorationFactory());

    context.registerClass(OrigBean.class);
    IBean<ReplacingBeanWithOwnOrder> bean = context.registerClass(ReplacingBeanWithOwnOrder.class);

    Assert.assertNotNull(bean.getBeanAnnotation(Replace.class));
    Assert.assertNotNull(bean.getBeanAnnotation(Order.class));
    Assert.assertEquals(15.0, bean.getBeanAnnotation(Order.class).value(), 0.001);

    OrigBean bean2 = context.getBean(OrigBean.class).getInstance();
    Assert.assertTrue(bean2 instanceof ReplacingBeanWithOwnOrder);
  }

  @Test
  public void testNotInheritedOrderOnNoReplace() {
    BeanManagerImplementor context = new BeanManagerImplementor(new SimpleBeanDecorationFactory());

    context.registerClass(OrigBean.class);
    IBean<NotReplacingBean> bean = context.registerClass(NotReplacingBean.class);

    Assert.assertNull(bean.getBeanAnnotation(Replace.class));
    Assert.assertNotNull(bean.getBeanAnnotation(Order.class));
    Assert.assertEquals(20.0, bean.getBeanAnnotation(Order.class).value(), 0.001);

    OrigBean bean2 = context.getBean(OrigBean.class).getInstance();
    Assert.assertTrue(bean2 instanceof OrigBean);
    NotReplacingBean bean3 = context.getBean(NotReplacingBean.class).getInstance();
    Assert.assertTrue(bean3 instanceof NotReplacingBean);
    Assert.assertEquals(2, context.getBeans(OrigBean.class).size());
  }

  @Test
  public void testInheritedOverSeveralLevels() {
    BeanManagerImplementor context = new BeanManagerImplementor(new SimpleBeanDecorationFactory());

    context.registerClass(OrigBean.class);
    context.registerClass(SecondLevelBean.class);
    IBean<ThirdLevelBean> bean = context.registerClass(ThirdLevelBean.class);

    Assert.assertNotNull(bean.getBeanAnnotation(Replace.class));
    Assert.assertNotNull(bean.getBeanAnnotation(Order.class));
    Assert.assertEquals(11.0, bean.getBeanAnnotation(Order.class).value(), 0.001);
  }

  @Test
  public void testNotInheritedOverSeveralLevels() {
    BeanManagerImplementor context = new BeanManagerImplementor(new SimpleBeanDecorationFactory());

    context.registerClass(OrigBean.class);
    context.registerClass(NotReplacingSecondLevelBean.class);
    IBean<NotReplacingThirdLevelBean> bean = context.registerClass(NotReplacingThirdLevelBean.class);

    Assert.assertNotNull(bean.getBeanAnnotation(Replace.class));
    Assert.assertNotNull(bean.getBeanAnnotation(Order.class));
    Assert.assertEquals(25.0, bean.getBeanAnnotation(Order.class).value(), 0.001);
  }

  @Test
  public void testNotInheritedOverSeveralLevelsWithoutOrder() {
    BeanManagerImplementor context = new BeanManagerImplementor(new SimpleBeanDecorationFactory());

    context.registerClass(OrigBean.class);
    context.registerClass(NotReplacingSecondLevelWithoutOrder.class);
    IBean<NotReplacingThirdLevelWithoutOrder> bean = context.registerClass(NotReplacingThirdLevelWithoutOrder.class);

    Assert.assertNotNull(bean.getBeanAnnotation(Replace.class));
    Assert.assertNull(bean.getBeanAnnotation(Order.class));
  }

  @Bean
  @Order(11)
  private static class OrigBean {
  }

  @Replace
  private static class ReplacingBeanWithourOrder extends OrigBean {
  }

  @Replace
  @Order(15)
  private static class ReplacingBeanWithOwnOrder extends OrigBean {
  }

  @Order(20)
  private static class NotReplacingBean extends OrigBean {
  }

  @Replace
  private static class SecondLevelBean extends OrigBean {
  }

  @Order(25)
  private static class NotReplacingSecondLevelBean extends OrigBean {
  }

  @Replace
  private static class ThirdLevelBean extends SecondLevelBean {
  }

  @Replace
  private static class NotReplacingThirdLevelBean extends NotReplacingSecondLevelBean {
  }

  private static class NotReplacingSecondLevelWithoutOrder extends OrigBean {
  }

  @Replace
  private static class NotReplacingThirdLevelWithoutOrder extends NotReplacingSecondLevelWithoutOrder {
  }
}
