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
package org.eclipse.scout.rt.ui.html.json;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.scout.commons.CollectionUtility;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.json.JSONArray;
import org.json.JSONObject;

public class JsonResponse {

  private static final IScoutLogger LOG = ScoutLogManager.getLogger(JsonResponse.class);

  public static final int ERR_SESSION_TIMEOUT = 10;

  private Integer m_errorCode;
  private String m_errorMessage;
  private final List<JSONObject> m_eventList;
  private final Map<String/*id*/, JSONObject> m_idToPropertyChangeEventMap;
  private Map<String, IJsonAdapter<?>> m_adapterMap;

  public JsonResponse() {
    m_eventList = new ArrayList<>();
    m_adapterMap = new HashMap<>();
    m_idToPropertyChangeEventMap = new HashMap<>();
  }

  // TODO AWE: (json) JsonEvent verwenden

  /**
   * event must have an 'id'
   */
  public void addPropertyChangeEvent(String id, String propertyName, Object newValue) {
    requiresId(id);
    //If text is null it won't be transferred -> set to empty string
    //FIXME CGU check if it is necessary on client to convert '' to null. Maybe not because !value also checks for emtpy string
    if (newValue == null) {
      newValue = "";
    }
    //coalesce
    JSONObject event = m_idToPropertyChangeEventMap.get(id);
    if (event == null) {
      event = new JSONObject();
      JsonObjectUtility.putProperty(event, "id", id);
      JsonObjectUtility.putProperty(event, "type", "property");
      m_eventList.add(event);
      m_idToPropertyChangeEventMap.put(id, event);
    }
    JSONObject props = event.optJSONObject("properties");
    if (props == null) {
      props = new JSONObject();
      JsonObjectUtility.putProperty(event, "properties", props);
    }
    JsonObjectUtility.putProperty(props, propertyName, newValue);
  }

  /**
   * Adds an adapter to the response. All adapters stored on the response are transferred to the client (browser)
   * as JSON object. Only new adapters must be transferred, adapters already transferred to the client can be
   * solely referenced by their ID.
   */
  public void addAdapter(IJsonAdapter<?> adapter) {
    if (!m_adapterMap.containsKey(adapter.getId())) {
      m_adapterMap.put(adapter.getId(), adapter);
    }
  }

  private void requiresId(String id) {
    if (id == null) {
      throw new JsonException("id is null");
    }
  }

  /**
   * Event must have an 'id'.
   */
  public void addActionEvent(String eventType, String id, JSONObject eventData) {
    requiresId(id);
    JSONObject event = eventData != null ? eventData : new JSONObject();
    JsonObjectUtility.putProperty(event, "id", id);
    JsonObjectUtility.putProperty(event, "type", eventType);
    m_eventList.add(event);
  }

  // FIXME CGU potential threading issue: toJson is called by servlet thread. Property-Change-Events may alter the eventList from client job thread

  /**
   * Returns a JSON string representation of this instance. This method is called at the end of a request.
   * The return value of this method is returned to the client-side GUI. There are some noteworthy points:
   * <ul>
   * <li>All new adapters (= adapters not yet transferred to the client), are put as a list in the 'adapterData'
   * property.</li>
   * <li>All events are transferred in the 'events' property.</li>
   * </ul>
   * This method will call the <code>toJson()</code> method on all adapter objects. Note that you can NOT create new
   * adapter instances when the toJson() method runs! All new adapter instances must be created before: either in
   * the <code>attachModel()</code> method or in an event handler method like <code>handleXYZ()</code>. The technical
   * reason for this is, first: new adapters are added to the current response (see AbstractJsonSession), but at the
   * point in time toJson() is called, we already have a new instance of the current response. Second: when we loop
   * through the adapterMap and call toJson() for each adapter, if the adapter would create another adapter in its
   * toJson() method, the adapterMap would grow, which would cause a ConcurrentModificationException. Additionally
   * we should conceptually separate object creation from JSON output creation.
   */
  public JSONObject toJson() {
    JSONObject json = new JSONObject();
    JSONObject adapterData = new JSONObject();
    // If you experience a ConcurrentModificationException at this point, then most likely you've created and added a new adapter
    // in your to toJson() method, which is conceptually wrong. You must create new adapters when the attachModel() method is
    // called or when an action-event is processed (typically in a handleXYZ() method).
    // To debug which adapter caused the error, add a syso for entry.getValue() in the following loop.
    for (Entry<String, IJsonAdapter<?>> entry : m_adapterMap.entrySet()) {
      JsonObjectUtility.putProperty(adapterData, entry.getKey(), entry.getValue().toJson());
    }

    JSONArray eventArray = new JSONArray();
    for (JSONObject event : m_eventList) {
      if (doAddEvent(event)) {
        eventArray.put(event);
      }
    }

    JsonObjectUtility.putProperty(json, "events", eventArray);
    JsonObjectUtility.putProperty(json, "adapterData", adapterData);
    JsonObjectUtility.putProperty(json, "errorCode", m_errorCode);
    JsonObjectUtility.putProperty(json, "errorMessage", m_errorMessage);
    return json;
  }

  /**
   * When we send a new adapter in the JSON response we can ignore all property change events
   * for that adapter, since the adapter data already describes the latest state of the adapter.
   */
  private boolean doAddEvent(JSONObject event) {
    String id = event.optString("id");
    String type = event.optString("type");
    if ("property".equals(type)) {
      if (m_adapterMap.containsKey(id)) {
        return false;
      }
    }
    return true;
  }

  public List<JSONObject> getEventList() {
    return CollectionUtility.arrayList(m_eventList);
  }

  public void setErrorCode(Integer errorCode) {
    m_errorCode = errorCode;
  }

  public void setErrorMessage(String errorMessage) {
    m_errorMessage = errorMessage;
  }

}
