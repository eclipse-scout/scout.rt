package org.eclipse.scout.rt.platform.internal;

import org.eclipse.scout.rt.platform.BeanMetaData;
import org.eclipse.scout.rt.platform.Replace;
import org.eclipse.scout.rt.platform.util.Assertions.AssertionException;
import org.junit.Assert;
import org.junit.Test;

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
}
