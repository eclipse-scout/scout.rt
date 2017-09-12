/*******************************************************************************
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.platform.reflect;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;

import org.junit.Test;

/**
 * JUnit tests for {@link FastBeanUtility}
 *
 * @since 3.9.0
 */
public class FastBeanUtilityTest {

  public static class MyBean {
    public Long getId() {
      return null;
    }

    @SuppressWarnings("unused")
    private void myMethod() {
    }

    public void setId(Long id) {
    }
  }

  public static class MyOtherBean {
    public Long getId() {
      return null;
    }

    @SuppressWarnings("unused")
    private void myMethod() {
    }

    public void setId(Long id) {
    }
  }

  public abstract static class AbstractSimple<ID> {
  }

  public static class LongSimple extends AbstractSimple<Long> {
    public Long getId() {
      return null;
    }

    public void setId(Long id) {

    }
  }

  public abstract static class AbstractGet<ID> {
    public abstract ID getId();
  }

  public static class LongGetWithSet extends AbstractGet<Long> {
    @Override
    public Long getId() {
      return null;
    }

    public void setId(Long id) {

    }
  }

  public abstract static class AbstractSet<ID> {
    public abstract void setId(ID id);
  }

  public static class LongSetWithGet extends AbstractSet<Long> {
    @Override
    public void setId(Long id) {
    }

    public Long getId() {
      return null;
    }
  }

  public abstract static class AbstractGetSet<ID> {
    public abstract ID getId();

    public abstract void setId(ID id);
  }

  public static class LongGetSet extends AbstractGetSet<Long> {
    @Override
    public Long getId() {
      return null;
    }

    @Override
    public void setId(Long id) {
    }
  }

  /**
   * Test for Bug 400240
   */
  @Test
  public void testDeclaredPublicMethods() {
    Method[] methods = FastBeanUtility.getDeclaredPublicMethods(MyBean.class);
    assertEquals("length", 2, methods.length);

    ArrayList<String> methodNames = new ArrayList<String>();
    for (Method method : methods) {
      methodNames.add(method.getName());
    }
    assertEquals("contains getId()", true, methodNames.contains("getId"));
    assertEquals("contains setId()", true, methodNames.contains("setId"));
  }

  @Test
  public void testSimple() {
    Class clazz1 = LongSimple.class;
    Method[] methods1 = FastBeanUtility.getDeclaredPublicMethods(clazz1);
    assertEquals(2, methods1.length);
    //
    Class clazz2 = clazz1.getSuperclass();
    Method[] methods2 = FastBeanUtility.getDeclaredPublicMethods(clazz2);
    assertEquals(0, methods2.length);
    //try all permutations
    for (Method[] m1 : perm2(methods1)) {
      HashMap<String, FastPropertyDescriptor> contributeMap = new HashMap<String, FastPropertyDescriptor>();
      FastBeanUtility.contributePropertyDescriptors(clazz1, clazz1, m1, contributeMap);
      assertEquals(1, contributeMap.size());
      assertNotNull(contributeMap.get("id"));
      assertNotNull(contributeMap.get("id").getReadMethod());
      assertNotNull(contributeMap.get("id").getWriteMethod());
      assertEquals(Long.class, contributeMap.get("id").getPropertyType());
    }
  }

  @Test
  public void testGetWithSet() {
    Class clazz1 = LongGetWithSet.class;
    Method[] methods1 = FastBeanUtility.getDeclaredPublicMethods(clazz1);
    assertEquals(3, methods1.length);
    //
    Class clazz2 = clazz1.getSuperclass();
    Method[] methods2 = FastBeanUtility.getDeclaredPublicMethods(clazz2);
    assertEquals(1, methods2.length);
    //try all permutations
    for (Method[] m1 : perm3(methods1)) {
      for (Method[] m2 : perm1(methods2)) {
        HashMap<String, FastPropertyDescriptor> contributeMap = new HashMap<String, FastPropertyDescriptor>();
        FastBeanUtility.contributePropertyDescriptors(clazz1, clazz1, m1, contributeMap);
        FastBeanUtility.contributePropertyDescriptors(clazz1, clazz2, m2, contributeMap);
        assertEquals(1, contributeMap.size());
        assertNotNull(contributeMap.get("id"));
        assertNotNull(contributeMap.get("id").getReadMethod());
        assertNotNull(contributeMap.get("id").getWriteMethod());
        assertEquals(Long.class, contributeMap.get("id").getPropertyType());
      }
    }
  }

  @Test
  public void testSetWithGet() {
    Class clazz1 = LongSetWithGet.class;
    Method[] methods1 = FastBeanUtility.getDeclaredPublicMethods(clazz1);
    assertEquals(3, methods1.length);
    //
    Class clazz2 = clazz1.getSuperclass();
    Method[] methods2 = FastBeanUtility.getDeclaredPublicMethods(clazz2);
    assertEquals(1, methods2.length);
    //try all permutations
    for (Method[] m1 : perm3(methods1)) {
      for (Method[] m2 : perm1(methods2)) {
        HashMap<String, FastPropertyDescriptor> contributeMap = new HashMap<String, FastPropertyDescriptor>();
        FastBeanUtility.contributePropertyDescriptors(clazz1, clazz1, m1, contributeMap);
        FastBeanUtility.contributePropertyDescriptors(clazz1, clazz2, m2, contributeMap);
        assertEquals(1, contributeMap.size());
        assertNotNull(contributeMap.get("id"));
        assertNotNull(contributeMap.get("id").getReadMethod());
        assertNotNull(contributeMap.get("id").getWriteMethod());
        assertEquals(Long.class, contributeMap.get("id").getPropertyType());
      }
    }
  }

  @Test
  public void testGetSet() {
    Class clazz1 = LongGetSet.class;
    Method[] methods1 = FastBeanUtility.getDeclaredPublicMethods(clazz1);
    assertEquals(4, methods1.length);
    //
    Class clazz2 = clazz1.getSuperclass();
    Method[] methods2 = FastBeanUtility.getDeclaredPublicMethods(clazz2);
    assertEquals(2, methods2.length);
    //try all permutations
    for (Method[] m1 : perm4(methods1)) {
      for (Method[] m2 : perm2(methods2)) {
        HashMap<String, FastPropertyDescriptor> contributeMap = new HashMap<String, FastPropertyDescriptor>();
        FastBeanUtility.contributePropertyDescriptors(clazz1, clazz1, m1, contributeMap);
        FastBeanUtility.contributePropertyDescriptors(clazz1, clazz2, m2, contributeMap);
        assertEquals(1, contributeMap.size());
        assertNotNull(contributeMap.get("id"));
        assertNotNull(contributeMap.get("id").getReadMethod());
        assertNotNull(contributeMap.get("id").getWriteMethod());
        assertEquals(Long.class, contributeMap.get("id").getPropertyType());
      }
    }
  }

  @Test
  public void testCompareMethods() {
    Class clazz1 = MyBean.class;
    Method[] methods1 = FastBeanUtility.getDeclaredPublicMethods(clazz1);
    Class clazz2 = MyOtherBean.class;
    Method[] methods2 = FastBeanUtility.getDeclaredPublicMethods(clazz2);

    assertTrue(FastBeanUtility.compareMethods(null, null));
    assertFalse(FastBeanUtility.compareMethods(methods1[0], null));
    assertFalse(FastBeanUtility.compareMethods(null, methods2[0]));
    assertTrue(FastBeanUtility.compareMethods(methods1[0], methods1[0]));
    assertFalse(FastBeanUtility.compareMethods(methods1[0], methods2[0]));
  }

  private ArrayList<Method[]> perm1(Method[] m) {
    ArrayList<Method[]> list = new ArrayList<Method[]>();
    list.add(m);
    return list;
  }

  private ArrayList<Method[]> perm2(Method[] m) {
    ArrayList<Method[]> list = new ArrayList<Method[]>();
    for (int i = 0; i < 2; i++) {
      for (int j = 0; j < 2; j++) {
        if (j == i) {
          continue;
        }
        list.add(new Method[]{m[i], m[j]});
      }
    }
    return list;
  }

  private ArrayList<Method[]> perm3(Method[] m) {
    ArrayList<Method[]> list = new ArrayList<Method[]>();
    for (int i = 0; i < 3; i++) {
      for (int j = 0; j < 3; j++) {
        if (j == i) {
          continue;
        }
        for (int k = 0; k < 3; k++) {
          if (k == i || k == j) {
            continue;
          }
          list.add(new Method[]{m[i], m[j], m[k]});
        }
      }
    }
    return list;
  }

  private ArrayList<Method[]> perm4(Method[] m) {
    ArrayList<Method[]> list = new ArrayList<Method[]>();
    for (int i = 0; i < 4; i++) {
      for (int j = 0; j < 4; j++) {
        if (j == i) {
          continue;
        }
        for (int k = 0; k < 4; k++) {
          if (k == i || k == j) {
            continue;
          }
          for (int l = 0; l < 4; l++) {
            if (l == i || l == j || l == k) {
              continue;
            }
            list.add(new Method[]{m[i], m[j], m[k], m[l]});
          }
        }
      }
    }
    return list;
  }
}
