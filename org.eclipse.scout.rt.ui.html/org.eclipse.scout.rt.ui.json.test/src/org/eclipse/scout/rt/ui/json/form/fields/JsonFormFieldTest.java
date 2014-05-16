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
package org.eclipse.scout.rt.ui.json.form.fields;

import static org.junit.Assert.assertEquals;

import org.eclipse.scout.rt.client.ui.form.fields.groupbox.AbstractGroupBox;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

public class JsonFormFieldTest extends BaseFormFieldTest {

  AbstractGroupBox model = new AbstractGroupBox() {
  };

  JsonFormField formField = new JsonFormField<>(model, session, "1");

  @Before
  public void setUp() {
    model.setLabel("fooBar");
    model.setEnabled(false);
    model.setVisible(false);
    model.setMandatory(true);
    model.setErrorStatus("allesFalsch");
  }

  @Test
  public void testToJson() throws JSONException {
    JSONObject json = formField.toJson();
    assertEquals("fooBar", json.get("label"));
    assertEquals(Boolean.FALSE, json.get("enabled"));
    assertEquals(Boolean.FALSE, json.get("visible"));
    JSONObject errorStatus = (JSONObject) json.get("errorStatus");
    assertEquals("allesFalsch", errorStatus.get("message"));
    assertEquals("status_error", errorStatus.get("iconName"));
  }

}
