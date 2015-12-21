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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.scout.rt.client.testenvironment.TestEnvironmentClientSession;
import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.client.ui.desktop.IDesktop;
import org.eclipse.scout.rt.client.ui.desktop.OpenUriAction;
import org.eclipse.scout.rt.client.ui.desktop.outline.IFormToolButton;
import org.eclipse.scout.rt.client.ui.desktop.outline.IOutline;
import org.eclipse.scout.rt.client.ui.desktop.outline.IOutlineViewButton;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.platform.job.Jobs;
import org.eclipse.scout.rt.platform.resource.BinaryResource;
import org.eclipse.scout.rt.testing.client.runner.ClientTestRunner;
import org.eclipse.scout.rt.testing.client.runner.RunWithClientSession;
import org.eclipse.scout.rt.testing.platform.runner.RunWithSubject;
import org.eclipse.scout.rt.testing.shared.TestingUtility;
import org.eclipse.scout.rt.ui.html.UiSession;
import org.eclipse.scout.rt.ui.html.json.IJsonAdapter;
import org.eclipse.scout.rt.ui.html.json.JsonAdapterRegistryTest;
import org.eclipse.scout.rt.ui.html.json.JsonEvent;
import org.eclipse.scout.rt.ui.html.json.JsonResponse;
import org.eclipse.scout.rt.ui.html.json.desktop.fixtures.DesktopWithNonDisplayableActions;
import org.eclipse.scout.rt.ui.html.json.desktop.fixtures.DesktopWithNonDisplayableOutline;
import org.eclipse.scout.rt.ui.html.json.desktop.fixtures.DesktopWithOneOutline;
import org.eclipse.scout.rt.ui.html.json.desktop.fixtures.DesktopWithOutlineForms;
import org.eclipse.scout.rt.ui.html.json.desktop.fixtures.NonDisplayableOutlineWithOneNode;
import org.eclipse.scout.rt.ui.html.json.fixtures.UiSessionMock;
import org.eclipse.scout.rt.ui.html.json.form.JsonForm;
import org.eclipse.scout.rt.ui.html.json.form.fixtures.FormWithOneField;
import org.eclipse.scout.rt.ui.html.json.menu.JsonMenu;
import org.eclipse.scout.rt.ui.html.json.testing.JsonTestUtility;
import org.eclipse.scout.rt.ui.html.res.BinaryResourceUrlUtility;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(ClientTestRunner.class)
@RunWithSubject("default")
@RunWithClientSession(TestEnvironmentClientSession.class)
public class JsonDesktopTest {

  private UiSession m_uiSession;

  @Before
  public void setUp() {
    m_uiSession = new UiSessionMock();
  }

  private JsonDesktop<IDesktop> createJsonDesktop(IDesktop desktop) {
    return m_uiSession.newJsonAdapter(desktop, null);
  }

  @Test
  public void testDisposeWithForms() {
    DesktopWithOutlineForms desktop = new DesktopWithOutlineForms();
    desktop.initDesktop();
    testDispose(desktop);
  }

  @Test
  public void testDisposeWithoutForms() {
    DesktopWithOneOutline desktop = new DesktopWithOneOutline();
    desktop.initDesktop();
    testDispose(desktop);
  }

  private void testDispose(IDesktop desktop) {
    JsonDesktop<IDesktop> jsonDesktop = createJsonDesktop(desktop);
    WeakReference<?> ref = new WeakReference<IJsonAdapter>(jsonDesktop);
    jsonDesktop.dispose();
    jsonDesktop = null;
    TestingUtility.assertGC(ref);
  }

  @Test
  public void testFormAddedAndRemoved() throws JSONException {
    DesktopWithOneOutline desktop = new DesktopWithOneOutline();
    desktop.initDesktop();
    JsonDesktop<IDesktop> jsonDesktop = createJsonDesktop(desktop);
    FormWithOneField form = new FormWithOneField();
    form.setDisplayParent(desktop);
    form.setShowOnStart(false);

    JsonForm formAdapter = (JsonForm) jsonDesktop.getAdapter(form);
    assertNull(formAdapter);

    desktop.showForm(form);

    formAdapter = (JsonForm) jsonDesktop.getAdapter(form);
    assertNotNull(formAdapter);

    JsonResponse jsonResp = m_uiSession.currentJsonResponse();
    List<JsonEvent> responseEvents = JsonTestUtility.extractEventsFromResponse(jsonResp, "formShow");
    assertTrue(responseEvents.size() == 1);

    JsonEvent event = responseEvents.get(0);
    String formId = event.getData().getString("form");

    // Add event must contain reference (by ID) to form.
    assertEquals(jsonDesktop.getId(), event.getTarget());
    assertEquals(formAdapter.getId(), formId);

    // adapter-data for form must exist in 'adapterData' property of response
    JSONObject json = jsonResp.toJson();
    JSONObject adapterData = JsonTestUtility.getAdapterData(json, formId);
    assertEquals(formAdapter.getId(), adapterData.getString("id"));
    assertEquals("Form", adapterData.getString("objectType"));
    String rootGroupBoxId = adapterData.getString("rootGroupBox");
    adapterData = JsonTestUtility.getAdapterData(json, rootGroupBoxId);
    assertEquals("GroupBox", adapterData.getString("objectType"));
    // we could continue to test the reference structure in the JSON response,
    // but for the moment this should be enough...

    desktop.hideForm(form);

    responseEvents = JsonTestUtility.extractEventsFromResponse(m_uiSession.currentJsonResponse(), "formHide");
    assertTrue(responseEvents.size() == 1);

    event = responseEvents.get(0);
    formId = event.getData().getString("form");

    // Remove event must only contain the id, no other properties
    assertEquals(jsonDesktop.getId(), event.getTarget());
    assertEquals(formAdapter.getId(), formId);
  }

  @Test
  public void testFormOpenedAndClosedInSameRequest() throws JSONException {
    JsonAdapterRegistryTest.testFormOpenedAndClosedInSameRequest(m_uiSession);
  }

  @Test
  public void testFormClosedBeforeRemovedInDifferentRequests() throws Exception {
    DesktopWithOneOutline desktop = new DesktopWithOneOutline();
    desktop.initDesktop();
    JsonDesktop<IDesktop> jsonDesktop = createJsonDesktop(desktop);
    FormWithOneField form = new FormWithOneField();
    form.setDisplayParent(desktop);
    form.setShowOnStart(false);

    JsonForm jsonForm = (JsonForm) jsonDesktop.getAdapter(form);
    assertNull(jsonForm);

    desktop.showForm(form);

    jsonForm = (JsonForm) jsonDesktop.getAdapter(form);
    assertNotNull(jsonForm);

    List<JsonEvent> responseEvents = JsonTestUtility.extractEventsFromResponse(m_uiSession.currentJsonResponse(), "formShow");
    assertEquals(1, responseEvents.size());

    form.start();

    JsonTestUtility.endRequest(m_uiSession);
    // -------------------------------
    // New request:

    form.doClose();
    responseEvents = JsonTestUtility.extractEventsFromResponse(m_uiSession.currentJsonResponse(), "formClosed");
    assertEquals(0, responseEvents.size());
    responseEvents = JsonTestUtility.extractEventsFromResponse(m_uiSession.currentJsonResponse(), "disposeAdapter");
    assertEquals(1, responseEvents.size());

    desktop.hideForm(form);
    responseEvents = JsonTestUtility.extractEventsFromResponse(m_uiSession.currentJsonResponse(), "formHide");
    // 0 instead of 1, because formClosed includes "formHide" implicitly. The event itself cannot be sent, because
    // the form adapter is already disposed when the form is closed.
    assertEquals(0, responseEvents.size());
  }

  @Test
  public void testFormClosedBeforeRemovedInSameRequest() throws JSONException {
    DesktopWithOneOutline desktop = new DesktopWithOneOutline();
    desktop.initDesktop();
    JsonDesktop<IDesktop> jsonDesktop = createJsonDesktop(desktop);
    FormWithOneField form = new FormWithOneField();
    form.setDisplayParent(desktop);
    form.setShowOnStart(false);

    JsonForm jsonForm = (JsonForm) jsonDesktop.getAdapter(form);
    assertNull(jsonForm);

    desktop.showForm(form);

    jsonForm = (JsonForm) jsonDesktop.getAdapter(form);
    assertNotNull(jsonForm);

    List<JsonEvent> responseEvents = JsonTestUtility.extractEventsFromResponse(m_uiSession.currentJsonResponse(), "formShow");
    assertEquals(1, responseEvents.size());

    form.start();
    form.doClose();
    responseEvents = JsonTestUtility.extractEventsFromResponse(m_uiSession.currentJsonResponse(), "formClosed");
    assertEquals(0, responseEvents.size());
    responseEvents = JsonTestUtility.extractEventsFromResponse(m_uiSession.currentJsonResponse(), "disposeAdapter");
    assertEquals(1, responseEvents.size());

    desktop.hideForm(form);
    responseEvents = JsonTestUtility.extractEventsFromResponse(m_uiSession.currentJsonResponse(), "formHide");
    assertEquals(0, responseEvents.size());
  }

  /**
   * Tests whether non displayable actions are sent.
   * <p>
   * This reduces response size and also leverages security because the menus are never visible to the user, not even
   * with the dev tools of the browser
   */
  @Test
  public void testDontSendNonDisplayableActions() throws Exception {
    IDesktop desktop = new DesktopWithNonDisplayableActions();
    desktop.initDesktop();
    JsonDesktop<IDesktop> jsonDesktop = createJsonDesktop(desktop);

    JsonMenu<IMenu> jsonDisplayableMenu = jsonDesktop.getAdapter(desktop.getMenu(DesktopWithNonDisplayableActions.DisplayableMenu.class));
    JsonMenu<IMenu> jsonNonDisplayableMenu = jsonDesktop.getAdapter(desktop.getMenu(DesktopWithNonDisplayableActions.NonDisplayableMenu.class));
    JsonFormToolButton<IFormToolButton<IForm>> jsonDisplayableToolButton = jsonDesktop.getAdapter(desktop.getToolButton(DesktopWithNonDisplayableActions.DisplayableFormToolButton.class));
    JsonFormToolButton<IFormToolButton<IForm>> jsonNonDisplayableToolButton = jsonDesktop.getAdapter(desktop.getToolButton(DesktopWithNonDisplayableActions.NonDisplayableFormToolButton.class));

    // Adapter for NonDisplayableMenu/ToolButton must not exist
    assertNull(jsonNonDisplayableMenu);
    assertNull(jsonNonDisplayableToolButton);

    // Json response must not contain NonDisplayableMenu/ToolButton
    JSONObject json = jsonDesktop.toJson();
    JSONArray jsonActions = json.getJSONArray("actions");
    assertEquals(2, jsonActions.length());
    List<String> ids = new ArrayList<String>();
    ids.add(jsonActions.getString(0));
    ids.add(jsonActions.getString(1));
    assertTrue(ids.contains(jsonDisplayableMenu.getId()));
    assertTrue(ids.contains(jsonDisplayableToolButton.getId()));
  }

  /**
   * Tests whether non displayable view buttons are sent.
   * <p>
   * This reduces response size and also leverages security because the menus are never visible to the user, not even
   * with the dev tools of the browser
   */
  @Test
  public void testDontSendNonDisplayableViewButtons() throws Exception {
    IDesktop desktop = new DesktopWithNonDisplayableActions();
    desktop.initDesktop();
    JsonDesktop<IDesktop> jsonDesktop = createJsonDesktop(desktop);

    JsonOutlineViewButton<IOutlineViewButton> jsonDisplayableAction = jsonDesktop.getAdapter(desktop.getViewButton(DesktopWithNonDisplayableActions.DisplayableOutlineViewButton.class));
    JsonOutlineViewButton<IOutlineViewButton> jsonNonDisplayableAction = jsonDesktop.getAdapter(desktop.getViewButton(DesktopWithNonDisplayableActions.NonDisplayableOutlineViewButton.class));

    // Adapter for NonDisplayableMenu must not exist
    assertNull(jsonNonDisplayableAction);

    // Json response must not contain NonDisplayableAction
    JSONObject json = jsonDesktop.toJson();
    JSONArray jsonActions = json.getJSONArray("viewButtons");
    assertEquals(1, jsonActions.length());
    assertEquals(jsonDisplayableAction.getId(), jsonActions.get(0));
  }

  /**
   * Tests whether non displayable outline is sent.
   * <p>
   * This reduces response size and also leverages security because the menus are never visible to the user, not even
   * with the dev tools of the browser
   */
  @Test
  public void testDontSendNonDisplayableOutline() throws Exception {
    IDesktop desktop = new DesktopWithNonDisplayableOutline();
    desktop.initDesktop();
    desktop.setOutline(NonDisplayableOutlineWithOneNode.class);
    JsonDesktop<IDesktop> jsonDesktop = createJsonDesktop(desktop);

    JsonOutline<IOutline> jsonNonDisplayableOutline = jsonDesktop.getAdapter(desktop.findOutline(NonDisplayableOutlineWithOneNode.class));

    // Adapter for NonDisplayableMenu must not exist
    assertNull(jsonNonDisplayableOutline);

    // Json response must not contain outline
    JSONObject json = jsonDesktop.toJson();
    String outlineId = json.optString("outline", null);
    assertNull(outlineId);
  }

  @Test
  public void testHandleModelDownloadResource() throws Exception {
    IDesktop desktop = new DesktopWithNonDisplayableOutline();
    desktop.initDesktop();
    JsonDesktop<IDesktop> jsonDesktop = createJsonDesktop(desktop);
    jsonDesktop.handleModelOpenUri(new BinaryResource("foo.txt", null), OpenUriAction.DOWNLOAD);
    List<JsonEvent> events = JsonTestUtility.extractEventsFromResponse(m_uiSession.currentJsonResponse(), "openUri");
    JSONObject data = events.get(0).getData();
    assertEquals("dynamic/" + m_uiSession.getUiSessionId() + "/2/0/4fd8cc85ca9eebd2fa3c550069ce2846", data.getString("uri")); // counter = 0 first for test run
    assertEquals("download", data.getString("action"));

    // cleanup
    Jobs.getJobManager().cancel(Jobs.newFutureFilterBuilder()
        .andMatchExecutionHint(DownloadHandlerStorage.RESOURCE_CLEANUP_JOB_MARKER)
        .toFilter(), true);
  }

  @Test
  public void testGetFilenameHash() throws Exception {
    assertEquals("d41d8cd98f00b204e9800998ecf8427e", BinaryResourceUrlUtility.getFilenameHash(null));
    assertEquals("d41d8cd98f00b204e9800998ecf8427e", BinaryResourceUrlUtility.getFilenameHash(""));
    assertEquals("202cb962ac59075b964b07152d234b70", BinaryResourceUrlUtility.getFilenameHash("123"));
    assertEquals("4fd8cc85ca9eebd2fa3c550069ce2846", BinaryResourceUrlUtility.getFilenameHash("foo.txt"));
  }
}
