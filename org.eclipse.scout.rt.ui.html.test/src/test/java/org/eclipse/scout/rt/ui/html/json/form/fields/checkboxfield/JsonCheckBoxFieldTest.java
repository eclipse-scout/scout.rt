/*
 * Copyright (c) 2014-2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.ui.html.json.form.fields.checkboxfield;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.eclipse.scout.rt.client.testenvironment.TestEnvironmentClientSession;
import org.eclipse.scout.rt.client.ui.form.fields.IValueField;
import org.eclipse.scout.rt.client.ui.form.fields.booleanfield.AbstractBooleanField;
import org.eclipse.scout.rt.client.ui.form.fields.booleanfield.IBooleanField;
import org.eclipse.scout.rt.testing.client.runner.ClientTestRunner;
import org.eclipse.scout.rt.testing.client.runner.RunWithClientSession;
import org.eclipse.scout.rt.testing.platform.runner.RunWithSubject;
import org.eclipse.scout.rt.ui.html.UiSessionTestUtility;
import org.eclipse.scout.rt.ui.html.json.JsonEvent;
import org.eclipse.scout.rt.ui.html.json.fixtures.UiSessionMock;
import org.eclipse.scout.rt.ui.html.json.form.fields.checkbox.JsonCheckBoxField;
import org.eclipse.scout.rt.ui.html.json.testing.JsonTestUtility;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(ClientTestRunner.class)
@RunWithSubject("default")
@RunWithClientSession(TestEnvironmentClientSession.class)
public class JsonCheckBoxFieldTest {
  private UiSessionMock m_uiSession;

  @Before
  public void setUp() {
    m_uiSession = new UiSessionMock();
  }

  @Test
  public void testUiValueChangeEvent() throws JSONException {
    IBooleanField field = new AbstractBooleanField() {
    };
    JsonCheckBoxField<IBooleanField> jsonField = UiSessionTestUtility.newJsonAdapter(m_uiSession, field, null);
    jsonField.toJson();

    JsonEvent event = createJsonValueChangeEvent(jsonField.getId(), true);
    jsonField.handleUiEvent(event);
    assertTrue(field.isChecked());

    event = createJsonValueChangeEvent(jsonField.getId(), false);
    jsonField.handleUiEvent(event);
    assertFalse(field.isChecked());
  }

  /**
   * Value is adjusted in execValidateValue -> New value has to be sent to UI correctly
   */
  @Test
  public void testValueRevertWhileUiValueChangeEvent() throws JSONException {
    IBooleanField field = new AbstractBooleanField() {
      @Override
      protected Boolean execValidateValue(Boolean rawValue) {
        return false;
      }
    };
    JsonCheckBoxField<IBooleanField> jsonField = UiSessionTestUtility.newJsonAdapter(m_uiSession, field, null);
    jsonField.toJson();

    JsonEvent event = createJsonValueChangeEvent(jsonField.getId(), true);
    jsonField.handleUiEvent(event);
    assertFalse(field.isChecked());

    Object value = JsonTestUtility.extractProperty(m_uiSession.currentJsonResponse(), jsonField.getId(), IValueField.PROP_VALUE);
    assertFalse((Boolean) value);
  }

  public static JsonEvent createJsonValueChangeEvent(String adapterId, boolean value) throws JSONException {
    JSONObject data = new JSONObject();
    data.put("value", value);
    return new JsonEvent(adapterId, "property", data);
  }
}
