/**
 *
 */
package org.eclipse.scout.rt.ui.html.json.desktop;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.lang.ref.WeakReference;
import java.util.List;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.ui.desktop.IDesktop;
import org.eclipse.scout.rt.ui.html.json.IJsonAdapter;
import org.eclipse.scout.rt.ui.html.json.IJsonSession;
import org.eclipse.scout.rt.ui.html.json.JsonEvent;
import org.eclipse.scout.rt.ui.html.json.JsonResponse;
import org.eclipse.scout.rt.ui.html.json.desktop.fixtures.DesktopWithOneOutline;
import org.eclipse.scout.rt.ui.html.json.desktop.fixtures.DesktopWithOutlineForms;
import org.eclipse.scout.rt.ui.html.json.fixtures.JsonSessionMock;
import org.eclipse.scout.rt.ui.html.json.form.JsonForm;
import org.eclipse.scout.rt.ui.html.json.form.fixtures.FormWithOneField;
import org.eclipse.scout.rt.ui.html.json.testing.JsonTestUtility;
import org.eclipse.scout.testing.client.runner.ScoutClientTestRunner;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(ScoutClientTestRunner.class)
public class JsonDesktopTest {

  IDesktop m_desktop;
  JsonDesktop<IDesktop> m_jsonDesktop;
  IJsonSession m_session;

  @Before
  public void setUp() {
    setUp(new DesktopWithOutlineForms());
  }

  private void setUp(IDesktop desktop) {
    this.m_desktop = desktop;
    m_session = new JsonSessionMock();
    m_jsonDesktop = new JsonDesktop<IDesktop>(desktop, m_session, m_session.createUniqueIdFor(null));
    m_jsonDesktop.attach();
  }

  @Test
  public void testDisposeWithForms() {
    assertGc();
  }

  @Test
  public void testDisposeWithoutForms() {
    setUp(new DesktopWithOneOutline());
    assertGc();
  }

  private void assertGc() {
    WeakReference<?> ref = new WeakReference<IJsonAdapter>(m_jsonDesktop);
    m_jsonDesktop.dispose();
    m_session.flush();
    m_jsonDesktop = null;
    JsonTestUtility.assertGC(ref);
  }

  @Test
  public void testFormAddedAndRemoved() throws ProcessingException, JSONException {
    FormWithOneField form = new FormWithOneField();
    form.setAutoAddRemoveOnDesktop(false);

    JsonForm formAdapter = (JsonForm) m_session.getJsonAdapter(form);
    assertNull(formAdapter);

    m_desktop.addForm(form);

    formAdapter = (JsonForm) m_session.getJsonAdapter(form);
    assertNotNull(formAdapter);

    JsonResponse jsonResp = m_session.currentJsonResponse();
    List<JsonEvent> responseEvents = JsonTestUtility.extractEventsFromResponse(jsonResp, "formAdded");
    assertTrue(responseEvents.size() == 1);

    JsonEvent event = responseEvents.get(0);
    String formId = event.getData().getString("form");

    // Add event must contain reference (by ID) to form.
    assertEquals(m_jsonDesktop.getId(), event.getId());
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

    m_desktop.removeForm(form);

    responseEvents = JsonTestUtility.extractEventsFromResponse(m_session.currentJsonResponse(), "formRemoved");
    assertTrue(responseEvents.size() == 1);

    event = responseEvents.get(0);
    formId = event.getData().getString("form");

    // Remove event must only contain the id, no other properties
    assertEquals(m_jsonDesktop.getId(), event.getId());
    assertEquals(formAdapter.getId(), formId);
  }

  @Test
  public void testFormClosedBeforeRemoved() throws ProcessingException, JSONException {
    FormWithOneField form = new FormWithOneField();
    form.setAutoAddRemoveOnDesktop(false);

    JsonForm jsonForm = (JsonForm) m_session.getJsonAdapter(form);
    assertNull(jsonForm);

    m_desktop.addForm(form);

    jsonForm = (JsonForm) m_session.getJsonAdapter(form);
    assertNotNull(jsonForm);

    List<JsonEvent> responseEvents = JsonTestUtility.extractEventsFromResponse(m_session.currentJsonResponse(), "formAdded");
    assertTrue(responseEvents.size() == 1);

    form.start();
    form.doClose();
    responseEvents = JsonTestUtility.extractEventsFromResponse(m_session.currentJsonResponse(), "formClosed");
    assertTrue(responseEvents.size() == 1);

    m_desktop.removeForm(form);
    responseEvents = JsonTestUtility.extractEventsFromResponse(m_session.currentJsonResponse(), "formRemoved");
    assertTrue(responseEvents.size() == 1);
  }

  @Test
  public void testHandleFormRemoved() throws Exception {

  }

}
