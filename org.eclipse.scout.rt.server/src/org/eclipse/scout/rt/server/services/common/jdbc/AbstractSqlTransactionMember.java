package org.eclipse.scout.rt.server.services.common.jdbc;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;

import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.server.transaction.AbstractTransactionMember;
import org.eclipse.scout.rt.server.transaction.ITransaction;

public abstract class AbstractSqlTransactionMember extends AbstractTransactionMember {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(AbstractSqlTransactionMember.class);

  private final Object m_activeStatementsLock = new Object();
  private final HashSet<Statement> m_activeStatements = new HashSet<Statement>();
  private boolean m_cancelled;

  public AbstractSqlTransactionMember(String transactionMemberId) {
    super(transactionMemberId);
  }

  @Override
  public boolean needsCommit() {
    return true;
  }

  @Override
  public boolean commitPhase1() {
    return true;
  }

  /**
   * Registers a statement in order to be canceled by {@link ITransaction#cancel()}
   * 
   * @param statement
   *          the statement to be registered
   * @throws SQLException
   */
  public void registerActiveStatement(Statement statement) throws SQLException {
    synchronized (m_activeStatementsLock) {
      if (m_cancelled) {
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
    HashSet<Statement> set;
    synchronized (m_activeStatementsLock) {
      if (m_cancelled) {
        return;
      }
      m_cancelled = true;
      set = new HashSet<Statement>(m_activeStatements);
      m_activeStatements.clear();
    }
    for (Statement s : set) {
      try {
        LOG.info("request sent to cancel jdbc statement");
        s.cancel();
      }
      catch (Throwable e) {
        LOG.error("failed to cancel jdbc statement", e);
      }
    }
  }

}
