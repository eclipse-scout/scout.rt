/*******************************************************************************
 * Copyright (c) 2010-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.platform.util.event;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.eclipse.scout.rt.platform.eventlistprofiler.EventListenerProfiler;
import org.eclipse.scout.rt.platform.eventlistprofiler.IEventListenerSnapshot;
import org.eclipse.scout.rt.platform.eventlistprofiler.IEventListenerSource;

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
public abstract class AbstractGroupedListenerList<LISTENER, EVENT, EVENT_TYPE> implements IEventListenerSource {
  private final Map<EVENT_TYPE, UnsafeFastListenerList<LISTENER>> m_listenerMap = new HashMap<>();
  private final Map<EVENT_TYPE, UnsafeFastListenerList<LISTENER>> m_lastListenerMap = new HashMap<>();

  public AbstractGroupedListenerList() {
    if (EventListenerProfiler.getInstance().isEnabled()) {
      EventListenerProfiler.getInstance().registerSourceAsWeakReference(this);
    }
  }

  protected Object lockObject() {
    return m_listenerMap;
  }

  protected abstract EVENT_TYPE allEventsType();

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
   *          is either a single event type or an array of types, may also be a primitive array such as int[]
   */
  public void add(LISTENER listener, boolean weak, @SuppressWarnings("unchecked") EVENT_TYPE... eventTypes) {
    if (listener == null) {
      return;
    }
    synchronized (lockObject()) {
      if (eventTypes == null || eventTypes.length == 0) {
        addInsideLock(m_listenerMap, listener, weak, allEventsType());
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
   *          is either a single event type or an array of types, may also be a primitive array such as int[]
   */
  public void addLastCalled(LISTENER listener, boolean weak, @SuppressWarnings("unchecked") EVENT_TYPE... eventTypes) {
    if (listener == null) {
      return;
    }
    synchronized (lockObject()) {
      if (eventTypes == null || eventTypes.length == 0) {
        addInsideLock(m_lastListenerMap, listener, weak, allEventsType());
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
        removeInsideLock(m_listenerMap, listener, allEventsType());
        removeInsideLock(m_lastListenerMap, listener, allEventsType());
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
    for (EVENT_TYPE eventType : new HashSet<>(listenerMap.keySet())) {
      if (queryType != allEventsType() && !Objects.equals(queryType, eventType)) {
        continue;
      }
      UnsafeFastListenerList<LISTENER> listeners = listenerMap.get(eventType);
      if (listeners == null) {
        continue;
      }
      listeners.remove(listener);
      if (listeners.isEmpty()) {
        listenerMap.remove(eventType);
      }
    }
  }

  private List<LISTENER> collectListenersInsideLock(EVENT_TYPE type) {
    ArrayList<LISTENER> result = new ArrayList<>();
    //lists are in reverse order
    collectListenersInsideLock(m_listenerMap, type, result);
    collectListenersInsideLock(m_listenerMap, allEventsType(), result);
    collectListenersInsideLock(m_lastListenerMap, type, result);
    collectListenersInsideLock(m_lastListenerMap, allEventsType(), result);
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
  public void dumpListenerList(IEventListenerSnapshot snapshot) {
    synchronized (lockObject()) {
      for (Map.Entry<EVENT_TYPE, UnsafeFastListenerList<LISTENER>> e : m_listenerMap.entrySet()) {
        EVENT_TYPE eventType = e.getKey();
        UnsafeFastListenerList<LISTENER> listeners = e.getValue();
        String context = eventType == allEventsType() ? null : "eventType: " + eventType;
        for (LISTENER listener : listeners.list()) {
          snapshot.add(listener.getClass(), context, listener);
        }
      }
      for (Map.Entry<EVENT_TYPE, UnsafeFastListenerList<LISTENER>> e : m_lastListenerMap.entrySet()) {
        EVENT_TYPE eventType = e.getKey();
        UnsafeFastListenerList<LISTENER> listeners = e.getValue();
        String context = eventType == allEventsType() ? "lastListeners" : "lastListeners of eventType: " + eventType;
        for (LISTENER listener : listeners.list()) {
          snapshot.add(listener.getClass(), context, listener);
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
