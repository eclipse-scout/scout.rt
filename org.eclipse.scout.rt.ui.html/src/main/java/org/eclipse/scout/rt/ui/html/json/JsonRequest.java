/*******************************************************************************
 * Copyright (c) 2014-2015 BSI Business Systems Integration AG.
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

import org.eclipse.scout.rt.platform.util.Assertions;
import org.eclipse.scout.rt.platform.util.Assertions.AssertionException;
import org.json.JSONArray;
import org.json.JSONObject;

public class JsonRequest {

  /**
   * The {@link JsonRequest} which is currently associated with the current thread.
   */
  public static final ThreadLocal<JsonRequest> CURRENT = new ThreadLocal<>();

  private static final String PROP_STARTUP = "startup";
  private static final String PROP_UNLOAD = "unload";
  private static final String PROP_LOG = "log";
  private static final String PROP_POLL = "pollForBackgroundJobs";
  private static final String PROP_CANCEL = "cancel";
  private static final String PROP_PING = "ping";

  public static final String PROP_UI_SESSION_ID = "uiSessionId";
  public static final String PROP_EVENTS = "events";

  private final JSONObject m_request;
  private final RequestType m_requestType;

  /**
   * Creates a new JsonRequest instance.
   *
   * @throws AssertionException
   *           if mandatory property 'uiSessionId' is not set for a request other than 'PING' request.
   */
  public JsonRequest(JSONObject request) {
    final RequestType requestType = RequestType.valueOf(request);
    Assertions.assertTrue(RequestType.PING_REQUEST.equals(requestType) || request.has(PROP_UI_SESSION_ID), "Missing property '%s' in request %s", PROP_UI_SESSION_ID, request);

    m_requestType = requestType;
    m_request = request;
  }

  protected JSONObject getRequestObject() {
    return m_request;
  }

  public String getUiSessionId() {
    return m_request.getString(PROP_UI_SESSION_ID);
  }

  public List<JsonEvent> getEvents() {
    JSONArray events = m_request.optJSONArray(PROP_EVENTS);
    if (events == null) {
      return new ArrayList<>(0);
    }
    List<JsonEvent> actionList = new ArrayList<>(events.length());
    for (int i = 0; i < events.length(); i++) {
      JSONObject json = events.getJSONObject(i);
      actionList.add(JsonEvent.fromJson(json));
    }
    return actionList;
  }

  /**
   * Returns the type of the {@link JsonRequest}, and is never <code>null</code>.
   */
  public RequestType getRequestType() {
    return m_requestType;
  }

  @Override
  public String toString() {
    return JsonObjectUtility.toString(m_request);
  }

  public static enum RequestType {
    REQUEST,
    STARTUP_REQUEST,
    UNLOAD_REQUEST,
    POLL_REQUEST,
    CANCEL_REQUEST,
    PING_REQUEST,
    LOG_REQUEST;

    /**
     * Returns the <code>enum constant</code> which represents the given {@link JSONObject}.
     */
    private static final RequestType valueOf(JSONObject request) {
      if (request.has(PROP_PING)) {
        return PING_REQUEST;
      }
      else if (request.optBoolean(PROP_STARTUP)) {
        return STARTUP_REQUEST;
      }
      else if (request.optBoolean(PROP_UNLOAD)) {
        return UNLOAD_REQUEST;
      }
      else if (request.has(PROP_POLL)) {
        return POLL_REQUEST;
      }
      else if (request.has(PROP_CANCEL)) {
        return CANCEL_REQUEST;
      }
      else if (request.has(PROP_LOG)) {
        return LOG_REQUEST;
      }
      else {
        return REQUEST;
      }
    }
  }
}
