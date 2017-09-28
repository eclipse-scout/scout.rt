/*******************************************************************************
 * Copyright (c) 2014-2017 BSI Business Systems Integration AG.
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
import org.eclipse.scout.rt.platform.util.ObjectUtility;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JsonRequest {
  private static final Logger LOG = LoggerFactory.getLogger(JsonRequest.class);

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
  private static final String PROP_SYNC_RESPONSE_QUEUE = "syncResponseQueue";

  public static final String PROP_UI_SESSION_ID = "uiSessionId";
  public static final String PROP_EVENTS = "events";
  public static final String PROP_EVENT = "event";
  public static final String PROP_MESSAGE = "message";
  public static final String PROP_SEQUENCE_NO = "#"; // request sequence no.
  public static final String PROP_ACK_SEQUENCE_NO = "#ACK"; // acknowledge response sequence no.

  private final JSONObject m_request;
  private final RequestType m_requestType;

  /**
   * Creates a new (validated) JsonRequest instance.
   *
   * @throws AssertionException
   *           if mandatory property 'uiSessionId' is not set for a request other than 'STARTUP' or 'PING' request.
   */
  public JsonRequest(JSONObject request) {
    final RequestType requestType = RequestType.valueOf(request);
    validate(requestType, request);
    m_requestType = requestType;
    m_request = request;
  }

  protected void validate(final RequestType requestType, JSONObject request) {
    // Ensure request contains an UI session ID, except for the startup and the ping request
    if (!ObjectUtility.isOneOf(requestType, RequestType.STARTUP_REQUEST, RequestType.PING_REQUEST)) {
      Assertions.assertTrue(request.has(PROP_UI_SESSION_ID), "Missing property '{}' in request {}", PROP_UI_SESSION_ID, request);
    }

    // Only normal /json requests may send events.
    if (requestType != RequestType.REQUEST && request.has(PROP_EVENTS)) {
      request.remove(PROP_EVENTS);

      String requestAsString = request.toString();
      if (requestAsString.length() > 10000) {
        // Truncate the message to prevent log inflation by malicious requests
        requestAsString = requestAsString.substring(0, 10000) + "...";
      }
      LOG.info("Request contains unexpected attribute '{}': {}", PROP_EVENTS, requestAsString);
    }
  }

  protected JSONObject getRequestObject() {
    return m_request;
  }

  public String getUiSessionId() {
    return m_request.optString(PROP_UI_SESSION_ID, null);
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

  /**
   * @return attribute {@link #PROP_EVENT} (for type {@link RequestType#LOG_REQUEST})
   */
  public JSONObject getEvent() {
    return m_request.optJSONObject(PROP_EVENT);
  }

  /**
   * @return attribute {@link #PROP_MESSAGE} (for type {@link RequestType#LOG_REQUEST})
   */
  public String getMessage() {
    return m_request.optString(PROP_MESSAGE, null);
  }

  /**
   * @return The acknowledged <i>response</i> sequence number
   */
  public Long getAckSequenceNo() {
    return JsonObjectUtility.optLong(m_request, PROP_ACK_SEQUENCE_NO);
  }

  /**
   * @return The sequence number of this <i>request</i>
   */
  public Long getSequenceNo() {
    return JsonObjectUtility.optLong(m_request, PROP_SEQUENCE_NO);
  }

  @Override
  public String toString() {
    return JsonObjectUtility.toString(m_request);
  }

  public enum RequestType {
    REQUEST,
    STARTUP_REQUEST,
    UNLOAD_REQUEST,
    POLL_REQUEST,
    CANCEL_REQUEST,
    PING_REQUEST,
    LOG_REQUEST,
    SYNC_RESPONSE_QUEUE;

    /**
     * Returns the <code>enum constant</code> which represents the given {@link JSONObject}.
     */
    private static RequestType valueOf(JSONObject request) {
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
      else if (request.has(PROP_SYNC_RESPONSE_QUEUE)) {
        return SYNC_RESPONSE_QUEUE;
      }
      else {
        return REQUEST;
      }
    }
  }
}
