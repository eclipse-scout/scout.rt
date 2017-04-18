package org.eclipse.scout.rt.mom.api.marshaller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.BeanMetaData;
import org.eclipse.scout.rt.platform.IBean;
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
}
