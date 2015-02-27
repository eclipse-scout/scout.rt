package org.eclipse.scout.rt.platform.cdi;

import org.eclipse.scout.commons.annotations.Priority;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 */
public class DynamicAnnotationTest {

  @Test
  public void testAnnotationEquality() {
    Assert.assertEquals(Bean01.class.getAnnotation(CreateImmediately.class), DynamicAnnotations.createCreateImmediately());
    Assert.assertEquals(Bean01.class.getAnnotation(ApplicationScoped.class), DynamicAnnotations.createApplicationScoped());
    Assert.assertEquals(Bean01.class.getAnnotation(Priority.class), DynamicAnnotations.createPriority(30));
    Assert.assertNotEquals(Bean01.class.getAnnotation(Priority.class), DynamicAnnotations.createPriority(20));
  }

  private static interface IBean01 {

  }

  @CreateImmediately
  @ApplicationScoped
  @Priority(30)
  private static class Bean01 implements IBean01 {

  }
}
