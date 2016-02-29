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
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.platform.util.CompareUtility;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JsonResponse {
  private static final Logger LOG = LoggerFactory.getLogger(JsonResponse.class);

  public static final int ERR_STARTUP_FAILED = 5;
  public static final int ERR_SESSION_TIMEOUT = 10;
  public static final int ERR_UI_PROCESSING = 20;

  public static final String PROP_SEQUENCE = "#";
  public static final String PROP_COMBINED = "combined";
  public static final String PROP_EVENTS = "events";
  public static final String PROP_ADAPTER_DATA = "adapterData";
  public static final String PROP_STARTUP_DATA = "startupData";
  public static final String PROP_ERROR = "error";
  public static final String PROP_ERROR_CODE = "code";
  public static final String PROP_ERROR_MESSAGE = "message";

  private final Long m_sequenceNo;
  private final Map<String/*adapterId*/, IJsonAdapter<?>> m_adapterMap;
  private final List<JsonEvent> m_eventList;
  private final Map<String/*adapterId*/, JsonEvent> m_idToPropertyChangeEventMap; // helper map to ensure max. 1 event per adapter
  private final Set<IJsonAdapter<?>> m_bufferedEventsAdapters;
  private volatile JSONObject m_startupData = null;
  private volatile boolean m_error;
  private volatile int m_errorCode;
  private volatile String m_errorMessage;
  private volatile boolean m_combined;

  private volatile boolean m_toJsonInProgress;
  private volatile boolean m_processingBufferedEvents;

  public JsonResponse() {
    this(null);
  }

  public JsonResponse(Long sequenceNo) {
    m_sequenceNo = sequenceNo;
    m_adapterMap = new HashMap<>();
    m_eventList = new ArrayList<>();
    m_idToPropertyChangeEventMap = new HashMap<>();
    m_bufferedEventsAdapters = new LinkedHashSet<>(); // use ordered map to make fireProcessBufferedEvents() deterministic
  }

  public Long getSequenceNo() {
    return m_sequenceNo;
  }

  /**
   * Adds an adapter to the response. All adapters stored on the response are transferred to the client (browser) as
   * JSON object. Only new adapters must be transferred, adapters already transferred to the client can be solely
   * referenced by their ID.
   * <p>
   * Note that in javascript the adapters are not created unless the first event is received or
   * {@link IJsonAdapter#isCreateImmediately()} is set
   */
  public void addAdapter(IJsonAdapter<?> adapter) {
    if (m_toJsonInProgress) {
      throw new IllegalStateException("It is not allowed to modify the adapter list while toJson is executed. Adapter: " + adapter);
    }

    if (!m_adapterMap.containsKey(adapter.getId())) {
      m_adapterMap.put(adapter.getId(), adapter);
    }
  }

  /**
   * @param id
   *          Adapter ID
   * @param propertyName
   *          property name
   * @param newValue
   *          property value
   */
  public void addPropertyChangeEvent(String id, String propertyName, Object newValue) {
    // coalesce
    JsonEvent event = m_idToPropertyChangeEventMap.get(id);
    if (event == null) {
      event = new JsonEvent(id, JsonEventType.PROPERTY.getEventType(), null);
      m_eventList.add(event);
      m_idToPropertyChangeEventMap.put(id, event);
    }

    JSONObject props = event.getData().optJSONObject("properties");
    if (props == null) {
      props = new JSONObject();
      event.getData().put("properties", props);
    }
    // Add special NULL object for null values to preserve them in the resulting JSON string
    props.put(propertyName, (newValue == null ? JSONObject.NULL : newValue));
  }

  public void addActionEvent(String eventTarget, String eventType) {
    addActionEvent(eventTarget, eventType, null);
  }

  /**
   * Note: when converting the response to JSON, events on adapters that are also part of this response are ignored, see
   * also {@link #doAddEvent(JsonEvent)}
   */
  public void addActionEvent(String eventTarget, String eventType, JSONObject eventData) {
    m_eventList.add(new JsonEvent(eventTarget, eventType, eventData));
  }

  /**
   * Same as {@link #addActionEvent(String, String, JSONObject)} but with an 'eventReference' added to the JSON event.
   * This reference is considered additionally to 'eventTarget' when removing events (see
   * {@link #removeJsonAdapter(String)}).
   * <p>
   * Note: when converting the response to JSON, events on adapters that are also part of this response are ignored, see
   * also {@link #doAddEvent(JsonEvent)}
   */
  public void addActionEvent(String eventTarget, String eventType, String eventReference, JSONObject eventData) {
    m_eventList.add(new JsonEvent(eventTarget, eventType, eventReference, eventData));
  }

  /**
   * Note: when converting the response to JSON, events on adapters that are also part of this response are ignored, see
   * also {@link #doAddEvent(JsonEvent)}
   */
  public void replaceActionEvent(String eventTarget, String eventType, JSONObject eventData) {
    for (Iterator<JsonEvent> it = m_eventList.iterator(); it.hasNext();) {
      JsonEvent event = it.next();
      // Same target and same type --> remove existing event
      if (CompareUtility.equals(event.getTarget(), eventTarget) &&
          CompareUtility.equals(event.getType(), eventType)) {
        it.remove();
      }
    }
    addActionEvent(eventTarget, eventType, eventData);
  }

  /**
   * Registers the given adapter as a holder of buffered events. Before executing {@link #toJson()} those buffers are
   * consumed automatically. (Additionally, all registered buffers can be consumed manually with
   * {@link #fireProcessBufferedEvents()}.)
   */
  public void registerBufferedEventsAdapter(IJsonAdapter<?> adapter) {
    if (m_processingBufferedEvents) {
      throw new IllegalStateException("Cannot register an adapter as buffered events provider while processing buffered events [" + adapter + "]");
    }
    if (adapter != null) {
      m_bufferedEventsAdapters.add(adapter);
    }
  }

  public void unregisterBufferedEventsAdapter(IJsonAdapter<?> adapter) {
    if (m_processingBufferedEvents) {
      throw new IllegalStateException("Cannot unregister an adapter as buffered events provider while processing buffered events [" + adapter + "]");
    }
    if (adapter != null) {
      m_bufferedEventsAdapters.remove(adapter);
    }
  }

  public void markAsStartupResponse() {
    m_startupData = new JSONObject();
  }

  public boolean isMarkedAsStartupResponse() {
    return m_startupData != null;
  }

  /**
   * The (mutable) <code>startupData</code> object if the response is marked as "startup response", <code>null</code>
   * otherwise.
   */
  public JSONObject getStartupData() {
    return m_startupData;
  }

  /**
   * Marks this JSON response as "error" (default is "success").
   *
   * @param errorCode
   *          An arbitrary number indicating the type of error.
   * @param errorMessage
   *          A message describing the error. This message is mostly useful for debugging purposes. Usually, it is not
   *          shown to the user, because it is not language-dependent. If possible, the displayed message is translated
   *          by the client using the <code>errorCode</code> parameter (see Session.js).
   */
  public void markAsError(int errorCode, String errorMessage) {
    m_error = true;
    m_errorCode = errorCode;
    m_errorMessage = errorMessage;
  }

  public boolean isMarkedAsError() {
    return m_error;
  }

  // FIXME cgu: potential threading issue: toJson is called by servlet thread. Property-Change-Events may alter the eventList from client job thread

  /**
   * Returns a JSON string representation of this instance. This method is called at the end of a request. The return
   * value of this method is returned to the client-side GUI. There are some noteworthy points:
   * <ul>
   * <li>All new adapters (= adapters not yet transferred to the client), are put as a list in the 'adapterData'
   * property.</li>
   * <li>All events are transferred in the 'events' property.</li>
   * </ul>
   * This method will call the <code>toJson()</code> method on all adapter objects. Note that you can NOT create new
   * adapter instances when the toJson() method runs! All new adapter instances must be created before: either in the
   * <code>attachModel()</code> method or in an event handler method like <code>handleXYZ()</code>. The technical reason
   * for this is, first: new adapters are added to the current response (see UiSession), but at the point in time
   * toJson() is called, we already have a new instance of the current response. Second: when we loop through the
   * adapterMap and call toJson() for each adapter, if the adapter would create another adapter in its toJson() method,
   * the adapterMap would grow, which would cause a ConcurrentModificationException. Additionally we should conceptually
   * separate object creation from JSON output creation.
   */
  public JSONObject toJson() {
    // Ensure all buffered events are handled. This might cause the addition of more events and adapters to this response.
    fireProcessBufferedEvents();

    JSONObject json = new JSONObject();
    JSONObject adapterData = new JSONObject();
    m_toJsonInProgress = true;
    try {
      // If you experience a ConcurrentModificationException at this point, then most likely you've created and added a new adapter
      // in your to toJson() method, which is conceptually wrong. You must create new adapters when the attachModel() method is
      // called or when an action-event is processed (typically in a handleXYZ() method).
      // To debug which adapter caused the error, add a syso for entry.getValue() in the following loop.
      List<String> adapterIds = null;
      for (Entry<String, IJsonAdapter<?>> entry : m_adapterMap.entrySet()) {
        JSONObject adapterJson = entry.getValue().toJson();
        if (adapterJson != null) {
          JsonObjectUtility.filterDefaultValues(adapterJson);
          adapterData.put(entry.getKey(), adapterJson);
          if (LOG.isDebugEnabled()) {
            if (adapterIds == null) {
              adapterIds = new LinkedList<String>();
            }
            adapterIds.add(entry.getValue().getId());
          }
        }
      }
      if (adapterIds != null) {
        LOG.debug("Adapter data created for these adapters: {}", adapterIds);
      }

      JSONArray eventArray = new JSONArray();
      for (JsonEvent event : m_eventList) {
        if (doAddEvent(event)) {
          eventArray.put(event.toJson());
        }
      }

      json.put(PROP_SEQUENCE, m_sequenceNo);
      json.put(PROP_STARTUP_DATA, m_startupData);
      if (m_combined) {
        json.put(PROP_COMBINED, true);
      }
      json.put(PROP_EVENTS, (eventArray.length() == 0 ? null : eventArray));
      json.put(PROP_ADAPTER_DATA, (adapterData.length() == 0 ? null : adapterData));
      if (m_error) {
        // !!! IMPORTANT: If you change the response structure here, it has to be changed accordingly in the hard coded string
        // org.eclipse.scout.rt.server.commons.servlet.filter.authentication.ServletFilterHelper.JSON_SESSION_TIMEOUT_RESPONSE
        JSONObject jsonError = new JSONObject();
        jsonError.put(PROP_ERROR_CODE, m_errorCode);
        jsonError.put(PROP_ERROR_MESSAGE, m_errorMessage);
        json.put(PROP_ERROR, jsonError);
      }
    }
    finally {
      m_toJsonInProgress = false;
    }
    return json;
  }

  /**
   * Causes all registered {@link IBufferedEventsProvider}s to process their buffered events. This may add some events
   * and adapters to this response. This method is called automatically during {@link #toJson()}.
   */
  public void fireProcessBufferedEvents() {
    m_processingBufferedEvents = true;
    try {
      // Use a copy of the original m_bufferedEventsAdapters list to prevent ConcurrentModificationExceptions
      // while we're looping through the list and removeJsonAdapter is called.
      for (IJsonAdapter<?> adapter : new ArrayList<>(m_bufferedEventsAdapters)) {
        if (!adapter.isDisposed()) {
          adapter.processBufferedEvents();
        }
      }
      // Remove adapter references from the response object, because it might be kept in memory for some time (response history)
      m_bufferedEventsAdapters.clear();
    }
    finally {
      m_processingBufferedEvents = false;
    }
  }

  /**
   * When we send a new adapter in the JSON response we have to ignore all events for that adapter, since the adapter
   * data already describes the latest state of the adapter.
   * <p>
   * For property change events this is just an optimization to reduce the response size.<br>
   * For other event types it may be crucial that the events are not sent.<br>
   * Example: NodesInserted event on tree must not be sent since the same nodes are already sent by Tree.toJson.
   */
  protected boolean doAddEvent(JsonEvent event) {
    if (m_adapterMap.containsKey(event.getTarget())) {
      return false;
    }
    return true;
  }

  /**
   * Removes all traces of the adapter with the given ID from the current response. This includes all events with the
   * given ID as target or "reference". Also, if the adapter was registered as "buffered events adapter", it is
   * unregistered automatically. Any deferred model event for this adapter will therefore not be handled.
   *
   * @param id
   *          Adapter ID to be removed
   */
  public void removeJsonAdapter(String id) {
    // Remove from adapterMap
    IJsonAdapter<?> removedAdapter = m_adapterMap.remove(id);

    // Remove all related events
    for (Iterator<JsonEvent> it = m_eventList.iterator(); it.hasNext();) {
      JsonEvent event = it.next();
      if (CompareUtility.equals(event.getTarget(), id)) {
        // Event is targeted to the removed adapter
        it.remove();
      }
      else if (removedAdapter != null && event.getReference() != null && CompareUtility.equals(event.getReference(), id)) {
        // Event is not directly targeted to the removed adapter but "references" it. If the adapter
        // was not yet sent to the UI (= it was contained in the m_adapterMap) we may safely purge
        // the event. Example: "showForm" + "hideForm" in the same request.
        it.remove();
      }
    }

    // Remove property change events for this adapter
    m_idToPropertyChangeEventMap.remove(id);

    // Unregister as buffered events adapter (we are no longer interested in those buffered events)
    for (Iterator<IJsonAdapter<?>> it = m_bufferedEventsAdapters.iterator(); it.hasNext();) {
      IJsonAdapter<?> adapter = it.next();
      if (CompareUtility.equals(adapter.getId(), id)) {
        it.remove();
      }
    }
  }

  /**
   * @return a copy of the event list
   */
  public List<JsonEvent> getEventList() {
    return CollectionUtility.arrayList(m_eventList);
  }

  protected List<JsonEvent> eventList() {
    return m_eventList;
  }

  protected Map<String, IJsonAdapter<?>> adapterMap() {
    return m_adapterMap;
  }

  protected JSONObject startupData() {
    return m_startupData;
  }

  protected boolean error() {
    return m_error;
  }

  protected int errorCode() {
    return m_errorCode;
  }

  protected String errorMessage() {
    return m_errorMessage;
  }

  public boolean isEmpty() {
    return m_adapterMap.isEmpty() && m_eventList.isEmpty() && m_bufferedEventsAdapters.isEmpty() && m_startupData == null;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder("JsonResponse [");
    if (m_sequenceNo != null) {
      sb.append("#").append(m_sequenceNo).append(", ");
    }
    if (m_startupData != null) {
      sb.append("STARTUP RESPONSE, ");
    }
    sb.append("adapters: ").append(m_adapterMap.size()).append(", ");
    sb.append("events: ").append(m_eventList.size()).append(", ");
    sb.append("buffered events adapters: ").append(m_bufferedEventsAdapters.size());
    if (m_error) {
      sb.append(", MARKED AS ERROR ").append(m_errorCode).append(": ").append(m_errorMessage);
    }
    sb.append("]");
    return sb.toString();
  }

  public void combine(JsonResponse response) {
    m_combined = true;
    m_adapterMap.putAll(response.m_adapterMap);
    m_eventList.addAll(response.m_eventList);
  }
}
