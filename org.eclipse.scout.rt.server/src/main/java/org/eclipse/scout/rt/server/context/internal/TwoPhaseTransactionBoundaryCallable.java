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
package org.eclipse.scout.rt.server.context.internal;

import java.util.concurrent.Callable;

import org.eclipse.scout.commons.Assertions;
import org.eclipse.scout.commons.IChainable;
import org.eclipse.scout.commons.annotations.Internal;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.context.RunMonitor;
import org.eclipse.scout.rt.platform.context.internal.InitThreadLocalCallable;
import org.eclipse.scout.rt.platform.job.IFuture;
import org.eclipse.scout.rt.server.transaction.ITransaction;
import org.eclipse.scout.rt.server.transaction.TransactionRequiredException;
import org.eclipse.scout.rt.server.transaction.TransactionScope;

/**
 * Depending on the {@link TransactionScope} and the existence of a caller transaction, this processor starts a new
 * transaction for the subsequent sequence of actions and ends the transaction according to the XA specification
 * (eXtended Architecture) upon completion. Thereto, the <code>2-phase-commit-protocol (2PC)</code> is applied in order
 * to successfully commit the transaction consistently over all involved transaction members like relational databases,
 * message queues, webservice consumers and so on.
 *
 * @param <RESULT>
 *          the result type of the job's computation.
 * @since 5.1
 * @see <i>design pattern: chain of responsibility</i>
 */
public class TwoPhaseTransactionBoundaryCallable<RESULT> implements Callable<RESULT>, IChainable<Callable<RESULT>> {

  private static final IScoutLogger LOG = ScoutLogManager.getLogger(TwoPhaseTransactionBoundaryCallable.class);

  protected final Callable<RESULT> m_next;
  protected final TransactionScope m_transactionScope;
  protected final ITransaction m_transaction;

  public TwoPhaseTransactionBoundaryCallable(final Callable<RESULT> next, final ITransaction transaction, final TransactionScope transactionScope) {
    m_next = Assertions.assertNotNull(next);
    m_transactionScope = (transactionScope != null ? transactionScope : TransactionScope.REQUIRES_NEW);
    m_transaction = transaction;
  }

  @Override
  public RESULT call() throws Exception {
    switch (m_transactionScope) {
      case MANDATORY:
        return runMandatoryTxBoundary();
      case REQUIRES_NEW:
        return runRequiresNewTxBoundary();
      case REQUIRED:
        return runRequiredTxBoundary();
      default:
        return Assertions.fail("Unsupported transaction scope [%s]", m_transactionScope);
    }
  }

  /**
   * Ensures a caller transaction to exist and continues the chain.
   */
  @Internal
  protected RESULT runMandatoryTxBoundary() throws Exception {
    if (m_transaction == null) {
      throw new TransactionRequiredException();
    }
    else {
      return initTxThreadLocalAndContinueChain(m_transaction);
    }
  }

  /**
   * Creates a new transaction and continues the chain. Upon completion, the transaction is committed or rolled back.
   */
  @Internal
  protected RESULT runRequiresNewTxBoundary() throws Exception {
    final ITransaction newTransaction = BEANS.get(ITransaction.class);

    RunMonitor.CURRENT.get().registerCancellable(newTransaction);
    try {
      return initTxThreadLocalAndContinueChain(newTransaction);
    }
    finally {
      endTransactionSafe(newTransaction);
      RunMonitor.CURRENT.get().unregisterCancellable(newTransaction);
    }
  }

  /**
   * Continues the chain on behalf of the current caller transaction. If not available, a new transaction is started and
   * upon completion, that transaction is committed or rolled back.
   */
  @Internal
  protected RESULT runRequiredTxBoundary() throws Exception {
    if (m_transaction != null) {
      return initTxThreadLocalAndContinueChain(m_transaction);
    }
    else {
      return runRequiresNewTxBoundary();
    }
  }

  /**
   * Registers the given transaction on {@link ITransaction#CURRENT}, delegates control to the next processor and
   * registers exceptions on the given transaction.
   */
  @Internal
  protected RESULT initTxThreadLocalAndContinueChain(final ITransaction tx) throws Exception {
    try {
      return new InitThreadLocalCallable<>(m_next, ITransaction.CURRENT, tx).call();
    }
    catch (final Exception | Error e) {
      tx.addFailure(e);
      throw e;
    }
  }

  /**
   * Commits the transaction on success, or rolls it back on error.
   */
  @Internal
  protected void endTransactionSafe(final ITransaction tx) {
    boolean commitSuccess = false;
    if (tx.hasFailures()) {
      commitSuccess = false;
    }
    else {
      // Commit the XA transaction in 2PC-style.
      commitSuccess = (commitPhase1Safe(tx) && commitPhase2Safe(tx));
    }

    if (!commitSuccess) {
      // Rollback the XA transaction because at least one XA member rejected the voting for commit or the final commit failed unexpectedly.
      rollbackSafe(tx);
    }

    releaseSafe(tx);
  }

  @Override
  public Callable<RESULT> getNext() {
    return m_next;
  }

  // === Utility methods for a safe interaction with a ITransaction ===

  /**
   * @return <code>true</code> on success, or <code>false</code> on failure.
   * @see ITransaction#commitPhase1()
   */
  @Internal
  protected boolean commitPhase1Safe(final ITransaction tx) {
    try {
      return tx.commitPhase1();
    }
    catch (ProcessingException | RuntimeException e) {
      LOG.error(String.format("Failed to commit XA transaction [2PC-phase='voting', job=%s, tx=%s]", getCurrentJobName(), tx), e);
      return false;
    }
  }

  /**
   * @return <code>true</code> on success, or <code>false</code> on failure.
   * @see ITransaction#commitPhase2()
   */
  @Internal
  protected boolean commitPhase2Safe(final ITransaction tx) {
    try {
      tx.commitPhase2();
      return true;
    }
    catch (final RuntimeException e) {
      LOG.error(String.format("Failed to commit XA transaction [2PC-phase='commit', job=%s, tx=%s]", getCurrentJobName(), tx), e);
      return false;
    }
  }

  /**
   * @return <code>true</code> on success, or <code>false</code> on failure.
   * @see ITransaction#rollback()
   */
  @Internal
  protected boolean rollbackSafe(final ITransaction tx) {
    try {
      tx.rollback();
      return true;
    }
    catch (final RuntimeException e) {
      LOG.error(String.format("Failed to rollback XA transaction [job=%s, tx=%s]", getCurrentJobName(), tx), e);
      return false;
    }
  }

  /**
   * @return <code>true</code> on success, or <code>false</code> on failure.
   * @see ITransaction#release()
   */
  @Internal
  protected boolean releaseSafe(final ITransaction tx) {
    try {
      tx.release();
      return true;
    }
    catch (final RuntimeException e) {
      LOG.error(String.format("Failed to release XA transaction members [job=%s, tx=%s]", getCurrentJobName(), tx), e);
      return false;
    }
  }

  @Internal
  protected String getCurrentJobName() {
    final IFuture<?> future = IFuture.CURRENT.get();
    if (future != null) {
      return future.getJobInput().name();
    }
    else {
      return "";
    }
  }
}
