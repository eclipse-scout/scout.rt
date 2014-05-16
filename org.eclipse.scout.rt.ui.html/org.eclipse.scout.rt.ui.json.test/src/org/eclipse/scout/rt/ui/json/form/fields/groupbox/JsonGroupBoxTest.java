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
package org.eclipse.scout.rt.ui.json.form.fields.groupbox;

import static org.junit.Assert.assertEquals;

import org.eclipse.scout.rt.client.ui.form.fields.groupbox.AbstractGroupBox;
import org.eclipse.scout.rt.ui.json.form.fields.BaseFormFieldTest;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

public class JsonGroupBoxTest extends BaseFormFieldTest {

  AbstractGroupBox model = new AbstractGroupBox() {
  };

  JsonGroupBox groupBox = new JsonGroupBox(model, session, "1");

  @Before
  public void setUp() {
    model.setBorderDecoration("x");
    model.setBorderVisible(true);
  }

  @Test
  public void testToJson() throws JSONException {
    JSONObject json = groupBox.toJson();
    assertEquals("x", json.get("borderDecoration"));
    assertEquals(Boolean.TRUE, json.get("borderVisible"));
  }

}
