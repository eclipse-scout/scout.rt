/**
 *
 */
package org.eclipse.scout.rt.ui.html.json.desktop;

import static org.eclipse.scout.rt.ui.html.json.testing.JsonTestUtility.extractEventsFromResponse;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.lang.ref.WeakReference;
import java.util.List;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.ui.desktop.IDesktop;
import org.eclipse.scout.rt.ui.html.json.IJsonSession;
import org.eclipse.scout.rt.ui.html.json.desktop.fixtures.DesktopWithOneOutline;
import org.eclipse.scout.rt.ui.html.json.desktop.fixtures.DesktopWithOutlineForms;
import org.eclipse.scout.rt.ui.html.json.fixtures.JsonSessionMock;
import org.eclipse.scout.rt.ui.html.json.form.JsonForm;
import org.eclipse.scout.rt.ui.html.json.form.fixtures.FormWithOneField;
import org.eclipse.scout.rt.ui.html.json.testing.JsonTestUtility;
import org.eclipse.scout.testing.client.runner.ScoutClientTestRunner;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(ScoutClientTestRunner.class)
public class JsonDesktopTest {

  @Test
  public void testDisposeWithoutForms() {
    IDesktop desktop = new DesktopWithOneOutline();
    JsonDesktop object = createJsonDesktopWithMocks(desktop);
    WeakReference<JsonDesktop> ref = new WeakReference<JsonDesktop>(object);

    object.dispose();
    object = null;
    JsonTestUtility.assertGC(ref);
  }

  @Test
  public void testDisposeWithForms() {
    IDesktop desktop = new DesktopWithOutlineForms();
    JsonDesktop object = createJsonDesktopWithMocks(desktop);
    WeakReference<JsonDesktop> ref = new WeakReference<JsonDesktop>(object);

    object.dispose();
    object = null;
    JsonTestUtility.assertGC(ref);
  }

  @Test
  public void testFormAddedAndRemoved() throws ProcessingException, JSONException {
    IDesktop desktop = new DesktopWithOutlineForms();
    JsonDesktop jsonDesktop = createJsonDesktopWithMocks(desktop);
    IJsonSession session = jsonDesktop.getJsonSession();

    FormWithOneField form = new FormWithOneField();
    form.setAutoAddRemoveOnDesktop(false);

    JsonForm jsonForm = (JsonForm) session.getJsonAdapter(form);
    assertNull(jsonForm);

    desktop.addForm(form);

    jsonForm = (JsonForm) session.getJsonAdapter(form);
    assertNotNull(jsonForm);

    List<JSONObject> responseEvents = extractEventsFromResponse(session.currentJsonResponse(), "formAdded");
    assertTrue(responseEvents.size() == 1);

    JSONObject event = responseEvents.get(0);
    JSONObject formObj = event.getJSONObject("form");

    //Add event must contain complete form
    assertEquals(jsonDesktop.getId(), event.get("id"));
    assertEquals(jsonForm.getId(), formObj.get("id"));
    assertTrue(formObj.getJSONObject("rootGroupBox").getJSONArray("formFields").length() > 0);

    desktop.removeForm(form);

    responseEvents = extractEventsFromResponse(session.currentJsonResponse(), "formRemoved");
    assertTrue(responseEvents.size() == 1);

    event = responseEvents.get(0);
    formObj = event.getJSONObject("form");

    //Remove event must only contain the id, no other properties
    assertEquals(jsonDesktop.getId(), event.get("id"));
    assertEquals(jsonForm.getId(), formObj.get("id"));
    assertNull(formObj.optJSONObject("rootGroupBox"));
  }

  @Test
  public void testFormClosedBeforeRemoved() throws ProcessingException, JSONException {
    IDesktop desktop = new DesktopWithOutlineForms();
    JsonDesktop jsonDesktop = createJsonDesktopWithMocks(desktop);
    IJsonSession session = jsonDesktop.getJsonSession();

    FormWithOneField form = new FormWithOneField();
    form.setAutoAddRemoveOnDesktop(false);

    JsonForm jsonForm = (JsonForm) session.getJsonAdapter(form);
    assertNull(jsonForm);

    desktop.addForm(form);

    jsonForm = (JsonForm) session.getJsonAdapter(form);
    assertNotNull(jsonForm);

    List<JSONObject> responseEvents = extractEventsFromResponse(session.currentJsonResponse(), "formAdded");
    assertTrue(responseEvents.size() == 1);

    form.start();
    form.doClose();

    responseEvents = extractEventsFromResponse(session.currentJsonResponse(), "formClosed");
    assertTrue(responseEvents.size() == 1);

    desktop.removeForm(form);

    responseEvents = extractEventsFromResponse(session.currentJsonResponse(), "formRemoved");
    assertTrue(responseEvents.size() == 0);
  }

  public static JsonDesktop createJsonDesktopWithMocks(IDesktop desktop) {
    JsonSessionMock jsonSession = new JsonSessionMock();
    JsonDesktop jsonDesktop = new JsonDesktop(desktop, jsonSession, jsonSession.createUniqueIdFor(null));
    jsonDesktop.attach();
    return jsonDesktop;
  }
}
