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

import java.util.concurrent.Callable;

import org.eclipse.scout.commons.Assertions;
import org.eclipse.scout.commons.annotations.Internal;
import org.eclipse.scout.commons.chain.IInvocationInterceptor;
import org.eclipse.scout.commons.chain.InvocationChain;
import org.eclipse.scout.commons.chain.InvocationChain.Chain;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.context.RunMonitor;
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
 * <p>
 * Instances of this class are to be added to a {@link InvocationChain} to participate in the execution of a
 * {@link Callable}.
 *
 * @since 5.1
 */
public class TransactionProcessor<RESULT> implements IInvocationInterceptor<RESULT> {

  private static final IScoutLogger LOG = ScoutLogManager.getLogger(TransactionProcessor.class);

  protected final TransactionScope m_transactionScope;
  protected final ITransaction m_transaction;

  public TransactionProcessor(final ITransaction transaction, final TransactionScope transactionScope) {
    m_transactionScope = (transactionScope != null ? transactionScope : TransactionScope.REQUIRES_NEW);
    m_transaction = transaction;
  }

  @Override
  public RESULT intercept(final Chain<RESULT> chain) throws Exception {
    switch (m_transactionScope) {
      case MANDATORY:
        return handleMandatoryTransaction(chain);
      case REQUIRES_NEW:
        return handleRequiresNewTransaction(chain);
      case REQUIRED:
        return handleRequiredTransaction(chain);
      default:
        return Assertions.fail("Unsupported transaction scope [%s]", m_transactionScope);
    }
  }

  /**
   * Ensures a caller transaction to exist and continues the chain.
   */
  @Internal
  protected RESULT handleMandatoryTransaction(final Chain<RESULT> chain) throws Exception {
    if (m_transaction == null) {
      throw new TransactionRequiredException();
    }
    else {
      return continueChainInTransaction(chain, m_transaction);
    }
  }

  /**
   * Creates a new transaction and continues the chain. Upon completion, the transaction is committed or rolled back.
   */
  @Internal
  protected RESULT handleRequiresNewTransaction(final Chain<RESULT> chain) throws Exception {
    final ITransaction newTransaction = BEANS.get(ITransaction.class);

    RunMonitor.CURRENT.get().registerCancellable(newTransaction);
    try {
      return continueChainInTransaction(chain, newTransaction);
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
  protected RESULT handleRequiredTransaction(final Chain<RESULT> chain) throws Exception {
    if (m_transaction != null) {
      return continueChainInTransaction(chain, m_transaction);
    }
    else {
      return handleRequiresNewTransaction(chain);
    }
  }

  /**
   * Registers the given transaction on {@link ITransaction#CURRENT}, delegates control to the next processor and
   * registers exceptions on the given transaction.
   */
  @Internal
  protected RESULT continueChainInTransaction(final Chain<RESULT> chain, final ITransaction transaction) throws Exception {
    final ITransaction originTransaction = ITransaction.CURRENT.get();

    ITransaction.CURRENT.set(transaction);
    try {
      return chain.continueChain();
    }
    catch (final Exception | Error e) {
      transaction.addFailure(e);
      throw e;
    }
    finally {
      if (originTransaction == null) {
        ITransaction.CURRENT.remove();
      }
      else {
        ITransaction.CURRENT.set(originTransaction);
      }
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
    catch (final RuntimeException e) {
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
      return future.getJobInput().getName();
    }
    else {
      return "";
    }
  }

  @Override
  public boolean isEnabled() {
    return true;
  }
}
