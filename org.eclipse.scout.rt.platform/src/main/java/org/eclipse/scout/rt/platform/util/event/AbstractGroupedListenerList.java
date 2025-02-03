/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.platform.util.event;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.eclipse.scout.rt.platform.events.IListenerListWithManagement;
import org.eclipse.scout.rt.platform.events.ISnapshotCollector;
import org.eclipse.scout.rt.platform.events.ListenerListRegistry;

/**
 * Thread safe event listener list with a single listener type, grouped by event types
 * <ul>
 * <li>support for @see WeakEventListener</li>
 * <li>register listeners for only a subset of int event types</li>
 * <li>register specific listeners at the end of the calling-chain, typically ui listeners</li>
 * </ul>
 *
 * @since 8.0
 */
public abstract class AbstractGroupedListenerList<LISTENER, EVENT, EVENT_TYPE> implements IListenerListWithManagement {
  private final Map<EVENT_TYPE, UnsafeFastListenerList<LISTENER>> m_listenerMap = new HashMap<>();
  private final Map<EVENT_TYPE, UnsafeFastListenerList<LISTENER>> m_lastListenerMap = new HashMap<>();

  public AbstractGroupedListenerList() {
    ListenerListRegistry.globalInstance().registerAsWeakReference(this);
  }

  protected Object lockObject() {
    return m_listenerMap;
  }

  /**
   * These are the known event types. This is used in {@link #add(Object, boolean, Object...)} when no event types are
   * specified. The listener is the added for all known event types as well as for the special event type
   * {@link #otherEventsType()}. That way all listeners per event type are in the correct order (last added, first
   * called)
   */
  protected abstract Set<EVENT_TYPE> knownEventTypes();

  /**
   * see {@link #knownEventTypes()}
   */
  protected abstract EVENT_TYPE otherEventsType();

  protected abstract EVENT_TYPE eventType(EVENT event);

  protected abstract void handleEvent(LISTENER listener, EVENT event);

  Map<EVENT_TYPE, UnsafeFastListenerList<LISTENER>> internalListenerMap(boolean lastCalled) {
    return (lastCalled ? m_lastListenerMap : m_listenerMap);
  }

  public void fireEvents(List<EVENT> events) {
    if (events == null || events.isEmpty()) {
      return;
    }
    for (EVENT e : events) {
      fireEvent(e);
    }
  }

  public void fireEvent(EVENT event) {
    List<LISTENER> listeners = list(eventType(event));
    if (!listeners.isEmpty()) {
      for (LISTENER listener : listeners) {
        handleEvent(listener, event);
      }
    }
  }

  public List<LISTENER> list(EVENT_TYPE eventType) {
    synchronized (lockObject()) {
      return collectListenersInsideLock(eventType);
    }
  }

  public Map<EVENT_TYPE, List<LISTENER>> listAll() {
    synchronized (lockObject()) {
      Set<EVENT_TYPE> types = new HashSet<>();
      types.addAll(m_listenerMap.keySet());
      types.addAll(m_lastListenerMap.keySet());
      Map<EVENT_TYPE, List<LISTENER>> map = new HashMap<>();
      for (EVENT_TYPE type : types) {
        map.put(type, collectListenersInsideLock(type));
      }
      return map;
    }
  }

  /**
   * The last listener added is the first to be called
   *
   * @param listener
   * @param weak
   * @param eventTypes
   *     is either a single event type or an array of types, may also be a primitive array such as int[]
   */
  public void add(LISTENER listener, boolean weak, @SuppressWarnings("unchecked") EVENT_TYPE... eventTypes) {
    if (listener == null) {
      return;
    }
    synchronized (lockObject()) {
      if (eventTypes == null || eventTypes.length == 0) {
        for (EVENT_TYPE eventType : knownEventTypes()) {
          addInsideLock(m_listenerMap, listener, weak, eventType);
        }
        addInsideLock(m_listenerMap, listener, weak, otherEventsType());
      }
      else {
        for (EVENT_TYPE eventType : eventTypes) {
          addInsideLock(m_listenerMap, listener, weak, eventType);
        }
      }
    }
  }

  /**
   * Add the listener to the list of <em>last</em> called listeners.
   *
   * @param listener
   * @param weak
   * @param eventTypes
   *     is either a single event type or an array of types, may also be a primitive array such as int[]
   */
  public void addLastCalled(LISTENER listener, boolean weak, @SuppressWarnings("unchecked") EVENT_TYPE... eventTypes) {
    if (listener == null) {
      return;
    }
    synchronized (lockObject()) {
      if (eventTypes == null || eventTypes.length == 0) {
        for (EVENT_TYPE eventType : knownEventTypes()) {
          addInsideLock(m_lastListenerMap, listener, weak, eventType);
        }
        addInsideLock(m_lastListenerMap, listener, weak, otherEventsType());
      }
      else {
        for (EVENT_TYPE eventType : eventTypes) {
          addInsideLock(m_lastListenerMap, listener, weak, eventType);
        }
      }
    }
  }

  public <T extends AbstractGroupedListenerList<LISTENER, EVENT, EVENT_TYPE>> void addAll(T other) {
    synchronized (lockObject()) {
      synchronized (other.lockObject()) {
        addAllInsideLock(other.internalListenerMap(false), this.internalListenerMap(false));
        addAllInsideLock(other.internalListenerMap(true), this.internalListenerMap(true));
      }
    }
  }

  /**
   * Removes the first listener which is equal to the listener parameter from this listener list.
   */
  public void remove(LISTENER listener, @SuppressWarnings("unchecked") EVENT_TYPE... eventTypes) {
    if (listener == null) {
      return;
    }
    synchronized (lockObject()) {
      if (eventTypes == null || eventTypes.length == 0) {
        removeInsideLock(m_listenerMap, listener, null);
        removeInsideLock(m_lastListenerMap, listener, null);
      }
      else {
        for (EVENT_TYPE eventType : eventTypes) {
          removeInsideLock(m_listenerMap, listener, eventType);
          removeInsideLock(m_lastListenerMap, listener, eventType);
        }
      }
    }
  }

  private void addAllInsideLock(Map<EVENT_TYPE, UnsafeFastListenerList<LISTENER>> srcMap, Map<EVENT_TYPE, UnsafeFastListenerList<LISTENER>> dstMap) {
    srcMap.forEach((eventType, srcList) -> dstMap
        .computeIfAbsent(eventType, eventType2 -> new UnsafeFastListenerList<>())
        .addAll(srcList));
  }

  private void addInsideLock(Map<EVENT_TYPE, UnsafeFastListenerList<LISTENER>> listenerMap, LISTENER listener, boolean weak, EVENT_TYPE eventType) {
    if (listener == null) {
      return;
    }
    UnsafeFastListenerList<LISTENER> listeners = listenerMap.get(eventType);
    if (listeners == null) {
      listeners = new UnsafeFastListenerList<>();
      listenerMap.put(eventType, listeners);
    }
    listeners.add(listener, weak);
  }

  private void removeInsideLock(Map<EVENT_TYPE, UnsafeFastListenerList<LISTENER>> listenerMap, LISTENER listener, EVENT_TYPE queryType) {
    if (listener == null) {
      return;
    }
    for (Iterator<Map.Entry<EVENT_TYPE, UnsafeFastListenerList<LISTENER>>> it = listenerMap.entrySet().iterator(); it.hasNext(); ) {
      Map.Entry<EVENT_TYPE, UnsafeFastListenerList<LISTENER>> e = it.next();
      EVENT_TYPE eventType = e.getKey();
      if (queryType != null && !Objects.equals(queryType, eventType)) {
        continue;
      }
      UnsafeFastListenerList<LISTENER> listeners = e.getValue();
      if (listeners == null) {
        continue;
      }
      listeners.remove(listener);
      if (listeners.isEmpty()) {
        it.remove();
      }
    }
  }

  private List<LISTENER> collectListenersInsideLock(EVENT_TYPE eventType) {
    ArrayList<LISTENER> result = new ArrayList<>();
    boolean knownEventType = knownEventTypes().contains(eventType);
    //lists are in reverse order
    collectListenersInsideLock(m_listenerMap, eventType, result);
    if (!knownEventType) {
      collectListenersInsideLock(m_listenerMap, otherEventsType(), result);
    }
    collectListenersInsideLock(m_lastListenerMap, eventType, result);
    if (!knownEventType) {
      collectListenersInsideLock(m_lastListenerMap, otherEventsType(), result);
    }
    return result;
  }

  private void collectListenersInsideLock(Map<EVENT_TYPE, UnsafeFastListenerList<LISTENER>> listenerMap, EVENT_TYPE eventType, ArrayList<LISTENER> result) {
    UnsafeFastListenerList<LISTENER> listeners = listenerMap.get(eventType);
    if (listeners == null) {
      return;
    }
    if (listeners.isEmpty()) {
      listenerMap.remove(eventType);
      return;
    }
    result.addAll(listeners.list());
  }

  @Override
  public void createSnapshot(ISnapshotCollector snapshot) {
    synchronized (lockObject()) {
      for (Map.Entry<EVENT_TYPE, UnsafeFastListenerList<LISTENER>> e : m_listenerMap.entrySet()) {
        EVENT_TYPE eventType = e.getKey();
        UnsafeFastListenerList<LISTENER> listeners = e.getValue();
        String context = "eventType: " + (eventType == otherEventsType() ? "unknown/other" : eventType);
        for (LISTENER listener : listeners.list()) {
          snapshot.add(context, listener);
        }
      }
      for (Map.Entry<EVENT_TYPE, UnsafeFastListenerList<LISTENER>> e : m_lastListenerMap.entrySet()) {
        EVENT_TYPE eventType = e.getKey();
        UnsafeFastListenerList<LISTENER> listeners = e.getValue();
        String context = "lastListeners of eventType: " + (eventType == otherEventsType() ? "unknown/other" : eventType);
        for (LISTENER listener : listeners.list()) {
          snapshot.add(context, listener);
        }
      }
    }
  }

  @Override
  public String toString() {
    LinkedHashSet<String> namesInReverseOrder = new LinkedHashSet<>();
    synchronized (lockObject()) {
      for (UnsafeFastListenerList<LISTENER> listeners : m_lastListenerMap.values()) {
        for (LISTENER listener : listeners.list()) {
          namesInReverseOrder.add(listener.toString());
        }
      }
      for (UnsafeFastListenerList<LISTENER> listeners : m_listenerMap.values()) {
        for (LISTENER listener : listeners.list()) {
          namesInReverseOrder.add(listener.toString());
        }
      }
    }
    String[] rev = namesInReverseOrder.toArray(new String[0]);
    StringBuilder sb = new StringBuilder("listeners[" + rev.length + "]");
    if (rev.length > 0) {
      sb.append(": ");
      for (int i = rev.length - 1; i >= 0; i--) {
        sb.append(rev[i]);
        if (i > 0) {
          sb.append(", ");
        }
      }
    }
    return sb.toString();
  }
}
