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
import org.eclipse.scout.rt.platform.chain.callable.CallableChain.Chain;
import org.eclipse.scout.rt.platform.chain.callable.ICallableInterceptor;
import org.eclipse.scout.rt.platform.context.RunMonitor;
import org.eclipse.scout.rt.platform.exception.DefaultExceptionTranslator;
import org.eclipse.scout.rt.platform.util.Assertions;
import org.eclipse.scout.rt.platform.util.IRegistrationHandle;
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
 * Instances of this class are to be added to a {@link CallableChain} to participate in the execution of a
 * {@link Callable}.
 *
 * @since 5.1
 */
public class TransactionProcessor<RESULT> implements ICallableInterceptor<RESULT> {

  protected final TransactionScope m_transactionScope;
  protected final ITransaction m_callerTransaction;

  public TransactionProcessor(final ITransaction callerTransaction, final TransactionScope transactionScope) {
    m_transactionScope = (transactionScope != null ? transactionScope : TransactionScope.REQUIRES_NEW);
    m_callerTransaction = callerTransaction;
  }

  @Override
  public RESULT intercept(Chain<RESULT> chain) throws Exception {
    try {
      switch (m_transactionScope) {
        case REQUIRES_NEW:
          return requiresNew(chain);
        case REQUIRED:
          return required(chain);
        case MANDATORY:
          return mandatory(chain);
        default:
          return Assertions.fail("Unsupported transaction scope [{}]", m_transactionScope);
      }
    }
    catch (Throwable t) {
      throw BEANS.get(DefaultExceptionTranslator.class).translate(t);
    }
  }

  @Override
  public boolean isEnabled() {
    return true;
  }

  /**
   * Decorates the calling context to run in a new transaction, which upon completion is committed or rolled back.
   */
  protected RESULT requiresNew(Chain<RESULT> chain) throws Throwable {
    final ITransaction newTransaction = BEANS.get(ITransaction.class);
    final IRegistrationHandle threadLocalRegistration = registerTransactionInThreadLocal(newTransaction);
    final IRegistrationHandle monitorRegistration = registerTransactionInRunMonitor(newTransaction);
    try {
      return chain.continueChain();
    }
    catch (Throwable t) {
      newTransaction.addFailure(t);
      throw t;
    }
    finally {
      BEANS.get(ITransactionCommitProtocol.class).commitOrRollback(newTransaction);
      threadLocalRegistration.dispose();
      monitorRegistration.dispose();

      if (newTransaction.hasFailures()) {
        throw newTransaction.getFailures()[0];
      }
    }
  }

  /**
   * Decorates the calling context to run on behalf of the current caller transaction. If not available, a new
   * transaction is started and upon completion, that transaction is committed or rolled back.
   */
  protected RESULT required(Chain<RESULT> chain) throws Throwable {
    if (m_callerTransaction != null) {
      return mandatory(chain);
    }
    else {
      return requiresNew(chain);
    }
  }

  /**
   * Ensures a caller transaction to exist and decorates the calling context to run on behalf of that transaction.
   */
  protected RESULT mandatory(Chain<RESULT> chain) throws Throwable {
    if (m_callerTransaction == null) {
      throw new TransactionRequiredException();
    }

    final IRegistrationHandle threadLocalRegistration = registerTransactionInThreadLocal(m_callerTransaction);
    try {
      return chain.continueChain();
    }
    catch (Throwable t) {
      m_callerTransaction.addFailure(t);
      throw t;
    }
    finally {
      threadLocalRegistration.dispose();
    }
  }

  /**
   * Registers the given transaction in the current {@link RunMonitor}, so it is cancelled once the monitor gets
   * cancelled.
   *
   * @return the 'undo-action' to unregister the transaction from the monitor.
   */
  protected IRegistrationHandle registerTransactionInRunMonitor(final ITransaction transaction) {
    RunMonitor.CURRENT.get().registerCancellable(transaction);

    // Return the 'undo-action' to unregister the transaction from the monitor.
    return new IRegistrationHandle() {

      @Override
      public void dispose() {
        RunMonitor.CURRENT.get().unregisterCancellable(transaction);
      }
    };
  }

  /**
   * Registers the given transaction in {@link ITransaction#CURRENT} thread-local, so it becomes the active transaction.
   *
   * @return the 'undo-action' to restore the thread-local value.
   */
  protected IRegistrationHandle registerTransactionInThreadLocal(final ITransaction transaction) {
    final ITransaction oldTransaction = ITransaction.CURRENT.get();
    ITransaction.CURRENT.set(transaction);

    // Return the 'undo-action' to restore the thread-local value.
    return new IRegistrationHandle() {

      @Override
      public void dispose() {
        if (oldTransaction == null) {
          ITransaction.CURRENT.remove();
        }
        else {
          ITransaction.CURRENT.set(oldTransaction);
        }
      }
    };
  }
}
