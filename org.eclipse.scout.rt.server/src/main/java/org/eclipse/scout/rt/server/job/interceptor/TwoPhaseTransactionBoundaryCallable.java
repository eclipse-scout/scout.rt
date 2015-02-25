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
package org.eclipse.scout.rt.server.job.interceptor;

import java.util.concurrent.Callable;

import org.eclipse.scout.commons.Assertions;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.commons.annotations.Internal;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.job.IJob;
import org.eclipse.scout.commons.job.interceptor.Chainable;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.server.transaction.ITransaction;
import org.eclipse.scout.rt.server.transaction.internal.ActiveTransactionRegistry;

/**
 * Processor that demarcates the transaction boundaries for the subsequent sequence of actions and ends the transaction
 * according to the XA specification (eXtended Architecture) upon completion. Thereto, the
 * <code>2-phase-commit-protocol (2PC)</code> is applied in order to successfully commit the transaction consistently
 * over all involved transaction members like relational databases, message queues and so on.
 * <p/>
 * This {@link Callable} is a processing object in the language of the design pattern 'chain-of-responsibility'.
 *
 * @param <RESULT>
 *          the result type of the job's computation.
 * @since 5.1
 */
public class TwoPhaseTransactionBoundaryCallable<RESULT> implements Callable<RESULT>, Chainable {

  private static final IScoutLogger LOG = ScoutLogManager.getLogger(TwoPhaseTransactionBoundaryCallable.class);

  @Internal
  protected final Callable<RESULT> m_next;
  @Internal
  protected final ITransaction m_transaction;

  /**
   * Creates a processor that demarcates the transaction boundaries for the subsequent sequence of actions and ends the
   * transaction according to the XA specification (eXtended Architecture) upon completion.
   *
   * @param next
   *          next processor in the chain; must not be <code>null</code>.
   * @param transaction
   *          transaction to set the demarcation boundaries; must not be <code>null</code>.
   */
  public TwoPhaseTransactionBoundaryCallable(final Callable<RESULT> next, final ITransaction transaction) {
    m_next = Assertions.assertNotNull(next);
    m_transaction = Assertions.assertNotNull(transaction);
  }

  @Override
  public RESULT call() throws Exception {
    ActiveTransactionRegistry.register(m_transaction);
    try {
      return runAsTransaction(m_transaction);
    }
    finally {
      ActiveTransactionRegistry.unregister(m_transaction);
    }
  }

  /**
   * Delegates control to the next processor, commits the transaction on success or rolls it back on error.
   *
   * @param tx
   *          {@link ITransaction} to be coordinated.
   * @return result of the processing.
   * @throws Exception
   *           if the processing throws an error.
   */
  @Internal
  protected RESULT runAsTransaction(final ITransaction tx) throws Exception {
    final TransactionSafeDelegator txDelegator = new TransactionSafeDelegator(tx); // ITransaction-delegate that does not propagate errors.

    RESULT result = null;
    Exception error = null;
    try {
      result = m_next.call();
    }
    catch (final Exception e) {
      error = e;
      tx.addFailure((e instanceof ProcessingException && e.getCause() != null) ? e.getCause() : e); // Use the cause if being wrapped with a ProcessingException.
    }

    boolean commitSuccess = false;
    if (tx.hasFailures()) {
      logTxFailures(tx);
      commitSuccess = false;
    }
    else {
      // Commit the XA transaction in 2PC-style.
      commitSuccess = (txDelegator.commitPhase1() && txDelegator.commitPhase2());
    }

    if (!commitSuccess) {
      // Rollback the XA transaction because at least one XA member rejected the voting for commit or the final commit failed unexpectedly.
      txDelegator.rollback();
    }

    txDelegator.release();

    if (error != null) {
      throw error;
    }
    else {
      return result;
    }
  }

  @Internal
  protected void logTxFailures(final ITransaction tx) {
    for (final Throwable txFailure : tx.getFailures()) {
      LOG.error(String.format("The current XA transaction has been rolled back because of a processing error. [reason=%s, job=%s, tx=%s]", StringUtility.nvl(txFailure.getMessage(), "n/a"), IJob.CURRENT.get().getName(), tx), txFailure);
    }
  }

  @Override
  public Object getNext() {
    return m_next;
  }
}
