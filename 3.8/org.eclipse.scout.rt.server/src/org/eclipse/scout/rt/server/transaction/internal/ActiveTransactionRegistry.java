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
package org.eclipse.scout.rt.server.transaction.internal;

import java.lang.ref.WeakReference;
import java.util.HashMap;

import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.server.IServerSession;
import org.eclipse.scout.rt.server.ServerJob;
import org.eclipse.scout.rt.server.transaction.ITransaction;

/**
 * Cache the transactions per session to enable for cancelling
 */
public class ActiveTransactionRegistry {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(ActiveTransactionRegistry.class);
  private static final String SESSION_STATE_KEY = "activeTransactions";

  private ActiveTransactionRegistry() {
  }

  private static SessionState getSessionState(boolean autoCreate) {
    IServerSession session = ServerJob.getCurrentSession();
    if (session == null) {
      return null;
    }
    synchronized (session) {
      SessionState state = (SessionState) session.getAttribute(SESSION_STATE_KEY);
      if (state == null && autoCreate) {
        state = new SessionState();
        session.setAttribute(SESSION_STATE_KEY, state);
      }
      return state;
    }
  }

  public static void register(ITransaction tx) {
    if (tx == null || tx.getTransactionSequence() == 0L) {
      return;
    }
    SessionState state = getSessionState(true);
    if (state == null) {
      LOG.error("failed to register transaction due to missing session");
      return;
    }
    synchronized (state.m_txMapLock) {
      state.m_txMap.put(tx.getTransactionSequence(), new WeakReference<ITransaction>(tx));
    }
  }

  public static void unregister(ITransaction tx) {
    if (tx == null || tx.getTransactionSequence() == 0L) {
      return;
    }
    SessionState state = getSessionState(false);
    if (state == null) {
      return;
    }
    synchronized (state.m_txMapLock) {
      state.m_txMap.remove(tx.getTransactionSequence());
    }
  }

  /**
   * @return true if cancel was successful and transaction was in fact cancelled, false otherwise
   */
  public static boolean cancel(long transactionSequence) {
    if (transactionSequence == 0L) {
      return false;
    }
    SessionState state = getSessionState(false);
    if (state == null) {
      return false;
    }
    ITransaction tx;
    synchronized (state.m_txMapLock) {
      WeakReference<ITransaction> ref = state.m_txMap.get(transactionSequence);
      if (ref == null) {
        return false;
      }
      tx = ref.get();
      if (tx == null) {
        return false;
      }
    }
    return tx.cancel();
  }

  private static class SessionState {
    final Object m_txMapLock = new Object();
    final HashMap<Long, WeakReference<ITransaction>> m_txMap = new HashMap<Long, WeakReference<ITransaction>>();
  }
}
