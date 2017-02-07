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
