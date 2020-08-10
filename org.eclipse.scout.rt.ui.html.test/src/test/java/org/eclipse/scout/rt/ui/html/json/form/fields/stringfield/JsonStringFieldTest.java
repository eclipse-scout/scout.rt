/*
 * Copyright (c) 2014-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.ui.html.json.form.fields.stringfield;

import static org.junit.Assert.assertEquals;

import org.eclipse.scout.rt.client.testenvironment.TestEnvironmentClientSession;
import org.eclipse.scout.rt.client.ui.form.fields.IValueField;
import org.eclipse.scout.rt.client.ui.form.fields.stringfield.AbstractStringField;
import org.eclipse.scout.rt.client.ui.form.fields.stringfield.IStringField;
import org.eclipse.scout.rt.testing.client.runner.ClientTestRunner;
import org.eclipse.scout.rt.testing.client.runner.RunWithClientSession;
import org.eclipse.scout.rt.testing.platform.runner.RunWithSubject;
import org.eclipse.scout.rt.ui.html.UiSessionTestUtility;
import org.eclipse.scout.rt.ui.html.json.JsonEvent;
import org.eclipse.scout.rt.ui.html.json.fixtures.UiSessionMock;
import org.eclipse.scout.rt.ui.html.json.form.fields.BaseFormFieldTest;
import org.eclipse.scout.rt.ui.html.json.form.fields.JsonValueField;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Testing: {@link AbstractStringField}
 */
@RunWith(ClientTestRunner.class)
@RunWithSubject("default")
@RunWithClientSession(TestEnvironmentClientSession.class)
public class JsonStringFieldTest extends BaseFormFieldTest {

  private JsonStringField<IStringField> m_jsonStringField;
  private IStringField m_stringField;

  @Before
  public void setUp() {
    m_stringField = new AbstractStringField() {
    };
    m_jsonStringField = UiSessionTestUtility.newJsonAdapter(new UiSessionMock(), m_stringField);
  }

  @Test
  public void testToJson() throws JSONException {
    m_stringField.setMultilineText(true);
    JSONObject json = m_jsonStringField.toJson();
    assertEquals(true, json.get(IStringField.PROP_MULTILINE_TEXT));
  }

  @Test
  public void testDisplayText() {
    JsonEvent inputEvent = createInputJsonEvent("testMessageCamelCase");
    applyAndCheckInputEvent(inputEvent, "testMessageCamelCase");
  }

  @Test
  public void testLowerCase() {
    m_stringField.setFormatLower();
    JsonEvent inputEvent = createInputJsonEvent("testMessageCamelCase");
    applyAndCheckInputEvent(inputEvent, "testmessagecamelcase");
  }

  @Test
  public void testUpperCase() {
    m_stringField.setFormatUpper();
    JsonEvent inputEvent = createInputJsonEvent("testMessageCamelCase");
    applyAndCheckInputEvent(inputEvent, "TESTMESSAGECAMELCASE");
  }

  protected JsonEvent createInputJsonEvent(String input) {
    JSONObject jsonObject = new JSONObject();
    jsonObject.put(IValueField.PROP_DISPLAY_TEXT, input);
    return new JsonEvent(m_jsonStringField.getId(), JsonValueField.EVENT_ACCEPT_INPUT, jsonObject);
  }

  protected void applyAndCheckInputEvent(JsonEvent inputEvent, String expectedDisplayText) {
    m_jsonStringField.handleUiEvent(inputEvent);
    assertEquals(expectedDisplayText, m_stringField.getDisplayText());
  }
}
