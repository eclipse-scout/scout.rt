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
package org.eclipse.scout.rt.ui.html.json.form.fields;

import static org.junit.Assert.assertEquals;

import org.eclipse.scout.rt.client.ui.form.fields.groupbox.AbstractGroupBox;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

public class JsonFormFieldTest extends BaseFormFieldTest {

  AbstractGroupBox m_model = new AbstractGroupBox() {
  };

  JsonFormField m_formField = new JsonFormField<>(m_model, m_session, m_session.createUniqueIdFor(null));

  @Before
  public void setUp() {
    m_model.setLabel("fooBar");
    m_model.setEnabled(false);
    m_model.setVisible(false);
    m_model.setMandatory(true);
    m_model.setErrorStatus("allesFalsch");
  }

  @Test
  public void testToJson() throws JSONException {
    JSONObject json = m_formField.toJson();
    assertEquals("fooBar", json.get("label"));
    assertEquals(Boolean.FALSE, json.get("enabled"));
    assertEquals(Boolean.FALSE, json.get("visible"));
    JSONObject errorStatus = (JSONObject) json.get("errorStatus");
    assertEquals("allesFalsch", errorStatus.get("message"));
    assertEquals("status_error", errorStatus.get("iconName"));
  }

}
