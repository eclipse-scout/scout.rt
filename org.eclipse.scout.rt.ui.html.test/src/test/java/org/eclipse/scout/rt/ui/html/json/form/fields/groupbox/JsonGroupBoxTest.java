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
package org.eclipse.scout.rt.ui.html.json.form.fields.groupbox;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.eclipse.scout.rt.client.testenvironment.TestEnvironmentClientSession;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.AbstractGroupBox;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.IGroupBox;
import org.eclipse.scout.rt.client.ui.form.fields.stringfield.AbstractStringField;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.testing.client.runner.ClientTestRunner;
import org.eclipse.scout.rt.testing.client.runner.RunWithClientSession;
import org.eclipse.scout.rt.testing.platform.runner.RunWithSubject;
import org.eclipse.scout.rt.ui.html.UiSessionTestUtility;
import org.eclipse.scout.rt.ui.html.json.fixtures.UiSessionMock;
import org.eclipse.scout.rt.ui.html.json.form.fields.BaseFormFieldTest;
import org.eclipse.scout.rt.ui.html.json.form.fields.JsonFormField;
import org.eclipse.scout.rt.ui.html.json.testing.JsonTestUtility;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(ClientTestRunner.class)
@RunWithSubject("default")
@RunWithClientSession(TestEnvironmentClientSession.class)
public class JsonGroupBoxTest extends BaseFormFieldTest {
  private UiSessionMock m_uiSession;

  private AbstractGroupBox m_model = new AbstractGroupBox() {
  };

  private JsonGroupBox m_groupBox = new JsonGroupBox<IGroupBox>(m_model, m_session, m_session.createUniqueId(), null);

  @Before
  public void setUp() {
    m_groupBox.init();
    m_model.setBorderDecoration("x");
    m_model.setBorderVisible(true);
    m_uiSession = new UiSessionMock();
  }

  @Test
  public void testToJson() throws JSONException {
    JSONObject json = m_groupBox.toJson();
    assertEquals("x", json.get("borderDecoration"));
    assertEquals(Boolean.TRUE, json.get("borderVisible"));
  }

  /**
   * Tests whether non displayable fields are sent.
   * <p>
   * This reduces response size and also leverages security because the fields are never visible to the user, not even
   * with the dev tools of the browser
   */
  @Test
  public void testDontSendNonDisplayableFields() throws Exception {
    IGroupBox groupBox = new GroupBoxWithNonDisplayableField();
    JsonTestUtility.initField(groupBox);

    JsonGroupBox<IGroupBox> jsonGroupBox = UiSessionTestUtility.newJsonAdapter(m_uiSession, groupBox, null);
    JsonFormField<IFormField> jsonDisplayableField = m_uiSession.getJsonAdapter(groupBox.getFieldByClass(GroupBoxWithNonDisplayableField.DisplayableField.class), jsonGroupBox);
    JsonFormField<IFormField> jsonNonDisplayableField = m_uiSession.getJsonAdapter(groupBox.getFieldByClass(GroupBoxWithNonDisplayableField.NonDisplayableField.class), jsonGroupBox);

    // Adapter for NonDisplayableField must not exist
    assertNull(jsonNonDisplayableField);

    // Json response must not contain NonDisplayableField
    JSONObject json = jsonGroupBox.toJson();
    JSONArray jsonFormFields = json.getJSONArray("fields");
    assertEquals(1, jsonFormFields.length());
    assertEquals(jsonDisplayableField.getId(), jsonFormFields.get(0));
  }

  private class GroupBoxWithNonDisplayableField extends AbstractGroupBox {

    @Order(10)
    public class DisplayableField extends AbstractStringField {

    }

    @Order(20)
    public class NonDisplayableField extends AbstractStringField {

      @Override
      protected void execInitField() {
        setVisibleGranted(false);
      }
    }
  }
}
