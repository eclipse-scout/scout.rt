/*******************************************************************************
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
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

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.chain.callable.CallableChain;
import org.eclipse.scout.rt.platform.chain.callable.ICallableDecorator;
import org.eclipse.scout.rt.platform.context.RunMonitor;
import org.eclipse.scout.rt.platform.util.Assertions;
import org.eclipse.scout.rt.server.transaction.ITransaction;
import org.eclipse.scout.rt.server.transaction.TransactionRequiredException;
import org.eclipse.scout.rt.server.transaction.TransactionScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Depending on the {@link TransactionScope} and the existence of a caller transaction, this processor starts a new
 * transaction for the subsequent sequence of actions and ends the transaction according to the XA specification
 * (eXtended Architecture) upon completion. Thereto, the <code>2-phase-commit-protocol (2PC)</code> is applied in order
 * to successfully commit the transaction consistently over all involved transaction members like relational databases,
 * message queues, webservice consumers and so on.
 * <p>
 * Instances of this class are to be added to a {@link CallableChain} to participate in the execution of a
 * {@link Callable}.
 *
 * @since 5.1
 */
public class TransactionProcessor<RESULT> implements ICallableDecorator<RESULT> {

  private static final Logger LOG = LoggerFactory.getLogger(TransactionProcessor.class);

  protected final TransactionScope m_transactionScope;
  protected final ITransaction m_callerTransaction;

  public TransactionProcessor(final ITransaction callerTransaction, final TransactionScope transactionScope) {
    m_transactionScope = (transactionScope != null ? transactionScope : TransactionScope.REQUIRES_NEW);
    m_callerTransaction = callerTransaction;
  }

  @Override
  public IUndecorator<RESULT> decorate() {
    switch (m_transactionScope) {
      case REQUIRES_NEW:
        return requiresNew();
      case REQUIRED:
        return required(m_callerTransaction);
      case MANDATORY:
        return mandatory(m_callerTransaction);
      default:
        return Assertions.fail("Unsupported transaction scope [{}]", m_transactionScope);
    }
  }

  /**
   * Decorates the calling context to run in a new transaction, which upon completion is committed or rolled back.
   */
  protected IUndecorator<RESULT> requiresNew() {
    final ITransaction newTransaction = BEANS.get(ITransaction.class);
    final Registration threadLocalRegistration = registerTransactionInThreadLocal(newTransaction);
    final Registration monitorRegistration = registerTransactionInRunMonitor(newTransaction);

    return new IUndecorator<RESULT>() {

      @Override
      public void undecorate(final RESULT callableResult, final Throwable callableException) {
        addTransactionalFailureIfNotNull(callableException);
        BEANS.get(ITransactionCommitProtocol.class).commitOrRollback(ITransaction.CURRENT.get());
        threadLocalRegistration.undo();
        monitorRegistration.undo();
      }
    };
  }

  /**
   * Decorates the calling context to run on behalf of the current caller transaction. If not available, a new
   * transaction is started and upon completion, that transaction is committed or rolled back.
   */
  protected IUndecorator<RESULT> required(final ITransaction callerTransaction) {
    if (callerTransaction != null) {
      return mandatory(callerTransaction);
    }
    else {
      return requiresNew();
    }
  }

  /**
   * Ensures a caller transaction to exist and decorates the calling context to run on behalf of that transaction.
   */
  protected IUndecorator<RESULT> mandatory(final ITransaction callerTransaction) {
    if (callerTransaction == null) {
      throw new TransactionRequiredException();
    }

    final Registration threadLocalRegistration = registerTransactionInThreadLocal(callerTransaction);

    return new IUndecorator<RESULT>() {

      @Override
      public void undecorate(final RESULT callableResult, final Throwable callableException) {
        addTransactionalFailureIfNotNull(callableException);
        threadLocalRegistration.undo();
      }
    };
  }

  /**
   * Registers the given transaction in the current {@link RunMonitor}, so it is cancelled once the monitor gets
   * cancelled.
   *
   * @return the 'undo-action' to unregister the transaction from the monitor.
   */
  protected Registration registerTransactionInRunMonitor(final ITransaction transaction) {
    RunMonitor.CURRENT.get().registerCancellable(transaction);

    // Return the 'undo-action' to unregister the transaction from the monitor.
    return new Registration() {

      @Override
      public void undo() {
        RunMonitor.CURRENT.get().unregisterCancellable(transaction);
      }
    };
  }

  /**
   * Registers the given transaction in {@link ITransaction#CURRENT} thread-local, so it becomes the active transaction.
   *
   * @return the 'undo-action' to restore the thread-local value.
   */
  protected Registration registerTransactionInThreadLocal(final ITransaction transaction) {
    final ITransaction oldTransaction = ITransaction.CURRENT.get();
    ITransaction.CURRENT.set(transaction);

    // Return the 'undo-action' to restore the thread-local value.
    return new Registration() {

      @Override
      public void undo() {
        if (oldTransaction == null) {
          ITransaction.CURRENT.remove();
        }
        else {
          ITransaction.CURRENT.set(oldTransaction);
        }
      }
    };
  }

  /**
   * In case the given failure is not <code>null</code>, that failure is added to the current transaction as
   * transactional failure. Upon completion, that causes a roll back of the transaction.
   */
  protected void addTransactionalFailureIfNotNull(final Throwable failure) {
    if (failure == null) {
      return;
    }

    try {
      ITransaction.CURRENT.get().addFailure(failure);
    }
    catch (final RuntimeException e) {
      LOG.error("Unexpected: Failed to register failure on transaction", e);
    }
  }

  /**
   * Represents a registration which can be undone.
   */
  protected interface Registration {

    /**
     * Invoke to undo the registration.
     */
    void undo();
  }
}
