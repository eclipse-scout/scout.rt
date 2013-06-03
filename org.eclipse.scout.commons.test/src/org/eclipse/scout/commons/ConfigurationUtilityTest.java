/*******************************************************************************
 * Copyright (c) 2013 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.commons;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.scout.commons.annotations.IOrdered;
import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.commons.annotations.Replace;
import org.eclipse.scout.commons.fixture.A;
import org.eclipse.scout.commons.fixture.AbstractC;
import org.junit.Test;

/**
 * JUnit tests for {@link ConfigurationUtility}
 * 
 * @since 3.8.0
 */
public class ConfigurationUtilityTest {

  @Test
  public void sortByOrder() {
    // null and empty
    assertNull(ConfigurationUtility.sortByOrder(null));
    assertTrue(ConfigurationUtility.sortByOrder(Collections.emptyList()).isEmpty());
    //
    OrderStatic10 static10 = new OrderStatic10();
    OrderStatic20Dynamic0 static20Dynamic0 = new OrderStatic20Dynamic0();
    OrderDynamic30 dynamic30 = new OrderDynamic30();
    //
    List<Object> orderedElements = Arrays.asList(dynamic30, static20Dynamic0, static10);
    Collection<Object> sorted = ConfigurationUtility.sortByOrder(orderedElements);
    assertArrayEquals(new Object[]{static10, static20Dynamic0, dynamic30}, sorted.toArray());
    //
    ReplaceOrderStatic10 replaceOrderStatic10 = new ReplaceOrderStatic10();
    orderedElements = Arrays.<Object> asList(dynamic30, static20Dynamic0, replaceOrderStatic10);
    sorted = ConfigurationUtility.sortByOrder(orderedElements);
    assertArrayEquals(new Object[]{replaceOrderStatic10, static20Dynamic0, dynamic30}, sorted.toArray());
  }

  @Test
  public void getEnclosingContainerType() {
    // null
    assertNull(ConfigurationUtility.getEnclosingContainerType(null));
    // objects
    assertSame(A.class, ConfigurationUtility.getEnclosingContainerType(new A()));
    assertSame(A.class, ConfigurationUtility.getEnclosingContainerType(new A().b));
    assertSame(A.class, ConfigurationUtility.getEnclosingContainerType(new A().b.c1));
    assertSame(AbstractC.class, ConfigurationUtility.getEnclosingContainerType(new A().b.c1.d));
    assertSame(AbstractC.class, ConfigurationUtility.getEnclosingContainerType(new A().b.c1.d.e));
    assertSame(A.class, ConfigurationUtility.getEnclosingContainerType(new A().b.c2));
    assertSame(AbstractC.class, ConfigurationUtility.getEnclosingContainerType(new A().b.c2.d));
    assertSame(AbstractC.class, ConfigurationUtility.getEnclosingContainerType(new A().b.c2.d.e));
    // primitive values
    assertSame(Integer.class, ConfigurationUtility.getEnclosingContainerType(42));
    assertSame(Double.class, ConfigurationUtility.getEnclosingContainerType(42d));
  }

  @Test
  public void getEnclosingContainerTypeAbstractInnerClasses() {
    assertSame(ConfigurationUtilityTest.class, ConfigurationUtility.getEnclosingContainerType(new InnerA()));
    assertSame(ConfigurationUtilityTest.class, ConfigurationUtility.getEnclosingContainerType(new InnerA().b));
    assertSame(ConfigurationUtilityTest.class, ConfigurationUtility.getEnclosingContainerType(new InnerA().b.c1));
    assertSame(AbstractInnerC.class, ConfigurationUtility.getEnclosingContainerType(new InnerA().b.c1.d));
    assertSame(AbstractInnerC.class, ConfigurationUtility.getEnclosingContainerType(new InnerA().b.c1.d.e));
    assertSame(ConfigurationUtilityTest.class, ConfigurationUtility.getEnclosingContainerType(new InnerA().b.c2));
    assertSame(AbstractInnerC.class, ConfigurationUtility.getEnclosingContainerType(new InnerA().b.c2.d));
    assertSame(AbstractInnerC.class, ConfigurationUtility.getEnclosingContainerType(new InnerA().b.c2.d.e));
  }

  @Test(expected = NullPointerException.class)
  public void removeReplacedClassesNull() {
    ConfigurationUtility.removeReplacedClasses(null);
  }

  @Test
  public void removeReplacedClassesEmpty() {
    Class<?>[] classes = new Class<?>[0];
    Class<? extends Object>[] actual = ConfigurationUtility.removeReplacedClasses(classes);
    assertNotNull(actual);
    assertSame(classes, actual);
    assertArrayEquals(new Class[0], actual);
  }

  @Test
  public void removeReplacedClassesNoReplacements() {
    Class<?>[] classes = new Class<?>[]{Original.class};
    Class<? extends Object>[] actual = ConfigurationUtility.removeReplacedClasses(classes);
    assertNotNull(actual);
    assertSame(classes, actual);
    assertArrayEquals(new Class[]{Original.class}, actual);
    //
    classes = new Class<?>[]{Original.class, String.class};
    actual = ConfigurationUtility.removeReplacedClasses(classes);
    assertNotNull(actual);
    assertSame(classes, actual);
    assertArrayEquals(new Class[]{Original.class, String.class}, actual);
    //
    classes = new Class<?>[]{Original.class, String.class, Long.class};
    actual = ConfigurationUtility.removeReplacedClasses(classes);
    assertNotNull(actual);
    assertSame(classes, actual);
    assertArrayEquals(new Class[]{Original.class, String.class, Long.class}, actual);
  }

  @Test
  public void removeReplacedClasses() {
    Class<?>[] classes = new Class<?>[]{Original.class, Replacement.class};
    Class<? extends Object>[] actual = ConfigurationUtility.removeReplacedClasses(classes);
    assertNotNull(actual);
    assertNotSame(classes, actual);
    assertArrayEquals(new Class[]{Replacement.class}, actual);
    //
    classes = new Class<?>[]{Replacement.class, Original.class};
    actual = ConfigurationUtility.removeReplacedClasses(classes);
    assertNotNull(actual);
    assertNotSame(classes, actual);
    assertArrayEquals(new Class[]{Replacement.class}, actual);
  }

  @Test
  public void removeReplacedClassesReplacementHierarchy() {
    Class<?>[] classes = new Class<?>[]{Original.class, Replacement.class, Replacement2.class};
    Class<? extends Object>[] actual = ConfigurationUtility.removeReplacedClasses(classes);
    assertNotNull(actual);
    assertNotSame(classes, actual);
    assertArrayEquals(new Class[]{Replacement2.class}, actual);
    //
    classes = new Class<?>[]{Replacement3.class, Replacement.class, Original.class, Replacement2.class};
    actual = ConfigurationUtility.removeReplacedClasses(classes);
    assertNotNull(actual);
    assertNotSame(classes, actual);
    assertArrayEquals(new Class[]{Replacement3.class}, actual);
  }

  @Test
  public void removeReplacedClassesMultyReplacementHierarchy() {
    Class<?>[] classes = new Class<?>[]{Original.class, Replacement.class, OtherReplacement.class};
    Class<? extends Object>[] actual = ConfigurationUtility.removeReplacedClasses(classes);
    assertNotNull(actual);
    assertNotSame(classes, actual);
    Set<Class<?>> expectedContents = new HashSet<Class<?>>();
    expectedContents.add(Replacement.class);
    expectedContents.add(OtherReplacement.class);
    assertEquals(2, actual.length);
    assertTrue(expectedContents.contains(actual[0]));
    assertTrue(expectedContents.contains(actual[1]));
  }

  @Test
  public void removeReplacedClassesPreserveOrder() {
    Class<?>[] classes = new Class<?>[]{Original.class, String.class, Replacement.class};
    Class<? extends Object>[] actual = ConfigurationUtility.removeReplacedClasses(classes);
    assertNotNull(actual);
    assertNotSame(classes, actual);
    assertArrayEquals(new Class[]{Replacement.class, String.class}, actual);
  }

  @Test
  public void removeReplacedClassesReplacementHierarchyNotCompletelyPartOfOriginalList() {
    Class<?>[] classes = new Class<?>[]{Original.class, String.class, Replacement3.class};
    Class<? extends Object>[] actual = ConfigurationUtility.removeReplacedClasses(classes);
    assertNotNull(actual);
    assertNotSame(classes, actual);
    assertArrayEquals(new Class[]{Replacement3.class, String.class}, actual);
  }

  @Test(expected = NullPointerException.class)
  public void getReplacementMappingNull() {
    ConfigurationUtility.getReplacementMapping(null);
  }

  @Test
  public void getReplacementMappingEmpty() {
    Class<?>[] classes = new Class<?>[0];
    Map<Class<?>, Class<?>> actual = ConfigurationUtility.getReplacementMapping(classes);
    assertNotNull(actual);
    assertTrue(actual.isEmpty());
  }

  @Test
  public void getReplacementMappingNoReplacements() {
    Class<?>[] classes = new Class<?>[]{Original.class};
    Map<Class<?>, Class<?>> actual = ConfigurationUtility.getReplacementMapping(classes);
    assertNotNull(actual);
    assertNotNull(actual);
    assertTrue(actual.isEmpty());
    //
    classes = new Class<?>[]{Original.class, String.class};
    actual = ConfigurationUtility.getReplacementMapping(classes);
    assertNotNull(actual);
    assertNotNull(actual);
    assertTrue(actual.isEmpty());
    //
    classes = new Class<?>[]{Original.class, String.class, Long.class};
    actual = ConfigurationUtility.getReplacementMapping(classes);
    assertNotNull(actual);
    assertNotNull(actual);
    assertTrue(actual.isEmpty());
  }

  @Test
  public void getReplacementMapping() {
    Class<?>[] classes = new Class<?>[]{Original.class, Replacement.class};
    Map<Class<?>, Class<?>> actual = ConfigurationUtility.getReplacementMapping(classes);
    assertNotNull(actual);
    assertEquals(1, actual.size());
    assertSame(Replacement.class, actual.get(Original.class));
    //
    classes = new Class<?>[]{Replacement.class, Original.class};
    actual = ConfigurationUtility.getReplacementMapping(classes);
    assertNotNull(actual);
    assertEquals(1, actual.size());
    assertSame(Replacement.class, actual.get(Original.class));
    //
    classes = new Class<?>[]{Original.class, String.class, Replacement.class};
    actual = ConfigurationUtility.getReplacementMapping(classes);
    assertNotNull(actual);
    assertEquals(1, actual.size());
    assertSame(Replacement.class, actual.get(Original.class));
  }

  @Test
  public void getReplacementMappingReplacementHierarchy() {
    Class<?>[] classes = new Class<?>[]{Original.class, Replacement.class, Replacement2.class};
    Map<Class<?>, Class<?>> actual = ConfigurationUtility.getReplacementMapping(classes);
    assertNotNull(actual);
    assertEquals(2, actual.size());
    assertSame(Replacement2.class, actual.get(Original.class));
    assertSame(Replacement2.class, actual.get(Replacement.class));
    //
    classes = new Class<?>[]{Replacement3.class, Replacement.class, Original.class, Replacement2.class};
    actual = ConfigurationUtility.getReplacementMapping(classes);
    assertNotNull(actual);
    assertEquals(3, actual.size());
    assertSame(Replacement3.class, actual.get(Original.class));
    assertSame(Replacement3.class, actual.get(Replacement.class));
    assertSame(Replacement3.class, actual.get(Replacement2.class));
  }

  @Test
  public void getReplacementMappingMultyReplacementHierarchy() {
    Class<?>[] classes = new Class<?>[]{Original.class, Replacement.class, OtherReplacement.class};
    Map<Class<?>, Class<?>> actual = ConfigurationUtility.getReplacementMapping(classes);
    assertNotNull(actual);
    assertEquals(1, actual.size());
    Class<?> replacedClass = actual.get(Original.class);
    assertNotNull(replacedClass);
    assertTrue(replacedClass == Replacement.class || replacedClass == OtherReplacement.class);
  }

  @Test
  public void getReplacementMappingReplacementHierarchyNotCompletelyPartOfOriginalList() {
    Class<?>[] classes = new Class<?>[]{Original.class, String.class, Replacement3.class};
    Map<Class<?>, Class<?>> actual = ConfigurationUtility.getReplacementMapping(classes);
    assertNotNull(actual);
    assertEquals(3, actual.size());
    assertSame(Replacement3.class, actual.get(Original.class));
    assertSame(Replacement3.class, actual.get(Replacement.class));
    assertSame(Replacement3.class, actual.get(Replacement2.class));
  }

  public static class InnerA {
    public InnerB b = new InnerB();

    public class InnerB {
      public InnerC1 c1 = new InnerC1();
      public InnerC2 c2 = new InnerC2();

      public class InnerC1 extends AbstractInnerC {
      }

      public class InnerC2 extends AbstractInnerC {
      }
    }
  }

  public static abstract class AbstractInnerC {
    public InnerD d = new InnerD();

    public class InnerD {
      public InnerE e = new InnerE();

      public class InnerE {
      }
    }
  }

  @Order(10)
  public static class OrderStatic10 {
  }

  @Order(20)
  public static class OrderStatic20Dynamic0 implements IOrdered {
    @Override
    public double getOrder() {
      return 0;
    }
  }

  public static class OrderDynamic30 implements IOrdered {
    @Override
    public double getOrder() {
      return 30;
    }
  }

  @Replace
  public static class ReplaceOrderStatic10 extends OrderStatic10 implements IOrdered {
    @Override
    public double getOrder() {
      return 40;
    }
  }

  public static class Original {
  }

  @Replace
  public static class Replacement extends Original {
  }

  @Replace
  public static class Replacement2 extends Replacement {
  }

  @Replace
  public static class Replacement3 extends Replacement2 {
  }

  @Replace
  public static class OtherReplacement extends Original {
  }
}
