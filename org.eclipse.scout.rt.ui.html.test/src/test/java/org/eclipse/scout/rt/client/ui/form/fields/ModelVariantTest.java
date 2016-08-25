/*******************************************************************************
 * Copyright (c) 2014-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client.ui.form.fields;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.eclipse.scout.rt.ui.html.json.JsonAdapterUtility;
import org.junit.Test;

public class ModelVariantTest {

  @ModelVariant("foo")
  static class A {
  }

  static class B extends A {
  }

  static class C {
  }

  /**
   * This test would fail if the ModelVariant isn't annotated with @Inherited.
   */
  @Test
  public void testInheritance() {
    A a = new A();
    B b = new B();
    assertTrue(a.getClass().isAnnotationPresent(ModelVariant.class));
    assertTrue(b.getClass().isAnnotationPresent(ModelVariant.class));
  }

  @Test
  public void testObjectType() {
    A a = new A();
    B b = new B();
    C c = new C();
    String objectTypeA = JsonAdapterUtility.getObjectType("Test", a);
    String objectTypeB = JsonAdapterUtility.getObjectType("Test", b);
    String objectTypeC = JsonAdapterUtility.getObjectType("Test", c);
    assertEquals("Test:foo", objectTypeA);
    assertEquals("Test:foo", objectTypeB);
    assertEquals("Test", objectTypeC);
  }
}
