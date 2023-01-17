/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.server.jdbc;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;

import org.eclipse.scout.rt.platform.transaction.AbstractTransactionMember;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractSqlTransactionMember extends AbstractTransactionMember {
  private static final Logger LOG = LoggerFactory.getLogger(AbstractSqlTransactionMember.class);

  private final Object m_activeStatementsLock = new Object();
  private final HashSet<Statement> m_activeStatements = new HashSet<>();
  private boolean m_cancelled;

  /** true during completion phase (commit/rollback) */
  private boolean m_finishingTransaction;

  public AbstractSqlTransactionMember(String transactionMemberId) {
    super(transactionMemberId);
  }

  @Override
  public boolean needsCommit() {
    return true;
  }

  protected void setFinishingTransaction(boolean finishingTransaction) {
    m_finishingTransaction = finishingTransaction;
  }

  /**
   * Registers a statement in order to be canceled by
   * {@link org.eclipse.scout.rt.platform.transaction.ITransaction#cancel(boolean)}
   *
   * @param statement
   *          the statement to be registered
   * @throws SQLException
   */
  public void registerActiveStatement(Statement statement) throws SQLException {
    synchronized (m_activeStatementsLock) {
      if (m_cancelled && !m_finishingTransaction) {
        throw new SQLException("Transaction was cancelled");
      }
      m_activeStatements.add(statement);
    }
  }

  /**
   * Removes a statement
   *
   * @param statement
   *          the statement to be removed
   */
  public void unregisterActiveStatement(Statement statement) {
    synchronized (m_activeStatementsLock) {
      m_activeStatements.remove(statement);
    }
  }

  @Override
  public void cancel() {
    Iterable<Statement> set;
    synchronized (m_activeStatementsLock) {
      if (m_cancelled) {
        return;
      }
      m_cancelled = true;
      set = new HashSet<>(m_activeStatements);
      m_activeStatements.clear();
    }
    for (Statement s : set) {
      try {
        LOG.info("request sent to cancel jdbc statement");
        s.cancel();
      }
      catch (Exception e) {
        LOG.error("failed to cancel jdbc statement", e);
      }
    }
  }
}
