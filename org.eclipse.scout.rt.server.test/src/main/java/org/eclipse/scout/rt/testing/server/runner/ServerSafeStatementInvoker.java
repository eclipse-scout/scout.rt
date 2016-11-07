package org.eclipse.scout.rt.testing.server.runner;

import org.eclipse.scout.rt.platform.util.concurrent.IRunnable;
import org.eclipse.scout.rt.server.transaction.ITransaction;
import org.eclipse.scout.rt.testing.platform.runner.SafeStatementInvoker;
import org.junit.runners.model.Statement;

/**
 * Server-side version of {@link SafeStatementInvoker}. It adds a failure to the current transaction so that it is
 * rolled-back on its completion.
 *
 * @since 6.0
 */
public class ServerSafeStatementInvoker implements IRunnable {

  private volatile Throwable m_throwable;
  private final Statement m_next;

  /**
   * @deprecated This server-side version of {@link SafeStatementInvoker} is not required in newer versions of scout
   *             anymore because the transaction support has been moved to the platform module.
   */
  @Deprecated
  public ServerSafeStatementInvoker(final Statement next) {
    m_next = next;
  }

  @Override
  public void run() throws Exception {
    try {
      m_next.evaluate();
    }
    catch (final Throwable t) {
      m_throwable = t;
      final ITransaction txn = ITransaction.CURRENT.get();
      if (txn != null) {
        txn.addFailure(new IllegalStateException("cancel transaction marker"));
      }
    }
  }

  /**
   * Throws the {@link Throwable} caught during execution or just returns.
   */
  public void throwOnError() throws Throwable {
    if (m_throwable != null) {
      throw m_throwable;
    }
  }
}
