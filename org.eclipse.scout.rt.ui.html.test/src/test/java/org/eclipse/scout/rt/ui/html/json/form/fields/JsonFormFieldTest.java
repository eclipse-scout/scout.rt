/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.ui.html.json.form.fields;

import static org.junit.Assert.assertEquals;

import org.eclipse.scout.rt.client.testenvironment.TestEnvironmentClientSession;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.AbstractGroupBox;
import org.eclipse.scout.rt.testing.client.runner.ClientTestRunner;
import org.eclipse.scout.rt.testing.client.runner.RunWithClientSession;
import org.eclipse.scout.rt.testing.platform.runner.RunWithSubject;
import org.eclipse.scout.rt.ui.html.json.fixtures.JsonAdapterMock;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(ClientTestRunner.class)
@RunWithSubject("default")
@RunWithClientSession(TestEnvironmentClientSession.class)
public class JsonFormFieldTest extends BaseFormFieldTest {

  AbstractGroupBox m_model = new AbstractGroupBox() {
  };

  JsonFormField m_formField = new JsonFormField<IFormField>(m_model, m_session, m_session.createUniqueId(), new JsonAdapterMock()) {
  };

  @Before
  public void setUp() {
    m_formField.init();
    m_model.setLabel("fooBar");
    m_model.setEnabled(false);
    m_model.setVisible(false);
    m_model.setMandatory(true);
    m_model.addErrorStatus("allesFalsch");
  }

  @Test
  public void testToJson() throws JSONException {
    JSONObject json = m_formField.toJson();
    assertEquals("fooBar", json.get("label"));
    assertEquals(Boolean.FALSE, json.get("enabled"));
    assertEquals(Boolean.FALSE, json.get("visible"));
    JSONObject errorStatus = (JSONObject) json.get("errorStatus");
    assertEquals("allesFalsch", errorStatus.get("message"));
  }
}
