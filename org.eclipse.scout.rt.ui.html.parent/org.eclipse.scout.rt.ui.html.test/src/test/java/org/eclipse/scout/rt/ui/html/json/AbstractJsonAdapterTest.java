/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.ui.html.json;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.eclipse.scout.rt.client.ui.form.fields.ModelVariant;
import org.eclipse.scout.rt.ui.html.json.fixtures.JsonSessionMock;
import org.json.JSONObject;
import org.junit.Test;

public class AbstractJsonAdapterTest {

  @ModelVariant("Bar")
  static class P_ModelVariant {

  }

  @Test
  public void testToJson() {
    JSONObject json = newAdapter(new Object()).toJson();
    assertEquals("x", json.optString("id"));
    assertEquals("Foo", json.optString("objectType"));
  }

  @Test
  public void testToJson_WithVariant() {
    JSONObject json = newAdapter(new P_ModelVariant()).toJson();
    assertEquals("x", json.optString("id"));
    assertEquals("Foo.Bar", json.optString("objectType"));
  }

  @Test
  public void testIsAttached() {
    AbstractJsonAdapter<?> adapter = newAdapter(new Object());
    adapter.attach();
    assertTrue(adapter.isAttached());

    adapter.dispose();
    assertFalse(adapter.isAttached());
  }

  private AbstractJsonAdapter<?> newAdapter(Object model) {
    AbstractJsonAdapter<?> adapter = new AbstractJsonAdapter<Object>(model, new JsonSessionMock(), "x", null) {
      @Override
      public String getObjectType() {
        return "Foo";
      }
    };
    return adapter;
  }
}
