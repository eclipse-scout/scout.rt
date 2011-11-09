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
package org.eclipse.scout.rt.server.services.common.jdbc.internal.exec;

import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.server.IServerSession;
import org.eclipse.scout.rt.server.ThreadContext;

/**
 * <p>
 * Store that holds currently running statements partitioned by {@link IServerSession}
 * </p>
 * <p>
 * Thereby, the session acts as weak key, meaning that statements will automatically be removed when the session is not
 * in ordinary use anymore.
 * </p>
 */
public class RunningStatementStore {

  private static final IScoutLogger LOG = ScoutLogManager.getLogger(RunningStatementStore.class);

  private final static WeakHashMap<IServerSession, Map<Long, Statement>> STATEMENT_MAP = new WeakHashMap<IServerSession, Map<Long, Statement>>();
  private final static Object STATEMENT_MAP_LOCK = new Object();

  private RunningStatementStore() {
  }

  /**
   * <p>
   * Registers a statement in {@link RunningStatementStore} in order to be canceled by
   * {@link RunningStatementStore#cancelAll()}. The statement is registered on behalf of the thread's current session
   * {@link ThreadContext#getServerSession()}.
   * </p>
   * <p>
   * The session itself acts as weak key and the related statements are automatically be removed if the session is not
   * in ordinary use anymore.
   * </p>
   * 
   * @param statement
   *          the statement to be registered
   */
  public static void register(Statement statement) {
    IServerSession session = ThreadContext.getServerSession();
    if (session == null) {
      LOG.error("failed to register statement due to missing session context");
      return;
    }
    Long seq = RequestSequenceThreadLocal.get();
    if (seq == null) {
      return;
    }

    Map<Long, Statement> statementMap;
    synchronized (STATEMENT_MAP_LOCK) {
      statementMap = STATEMENT_MAP.get(session);
      if (statementMap == null) {
        statementMap = new HashMap<Long, Statement>();
        STATEMENT_MAP.put(session, statementMap);
      }
    }
    statementMap.put(seq, statement);
  }

  /**
   * <p>
   * Removes a statement of the {@link ThreadContext#getServerSession()} from within the {@link RunningStatementStore}.
   * If no such statement is registered or was already removed, this call has no effect.
   * </p>
   * 
   * @param statement
   *          the statement to be removed
   */
  public static void unregister(Statement statement) {
    IServerSession session = ThreadContext.getServerSession();
    if (session == null) {
      LOG.error("failed to unregister statement due to missing session context");
      return;
    }
    Long seq = RequestSequenceThreadLocal.get();
    if (seq == null) {
      return;
    }
    Map<Long, Statement> statementMap;
    synchronized (STATEMENT_MAP_LOCK) {
      statementMap = STATEMENT_MAP.get(session);
    }
    if (statementMap == null) {
      return;
    }
    statementMap.remove(seq);
  }

  /**
   * <p>
   * Cancels all running statements that belong to the current session {@link ThreadContext#getServerSession()}. This
   * method has no effect if no statements are registered.
   * </p>
   * <p>
   * In consequence all registered statements of the current session are removed from {@link RunningStatementStore}.
   * </p>
   */
  public static void cancel(long requestSequence) {
    IServerSession session = ThreadContext.getServerSession();
    if (session == null) {
      LOG.error("failed to find statement due to missing session context");
      return;
    }

    Map<Long, Statement> statementMap;
    synchronized (STATEMENT_MAP_LOCK) {
      statementMap = STATEMENT_MAP.get(session);
    }
    if (statementMap == null) {
      return;
    }
    Statement statement = statementMap.get(requestSequence);
    if (statement != null) {
      try {
        statement.cancel();
        LOG.info("request sent to cancel processing statement");
      }
      catch (Throwable e) {
        LOG.error("failed to cancel processing statement", e);
      }
    }
  }
}
