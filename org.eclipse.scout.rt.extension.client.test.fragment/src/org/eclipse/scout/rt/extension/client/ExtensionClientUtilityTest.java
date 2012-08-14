/*******************************************************************************
 * Copyright (c) 2012 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.extension.client;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.junit.Before;
import org.junit.Test;

/**
 * @since 3.9.0
 */
public class ExtensionClientUtilityTest {

  private List<Object> m_instanceList;
  private P_A m_a;
  private P_AExt m_aExt;
  private P_B m_b;
  private P_C m_c;
  private P_D m_d;

  @Before
  public void before() {
    m_a = new P_A();
    m_aExt = new P_AExt();
    m_b = new P_B();
    m_c = new P_C();
    m_d = new P_D();
    m_instanceList = new ArrayList<Object>();
    m_instanceList.add(m_a);
    m_instanceList.add(m_aExt);
    m_instanceList.add(m_b);
    m_instanceList.add(m_c);
  }

  @Test
  public void testRemoveByTypeNullAndEmpty() throws Exception {
    // expecting no exception
    ExtensionClientUtility.removeByType(null);
    ExtensionClientUtility.removeByType(null, (Class<?>) null);
    ExtensionClientUtility.removeByType(null, (Class<?>[]) null);

    List<IMenu> emptyList = Collections.emptyList();
    ExtensionClientUtility.removeByType(emptyList);
    assertTrue(emptyList.isEmpty());

    ExtensionClientUtility.removeByType(emptyList, (Class<?>) null);
    assertTrue(emptyList.isEmpty());

    ExtensionClientUtility.removeByType(emptyList, (Class<?>[]) null);
    assertTrue(emptyList.isEmpty());

    ExtensionClientUtility.removeByType(emptyList, IMenu.class);
    assertTrue(emptyList.isEmpty());
  }

  @Test
  public void testRemoveByType() throws Exception {
    // only m_a is removed, m_aExt is still in the list
    ExtensionClientUtility.removeByType(m_instanceList, P_A.class);
    assertTrue(m_instanceList.contains(m_aExt));
    assertEquals(Arrays.asList(m_aExt, m_b, m_c), m_instanceList);

    // no effects when removing types that are not part of the list
    ExtensionClientUtility.removeByType(m_instanceList, P_A.class);
    assertEquals(Arrays.asList(m_aExt, m_b, m_c), m_instanceList);

    // remove more than one object
    ExtensionClientUtility.removeByType(m_instanceList, P_C.class, P_AExt.class);
    assertEquals(Collections.singletonList(m_b), m_instanceList);
  }

  @Test
  public void testprocessReplaceAnnotationsNullAndEmpty() {
    // no exceptions
    ExtensionClientUtility.processReplaceAnnotations(null);
    ExtensionClientUtility.processReplaceAnnotations(Collections.emptyList());
  }

  @Test
  public void testprocessReplaceAnnotationsNoRemoveAnnotation() {
    List<Object> list = Arrays.asList(m_a, m_b, m_c);
    ExtensionClientUtility.processReplaceAnnotations(list);
    assertEquals(Arrays.asList(m_a, m_b, m_c), list);
  }

  @Test
  public void testprocessReplaceAnnotationsSuperclass() {
    // m_a is removed
    ExtensionClientUtility.processReplaceAnnotations(m_instanceList);
    assertEquals(Arrays.asList(m_aExt, m_b, m_c), m_instanceList);

    // no effect when processing the list a second time
    ExtensionClientUtility.processReplaceAnnotations(m_instanceList);
    assertEquals(Arrays.asList(m_aExt, m_b, m_c), m_instanceList);
  }

  @Test
  public void testprocessReplaceAnnotationsCustomCalss() {
    List<Object> list = new ArrayList<Object>();
    list.add(m_a);
    list.add(m_b);
    list.add(m_c);
    list.add(m_d);
    ExtensionClientUtility.processReplaceAnnotations(list);
    assertEquals(Arrays.asList(m_a, m_c, m_d), list);
  }

  @Test
  public void testprocessReplaceAnnotationsAbstractClass1() {
    P_E1 e1 = new P_E1();
    P_F f = new P_F();
    List<Object> list = new ArrayList<Object>();
    list.add(f);
    list.add(e1);
    ExtensionClientUtility.processReplaceAnnotations(list);
    assertEquals(Arrays.asList(f), list);
  }

  @Test
  public void testprocessReplaceAnnotationsAbstractClass2() {
    P_E1 e1 = new P_E1();
    P_E2 e2 = new P_E2();
    P_F f = new P_F();
    List<Object> list = new ArrayList<Object>();
    list.add(e2);
    list.add(f);
    list.add(e1);
    ExtensionClientUtility.processReplaceAnnotations(list);
    assertEquals(Arrays.asList(f, e1), list);
  }

  @Test
  public void testprocessReplaceAnnotationsPrimitiveType() {
    P_G g = new P_G();
    List<Object> list = new ArrayList<Object>();
    list.add(g);
    ExtensionClientUtility.processReplaceAnnotations(list);
    assertEquals(Arrays.asList(g), list);
  }

  private static class P_A {
  }

  @Replace
  private static class P_AExt extends P_A {
  }

  private static class P_B {
  }

  private static class P_C {
  }

  @Replace(P_B.class)
  private static class P_D {
  }

  private static abstract class AbstractE {
  }

  private static class P_E1 extends AbstractE {
  }

  private static class P_E2 extends AbstractE {
  }

  @Replace(AbstractE.class)
  private static class P_F {
  }

  @Replace(int.class)
  private static class P_G {
  }
}
