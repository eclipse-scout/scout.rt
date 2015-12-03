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
package org.eclipse.scout.rt.ui.html.json;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.eclipse.scout.rt.client.testenvironment.TestEnvironmentClientSession;
import org.eclipse.scout.rt.client.ui.form.fields.ModelVariant;
import org.eclipse.scout.rt.testing.client.runner.ClientTestRunner;
import org.eclipse.scout.rt.testing.client.runner.RunWithClientSession;
import org.eclipse.scout.rt.testing.platform.runner.RunWithSubject;
import org.eclipse.scout.rt.ui.html.json.fixtures.UiSessionMock;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(ClientTestRunner.class)
@RunWithSubject("default")
@RunWithClientSession(TestEnvironmentClientSession.class)
public class AbstractJsonAdapterTest {

  @ModelVariant("Bar")
  static class P_ModelVariant {
  }

  @Test
  public void testToJson() {
    JSONObject json = newAdapter(new Object()).toJson();
    assertEquals("x", json.getString("id"));
    assertEquals("Foo", json.getString("objectType"));
  }

  @Test
  public void testToJson_WithVariant() {
    JSONObject json = newAdapter(new P_ModelVariant()).toJson();
    assertEquals("x", json.getString("id"));
    assertEquals("Foo.Bar", json.getString("objectType"));
  }

  @Test
  public void testIsAttached() {
    AbstractJsonAdapter<?> adapter = newAdapter(new Object());
    adapter.init();
    assertTrue(adapter.isInitialized());

    adapter.dispose();
    assertTrue(adapter.isDisposed());
  }

  private AbstractJsonAdapter<?> newAdapter(Object model) {
    AbstractJsonAdapter<?> adapter = new AbstractJsonAdapter<Object>(model, new UiSessionMock(), "x", null) {
      @Override
      public String getObjectType() {
        return "Foo";
      }
    };
    return adapter;
  }
}
