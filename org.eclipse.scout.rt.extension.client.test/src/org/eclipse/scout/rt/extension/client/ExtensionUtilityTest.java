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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.eclipse.scout.commons.annotations.Replace;
import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.extension.client.EnclosingObjectFixture.InnerClass.InnerInnerClass;
import org.eclipse.scout.rt.extension.client.EnclosingObjectFixture.InnerClass.InnerInnerClass.InnerInnerInnerClass;
import org.eclipse.scout.rt.extension.client.EnclosingObjectFixture.StaticInnerClass.InnerStaticInnerClass;
import org.eclipse.scout.rt.extension.client.EnclosingObjectFixture.StaticInnerClass.InnerStaticInnerClass.InnerInnerStaticInnerClass;
import org.eclipse.scout.rt.extension.client.EnclosingObjectFixture.StaticPathInnerClass.InnerStaticPathInnerClass.InnerInnerStaticPathInnerClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @since 3.9.0
 */
public class ExtensionUtilityTest {

  private List<Object> m_instanceList;
  private P_A m_a;
  private P_AExt m_aExt;
  private P_B m_b;
  private P_C m_c;

  @Before
  public void before() {
    m_a = new P_A();
    m_aExt = new P_AExt();
    m_b = new P_B();
    m_c = new P_C();
    m_instanceList = new ArrayList<Object>();
    m_instanceList.add(m_a);
    m_instanceList.add(m_aExt);
    m_instanceList.add(m_b);
    m_instanceList.add(m_c);
  }

  @Test
  public void testRemoveByTypeNullAndEmpty() throws Exception {
    // expecting no exception
    ExtensionUtility.removeByType(null);
    ExtensionUtility.removeByType(null, (Class<?>) null);
    ExtensionUtility.removeByType(null, (Class<?>[]) null);

    List<IMenu> emptyList = Collections.emptyList();
    ExtensionUtility.removeByType(emptyList);
    Assert.assertTrue(emptyList.isEmpty());

    ExtensionUtility.removeByType(emptyList, (Class<?>) null);
    Assert.assertTrue(emptyList.isEmpty());

    ExtensionUtility.removeByType(emptyList, (Class<?>[]) null);
    Assert.assertTrue(emptyList.isEmpty());

    ExtensionUtility.removeByType(emptyList, IMenu.class);
    Assert.assertTrue(emptyList.isEmpty());
  }

  @Test
  public void testRemoveByType() throws Exception {
    // only m_a is removed, m_aExt is still in the list
    ExtensionUtility.removeByType(m_instanceList, P_A.class);
    Assert.assertTrue(m_instanceList.contains(m_aExt));
    Assert.assertEquals(Arrays.asList(m_aExt, m_b, m_c), m_instanceList);

    // no effects when removing types that are not part of the list
    ExtensionUtility.removeByType(m_instanceList, P_A.class);
    Assert.assertEquals(Arrays.asList(m_aExt, m_b, m_c), m_instanceList);

    // remove more than one object
    ExtensionUtility.removeByType(m_instanceList, P_C.class, P_AExt.class);
    Assert.assertEquals(Collections.singletonList(m_b), m_instanceList);
  }

  @Test
  public void testGetEnclosingObject() {
    Assert.assertNull(ExtensionUtility.getEnclosingObject(null));
    Assert.assertNull(ExtensionUtility.getEnclosingObject(Long.valueOf(42)));
    Assert.assertNull(ExtensionUtility.getEnclosingObject(new EnclosingObjectFixture()));
    //
    // static inner class
    EnclosingObjectFixture.StaticInnerClass staticInner = new EnclosingObjectFixture.StaticInnerClass();
    Assert.assertNull(ExtensionUtility.getEnclosingObject(staticInner));
    //
    InnerStaticInnerClass innerStaticInner = staticInner.createInner();
    Assert.assertSame(staticInner, ExtensionUtility.getEnclosingObject(innerStaticInner));
    //
    InnerInnerStaticInnerClass innerInnerStaticInner = innerStaticInner.createInner();
    Assert.assertSame(innerStaticInner, ExtensionUtility.getEnclosingObject(innerInnerStaticInner));
    //
    Assert.assertSame(innerInnerStaticInner, ExtensionUtility.getEnclosingObject(innerInnerStaticInner.createInner()));
    //
    // statci path inner class
    Assert.assertNull(ExtensionUtility.getEnclosingObject(new EnclosingObjectFixture.StaticPathInnerClass()));
    Assert.assertNull(ExtensionUtility.getEnclosingObject(new EnclosingObjectFixture.StaticPathInnerClass.InnerStaticPathInnerClass()));
    //
    InnerInnerStaticPathInnerClass innerInnerStaticPathInner = new InnerInnerStaticPathInnerClass();
    Assert.assertNull(ExtensionUtility.getEnclosingObject(innerInnerStaticPathInner));
    //
    Assert.assertSame(innerInnerStaticPathInner, ExtensionUtility.getEnclosingObject(innerInnerStaticPathInner.createInner()));
    //
    // inner class
    EnclosingObjectFixture primary = new EnclosingObjectFixture();
    EnclosingObjectFixture.InnerClass inner = primary.createInner();
    Assert.assertSame(primary, ExtensionUtility.getEnclosingObject(inner));
    //
    InnerInnerClass innerInner = inner.createInner();
    Assert.assertSame(inner, ExtensionUtility.getEnclosingObject(innerInner));
    //
    InnerInnerInnerClass innerInnerInner = innerInner.createInner();
    Assert.assertSame(innerInner, ExtensionUtility.getEnclosingObject(innerInnerInner));
    //
    Assert.assertSame(innerInnerInner, ExtensionUtility.getEnclosingObject(innerInnerInner.createInner()));
  }

  @Test
  public void testGetEnclosingObjectByType() {
    Assert.assertNull(ExtensionUtility.getEnclosingObject(null, null));
    Assert.assertNull(ExtensionUtility.getEnclosingObject(Long.valueOf(42), null));
    Assert.assertNull(ExtensionUtility.getEnclosingObject(new EnclosingObjectFixture(), null));
    //
    // static inner class
    EnclosingObjectFixture.StaticInnerClass staticInner = new EnclosingObjectFixture.StaticInnerClass();
    Assert.assertNull(ExtensionUtility.getEnclosingObject(staticInner, EnclosingObjectFixture.StaticInnerClass.class));
    //
    InnerStaticInnerClass innerStaticInner = staticInner.createInner();
    Assert.assertSame(staticInner, ExtensionUtility.getEnclosingObject(innerStaticInner, EnclosingObjectFixture.StaticInnerClass.class));
    //
    InnerInnerStaticInnerClass innerInnerStaticInner = innerStaticInner.createInner();
    Assert.assertSame(staticInner, ExtensionUtility.getEnclosingObject(innerInnerStaticInner, EnclosingObjectFixture.StaticInnerClass.class));
    //
    Assert.assertSame(staticInner, ExtensionUtility.getEnclosingObject(innerInnerStaticInner.createInner(), EnclosingObjectFixture.StaticInnerClass.class));
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

  private abstract static class AbstractE {
  }

  private static class P_E1 extends AbstractE {
  }

  private static class P_E2 extends AbstractE {
  }
}
