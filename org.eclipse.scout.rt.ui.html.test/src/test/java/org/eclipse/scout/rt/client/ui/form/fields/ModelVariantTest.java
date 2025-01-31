/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.form.fields;

import static org.junit.Assert.*;

import org.eclipse.scout.rt.ui.html.json.JsonAdapterUtility;
import org.junit.Test;

public class ModelVariantTest {

  @ModelVariant("Foo")
  static class A {
  }

  static class B extends A {
  }

  static class C {
  }

  @ModelVariant("mynamespace.Bar")
  static class D {
  }

  /**
   * This test would fail if the ModelVariant isn't annotated with @Inherited.
   */
  @Test
  public void testInheritance() {
    assertTrue(new A().getClass().isAnnotationPresent(ModelVariant.class));
    assertTrue(new B().getClass().isAnnotationPresent(ModelVariant.class));
  }

  @Test
  public void testObjectType() {
    assertEquals("BeanColumn:Foo", JsonAdapterUtility.getObjectType("BeanColumn", new A()));
    assertEquals("BeanColumn:Foo", JsonAdapterUtility.getObjectType("BeanColumn", new B()));
    assertEquals("BeanColumn", JsonAdapterUtility.getObjectType("BeanColumn", new C()));
    assertEquals("BeanColumn:mynamespace.Bar", JsonAdapterUtility.getObjectType("BeanColumn", new D()));
  }
}
