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

import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Keeps track of all {@link UiSession}s with a poll request currently waiting in
 * {@link UiSession#waitForBackgroundJobs(org.eclipse.scout.rt.ui.html.json.JsonRequest, int)}.
 * <p>
 * Calling {@link #shutdown()} releases all waiting pollers immediately and prevents new pollers from blocking. This
 * should be called when the servlet container initiates a graceful shutdown, because otherwise the blocked poll
 * requests delay the shutdown until the polling interval has elapsed (or the container's stop timeout is reached).
 */
@ApplicationScoped
public class UiPollerRegistry {

  private static final Logger LOG = LoggerFactory.getLogger(UiPollerRegistry.class);

  protected final Set<UiSession> m_waitingSessions = ConcurrentHashMap.newKeySet();
  private volatile boolean m_shutdown;

  protected void register(UiSession uiSession) {
    m_waitingSessions.add(uiSession);
  }

  protected void unregister(UiSession uiSession) {
    m_waitingSessions.remove(uiSession);
  }

  /**
   * @return <code>true</code> if {@link #shutdown()} was called. Pollers must not block anymore in this case.
   */
  public boolean isShutdown() {
    return m_shutdown;
  }

  /**
   * Releases all currently waiting pollers and ensures that subsequent poll requests return immediately. This method
   * does not block.
   */
  public void shutdown() {
    m_shutdown = true;
    LOG.info("Releasing {} waiting poll request(s) due to shutdown", m_waitingSessions.size());
    for (UiSession uiSession : m_waitingSessions) {
      uiSession.signalPoller();
    }
  }
}
