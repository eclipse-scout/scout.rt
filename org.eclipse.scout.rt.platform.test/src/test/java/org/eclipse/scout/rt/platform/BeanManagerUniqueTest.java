package org.eclipse.scout.rt.platform;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.eclipse.scout.rt.platform.internal.BeanManagerImplementor;
import org.eclipse.scout.rt.platform.util.Assertions.AssertionException;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class BeanManagerUniqueTest {

  private static BeanManagerImplementor context;

  @BeforeClass
  public static void registerBeans() {
    context = new BeanManagerImplementor(new SimpleBeanDecorationFactory());
    context.registerClass(BaseClass.class);
    context.registerClass(ChildClassB.class);
    context.registerClass(ChildClassA.class);
  }

  @AfterClass
  public static void unregisterBeans() {
    context = null;
  }

  @Test
  public void testUnique() {
    assertNull(context.uniqueBean(AbstractBaseClass.class)); // multiple instances possible, uniqueBean should return null
    assertEquals(BaseClass.class, context.uniqueBean(BaseClass.class).getBeanClazz());
    assertEquals(ChildClassA.class, context.uniqueBean(ChildClassA.class).getBeanClazz());
    assertEquals(ChildClassB.class, context.uniqueBean(ChildClassB.class).getBeanClazz());
  }

  @Test(expected = AssertionException.class)
  public void testGetAbstractBaseClass() {
    context.getBean(AbstractBaseClass.class);
  }

  @Test
  public void testGetBaseClass() {
    assertEquals(BaseClass.class, context.getBean(BaseClass.class).getBeanClazz());
  }

  @Test(expected = AssertionException.class)
  public void testOptAbstractBaseClass() {
    context.optBean(AbstractBaseClass.class);
  }

  @Test
  public void testOptBaseClass() {
    assertEquals(BaseClass.class, context.optBean(BaseClass.class).getBeanClazz());
  }

  private static abstract class AbstractBaseClass {
  }

  private static class BaseClass extends AbstractBaseClass {
  }

  private static class ChildClassB extends BaseClass {
  }

  private static class ChildClassA extends BaseClass {
  }
}
