/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.platform;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.eclipse.scout.rt.platform.internal.BeanManagerImplementor;
import org.junit.Assert;
import org.junit.Test;

public class DynamicAnnotationTest {

  @Test
  public void testAnnotationEquality() {
    Assert.assertEquals(Bean01.class.getAnnotation(CreateImmediately.class), AnnotationFactory.createCreateImmediately());
    Assert.assertEquals(Bean01.class.getAnnotation(ApplicationScoped.class), AnnotationFactory.createApplicationScoped());
    Assert.assertEquals(Bean01.class.getAnnotation(Replace.class), AnnotationFactory.createReplace());
    Assert.assertEquals(AnnotationFactory.createCreateImmediately(), Bean01.class.getAnnotation(CreateImmediately.class));
    Assert.assertEquals(AnnotationFactory.createApplicationScoped(), Bean01.class.getAnnotation(ApplicationScoped.class));
    Assert.assertEquals(AnnotationFactory.createReplace(), Bean01.class.getAnnotation(Replace.class));
    Assert.assertEquals(AnnotationFactory.createCreateImmediately().hashCode(), Bean01.class.getAnnotation(CreateImmediately.class).hashCode());
    Assert.assertEquals(AnnotationFactory.createApplicationScoped().hashCode(), Bean01.class.getAnnotation(ApplicationScoped.class).hashCode());
    Assert.assertEquals(AnnotationFactory.createReplace().hashCode(), Bean01.class.getAnnotation(Replace.class).hashCode());
  }

  @SuppressWarnings("SimplifiableAssertion")
  @Test
  public void testOrder() {
    Order orderInstanceFromJre = Bean01.class.getAnnotation(Order.class);
    Order orderInstanceFromJre2 = Bean02.class.getAnnotation(Order.class);
    Order orderInstanceFromFactory = AnnotationFactory.createOrder(orderInstanceFromJre.value());
    Order orderInstanceFromFactory2 = AnnotationFactory.createOrder(orderInstanceFromJre2.value());

    // test symmetry
    Assert.assertTrue(orderInstanceFromFactory.equals(orderInstanceFromJre));
    Assert.assertTrue(orderInstanceFromJre.equals(orderInstanceFromFactory));

    // test special cases
    //noinspection EqualsWithItself
    Assert.assertTrue(orderInstanceFromFactory.equals(orderInstanceFromFactory));
    Assert.assertTrue(orderInstanceFromJre.equals(orderInstanceFromFactory));
    Assert.assertFalse(orderInstanceFromFactory.equals(null));
    Assert.assertFalse(orderInstanceFromJre.equals(null));
    Assert.assertFalse(orderInstanceFromFactory.equals(new Object()));
    Assert.assertFalse(orderInstanceFromJre.equals(new Object()));
    Assert.assertFalse(orderInstanceFromFactory.equals(new Object()));
    Assert.assertFalse(orderInstanceFromJre.equals(new Object()));
    Assert.assertFalse(orderInstanceFromJre.equals(orderInstanceFromJre2));
    Assert.assertFalse(orderInstanceFromJre2.equals(orderInstanceFromJre));
    Assert.assertFalse(orderInstanceFromFactory.equals(orderInstanceFromFactory2));
    Assert.assertFalse(orderInstanceFromFactory2.equals(orderInstanceFromFactory));

    // check that hashCode is correctly implemented
    Assert.assertEquals(orderInstanceFromJre.hashCode(), orderInstanceFromFactory.hashCode());

    // test annotation type
    Assert.assertSame(orderInstanceFromJre.annotationType(), orderInstanceFromFactory.annotationType());
  }

  @Test
  public void testInheritedAnnotation() {
    BeanManagerImplementor beanManager = new BeanManagerImplementor(new SimpleBeanDecorationFactory());
    IBean<Bean01> bean01 = beanManager.registerClass(Bean01.class);
    IBean<Bean03> bean03 = beanManager.registerClass(Bean03.class);
    IBean<Bean04> bean04 = beanManager.registerClass(Bean04.class);
    IBean<Bean05> bean05 = beanManager.registerClass(Bean05.class);

    Assert.assertFalse(bean01.hasAnnotation(InheritedWithValue.class));
    Assert.assertTrue(bean03.hasAnnotation(InheritedWithValue.class));
    Assert.assertTrue(bean04.hasAnnotation(InheritedWithValue.class));
    Assert.assertTrue(bean05.hasAnnotation(InheritedWithValue.class));

    Assert.assertEquals("Bean03", bean03.getBeanAnnotation(InheritedWithValue.class).value());
    Assert.assertEquals("Bean04", bean04.getBeanAnnotation(InheritedWithValue.class).value());
    Assert.assertEquals("Bean04", bean05.getBeanAnnotation(InheritedWithValue.class).value());
  }

  private interface IBean01 {
  }

  @CreateImmediately
  @ApplicationScoped
  @Order(-30)
  @Replace
  private static class Bean01 implements IBean01 {
  }

  @Order(3)
  private static class Bean02 {
  }

  @InheritedWithValue("Bean03")
  private static class Bean03 extends Bean01 {
  }

  @InheritedWithValue("Bean04")
  private static class Bean04 extends Bean03 {
  }

  private static class Bean05 extends Bean04 {
  }

  @Inherited
  @Retention(RetentionPolicy.RUNTIME)
  @Target(ElementType.TYPE)
  private @interface InheritedWithValue {
    String value();
  }
}
