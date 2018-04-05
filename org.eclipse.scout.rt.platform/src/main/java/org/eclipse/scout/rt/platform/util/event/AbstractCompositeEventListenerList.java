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

import org.eclipse.scout.rt.platform.eventlistprofiler.EventListenerProfiler;
import org.eclipse.scout.rt.platform.eventlistprofiler.IEventListenerSnapshot;
import org.eclipse.scout.rt.platform.eventlistprofiler.IEventListenerSource;

/**
 * Thread safe event listener list with a single listener type and events with int type
 * <ul>
 * <li>support for @see WeakEventListener</li>
 * <li>register listeners for only a subset of int event types</li>
 * <li>register specific listeners at the end of the calling-chain, typically ui listeners</li>
 * </ul>
 *
 * @since 7.1
 */
public abstract class AbstractCompositeEventListenerList<LISTENER, EVENT> implements IEventListenerSource {
  private final Map<Integer, UnsafeSimpleEventListenerList<LISTENER>> m_listenerMap = new HashMap<>();
  private final Map<Integer, UnsafeSimpleEventListenerList<LISTENER>> m_lastListenerMap = new HashMap<>();

  public AbstractCompositeEventListenerList() {
    if (EventListenerProfiler.getInstance().isEnabled()) {
      EventListenerProfiler.getInstance().registerSourceAsWeakReference(this);
    }
  }

  protected Object lockObject() {
    return m_listenerMap;
  }

  protected int allEventsType() {
    return -1;
  }

  protected abstract int eventType(EVENT event);

  protected abstract void handleEvent(LISTENER listener, EVENT event);

  /**
   * used for unit testing only
   */
  UnsafeSimpleEventListenerList internalListenerList(int eventType, boolean lastCalled) {
    return (lastCalled ? m_lastListenerMap : m_listenerMap).get(eventType);
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

  public List<LISTENER> list(int eventType) {
    synchronized (lockObject()) {
      return collectListenersInsideLock(eventType);
    }
  }

  /**
   * The last listener added is the first to be called
   */
  public void add(LISTENER listener, boolean weak, int... eventTypes) {
    if (listener == null) {
      return;
    }
    synchronized (lockObject()) {
      if (eventTypes == null || eventTypes.length == 0) {
        addInsideLock(m_listenerMap, listener, weak, allEventsType());
      }
      else {
        for (int eventType : eventTypes) {
          addInsideLock(m_listenerMap, listener, weak, eventType);
        }
      }
    }
  }

  /**
   * Add the listener to the list of <em>last</em> called listeners.
   */
  public void addLastCalled(LISTENER listener, boolean weak, int... eventTypes) {
    if (listener == null) {
      return;
    }
    synchronized (lockObject()) {
      if (eventTypes == null || eventTypes.length == 0) {
        addInsideLock(m_lastListenerMap, listener, weak, allEventsType());
      }
      else {
        for (int eventType : eventTypes) {
          addInsideLock(m_lastListenerMap, listener, weak, eventType);
        }
      }
    }
  }

  /**
   * Removes the first listener which is equal to the listener parameter from this listener list.
   */
  public void remove(LISTENER listener, int... eventTypes) {
    if (listener == null) {
      return;
    }
    synchronized (lockObject()) {
      if (eventTypes == null || eventTypes.length == 0) {
        removeInsideLock(m_listenerMap, listener, allEventsType());
        removeInsideLock(m_lastListenerMap, listener, allEventsType());
      }
      else {
        for (int eventType : eventTypes) {
          removeInsideLock(m_listenerMap, listener, eventType);
          removeInsideLock(m_lastListenerMap, listener, eventType);
        }
      }
    }
  }

  private void addInsideLock(Map<Integer, UnsafeSimpleEventListenerList<LISTENER>> listenerMap, LISTENER listener, boolean weak, int eventType) {
    if (listener == null) {
      return;
    }
    UnsafeSimpleEventListenerList<LISTENER> listeners = listenerMap.get(eventType);
    if (listeners == null) {
      listeners = new SimpleEventListenerList<>();
      listenerMap.put(eventType, listeners);
    }
    listeners.add(listener, weak);
  }

  private void removeInsideLock(Map<Integer, UnsafeSimpleEventListenerList<LISTENER>> listenerMap, LISTENER listener, int queryType) {
    if (listener == null) {
      return;
    }
    for (int eventType : new HashSet<>(listenerMap.keySet())) {
      if (queryType != allEventsType() && queryType != eventType) {
        continue;
      }
      UnsafeSimpleEventListenerList<LISTENER> listeners = listenerMap.get(eventType);
      if (listeners == null) {
        continue;
      }
      listeners.remove(listener);
      if (listeners.isEmpty()) {
        listenerMap.remove(eventType);
      }
    }
  }

  private List<LISTENER> collectListenersInsideLock(int type) {
    ArrayList<LISTENER> result = new ArrayList<>();
    //lists are in reverse order
    collectListenersInsideLock(m_listenerMap, type, result);
    collectListenersInsideLock(m_listenerMap, allEventsType(), result);
    collectListenersInsideLock(m_lastListenerMap, type, result);
    collectListenersInsideLock(m_lastListenerMap, allEventsType(), result);
    return result;
  }

  private void collectListenersInsideLock(Map<Integer, UnsafeSimpleEventListenerList<LISTENER>> listenerMap, int eventType, ArrayList<LISTENER> result) {
    UnsafeSimpleEventListenerList<LISTENER> listeners = listenerMap.get(eventType);
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
      for (Map.Entry<Integer, UnsafeSimpleEventListenerList<LISTENER>> e : m_listenerMap.entrySet()) {
        int eventType = e.getKey();
        UnsafeSimpleEventListenerList<LISTENER> listeners = e.getValue();
        String context = eventType == allEventsType() ? null : "eventType: " + eventType;
        for (LISTENER listener : listeners.list()) {
          snapshot.add(listener.getClass(), context, listener);
        }
      }
      for (Map.Entry<Integer, UnsafeSimpleEventListenerList<LISTENER>> e : m_lastListenerMap.entrySet()) {
        int eventType = e.getKey();
        UnsafeSimpleEventListenerList<LISTENER> listeners = e.getValue();
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
      for (UnsafeSimpleEventListenerList<LISTENER> listeners : m_lastListenerMap.values()) {
        for (LISTENER listener : listeners.list()) {
          namesInReverseOrder.add(listener.toString());
        }
      }
      for (UnsafeSimpleEventListenerList<LISTENER> listeners : m_listenerMap.values()) {
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
