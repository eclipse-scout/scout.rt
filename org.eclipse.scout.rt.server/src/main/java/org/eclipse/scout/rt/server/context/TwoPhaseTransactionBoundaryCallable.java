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
package org.eclipse.scout.rt.server.context;

import org.eclipse.scout.commons.Assertions;
import org.eclipse.scout.commons.Assertions.AssertionException;
import org.eclipse.scout.commons.ICallable;
import org.eclipse.scout.commons.IChainable;
import org.eclipse.scout.commons.annotations.Internal;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.platform.ExceptionTranslator;
import org.eclipse.scout.rt.platform.OBJ;
import org.eclipse.scout.rt.platform.context.InitThreadLocalCallable;
import org.eclipse.scout.rt.platform.job.IFuture;
import org.eclipse.scout.rt.server.transaction.ITransaction;
import org.eclipse.scout.rt.server.transaction.ITransactionProvider;
import org.eclipse.scout.rt.server.transaction.TransactionRequiredException;
import org.eclipse.scout.rt.server.transaction.TransactionScope;
import org.eclipse.scout.rt.server.transaction.internal.ActiveTransactionRegistry;

/**
 * Depending on the {@link TransactionScope} and the existence of a caller transaction, this processor starts a new
 * transaction for the subsequent sequence of actions and ends the transaction according to the XA specification
 * (eXtended Architecture) upon completion. Thereto, the <code>2-phase-commit-protocol (2PC)</code> is applied in order
 * to successfully commit the transaction consistently over all involved transaction members like relational databases,
 * message queues and so on.
 *
 * @param <RESULT>
 *          the result type of the job's computation.
 * @since 5.1
 * @see <i>design pattern: chain of responsibility</i>
 */
public class TwoPhaseTransactionBoundaryCallable<RESULT> implements ICallable<RESULT>, IChainable<ICallable<RESULT>> {

  private static final IScoutLogger LOG = ScoutLogManager.getLogger(TwoPhaseTransactionBoundaryCallable.class);

  protected final ICallable<RESULT> m_next;
  protected final long m_transactionId;
  protected final TransactionScope m_transactionScope;

  public TwoPhaseTransactionBoundaryCallable(final ICallable<RESULT> next, final TransactionScope transactionScope, final long transactionId) {
    m_next = Assertions.assertNotNull(next);
    m_transactionScope = (transactionScope != null ? transactionScope : TransactionScope.REQUIRES_NEW);
    m_transactionId = transactionId;
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
        throw new AssertionException("Unsupported transaction scope [%s]", m_transactionScope);
    }
  }

  /**
   * Ensures a caller transaction to exist and continues the chain.
   */
  @Internal
  protected RESULT runMandatoryTxBoundary() throws Exception {
    final ITransaction callerTransaction = ITransaction.CURRENT.get();

    if (callerTransaction == null) {
      throw new TransactionRequiredException();
    }
    else {
      return initTxThreadLocalAndContinueChain(callerTransaction);
    }
  }

  /**
   * Creates a new transaction and continues the chain. Upon completion, the transaction is committed or rolled back.
   */
  @Internal
  protected RESULT runRequiresNewTxBoundary() throws Exception {
    final ITransaction transaction = OBJ.get(ITransactionProvider.class).provide(m_transactionId);

    ActiveTransactionRegistry.register(transaction);
    try {
      return initTxThreadLocalAndContinueChain(transaction);
    }
    finally {
      endTransactionSafe(transaction);
      ActiveTransactionRegistry.unregister(transaction);
    }
  }

  /**
   * Continues the chain on behalf of the current caller transaction. If not available, a new transaction is started and
   * upon completion, that transaction is committed or rolled back.
   */
  @Internal
  protected RESULT runRequiredTxBoundary() throws Exception {
    final ITransaction callerTransaction = ITransaction.CURRENT.get();

    if (callerTransaction != null) {
      return initTxThreadLocalAndContinueChain(callerTransaction);
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
      tx.addFailure(OBJ.get(ExceptionTranslator.class).translate(e));
      throw e;
    }
    catch (final Throwable t) {
      tx.addFailure(OBJ.get(ExceptionTranslator.class).translate(t));
      throw new Error(t);
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
  public ICallable<RESULT> getNext() {
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
      return future.getJobInput().identifier();
    }
    else {
      return "";
    }
  }
}
