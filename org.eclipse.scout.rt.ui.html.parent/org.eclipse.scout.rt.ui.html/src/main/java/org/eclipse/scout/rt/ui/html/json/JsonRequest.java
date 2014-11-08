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
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class JsonRequest {

  public static final String PROP_STARTUP = "startup";
  public static final String PROP_UNLOAD = "unload";
  public static final String PROP_PING = "ping";
  public static final String PROP_JSON_SESSION_ID = "jsonSessionId";
  public static final String PROP_EVENTS = "events";

  private final JSONObject m_request;

  public JsonRequest(JSONObject request) {
    m_request = request;
  }

  protected JSONObject getRequestObject() {
    return m_request;
  }

  public String getJsonSessionId() {
    return JsonObjectUtility.getString(m_request, PROP_JSON_SESSION_ID);
  }

  public List<JsonEvent> getEvents() {
    JSONArray events = m_request.optJSONArray(PROP_EVENTS);
    if (events == null) {
      return new ArrayList<>(0);
    }
    List<JsonEvent> actionList = new ArrayList<>(events.length());
    for (int i = 0; i < events.length(); i++) {
      JSONObject json = JsonObjectUtility.getJSONObject(events, i);
      actionList.add(JsonEvent.fromJson(json));
    }
    return actionList;
  }

  public boolean isStartupRequest() {
    return m_request.optBoolean(PROP_STARTUP);
  }

  public boolean isUnloadRequest() {
    return m_request.optBoolean(PROP_UNLOAD);
  }

  public boolean isPingRequest() {
    return m_request.optBoolean(PROP_PING);
  }

  @Override
  public String toString() {
    if (m_request == null) {
      return "null";
    }
    try {
      return m_request.toString(2);
    }
    catch (JSONException e) {
      return m_request.toString();
    }
  }
}
