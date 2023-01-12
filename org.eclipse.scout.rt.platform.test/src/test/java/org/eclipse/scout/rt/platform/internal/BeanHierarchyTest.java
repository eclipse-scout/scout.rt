/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.platform.internal;

import static org.junit.Assert.*;

import java.util.Arrays;

import org.eclipse.scout.rt.platform.BeanMetaData;
import org.eclipse.scout.rt.platform.IBean;
import org.eclipse.scout.rt.platform.Replace;
import org.eclipse.scout.rt.platform.util.Assertions.AssertionException;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

public class BeanHierarchyTest {

  @Test(expected = AssertionException.class)
  public void testReplaceWithoutSuperClass() {
    BeanHierarchy<InvalidReplaceA> h = new BeanHierarchy<>(InvalidReplaceA.class);
    h.addBean(new BeanImplementor<>(new BeanMetaData(InvalidReplaceA.class)));
    h.queryAll();
    Assert.fail();
  }

  @Test(expected = AssertionException.class)
  public void testReplaceOnInterface() {
    BeanHierarchy<InvalidReplaceB> h = new BeanHierarchy<>(InvalidReplaceB.class);
    h.addBean(new BeanImplementor<>(new BeanMetaData(InvalidReplaceB.class)));
    h.queryAll();
    Assert.fail();
  }

  @Test(expected = AssertionException.class)
  public void testReplaceOnPrimitive() {
    BeanHierarchy<Integer> h = new BeanHierarchy<>(int.class);
    BeanMetaData withReplace = new BeanMetaData(int.class).withReplace(true);
    h.addBean(new BeanImplementor<>(withReplace));
    h.queryAll();
    Assert.fail();
  }

  @Test(expected = AssertionException.class)
  public void testReplaceWithAbstractSuperClass() {
    BeanHierarchy<InvalidReplaceWithAbstractSuperClass> h = new BeanHierarchy<>(InvalidReplaceWithAbstractSuperClass.class);
    BeanMetaData withReplace = new BeanMetaData(InvalidReplaceWithAbstractSuperClass.class).withReplace(true);
    h.addBean(new BeanImplementor<>(withReplace));
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
    h.addBean(new BeanImplementor<>(new BeanMetaData(SubClassA.class)));
    h.addBean(new BeanImplementor<>(new BeanMetaData(SubClassB.class)));
    h.addBean(new BeanImplementor<>(new BeanMetaData(SubClassB1.class).withReplace(true)));
    h.addBean(new BeanImplementor<>(new BeanMetaData(SubClassB2.class).withReplace(true).withOrder(IBean.DEFAULT_BEAN_ORDER - 1)));

    assertEquals(SubClassA.class, h.getExactBean(SubClassA.class).getBeanClazz());
    assertEquals(SubClassB.class, h.getExactBean(SubClassB.class).getBeanClazz());
    assertEquals(SubClassB1.class, h.getExactBean(SubClassB1.class).getBeanClazz());
    assertEquals(SubClassB2.class, h.getExactBean(SubClassB2.class).getBeanClazz());
    assertNull(h.getExactBean(String.class));
  }

  @Test
  public void testGetRegisteredBeanWithMultipleRegistration() {
    BeanHierarchy<AbstractBaseClass> h = new BeanHierarchy<>(AbstractBaseClass.class);
    h.addBean(new BeanImplementor<>(new BeanMetaData(SubClassA.class)));
    assertEquals(SubClassA.class, h.getExactBean(SubClassA.class).getBeanClazz());

    SubClassA subA1 = Mockito.mock(SubClassA.class);
    SubClassA subA2 = Mockito.mock(SubClassA.class);
    h.addBean(new BeanImplementor<>(new BeanMetaData(SubClassA.class, subA1).withReplace(true).withApplicationScoped(true).withOrder(100)));
    h.addBean(new BeanImplementor<>(new BeanMetaData(SubClassA.class, subA2).withReplace(true).withApplicationScoped(true).withOrder(101)));

    assertEquals(SubClassA.class, h.getExactBean(SubClassA.class).getBeanClazz());
    assertEquals(subA1, h.getExactBean(SubClassA.class).getInstance()); // expect to get instance of registered bean with lowest order
  }

  @Test
  public void testInsertionOrderIsRespected() {
    BeanHierarchy<ITestInterface> h = new BeanHierarchy<>(ITestInterface.class);
    BeanImplementor<ITestInterface> bean1 = new BeanImplementor<>(new BeanMetaData(ITestInterface.class, Mockito.mock(ITestInterface.class)).withApplicationScoped(true).withOrder(100));
    BeanImplementor<ITestInterface> bean2 = new BeanImplementor<>(new BeanMetaData(ITestInterface.class, Mockito.mock(ITestInterface.class)).withApplicationScoped(true).withOrder(200));
    BeanImplementor<ITestInterface> bean3 = new BeanImplementor<>(new BeanMetaData(ITestInterface.class, Mockito.mock(ITestInterface.class)).withApplicationScoped(true).withOrder(200));
    h.addBean(bean1);
    h.addBean(bean2);
    h.addBean(bean3);

    assertEquals(Arrays.asList(bean1), h.sortedBeanCopy());
    h.removeBean(bean1);

    assertEquals(Arrays.asList(bean3), h.sortedBeanCopy());
  }

  public interface ITestInterface {
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
