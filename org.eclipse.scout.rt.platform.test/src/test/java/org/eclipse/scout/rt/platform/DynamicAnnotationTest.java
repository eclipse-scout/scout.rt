/*******************************************************************************
 * Copyright (c) 2010-2016 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.platform;

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

  private static interface IBean01 {

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
}
