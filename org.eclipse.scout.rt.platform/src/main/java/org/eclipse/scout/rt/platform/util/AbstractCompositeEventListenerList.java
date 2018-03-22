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
package org.eclipse.scout.rt.platform.util;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import org.eclipse.scout.rt.platform.eventlistprofiler.EventListenerProfiler;
import org.eclipse.scout.rt.platform.eventlistprofiler.IEventListenerSnapshot;
import org.eclipse.scout.rt.platform.eventlistprofiler.IEventListenerSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
  private static final Logger LOG = LoggerFactory.getLogger(AbstractCompositeEventListenerList.class);

  private final Object m_lock = new Object();

  private final Map<Integer, SimpleEventListenerList<LISTENER>> m_listenerMap = new HashMap<>();
  private final Map<Integer, SimpleEventListenerList<LISTENER>> m_lastListenerMap = new HashMap<>();

  public AbstractCompositeEventListenerList() {
    if (EventListenerProfiler.getInstance().isEnabled()) {
      EventListenerProfiler.getInstance().registerSourceAsWeakReference(this);
    }
  }

  protected int allEventsType() {
    return -1;
  }

  protected abstract int eventType(EVENT event);

  protected abstract void handleEvent(LISTENER listener, EVENT event);

  /**
   * used for unit testing only
   */
  SimpleEventListenerList internalSize(int eventType, boolean lastCalled) {
    return (lastCalled ? m_lastListenerMap : m_listenerMap).get(eventType);
  }

  public void fireEvents(List<EVENT> events) {
    if (events == null || events.isEmpty()) {
      return;
    }
    synchronized (m_lock) {
      for (EVENT e : events) {
        fireEventInsideLock(e);
      }
    }
  }

  public void fireEvent(EVENT e) {
    synchronized (m_lock) {
      fireEventInsideLock(e);
    }
  }

  /**
   * The last listener added is the first to be called
   */
  public void add(LISTENER listener, boolean weak, int... eventTypes) {
    if (listener == null) {
      return;
    }
    synchronized (m_lock) {
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
    synchronized (m_lock) {
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
    synchronized (m_lock) {
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

  private void addInsideLock(Map<Integer, SimpleEventListenerList<LISTENER>> listenerMap, LISTENER listener, boolean weak, int eventType) {
    if (listener == null) {
      return;
    }
    SimpleEventListenerList<LISTENER> listeners = listenerMap.get(eventType);
    if (listeners == null) {
      listeners = new SimpleEventListenerList<>();
      listenerMap.put(eventType, listeners);
    }
    listeners.add(listener, weak);
  }

  private void removeInsideLock(Map<Integer, SimpleEventListenerList<LISTENER>> listenerMap, LISTENER listener, int queryType) {
    if (listener == null) {
      return;
    }
    for (int eventType : new HashSet<>(listenerMap.keySet())) {
      if (queryType != allEventsType() && queryType != eventType) {
        continue;
      }
      SimpleEventListenerList<LISTENER> listeners = listenerMap.get(eventType);
      if (listeners == null) {
        continue;
      }
      listeners.remove(listener);
      if (listeners.isEmpty()) {
        listenerMap.remove(eventType);
      }
    }
  }

  private void fireEventInsideLock(EVENT e) {
    //lists are in reverse order
    fireEventInsideLock(m_listenerMap, eventType(e), e);
    fireEventInsideLock(m_listenerMap, allEventsType(), e);
    fireEventInsideLock(m_lastListenerMap, eventType(e), e);
    fireEventInsideLock(m_lastListenerMap, allEventsType(), e);
  }

  private void fireEventInsideLock(Map<Integer, SimpleEventListenerList<LISTENER>> listenerMap, int eventType, EVENT event) {
    SimpleEventListenerList<LISTENER> listeners = listenerMap.get(eventType);
    if (listeners == null) {
      return;
    }
    if (listeners.isEmpty()) {
      listenerMap.remove(eventType);
      return;
    }
    //fire
    for (LISTENER listener : listeners) {
      try {
        handleEvent(listener, event);
      }
      catch (Exception e) {//NOSONAR
        LOG.error("calling '{}' with event '{}'", listener, event, e);
      }
    }
  }

  @Override
  public void dumpListenerList(IEventListenerSnapshot snapshot) {
    synchronized (m_lock) {
      for (Map.Entry<Integer, SimpleEventListenerList<LISTENER>> e : m_listenerMap.entrySet()) {
        int eventType = e.getKey();
        SimpleEventListenerList<LISTENER> listeners = e.getValue();
        String context = eventType == allEventsType() ? null : "eventType: " + eventType;
        for (LISTENER listener : listeners) {
          snapshot.add(listener.getClass(), context, listener);
        }
      }
      for (Map.Entry<Integer, SimpleEventListenerList<LISTENER>> e : m_lastListenerMap.entrySet()) {
        int eventType = e.getKey();
        SimpleEventListenerList<LISTENER> listeners = e.getValue();
        String context = eventType == allEventsType() ? "lastListeners" : "lastListeners of eventType: " + eventType;
        for (LISTENER listener : listeners) {
          snapshot.add(listener.getClass(), context, listener);
        }
      }
    }
  }

  @Override
  public String toString() {
    LinkedHashSet<String> namesInReverseOrder = new LinkedHashSet<>();
    synchronized (m_lock) {
      for (SimpleEventListenerList<LISTENER> listeners : m_lastListenerMap.values()) {
        for (LISTENER listener : listeners) {
          namesInReverseOrder.add(listener.toString());
        }
      }
      for (SimpleEventListenerList<LISTENER> listeners : m_listenerMap.values()) {
        for (LISTENER listener : listeners) {
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
