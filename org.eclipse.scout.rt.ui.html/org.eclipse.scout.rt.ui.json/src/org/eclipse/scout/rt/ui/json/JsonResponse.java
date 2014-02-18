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
  private final Map<String/*id*/, JSONObject> m_idToEventMap;

  public JsonResponse() {
    m_eventList = new ArrayList<>();
    m_idToEventMap = new HashMap<>();
  }

  /**
   * event must have 'id'
   */
  public void addCreateEvent(JSONObject eventData) {
    if (eventData == null) {
      return;
    }
    try {
      String id = eventData.getString("id");
      if (id == null) {
        throw new JSONException("id is null");
      }
      JSONObject e = new JSONObject();
      e.put("id", id);
      e.put("type", "create");
      e.put("data", eventData);
      m_eventList.add(e);
      m_idToEventMap.put(id, e);
    }
    catch (JSONException ex) {
      LOG.error("", ex);
    }
  }

  /**
   * event must have 'id'
   */
  public void addUpdateEvent(String id, String name, Object newValue) {
    try {
      if (id == null) {
        throw new JSONException("id is null");
      }
      //coalesce
//      JSONObject e = m_idToEventMap.get(id); //TODO does not work when having update and create events for the same id
//      if (e == null) {
      JSONObject e = new JSONObject();
      m_eventList.add(e);
      m_idToEventMap.put(id, e);
      e.put("id", id);
      e.put("type", "update");
      e.put("data", new JSONObject());
//      }
      e.getJSONObject("data").put(name, newValue);
    }
    catch (JSONException ex) {
      LOG.error("", ex);
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
