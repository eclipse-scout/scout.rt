package org.eclipse.scout.rt.testing.platform.runner.statement;

import org.eclipse.scout.rt.platform.transaction.ITransaction;
import org.junit.runners.model.Statement;

/**
 * Statement that adds a failure to the current {@link ITransaction} if the next {@link Statement} throws any
 * {@link Throwable}.
 *
 * @since 6.0
 */
public class TransactionAddFailureOnAnyExceptionStatement extends Statement {

  private final Statement m_next;

  public TransactionAddFailureOnAnyExceptionStatement(Statement next) {
    m_next = next;
  }

  @Override
  public void evaluate() throws Throwable {
    try {
      m_next.evaluate();
    }
    catch (Throwable t) {
      final ITransaction txn = ITransaction.CURRENT.get();
      if (txn != null) {
        txn.addFailure(new IllegalStateException("cancel transaction marker"));
      }
      throw t;
    }
  }
}
