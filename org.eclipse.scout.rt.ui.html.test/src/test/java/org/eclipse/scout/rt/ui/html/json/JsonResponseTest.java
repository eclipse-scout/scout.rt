/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.ui.html.json;

import static org.junit.Assert.*;

import java.util.Map;

import org.eclipse.scout.rt.client.testenvironment.TestEnvironmentClientSession;
import org.eclipse.scout.rt.testing.client.runner.ClientTestRunner;
import org.eclipse.scout.rt.testing.client.runner.RunWithClientSession;
import org.eclipse.scout.rt.testing.platform.runner.RunWithSubject;
import org.eclipse.scout.rt.ui.html.json.fixtures.UiSessionMock;
import org.eclipse.scout.rt.ui.html.json.testing.JsonTestUtility;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

@RunWith(ClientTestRunner.class)
@RunWithSubject("default")
@RunWithClientSession(TestEnvironmentClientSession.class)
public class JsonResponseTest {

  private UiSessionMock m_uiSession = new UiSessionMock();

  @Test
  public void testJsonEventPropertyChangeEvent() {
    // Check empty response
    JSONObject json = m_uiSession.currentJsonResponse().toJson();

    assertNotNull(json);
    JSONArray events = json.optJSONArray(JsonResponse.PROP_EVENTS);
    assertNull(events);

    // Check single property change event
    JsonTestUtility.endRequest(m_uiSession);
    final String testId = "ID007";
    final String testPropName = "a stränge prøpertÿ name";
    final String testValue = "#";
    m_uiSession.currentJsonResponse().addPropertyChangeEvent(testId, testPropName, testValue);
    json = m_uiSession.currentJsonResponse().toJson();

    assertNotNull(json);
    events = json.getJSONArray(JsonResponse.PROP_EVENTS);
    assertEquals(1, events.length());

    JSONObject event = events.getJSONObject(0);
    assertEquals(testId, event.get("target"));
    assertEquals("property", event.get("type"));
    JSONObject props = event.getJSONObject("properties");
    assertNotNull(props);
    assertEquals(1, props.length());
    assertEquals(testPropName, props.keys().next());
    Object value = props.get(testPropName);
    assertEquals(testValue, value);
  }

  /**
   * Test that properties with the value null get get added to the response. Normal Java "null" objects would get
   * removed by JSONObject, therefore the special NULL marker object should be used.
   */
  @SuppressWarnings({"SimplifiableJUnitAssertion", "ConstantConditions"})
  @Test
  public void testJsonEventPropertyNullToEmptyString() throws JSONException {
    m_uiSession.currentJsonResponse().addPropertyChangeEvent("-1", "name", null);
    JSONObject json = m_uiSession.currentJsonResponse().toJson();
    JSONArray events = json.getJSONArray(JsonResponse.PROP_EVENTS);
    JSONObject props = events.getJSONObject(0).getJSONObject("properties");
    assertTrue(props.has("name"));
    assertTrue(props.isNull("name"));
    assertNotEquals(null, props.get("name"));
    // JSONObject.NULL is equal to null, but this cannot be checked with assertEquals() due to the junit implementation
    assertEquals(JSONObject.NULL, props.get("name")); // NULL object is equal to null
    assertTrue(props.get("name").equals(null));
    assertTrue(events.toString().contains("\"name\":null"));
  }

  @Test
  public void testDoAddEvent_PropertyChange() {
    IJsonAdapter<?> mockAdapter = Mockito.mock(IJsonAdapter.class);
    Mockito.when(mockAdapter.getId()).thenReturn("foo");

    // when m_adapterMap does not contain the ID 'foo', property change should be added
    JsonResponse resp = new JsonResponse();
    resp.addPropertyChangeEvent(mockAdapter.getId(), "name", "andre");
    JSONObject json = resp.toJson();
    JSONObject propertyChange = JsonTestUtility.getPropertyChange(json, 0);
    assertEquals("andre", propertyChange.getString("name"));

    // when m_adapterMap contains the ID 'foo', property change must be removed
    resp = new JsonResponse();
    resp.addAdapter(mockAdapter);
    resp.addPropertyChangeEvent(mockAdapter.getId(), "name", "andre");
    json = resp.toJson();
    assertNull(json.optJSONArray(JsonResponse.PROP_EVENTS));

    // protected action events must be preserved, event when the corresponding adapter is in m_adapterMap
    resp = new JsonResponse();
    resp.addAdapter(mockAdapter);
    resp.addActionEvent(mockAdapter.getId(), "normalEvent");
    resp.addActionEvent(mockAdapter.getId(), "protectedEvent").protect();
    json = resp.toJson();
    assertNotNull(json.optJSONArray(JsonResponse.PROP_EVENTS));
    assertEquals(1, json.optJSONArray(JsonResponse.PROP_EVENTS).length());
    assertEquals("protectedEvent", json.optJSONArray(JsonResponse.PROP_EVENTS).getJSONObject(0).getString("type"));

    // protected events must be removed when the adapter is removed
    resp = new JsonResponse();
    resp.addAdapter(mockAdapter);
    resp.addActionEvent(mockAdapter.getId(), "normalEvent");
    resp.addActionEvent(mockAdapter.getId(), "protectedEvent").protect();
    resp.removeJsonAdapter(mockAdapter.getId());
    json = resp.toJson();
    assertNull(json.optJSONArray(JsonResponse.PROP_EVENTS));
  }

  public static Map<String, IJsonAdapter<?>> getAdapterData(JsonResponse jsonResponse) {
    return jsonResponse.adapterMap();
  }
}
