/*******************************************************************************
 * Copyright (c) 2014-2016 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.ui.html.json.table.control;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.eclipse.scout.rt.client.testenvironment.TestEnvironmentClientSession;
import org.eclipse.scout.rt.client.ui.basic.table.controls.ITableControl;
import org.eclipse.scout.rt.testing.client.runner.ClientTestRunner;
import org.eclipse.scout.rt.testing.client.runner.RunWithClientSession;
import org.eclipse.scout.rt.testing.platform.runner.RunWithSubject;
import org.eclipse.scout.rt.ui.html.UiSessionTestUtility;
import org.eclipse.scout.rt.ui.html.json.IJsonAdapter;
import org.eclipse.scout.rt.ui.html.json.JsonEvent;
import org.eclipse.scout.rt.ui.html.json.fixtures.UiSessionMock;
import org.eclipse.scout.rt.ui.html.json.form.fixtures.FormWithOneField;
import org.eclipse.scout.rt.ui.html.json.table.fixtures.FormTableControl;
import org.eclipse.scout.rt.ui.html.json.table.fixtures.Table;
import org.eclipse.scout.rt.ui.html.json.testing.JsonTestUtility;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(ClientTestRunner.class)
@RunWithSubject("default")
@RunWithClientSession(TestEnvironmentClientSession.class)
public class JsonFormTableControlTest {

  private UiSessionMock m_uiSession;

  @Before
  public void setUp() {
    m_uiSession = new UiSessionMock();
  }

  @Test
  public void testLazyLoadingForm_onModelSelectionChanged() throws JSONException {
    FormWithOneField form = new FormWithOneField();
    form.setShowOnStart(false);
    FormTableControl control = new FormTableControl();
    control.setTable(new Table());
    control.setForm(form);
    control.decorateForm();
    JsonTableControl<ITableControl> jsonControl = UiSessionTestUtility.newJsonAdapter(m_uiSession, control, null);
    assertNull(jsonControl.getAdapter(form));

    control.setSelected(true);

    IJsonAdapter<?> formAdapter = jsonControl.getAdapter(form);
    assertNotNull(formAdapter);
    String formId = JsonTestUtility.extractProperty(m_uiSession.currentJsonResponse(), jsonControl.getId(), "form");
    assertEquals(formAdapter.getId(), formId);
  }

  @Test
  public void testLazyLoadingForm_onUiSelectionChanged() throws Exception {
    FormWithOneField form = new FormWithOneField();
    FormTableControl control = new FormTableControl();
    control.setTable(new Table());
    control.setForm(form);
    control.decorateForm();
    JsonTableControl<ITableControl> jsonControl = UiSessionTestUtility.newJsonAdapter(m_uiSession, control, null);

    assertNull(jsonControl.getAdapter(form));

    JsonEvent event = createJsonSelectedEvent(jsonControl.getId(), true);
    jsonControl.handleUiEvent(event);

    // Form needs to be created and sent if selection changes to true
    IJsonAdapter<?> formAdapter = jsonControl.getAdapter(form);
    assertNotNull(formAdapter);
    String formId = JsonTestUtility.extractProperty(m_uiSession.currentJsonResponse(), jsonControl.getId(), "form");
    assertEquals(formAdapter.getId(), formId);
    JsonTestUtility.endRequest(m_uiSession);

    // Don't send form again on subsequent selection changes
    event = createJsonSelectedEvent(jsonControl.getId(), false);
    jsonControl.handleUiEvent(event);
    formId = JsonTestUtility.extractProperty(m_uiSession.currentJsonResponse(), jsonControl.getId(), "form");
    assertNull(formId);
    JsonTestUtility.endRequest(m_uiSession);

    event = createJsonSelectedEvent(jsonControl.getId(), true);
    jsonControl.handleUiEvent(event);
    formId = JsonTestUtility.extractProperty(m_uiSession.currentJsonResponse(), jsonControl.getId(), "form");
    assertNull(formId);
  }

  @Test
  public void testLazyLoadingForm_onModelFormChanged() throws JSONException {
    FormWithOneField form = new FormWithOneField();
    form.setShowOnStart(false);
    FormTableControl control = new FormTableControl();
    control.setTable(new Table());
    control.setForm(form);
    control.decorateForm();
    JsonTableControl<ITableControl> jsonControl = UiSessionTestUtility.newJsonAdapter(m_uiSession, control, null);
    assertNull(jsonControl.getAdapter(form));

    FormWithOneField anotherForm = new FormWithOneField();
    anotherForm.setShowOnStart(false);
    control.setForm(anotherForm);
    control.decorateForm();

    // Both forms have to be null because the control has not been selected yet
    assertNull(jsonControl.getAdapter(form));
    assertNull(jsonControl.getAdapter(anotherForm));

    control.setSelected(true);

    IJsonAdapter<?> formAdapter = jsonControl.getAdapter(anotherForm);
    // Form is still null because it was exchanged with anotherForm
    assertNull(jsonControl.getAdapter(form));
    assertNotNull(formAdapter);
    String formId = JsonTestUtility.extractProperty(m_uiSession.currentJsonResponse(), jsonControl.getId(), "form");
    assertEquals(formAdapter.getId(), formId);
  }

  @Test
  public void testNonLazyLoadingFormWhenSelected() throws JSONException {
    FormWithOneField form = new FormWithOneField();
    FormTableControl control = new FormTableControl();
    control.setTable(new Table());
    control.setForm(form);
    control.decorateForm();
    control.setSelected(true);
    JsonTableControl<ITableControl> jsonControl = UiSessionTestUtility.newJsonAdapter(m_uiSession, control, null);

    IJsonAdapter<?> formAdapter = jsonControl.getAdapter(form);
    assertNotNull(formAdapter);

    // Expects formId is sent along with the control and not with a separate property change event
    String formId = JsonTestUtility.extractProperty(m_uiSession.currentJsonResponse(), jsonControl.getId(), "form");
    assertNull(formId);
  }

  public static JsonEvent createJsonSelectedEvent(String adapterId, boolean selected) throws JSONException {
    JSONObject data = new JSONObject();
    data.put("selected", selected);
    return new JsonEvent(adapterId, "property", data);
  }

}
