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
package org.eclipse.scout.commons.job.internal;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.scout.commons.CollectionUtility;
import org.eclipse.scout.commons.job.IJobChangeEvent;
import org.eclipse.scout.commons.job.IJobChangeEventFilter;
import org.eclipse.scout.commons.job.IJobChangeListener;
import org.eclipse.scout.commons.job.IJobChangeListeners;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;

/**
 *
 */
public final class JobChangeListeners implements IJobChangeListeners {

  private static final IScoutLogger LOG = ScoutLogManager.getLogger(JobChangeListeners.class);

  private final transient List<P_ListenerItem> m_listeners = new ArrayList<>();

  @Override
  public void add(IJobChangeListener listener) {
    add(listener, null);
  }

  @Override
  public void add(IJobChangeListener listener, IJobChangeEventFilter eventFilter) {
    if (listener == null) {
      return;
    }
    P_ListenerItem item = new P_ListenerItem(listener, eventFilter);
    synchronized (this) {
      m_listeners.add(item);
    }
  }

  @Override
  public void remove(IJobChangeListener listener) {
    remove(listener, null);
  }

  @Override
  public void remove(IJobChangeListener listener, IJobChangeEventFilter eventFilter) {
    if (listener == null) {
      return;
    }
    synchronized (this) {
      Iterator<P_ListenerItem> iterator = m_listeners.iterator();
      while (iterator.hasNext()) {
        P_ListenerItem item = iterator.next();
        if (item.getListener() == listener && item.getEventFilter() == eventFilter) {
          iterator.remove();
        }
      }
    }
  }

  @Override
  public void fireEvent(IJobChangeEvent eventToFire) {
    List<P_ListenerItem> listeners = null;
    synchronized (this) {
      listeners = CollectionUtility.arrayList(m_listeners);
    }

    for (P_ListenerItem item : listeners) {
      IJobChangeEventFilter filter = item.getEventFilter();
      if (filter == null || filter.accept(eventToFire)) {
        try {
          item.getListener().jobChanged(eventToFire);
        }
        catch (RuntimeException re) {
          LOG.error("Error invoking job change listener '" + item.getListener().getClass().getName() + "'.", re);
        }
      }
    }
  }

  private static final class P_ListenerItem {
    private final IJobChangeListener m_listener;
    private final IJobChangeEventFilter m_eventFilter;

    private P_ListenerItem(IJobChangeListener listener, IJobChangeEventFilter eventFilter) {
      m_listener = listener;
      m_eventFilter = eventFilter;
    }

    public IJobChangeListener getListener() {
      return m_listener;
    }

    public IJobChangeEventFilter getEventFilter() {
      return m_eventFilter;
    }
  }
}
