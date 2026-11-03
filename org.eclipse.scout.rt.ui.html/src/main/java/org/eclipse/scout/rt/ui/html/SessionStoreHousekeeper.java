/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.ui.html;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.config.CONFIG;
import org.eclipse.scout.rt.platform.exception.ExceptionHandler;
import org.eclipse.scout.rt.platform.job.FixedDelayScheduleBuilder;
import org.eclipse.scout.rt.platform.job.IFuture;
import org.eclipse.scout.rt.platform.job.JobInput;
import org.eclipse.scout.rt.platform.job.Jobs;
import org.eclipse.scout.rt.ui.html.UiHtmlConfigProperties.OrphanedUiSessionTimeoutProperty;
import org.eclipse.scout.rt.ui.html.UiHtmlConfigProperties.SessionStoreHousekeepingIntervalProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Periodically disposes orphaned UI sessions of all registered session stores. These are UI sessions that are still
 * registered, but no longer used by a browser tab (e.g. because the tab was closed during startup and therefore could
 * not send an unload request), while the HTTP session is kept alive by other tabs.
 * <p>
 * Can be disabled by setting {@link SessionStoreHousekeepingIntervalProperty} to 0.
 */
@ApplicationScoped
public class SessionStoreHousekeeper {

  private static final Logger LOG = LoggerFactory.getLogger(SessionStoreHousekeeper.class);

  protected final Set<ISessionStore> m_stores = ConcurrentHashMap.newKeySet();
  protected final Object m_jobLock = new Object();
  protected IFuture<?> m_job;

  public void register(ISessionStore store) {
    if (!isEnabled()) {
      return;
    }
    m_stores.add(store);
    ensureJobRunning();
  }

  public void unregister(ISessionStore store) {
    m_stores.remove(store);
  }

  protected void ensureJobRunning() {
    synchronized (m_jobLock) {
      if (m_job != null && !m_job.isDone()) {
        return;
      }
      long interval = getInterval();
      if (interval <= 0) {
        return;
      }
      m_job = Jobs.schedule(this::sweep, Jobs.newInput()
          .withName("UI session housekeeping")
          .withExecutionHint(JobInput.EXECUTION_HINT_TESTING_DO_NOT_WAIT_FOR_THIS_JOB)
          .withExceptionHandling(BEANS.get(ExceptionHandler.class), true)
          .withExecutionTrigger(Jobs.newExecutionTrigger()
              .withStartIn(interval, TimeUnit.SECONDS)
              .withSchedule(FixedDelayScheduleBuilder.repeatForever(interval, TimeUnit.SECONDS))));
    }
  }

  protected boolean isEnabled() {
    return getInterval() > 0;
  }

  protected long getInterval() {
    Integer interval = CONFIG.getPropertyValue(SessionStoreHousekeepingIntervalProperty.class);
    return interval == null ? 0 : interval;
  }

  protected void sweep() {
    long maxSilentMillis = TimeUnit.SECONDS.toMillis(CONFIG.getPropertyValue(OrphanedUiSessionTimeoutProperty.class));
    for (ISessionStore store : m_stores) {
      try {
        if (!isStoreAlive(store)) {
          LOG.info("Session housekeeping: Removing session store of invalid HTTP session {}", store.getHttpSessionId());
          m_stores.remove(store);
          continue;
        }
        store.disposeOrphanedUiSessions(maxSilentMillis);
      }
      catch (RuntimeException e) {
        LOG.warn("Session housekeeping failed for HTTP session {}", store.getHttpSessionId(), e);
      }
    }
  }

  protected boolean isStoreAlive(ISessionStore store) {
    if (!store.isHttpSessionValid()) {
      return false;
    }
    try {
      store.getHttpSession().getCreationTime(); // throws IllegalStateException if the session is invalidated
      return true;
    }
    catch (IllegalStateException e) {
      return false;
    }
  }
}
