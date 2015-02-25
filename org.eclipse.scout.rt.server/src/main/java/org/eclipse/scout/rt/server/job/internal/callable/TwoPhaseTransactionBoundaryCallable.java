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
package org.eclipse.scout.rt.server.job.internal.callable;

import java.lang.reflect.UndeclaredThrowableException;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import org.eclipse.scout.commons.Assertions;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.commons.annotations.Internal;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.job.IFuture;
import org.eclipse.scout.commons.job.IJobInput;
import org.eclipse.scout.commons.job.internal.callable.Chainable;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.server.job.internal.ServerJobFuture;
import org.eclipse.scout.rt.server.transaction.ITransaction;

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
public class TwoPhaseTransactionBoundaryCallable<RESULT> implements Callable<RESULT>, Chainable<RESULT> {

  private static final IScoutLogger LOG = ScoutLogManager.getLogger(TwoPhaseTransactionBoundaryCallable.class);

  @Internal
  protected final Callable<RESULT> m_next;
  @Internal
  protected final ITransaction m_transaction;
  @Internal
  protected IJobInput m_input;

  /**
   * Creates a processor that demarcates the transaction boundaries for the subsequent sequence of actions and ends the
   * transaction according to the XA specification (eXtended Architecture) upon completion.
   *
   * @param next
   *          next processor in the chain; must not be <code>null</code>.
   * @param transaction
   *          transaction to set the demarcation boundaries; must not be <code>null</code>.
   * @param input
   *          used for logging purpose.
   */
  public TwoPhaseTransactionBoundaryCallable(final Callable<RESULT> next, final ITransaction transaction, final IJobInput input) {
    m_next = Assertions.assertNotNull(next);
    m_transaction = Assertions.assertNotNull(transaction);
    m_input = input;
  }

  @Override
  public RESULT call() throws Exception {
    // Register the transaction on the current Future.
    final Future<?> future = Assertions.assertNotNull(IFuture.CURRENT.get(), "Unexpected inconsistency: No Future bound to current thread. [thread=%s, job=%s]", Thread.currentThread().getName(), m_input.getIdentifier("n/a"));
    Assertions.assertTrue(future instanceof ServerJobFuture, "Unexpected inconsistency: Current Future of the wrong type. [expected=%s, actual=%s, thread=%s, job=%s]", ServerJobFuture.class.getSimpleName(), future.getClass().getSimpleName(), Thread.currentThread().getName(), m_input.getIdentifier("n/a"));

    ((ServerJobFuture) future).register(m_transaction);
    try {
      return runAsTransaction(m_transaction);
    }
    finally {
      ((ServerJobFuture) future).unregister(m_transaction);
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
    catch (final UndeclaredThrowableException e) {
      error = e;
      tx.addFailure(e.getCause() != null ? e.getCause() : e);
    }
    catch (final Exception e) {
      error = e;
      tx.addFailure((e instanceof ProcessingException && e.getCause() != null) ? e.getCause() : e); // Consider the real cause if wrapped by a ProcessingException.
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
    for (final Throwable failure : tx.getFailures()) {
      LOG.error(String.format("Current transaction was rolled back because of a processing error. [reason=%s, job=%s, tx=%s]", StringUtility.nvl(failure.getMessage(), "n/a"), m_input.getIdentifier("n/a"), tx), failure);
    }
  }

  /**
   * @return the underlying transaction.
   */
  public ITransaction getTransaction() {
    return m_transaction;
  }

  @Override
  public Callable<RESULT> getNext() {
    return m_next;
  }
}
