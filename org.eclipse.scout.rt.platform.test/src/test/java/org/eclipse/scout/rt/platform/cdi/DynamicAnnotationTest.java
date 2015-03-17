package org.eclipse.scout.rt.platform.cdi;

import org.eclipse.scout.commons.annotations.Priority;
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
    Assert.assertEquals(Bean01.class.getAnnotation(Priority.class), AnnotationFactory.createPriority(30));
    Assert.assertNotEquals(Bean01.class.getAnnotation(Priority.class), AnnotationFactory.createPriority(20));
  }

  private static interface IBean01 {

  }

  @CreateImmediately
  @ApplicationScoped
  @Priority(30)
  private static class Bean01 implements IBean01 {

  }
}
