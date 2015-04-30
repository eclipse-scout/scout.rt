/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.server.context;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.context.IRunMonitor;
import org.eclipse.scout.rt.server.IServerSession;
import org.eclipse.scout.rt.server.session.ServerSessionProvider;

/**
 * Cache the {@link IRunMonitor} per session in order to allow cancelling
 */
@ApplicationScoped
public class RunMonitorCancelRegistry {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(RunMonitorCancelRegistry.class);
  protected static final String SESSION_STATE_KEY = "activeTransactions";

  public RunMonitorCancelRegistry() {
  }

  public void register(long id, IRunMonitor monitor) {
    if (id == 0L) {
      return;
    }
    SessionState state = getSessionState(true);
    if (state == null) {
      LOG.error("failed to register transaction due to missing session");
      return;
    }
    synchronized (state.m_txMapLock) {
      state.m_txMap.put(id, new WeakReference<IRunMonitor>(monitor));
    }
  }

  public void unregister(long id) {
    if (id == 0L) {
      return;
    }
    SessionState state = getSessionState(false);
    if (state == null) {
      return;
    }
    synchronized (state.m_txMapLock) {
      state.m_txMap.remove(id);
    }
  }

  /**
   * @return true if cancel was successful and transaction was in fact cancelled, false otherwise
   */
  public boolean cancel(long id) {
    if (id == 0L) {
      return false;
    }
    SessionState state = getSessionState(false);
    if (state == null) {
      return false;
    }
    IRunMonitor monitor;
    synchronized (state.m_txMapLock) {
      WeakReference<IRunMonitor> ref = state.m_txMap.get(id);
      if (ref == null) {
        return false;
      }
      monitor = ref.get();
      if (monitor == null) {
        return false;
      }
    }
    return monitor.cancel(true);
  }

  protected SessionState getSessionState(boolean autoCreate) {
    IServerSession session = ServerSessionProvider.currentSession();
    if (session == null) {
      return null;
    }
    synchronized (session) {
      SessionState state = (SessionState) session.getData(SESSION_STATE_KEY);
      if (state == null && autoCreate) {
        state = new SessionState();
        session.setData(SESSION_STATE_KEY, state);
      }
      return state;
    }
  }

  protected static class SessionState {
    final Object m_txMapLock = new Object();
    final Map<Long, WeakReference<IRunMonitor>> m_txMap = new HashMap<>();
  }
}
