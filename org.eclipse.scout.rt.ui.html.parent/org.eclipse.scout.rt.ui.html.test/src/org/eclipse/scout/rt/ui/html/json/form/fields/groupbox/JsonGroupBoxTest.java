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
package org.eclipse.scout.rt.ui.html.json.form.fields.groupbox;

import static org.junit.Assert.assertEquals;

import org.eclipse.scout.rt.client.ui.form.fields.groupbox.AbstractGroupBox;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.IGroupBox;
import org.eclipse.scout.rt.ui.html.json.form.fields.BaseFormFieldTest;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

public class JsonGroupBoxTest extends BaseFormFieldTest {

  AbstractGroupBox m_model = new AbstractGroupBox() {
  };

  JsonGroupBox m_groupBox = new JsonGroupBox<IGroupBox>(m_model, m_session, m_session.createUniqueIdFor(null));

  @Before
  public void setUp() {
    m_model.setBorderDecoration("x");
    m_model.setBorderVisible(true);
  }

  @Test
  public void testToJson() throws JSONException {
    JSONObject json = m_groupBox.toJson();
    assertEquals("x", json.get("borderDecoration"));
    assertEquals(Boolean.TRUE, json.get("borderVisible"));
  }

}
