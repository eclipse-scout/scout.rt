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
package org.eclipse.scout.rt.ui.html.json.desktop;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Collections;
import java.util.List;

import org.eclipse.scout.rt.client.testenvironment.TestEnvironmentClientSession;
import org.eclipse.scout.rt.client.ui.basic.table.controls.ITableControl;
import org.eclipse.scout.rt.client.ui.desktop.IDesktop;
import org.eclipse.scout.rt.client.ui.desktop.outline.IOutline;
import org.eclipse.scout.rt.client.ui.desktop.outline.IOutlineViewButton;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.testing.client.runner.ClientTestRunner;
import org.eclipse.scout.rt.testing.client.runner.RunWithClientSession;
import org.eclipse.scout.rt.testing.platform.runner.RunWithSubject;
import org.eclipse.scout.rt.ui.html.UiSessionTestUtility;
import org.eclipse.scout.rt.ui.html.json.IJsonAdapter;
import org.eclipse.scout.rt.ui.html.json.JsonEvent;
import org.eclipse.scout.rt.ui.html.json.desktop.fixtures.OutlineViewButton;
import org.eclipse.scout.rt.ui.html.json.desktop.fixtures.OutlineWithOneNode;
import org.eclipse.scout.rt.ui.html.json.fixtures.UiSessionMock;
import org.eclipse.scout.rt.ui.html.json.form.fixtures.FormWithOneField;
import org.eclipse.scout.rt.ui.html.json.table.control.JsonTableControl;
import org.eclipse.scout.rt.ui.html.json.table.fixtures.FormTableControl;
import org.eclipse.scout.rt.ui.html.json.testing.JsonTestUtility;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

@RunWith(ClientTestRunner.class)
@RunWithSubject("default")
@RunWithClientSession(TestEnvironmentClientSession.class)
public class JsonOutlineViewButtonTest {

  private UiSessionMock m_uiSession;

  @Before
  public void setUp() {
    m_uiSession = new UiSessionMock();
  }

  @Test
  public void testLazyLoadingOutline_onModelSelectionChanged() throws JSONException {
    OutlineWithOneNode outline = new OutlineWithOneNode();
    IDesktop desktop = Mockito.mock(IDesktop.class);
    Mockito.when(desktop.getAvailableOutlines()).thenReturn(Collections.<IOutline> singletonList(outline));
    IOutlineViewButton button = new OutlineViewButton(desktop, outline.getClass());
    JsonOutlineViewButton<IOutlineViewButton> jsonViewButton = UiSessionTestUtility.newJsonAdapter(m_uiSession, button, null);
    assertNull(jsonViewButton.getAdapter(outline));

    button.setSelected(true);

    IJsonAdapter<?> outlineAdapter = jsonViewButton.getAdapter(outline);
    assertNotNull(outlineAdapter);
    String outlineId = JsonTestUtility.extractProperty(m_uiSession.currentJsonResponse(), jsonViewButton.getId(), "outline");
    assertEquals(outlineAdapter.getId(), outlineId);
  }

  @Test
  public void testLazyLoadingOutline_onUiSelectionChanged() throws Exception {
    OutlineWithOneNode outline = new OutlineWithOneNode();
    IDesktop desktop = Mockito.mock(IDesktop.class);
    Mockito.when(desktop.getAvailableOutlines()).thenReturn(Collections.<IOutline> singletonList(outline));
    IOutlineViewButton button = new OutlineViewButton(desktop, outline.getClass());
    JsonOutlineViewButton<IOutlineViewButton> jsonViewButton = UiSessionTestUtility.newJsonAdapter(m_uiSession, button, null);
    assertNull(jsonViewButton.getAdapter(outline));

    JsonEvent event = createJsonDoActionEvent(jsonViewButton.getId());
    assertEquals("doAction", event.getType());
    jsonViewButton.handleUiEvent(event);

    // Outline needs to be created and sent if selection changes to true
    IJsonAdapter<?> outlineAdapter = jsonViewButton.getAdapter(outline);
    assertNotNull(outlineAdapter);
    String outlineId = JsonTestUtility.extractProperty(m_uiSession.currentJsonResponse(), jsonViewButton.getId(), "outline");
    assertEquals(outlineAdapter.getId(), outlineId);
  }

  @Test
  public void testNonLazyLoadingOutlineWhenSelected() throws JSONException {
    OutlineWithOneNode outline = new OutlineWithOneNode();
    IDesktop desktop = Mockito.mock(IDesktop.class);
    Mockito.when(desktop.getAvailableOutlines()).thenReturn(Collections.<IOutline> singletonList(outline));
    IOutlineViewButton button = new OutlineViewButton(desktop, outline.getClass());
    button.setSelected(true);
    JsonOutlineViewButton<IOutlineViewButton> jsonViewButton = UiSessionTestUtility.newJsonAdapter(m_uiSession, button, null);

    IJsonAdapter<?> outlineAdapter = jsonViewButton.getAdapter(outline);
    assertNotNull(outlineAdapter);

    // Expects outlineId is sent along with the button and not with a separate property change event
    String outlineId = JsonTestUtility.extractProperty(m_uiSession.currentJsonResponse(), jsonViewButton.getId(), "outline");
    assertNull(outlineId);
  }

  private static JsonEvent createJsonDoActionEvent(String adapterId) throws JSONException {
    return new JsonEvent(adapterId, "doAction", null);
  }

  @Test
  public void testAutoFormClose() throws Exception {
    // Table control with configured form
    FormTableControl control = new FormTableControl() {
      @Override
      protected Class<? extends IForm> getConfiguredForm() {
        return FormWithOneField.class;
      }
    };
    JsonTableControl<ITableControl> jsonControl = UiSessionTestUtility.newJsonAdapter(m_uiSession, control, null);

    // Initially, no form is created
    IForm form = control.getForm();
    assertNull(form);

    // Select control -> form is created
    control.setSelected(true);
    form = control.getForm();
    IJsonAdapter<?> formAdapter = jsonControl.getAdapter(form);
    assertNotNull(formAdapter);
    String formId = JsonTestUtility.extractProperty(m_uiSession.currentJsonResponse(), jsonControl.getId(), "form");
    assertEquals(formAdapter.getId(), formId);

    JsonTestUtility.endRequest(m_uiSession);

    // Close the form -> removes it from the control
    form.doClose();
    assertFalse(control.isSelected());
    assertNull(control.getForm());

    assertNull(jsonControl.getAdapter(form));
    List<JsonEvent> events = JsonTestUtility.extractEventsFromResponse(m_uiSession.currentJsonResponse(), "property", jsonControl.getId());
    assertEquals(1, events.size());
    JSONObject props = events.get(0).getData().getJSONObject("properties");
    assertFalse(props.getBoolean("selected"));
    assertTrue(props.get("form") == JSONObject.NULL); // "null" must be sent to the UI
    JsonTestUtility.endRequest(m_uiSession);

    // Select control again -> new form is started
    control.setSelected(true);
    IForm form2 = control.getForm();
    assertNotSame(form, form2);
    IJsonAdapter<?> formAdapter2 = jsonControl.getAdapter(form2);
    assertNotNull(formAdapter2);
    assertNotEquals(formAdapter2.getId(), formId);
  }
}
