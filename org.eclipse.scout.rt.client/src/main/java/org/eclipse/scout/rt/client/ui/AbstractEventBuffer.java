/*******************************************************************************
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;

/**
 * A buffer for events ({@link IModelEvent}) with coalesce functionality:
 * <p>
 * <ul>
 * <li>Unnecessary events are removed.
 * <li>Events are merged, if possible.
 * </ul>
 * </p>
 * Not thread safe, to be accessed in client model job.
 *
 * @param T
 *          event type
 */
public abstract class AbstractEventBuffer<T extends IModelEvent> {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(AbstractEventBuffer.class);

  private List<T> m_buffer = new LinkedList<>();

  protected List<T> getBuffer() {
    return m_buffer;
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
  public void add(T e) {
    if (LOG.isDebugEnabled()) {
      LOG.debug(String.format("Adding '%1$s'", e));
    }
    m_buffer.add(e);
  }

  /**
   * Remove all current events from the buffer.
   *
   * @return the coalesced list of events.
   */
  public List<T> removeEvents() {
    List<T> res = new ArrayList<T>(coalesce(consume()));
    if (LOG.isDebugEnabled()) {
      LOG.debug(String.format("Removing Events from buffer '%1$s'", res));
    }
    return res;
  }

  /**
   * Returns current events and empties the buffer.
   */
  protected List<T> consume() {
    List<T> list = m_buffer;
    m_buffer = new LinkedList<T>();
    return list;
  }

  protected abstract List<? extends T> coalesce(List<T> events);

  /**
   * Removes all events of the same type from the list.
   *
   * @param type
   *          Event type to remove from the list.
   * @param events
   *          List to filter. Must not be <code>null</code>.
   */
  protected void remove(int type, List<T> events) {
    remove(Collections.singletonList(type), events);
  }

  /**
   * Removes all events of the same type from the list.
   *
   * @param types
   *          List of event types to remove from the list. Must not be <code>null</code>.
   * @param events
   *          List to filter. Must not be <code>null</code>.
   */
  protected void remove(List<Integer> types, List<T> events) {
    for (Iterator<T> it = events.iterator(); it.hasNext();) {
      T event = it.next();
      if (types.contains(event.getType())) {
        it.remove();
      }
    }
  }
}
