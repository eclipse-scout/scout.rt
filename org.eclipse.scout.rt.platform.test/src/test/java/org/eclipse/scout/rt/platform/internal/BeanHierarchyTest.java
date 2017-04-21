package org.eclipse.scout.rt.platform.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.eclipse.scout.rt.platform.BeanMetaData;
import org.eclipse.scout.rt.platform.IBean;
import org.eclipse.scout.rt.platform.Replace;
import org.eclipse.scout.rt.platform.util.Assertions.AssertionException;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * <h3>{@link BeanHierarchyTest}</h3>
 */
public class BeanHierarchyTest {

  @Test(expected = AssertionException.class)
  public void testReplaceWithoutSuperClass() {
    BeanHierarchy<InvalidReplaceA> h = new BeanHierarchy<>(InvalidReplaceA.class);
    h.addBean(new BeanImplementor<InvalidReplaceA>(new BeanMetaData(InvalidReplaceA.class)));
    h.queryAll();
    Assert.fail();
  }

  @Test(expected = AssertionException.class)
  public void testReplaceOnInterface() {
    BeanHierarchy<InvalidReplaceB> h = new BeanHierarchy<>(InvalidReplaceB.class);
    h.addBean(new BeanImplementor<InvalidReplaceB>(new BeanMetaData(InvalidReplaceB.class)));
    h.queryAll();
    Assert.fail();
  }

  @Test(expected = AssertionException.class)
  public void testReplaceOnPrimitive() {
    BeanHierarchy<Integer> h = new BeanHierarchy<Integer>(int.class);
    BeanMetaData withReplace = new BeanMetaData(int.class).withReplace(true);
    h.addBean(new BeanImplementor<Integer>(withReplace));
    h.queryAll();
    Assert.fail();
  }

  @Test(expected = AssertionException.class)
  public void testReplaceWithAbstractSuperClass() {
    BeanHierarchy<InvalidReplaceWithAbstractSuperClass> h = new BeanHierarchy<InvalidReplaceWithAbstractSuperClass>(InvalidReplaceWithAbstractSuperClass.class);
    BeanMetaData withReplace = new BeanMetaData(InvalidReplaceWithAbstractSuperClass.class).withReplace(true);
    h.addBean(new BeanImplementor<InvalidReplaceWithAbstractSuperClass>(withReplace));
    h.queryAll();
    Assert.fail();
  }

  @Replace
  private static class InvalidReplaceA {
  }

  private static abstract class AbstractFixture {
  }

  @Replace
  private static class InvalidReplaceWithAbstractSuperClass extends AbstractFixture {
  }

  @Replace
  private static interface InvalidReplaceB {
  }

  @Test
  public void testGetRegisteredBean() {
    BeanHierarchy<AbstractBaseClass> h = new BeanHierarchy<>(AbstractBaseClass.class);
    h.addBean(new BeanImplementor<AbstractBaseClass>(new BeanMetaData(SubClassA.class)));
    h.addBean(new BeanImplementor<AbstractBaseClass>(new BeanMetaData(SubClassB.class)));
    h.addBean(new BeanImplementor<AbstractBaseClass>(new BeanMetaData(SubClassB1.class).withReplace(true)));
    h.addBean(new BeanImplementor<AbstractBaseClass>(new BeanMetaData(SubClassB2.class).withReplace(true).withOrder(IBean.DEFAULT_BEAN_ORDER - 1)));

    assertEquals(SubClassA.class, h.getExactBean(SubClassA.class).getBeanClazz());
    assertEquals(SubClassB.class, h.getExactBean(SubClassB.class).getBeanClazz());
    assertEquals(SubClassB1.class, h.getExactBean(SubClassB1.class).getBeanClazz());
    assertEquals(SubClassB2.class, h.getExactBean(SubClassB2.class).getBeanClazz());
    assertNull(h.getExactBean(String.class));
  }

  @Test
  public void testGetRegisteredBeanWithMultipleRegistration() {
    BeanHierarchy<AbstractBaseClass> h = new BeanHierarchy<>(AbstractBaseClass.class);
    h.addBean(new BeanImplementor<AbstractBaseClass>(new BeanMetaData(SubClassA.class)));
    assertEquals(SubClassA.class, h.getExactBean(SubClassA.class).getBeanClazz());

    SubClassA subA1 = Mockito.mock(SubClassA.class);
    SubClassA subA2 = Mockito.mock(SubClassA.class);
    h.addBean(new BeanImplementor<AbstractBaseClass>(new BeanMetaData(SubClassA.class, subA1).withReplace(true).withApplicationScoped(true).withOrder(100)));
    h.addBean(new BeanImplementor<AbstractBaseClass>(new BeanMetaData(SubClassA.class, subA2).withReplace(true).withApplicationScoped(true).withOrder(101)));

    assertEquals(SubClassA.class, h.getExactBean(SubClassA.class).getBeanClazz());
    assertEquals(subA1, h.getExactBean(SubClassA.class).getInstance()); // expect to get instance of registered bean with lowest order
  }

  public static abstract class AbstractBaseClass {
  }

  public static class SubClassA extends AbstractBaseClass {
  }

  public static class SubClassB extends AbstractBaseClass {
  }

  public static class SubClassB1 extends SubClassB {
  }

  public static class SubClassB2 extends SubClassB {
  }
}
