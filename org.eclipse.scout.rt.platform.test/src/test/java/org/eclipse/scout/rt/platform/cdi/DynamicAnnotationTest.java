package org.eclipse.scout.rt.platform.cdi;

import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.rt.platform.AnnotationFactory;
import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.CreateImmediately;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 */
public class DynamicAnnotationTest {

  @Test
  public void testAnnotationEquality() {
    Assert.assertEquals(Bean01.class.getAnnotation(CreateImmediately.class), AnnotationFactory.createCreateImmediately());
    Assert.assertEquals(Bean01.class.getAnnotation(ApplicationScoped.class), AnnotationFactory.createApplicationScoped());
    Assert.assertEquals(Bean01.class.getAnnotation(Order.class), AnnotationFactory.createOrder(-30));
    Assert.assertNotEquals(Bean01.class.getAnnotation(Order.class), AnnotationFactory.createOrder(-20));
  }

  private static interface IBean01 {

  }

  @CreateImmediately
  @ApplicationScoped
  @Order(30)
  private static class Bean01 implements IBean01 {

  }
}
