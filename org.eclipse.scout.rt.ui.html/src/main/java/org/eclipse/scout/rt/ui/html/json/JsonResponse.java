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
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.scout.commons.CollectionUtility;
import org.eclipse.scout.commons.CompareUtility;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.json.JSONArray;
import org.json.JSONObject;

public class JsonResponse {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(JsonResponse.class);

  public static final int ERR_STARTUP_FAILED = 5;
  public static final int ERR_SESSION_TIMEOUT = 10;
  public static final int ERR_UI_PROCESSING = 20;

  public static final String PROP_EVENTS = "events";
  public static final String PROP_ADAPTER_DATA = "adapterData";
  public static final String PROP_ERROR = "error";
  public static final String PROP_ERROR_CODE = "errorCode";
  public static final String PROP_ERROR_MESSAGE = "errorMessage";

  private final Map<String/*adapterId*/, IJsonAdapter<?>> m_adapterMap;
  private final List<JsonEvent> m_eventList;
  private final Map<String/*adapterId*/, JsonEvent> m_idToPropertyChangeEventMap; // helper map to ensure max. 1 event per adapter
  private final Set<IJsonAdapter<?>> m_bufferedEventsAdapters;
  private boolean m_error;
  private int m_errorCode;
  private String m_errorMessage;

  private boolean m_toJsonInProgress;
  private boolean m_processingBufferedEvents;

  public JsonResponse() {
    m_adapterMap = new HashMap<>();
    m_eventList = new ArrayList<>();
    m_idToPropertyChangeEventMap = new HashMap<>();
    m_bufferedEventsAdapters = new HashSet<>();
  }

  /**
   * Adds an adapter to the response. All adapters stored on the response are transferred to the client (browser)
   * as JSON object. Only new adapters must be transferred, adapters already transferred to the client can be
   * solely referenced by their ID.
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
      JsonObjectUtility.putProperty(event.getData(), "properties", props);
    }

    // If text is null it won't be transferred -> set to empty string
    if (newValue == null) {
      newValue = "";
    }
    JsonObjectUtility.putProperty(props, propertyName, newValue);
  }

  /**
   * Note: when converting the response to JSON, events on adapters that are also part of this response are ignored, see
   * also {@link #doAddEvent(JsonEvent)}
   */
  public void addActionEvent(String eventTarget, String eventType, JSONObject eventData) {
    m_eventList.add(new JsonEvent(eventTarget, eventType, eventData));
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

  /**
   * Marks this JSON response as "error" (default is "success").
   *
   * @param errorCode
   *          An arbitrary number indicating the type of error.
   * @param errorMessage
   *          A message describing the error. This message is mostly useful for debugging purposes. Usually, it is
   *          not shown to the user, because it is not language-dependent. If possible, the displayed message is
   *          translated by the client using the <code>errorCode</code> parameter (see Session.js).
   */
  public void markAsError(int errorCode, String errorMessage) {
    m_error = true;
    m_errorCode = errorCode;
    m_errorMessage = errorMessage;
  }

  public boolean isMarkedAsError() {
    return m_error;
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
          JsonObjectUtility.putProperty(adapterData, entry.getKey(), adapterJson);
          if (LOG.isDebugEnabled()) {
            if (adapterIds == null) {
              adapterIds = new LinkedList<String>();
            }
            adapterIds.add(entry.getValue().getId());
          }
        }
      }
      if (adapterIds != null && LOG.isDebugEnabled()) {
        LOG.debug("Adapter data created for these adapters: " + adapterIds);
      }

      JSONArray eventArray = new JSONArray();
      for (JsonEvent event : m_eventList) {
        if (doAddEvent(event)) {
          eventArray.put(event.toJson());
        }
      }

      JsonObjectUtility.putProperty(json, PROP_EVENTS, (eventArray.length() == 0 ? null : eventArray));
      JsonObjectUtility.putProperty(json, PROP_ADAPTER_DATA, (adapterData.length() == 0 ? null : adapterData));
      if (m_error) {
        JSONObject jsonError = JsonObjectUtility.newOrderedJSONObject();
        JsonObjectUtility.putProperty(jsonError, PROP_ERROR_CODE, m_errorCode);
        JsonObjectUtility.putProperty(jsonError, PROP_ERROR_MESSAGE, m_errorMessage);
        JsonObjectUtility.putProperty(json, PROP_ERROR, jsonError);
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
      for (IJsonAdapter<?> adapter : m_bufferedEventsAdapters) {
        if (!adapter.isDisposed()) {
          adapter.processBufferedEvents();
        }
      }
    }
    finally {
      m_processingBufferedEvents = false;
    }
  }

  /**
   * When we send a new adapter in the JSON response we have to ignore all events
   * for that adapter, since the adapter data already describes the latest state of the adapter.
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
   * Removes all traces of the adapter with the given ID from the current response. This includes all events with
   * the given ID as target. Also, if the adapter was registered as "buffered events adapter", it is unregistered
   * automatically. Any deferred model event for this adapter will therefore not be handled.
   *
   * @param id
   *          Adapter ID to be removed
   */
  public void removeJsonAdapter(String id) {
    // Remove from adapterMap
    m_adapterMap.remove(id);

    // Remove all events with the given ID as event target
    for (Iterator<JsonEvent> it = m_eventList.iterator(); it.hasNext();) {
      JsonEvent event = it.next();
      if (CompareUtility.equals(event.getTarget(), id)) {
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

  protected Map<String, IJsonAdapter<?>> adapterMap() {
    return m_adapterMap;
  }

  protected List<JsonEvent> eventList() {
    return m_eventList;
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
}
