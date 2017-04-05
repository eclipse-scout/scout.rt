package org.eclipse.scout.rt.mom.api.marshaller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.scout.rt.platform.AnnotationFactory;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.BeanMetaData;
import org.eclipse.scout.rt.platform.CreateImmediately;
import org.eclipse.scout.rt.platform.IBean;
import org.eclipse.scout.rt.platform.Replace;
import org.eclipse.scout.rt.platform.internal.BeanImplementor;
import org.eclipse.scout.rt.testing.shared.TestingUtility;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * Test for {@link BeanManagerUtility}
 */
public class BeanManagerUtilityTest {

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

  private static final List<IBean<?>> s_beans = new ArrayList<>();

  protected static final BeanManagerUtility s_utility = BEANS.get(BeanManagerUtility.class);

  @BeforeClass
  public static void beforeClass() throws Exception {
    s_beans.add(TestingUtility.registerBean(new BeanMetaData(SubClassA.class)));
    s_beans.add(TestingUtility.registerBean(new BeanMetaData(SubClassB.class)));
    s_beans.add(TestingUtility.registerBean(new BeanMetaData(SubClassB1.class).withReplace(true)));
    s_beans.add(TestingUtility.registerBean(new BeanMetaData(SubClassB2.class).withOrder(TestingUtility.TESTING_BEAN_ORDER + 1).withReplace(true)));
  }

  @AfterClass
  public static void afterClass() throws Exception {
    TestingUtility.unregisterBeans(s_beans);
  }

  @Test
  public void testIsBeanClass() {
    assertTrue(s_utility.isBeanClass(SubClassA.class));
    assertTrue(s_utility.isBeanClass(SubClassB.class));
    assertTrue(s_utility.isBeanClass(SubClassB1.class));
    assertTrue(s_utility.isBeanClass(SubClassB2.class));
    assertTrue(s_utility.isBeanClass(BeanManagerUtility.class));
    assertFalse(s_utility.isBeanClass(String.class));
  }

  @Test
  public void testLookupClass_noScoutBean() {
    assertNull(s_utility.lookupClass(String.class));
  }

  @Test
  public void testLookupClass_oneScoutBean() {
    assertEquals(SubClassA.class, s_utility.lookupClass(SubClassA.class));
  }

  @Test
  public void testLookupClass_uniqueScoutBean() {
    assertEquals(SubClassB1.class, s_utility.lookupClass(SubClassB.class));
  }

  @Test
  public void testLookupClass_multipleScoutBeans() {
    assertNull(s_utility.lookupClass(AbstractBaseClass.class));
  }

  @Test
  public void testLookupRegisteredBean() {
    assertEquals(SubClassA.class, s_utility.lookupRegisteredBean(SubClassA.class).getBeanClazz());
    assertEquals(SubClassB.class, s_utility.lookupRegisteredBean(SubClassB.class).getBeanClazz());
    assertEquals(SubClassB1.class, s_utility.lookupRegisteredBean(SubClassB1.class).getBeanClazz());
    assertEquals(SubClassB2.class, s_utility.lookupRegisteredBean(SubClassB2.class).getBeanClazz());
    assertNull(s_utility.lookupRegisteredBean(String.class));
  }

  @JsonTypeName("foo")
  public static class ClassWithJsonTypeName {
  }

  @Test
  public void testHasAnnotation() {
    assertFalse(s_utility.hasAnnotation(null, null));
    assertFalse(s_utility.hasAnnotation(null, Replace.class));
    assertFalse(s_utility.hasAnnotation(new BeanImplementor<>(new BeanMetaData(String.class)), Replace.class));

    assertTrue(s_utility.hasAnnotation(new BeanImplementor<>(new BeanMetaData(String.class).withReplace(true)), Replace.class));
    assertTrue(s_utility.hasAnnotation(new BeanImplementor<>(new BeanMetaData(String.class).withAnnotation(AnnotationFactory.createCreateImmediately())), CreateImmediately.class));
    assertTrue(s_utility.hasAnnotation(new BeanImplementor<>(new BeanMetaData(ClassWithJsonTypeName.class)), JsonTypeName.class));
  }
}
