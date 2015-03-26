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
package org.eclipse.scout.rt.ui.html.json.table.control;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.ui.basic.table.control.ITableControl;
import org.eclipse.scout.rt.ui.html.json.IJsonAdapter;
import org.eclipse.scout.rt.ui.html.json.JsonEvent;
import org.eclipse.scout.rt.ui.html.json.fixtures.JsonSessionMock;
import org.eclipse.scout.rt.ui.html.json.form.fixtures.FormWithOneField;
import org.eclipse.scout.rt.ui.html.json.table.fixtures.Table;
import org.eclipse.scout.rt.ui.html.json.table.fixtures.TableControl;
import org.eclipse.scout.rt.ui.html.json.testing.JsonTestUtility;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

public class JsonTableControlTest {

  private JsonSessionMock m_jsonSession;

  @Before
  public void setUp() {
    m_jsonSession = new JsonSessionMock();
  }

  @Test
  public void testLazyLoadingForm_onModelSelectionChanged() throws ProcessingException, JSONException {
    FormWithOneField form = new FormWithOneField();
    form.setAutoAddRemoveOnDesktop(false);
    TableControl control = new TableControl();
    control.setTable(new Table());
    control.setForm(form);
    control.decorateForm();
    JsonTableControl<ITableControl> jsonControl = m_jsonSession.newJsonAdapter(control, null, null);
    assertNull(jsonControl.getAdapter(form));

    control.setSelected(true);

    IJsonAdapter<?> formAdapter = jsonControl.getAdapter(form);
    assertNotNull(formAdapter);
    String formId = JsonTestUtility.extractProperty(m_jsonSession.currentJsonResponse(), jsonControl.getId(), "form");
    assertEquals(formAdapter.getId(), formId);
  }

  @Test
  public void testLazyLoadingForm_onUiSelectionChanged() throws Exception {
    FormWithOneField form = new FormWithOneField();
    TableControl control = new TableControl();
    control.setTable(new Table());
    control.setForm(form);
    control.decorateForm();
    JsonTableControl<ITableControl> jsonControl = m_jsonSession.newJsonAdapter(control, null, null);

    assertNull(jsonControl.getAdapter(form));

    JsonEvent event = createJsonSelectedEvent(jsonControl.getId(), true);
    jsonControl.handleUiEvent(event);

    // Form needs to be created and sent if selection changes to true
    IJsonAdapter<?> formAdapter = jsonControl.getAdapter(form);
    assertNotNull(formAdapter);
    String formId = JsonTestUtility.extractProperty(m_jsonSession.currentJsonResponse(), jsonControl.getId(), "form");
    assertEquals(formAdapter.getId(), formId);
    JsonTestUtility.endRequest(m_jsonSession);

    // Don't send form again on subsequent selection changes
    event = createJsonSelectedEvent(jsonControl.getId(), false);
    jsonControl.handleUiEvent(event);
    formId = JsonTestUtility.extractProperty(m_jsonSession.currentJsonResponse(), jsonControl.getId(), "form");
    assertNull(formId);
    JsonTestUtility.endRequest(m_jsonSession);

    event = createJsonSelectedEvent(jsonControl.getId(), true);
    jsonControl.handleUiEvent(event);
    formId = JsonTestUtility.extractProperty(m_jsonSession.currentJsonResponse(), jsonControl.getId(), "form");
    assertNull(formId);
  }

  @Test
  public void testNonLazyLoadingFormWhenSelected() throws ProcessingException, JSONException {
    FormWithOneField form = new FormWithOneField();
    TableControl control = new TableControl();
    control.setTable(new Table());
    control.setForm(form);
    control.decorateForm();
    control.setSelected(true);
    JsonTableControl<ITableControl> jsonControl = m_jsonSession.newJsonAdapter(control, null, null);

    IJsonAdapter<?> formAdapter = jsonControl.getAdapter(form);
    assertNotNull(formAdapter);

    // Expects formId is sent along with the control and not with a separate property change event
    String formId = JsonTestUtility.extractProperty(m_jsonSession.currentJsonResponse(), jsonControl.getId(), "form");
    assertNull(formId);
  }

  public static JsonEvent createJsonSelectedEvent(String adapterId, boolean selected) throws JSONException {
    JSONObject data = new JSONObject();
    data.put("selected", selected);
    return new JsonEvent(adapterId, "selected", data);
  }

}
