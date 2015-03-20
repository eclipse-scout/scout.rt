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
package org.eclipse.scout.rt.platform.job.internal;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;

import org.eclipse.scout.commons.Assertions;
import org.eclipse.scout.commons.filter.Filters;
import org.eclipse.scout.commons.filter.IFilter;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.platform.job.listener.IJobListener;
import org.eclipse.scout.rt.platform.job.listener.JobEvent;

/**
 * Responsible for notifying all job listeners about job lifecycle events.
 *
 * @since 5.1
 */
class JobListeners {

  private static final IScoutLogger LOG = ScoutLogManager.getLogger(JobListeners.class);

  private final Map<IJobListener, IFilter<JobEvent>> m_listenerMap;

  private final ReadLock m_readLock;
  private final WriteLock m_writeLock;

  JobListeners() {
    m_listenerMap = new HashMap<>();

    final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    m_readLock = lock.readLock();
    m_writeLock = lock.writeLock();
  }

  /**
   * Registers the given listener to be notified about job lifecycle events. If the listener is already registered, that
   * previous registration is replaced.
   *
   * @param listener
   *          listener to be registered.
   * @param filter
   *          filter to only get notified about events of interest - that is for events accepted by the filter.
   * @return the given listener.
   */
  IJobListener add(final IJobListener listener, final IFilter<JobEvent> filter) {
    Assertions.assertNotNull(listener, "Listener must not be null");
    m_writeLock.lock();
    try {
      m_listenerMap.put(listener, Filters.alwaysFilterIfNull(filter));
      return listener;
    }
    finally {
      m_writeLock.unlock();
    }
  }

  /**
   * Removes the given listener from the list.
   *
   * @param listener
   *          listener to be removed.
   */
  void remove(final IJobListener listener) {
    if (listener == null) {
      return;
    }
    m_writeLock.lock();
    try {
      m_listenerMap.remove(listener);
    }
    finally {
      m_writeLock.unlock();
    }
  }

  /**
   * Notifies all listener about an event, unless not accept by the filter.
   *
   * @param eventToFire
   *          The event to fire.
   */
  void fireEvent(final JobEvent eventToFire) {
    final Map<IJobListener, IFilter<JobEvent>> listeners;
    m_readLock.lock();
    try {
      listeners = new HashMap<>(m_listenerMap);
    }
    finally {
      m_readLock.unlock();
    }

    for (final IJobListener listener : listeners.keySet()) {
      if (listeners.get(listener).accept(eventToFire)) {
        try {
          listener.changed(eventToFire);
        }
        catch (final RuntimeException e) {
          LOG.error(String.format("Failed to notify listener about lifecycle event [listener=%s, event=%s]", listener.getClass().getName(), eventToFire), e);
        }
      }
    }
  }
}
