/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.eclipse.scout.rt.platform.Bean;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.platform.util.ObjectUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A buffer for events ({@link IModelEvent}) with coalesce functionality:
 * <ul>
 * <li>Unnecessary events are removed.
 * <li>Events are merged, if possible.
 * </ul>
 * Not thread safe, to be accessed in client model job.
 *
 * @param T
 *          event type
 */
@Bean
public abstract class AbstractEventBuffer<T extends IModelEvent> {
  private static final Logger LOG = LoggerFactory.getLogger(AbstractEventBuffer.class);

  private List<T> m_buffer = new LinkedList<>();
  private T m_lastAddedEvent = null;

  public List<T> getBufferInternal() {
    return m_buffer;
  }

  protected T getLastAddedEventInternal() {
    return m_lastAddedEvent;
  }

  protected void setLastAddedEventInternal(T lastAddedEvent) {
    m_lastAddedEvent = lastAddedEvent;
  }

  /**
   * @return <code>true</code>, if empty, <code>false</code> otherwise.
   */
  public boolean isEmpty() {
    return m_buffer.isEmpty();
  }

  /**
   * The current number of events in the buffer (pre-coalesced).
   */
  public int size() {
    return m_buffer.size();
  }

  /**
   * Add a new event to the buffer
   */
  public void add(T event) {
    LOG.debug("Adding '{}'", event);
    // Optimization: If the new event is completely identical to the last
    // inserted one, don't add it. This helps preventing large lists
    // of buffered events which are expensive to coalesce.
    if (isIdenticalEvent(m_lastAddedEvent, event)) {
      return;
    }
    m_buffer.add(event);
    m_lastAddedEvent = event;
  }

  /**
   * Remove all current events from the buffer.
   *
   * @return the coalesced list of events.
   */
  public List<T> consumeAndCoalesceEvents() {
    List<T> result = coalesce(consume());
    LOG.debug("Consumed events from buffer '{}'", result);
    return result;
  }

  /**
   * Returns current events and empties the buffer.
   */
  protected List<T> consume() {
    List<T> list = m_buffer;
    m_buffer = new LinkedList<>();
    m_lastAddedEvent = null;
    return list;
  }

  protected abstract List<T> coalesce(List<T> events);

  /**
   * Removes all events of the same type from the list.
   *
   * @param type
   *          Event type to remove from the list.
   * @param events
   *          List to filter. Must not be <code>null</code>.
   */
  protected void remove(int type, List<T> events) {
    remove(Collections.singleton(type), events);
  }

  /**
   * Removes all events of the same type from the list.
   *
   * @param types
   *          List of event types to remove from the list. Must not be <code>null</code>.
   * @param events
   *          List to filter. Must not be <code>null</code>.
   */
  protected void remove(Set<Integer> types, List<T> events) {
    events.removeIf(event -> types.contains(event.getType()));
  }

  protected boolean isIdenticalEvent(T event1, T event2) {
    return ObjectUtility.equals(event1, event2);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder("EventBuffer");
    if (isEmpty()) {
      sb.append(" is empty");
    }
    else {
      sb.append(" contains ").append(size()).append(" events:");
      sb.append("\n- ");
      sb.append(CollectionUtility.format(m_buffer, "\n- "));
    }
    return sb.toString();
  }
}
