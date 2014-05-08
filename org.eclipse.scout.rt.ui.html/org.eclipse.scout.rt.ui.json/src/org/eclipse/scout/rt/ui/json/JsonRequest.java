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
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class JsonRequest {

  public static final String PROP_STARTUP = "startup";
  public static final String PROP_SESSION_PART_ID = "sessionPartId";
  public static final String PROP_USER_AGENT = "userAgent";
  public static final String PROP_EVENTS = "events";

  private final JSONObject m_request;

  public JsonRequest(JSONObject request) {
    m_request = request;
  }

  public String getSessionPartId() {
    try {
      return m_request.getString(PROP_SESSION_PART_ID);
    }
    catch (JSONException e) {
      throw new JsonException(e);
    }
  }

  public List<JsonEvent> getEvents() {
    try {
      JSONArray events = m_request.optJSONArray(PROP_EVENTS);
      if (events == null) {
        return new ArrayList<>(0);
      }
      List<JsonEvent> actionList = new ArrayList<>(events.length());
      for (int i = 0; i < events.length(); i++) {
        actionList.add(new JsonEvent(events.getJSONObject(i)));
      }
      return actionList;
    }
    catch (JSONException e) {
      throw new JsonException(e);
    }
  }

  public JSONObject getRequestObject() {
    return m_request;
  }

  public boolean isStartupRequest() {
    return Boolean.TRUE.equals(m_request.opt(PROP_STARTUP));
  }

  /**
   * Only set on startup requests
   */
  public JSONObject getUserAgent() {
    return m_request.optJSONObject(PROP_USER_AGENT);
  }
}
