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
import java.util.Collections;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class JsonRequest {
  public static final String PROP_STARTUP = "startup";
  public static final String PROP_EVENTS = "events";

  private final JSONObject m_request;

  public JsonRequest(JSONObject request) {
    m_request = request;
  }

  public String getSessionPartId() {
    try {
      return m_request.getString("sessionPartId");
    }
    catch (JSONException e) {
      throw new JsonUIException(e);
    }
  }

  public List<JsonEvent> getEvents() {
    try {
      JSONArray events = m_request.getJSONArray(PROP_EVENTS);
      List<JsonEvent> actionList = new ArrayList<>();
      for (int i = 0; i < events.length(); i++) {
        actionList.add(new JsonEvent(events.getJSONObject(i)));
      }
      return Collections.unmodifiableList(actionList);
    }
    catch (JSONException e) {
      throw new JsonUIException(e);
    }
  }

  public JSONObject getRequestObject() {
    return m_request;
  }

  public boolean isStartupRequest() {
    return Boolean.TRUE.equals(m_request.opt(PROP_STARTUP));
  }
}
