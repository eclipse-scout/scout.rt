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

import static org.eclipse.scout.rt.ui.html.json.JsonObjectUtility.putProperty;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.scout.commons.CollectionUtility;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.json.JSONArray;
import org.json.JSONObject;

public class JsonResponse {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(AbstractJsonSession.class);

  public static final int ERR_SESSION_TIMEOUT = 10;

  private Integer m_errorCode;
  private String m_errorMessage;
  private final List<JSONObject> m_eventList;
  private final Map<String/*id*/, JSONObject> m_idToPropertyChangeEventMap;

  public JsonResponse() {
    m_eventList = new ArrayList<>();
    m_idToPropertyChangeEventMap = new HashMap<>();
  }

  /**
   * @param parentId
   * @param json
   *          must have an 'id' and a 'objectType'
   */
  public void addCreateEvent(String parentId, JSONObject json) {
    if (json == null) {
      return;
    }
    String id = json.optString("id", null);
    if (id == null) {
      throw new JsonException("id is null");
    }
    String objectType = json.optString("objectType", null);
    if (objectType == null) {
      throw new JsonException("objectType is null");
    }
    putProperty(json, "type_", "create");
    if (parentId != null) {
      putProperty(json, "parentId", parentId);
    }
    m_eventList.add(json);
  }

  /**
   * event must have an 'id'
   */
  public void addPropertyChangeEvent(String id, String propertyName, Object newValue) {
    if (id == null) {
      throw new JsonException("id is null");
    }
    //If text is null it won't be transferred -> set to empty string
    //FIXME CGU check if it is necessary on client to convert '' to null. Maybe not because !value also checks for emtpy string
    if (newValue == null) {
      newValue = "";
    }
    //coalesce
    JSONObject event = m_idToPropertyChangeEventMap.get(id);
    if (event == null) {
      event = new JSONObject();
      putProperty(event, "id", id);
      putProperty(event, "type_", "property");
      m_eventList.add(event);
      m_idToPropertyChangeEventMap.put(id, event);
    }
    putProperty(event, propertyName, newValue);
  }

  /**
   * event must have an 'id'
   */
  public void addActionEvent(String eventType, String id, JSONObject eventData) {
    if (id == null) {
      throw new JsonException("id is null");
    }
    JSONObject event = eventData != null ? eventData : new JSONObject();
    putProperty(event, "id", id);
    putProperty(event, "type_", eventType);
    m_eventList.add(event);
  }

  public JSONObject toJson() {
    JSONObject response = new JSONObject();
    JSONArray eventArray = new JSONArray();
    for (JSONObject e : m_eventList) {
      eventArray.put(e);
    }
    putProperty(response, "events", eventArray);
    putProperty(response, "errorCode", m_errorCode);
    putProperty(response, "errorMessage", m_errorMessage);
    return response;
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
