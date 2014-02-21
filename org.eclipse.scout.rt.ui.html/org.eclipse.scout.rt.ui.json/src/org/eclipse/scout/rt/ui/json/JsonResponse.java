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
package org.eclipse.scout.rt.ui.json;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class JsonResponse {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(AbstractJsonSession.class);

  private final List<JSONObject> m_eventList;
  private final Map<String/*id*/, JSONObject> m_idToPropertyChangeEventMap;

  public JsonResponse() {
    m_eventList = new ArrayList<>();
    m_idToPropertyChangeEventMap = new HashMap<>();
  }

  /**
   * @param parentId
   * @param object
   *          must have an 'id' and a 'objectType'
   */
  public void addCreateEvent(String parentId, JSONObject object) throws JsonUIException {
    if (object == null) {
      return;
    }
    try {
      String id = object.getString("id");
      if (id == null) {
        throw new JsonUIException("id is null");
      }
      String objectType = object.getString("objectType");
      if (objectType == null) {
        throw new JsonUIException("objectType is null");
      }
      object.put("type_", "create");
      if (parentId != null) {
        object.put("parentId", parentId);
      }
      m_eventList.add(object);
    }
    catch (JSONException ex) {
      throw new JsonUIException(ex);
    }
  }

  /**
   * event must have an 'id'
   */
  public void addPropertyChangeEvent(String id, String propertyName, Object newValue) throws JsonUIException {
    try {
      if (id == null) {
        throw new JsonUIException("id is null");
      }
      //coalesce
      JSONObject event = m_idToPropertyChangeEventMap.get(id);
      if (event == null) {
        event = new JSONObject();
        event.put("id", id);
        event.put("type_", "property");
        m_eventList.add(event);
        m_idToPropertyChangeEventMap.put(id, event);
      }
      event.put(propertyName, newValue);
    }
    catch (JSONException ex) {
      throw new JsonUIException(ex);
    }
  }

  /**
   * event must have an 'id'
   */
  public void addActionEvent(String eventType, String id, JSONObject eventData) throws JsonUIException {
    try {
      if (id == null) {
        throw new JSONException("id is null");
      }
      //coalesce
      JSONObject event = eventData != null ? eventData : new JSONObject();
      event.put("id", id);
      event.put("type_", eventType);
      m_eventList.add(event);
    }
    catch (JSONException ex) {
      throw new JsonUIException(ex);
    }
  }

  public JSONObject toJson() {
    JSONObject response = new JSONObject();
    try {
      JSONArray eventArray = new JSONArray();
      for (JSONObject e : m_eventList) {
        eventArray.put(e);
      }
      response.put("events", eventArray);
    }
    catch (JSONException ex) {
      LOG.error("", ex);
    }
    return response;
  }

}
