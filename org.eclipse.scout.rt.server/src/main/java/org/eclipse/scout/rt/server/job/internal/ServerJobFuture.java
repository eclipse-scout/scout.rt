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
package org.eclipse.scout.rt.server.job.internal;

import java.util.Collections;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.RunnableScheduledFuture;

import org.eclipse.scout.commons.job.internal.JobFuture;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.server.job.ServerJobInput;
import org.eclipse.scout.rt.server.transaction.ITransaction;

/**
 * {@link RunnableScheduledFuture} representing a task associated with a {@link ServerJobInput} and functionality to
 * cancel related transaction(s) once this Future is being cancelled.
 *
 * @see RunnableScheduledFuture
 * @see ServerJobManager
 * @since 5.1
 */
public class ServerJobFuture<RESULT> extends JobFuture<RESULT> {

  private static final IScoutLogger LOG = ScoutLogManager.getLogger(ServerJobFuture.class);

  private final Queue<ITransaction> m_transactions = new ConcurrentLinkedQueue<>(); // because register/unregister and cancel might occur in parallel.

  public ServerJobFuture(final RunnableScheduledFuture<RESULT> delegate, final ServerJobInput input) {
    super(delegate, input);
  }

  /**
   * Registers the given {@link ITransaction} to be cancelled once this Future gets cancelled.
   *
   * @see #unregister(ITransaction)
   */
  public void register(final ITransaction transaction) {
    m_transactions.add(transaction);
  }

  /**
   * Unregisters the given {@link ITransaction}.
   *
   * @see #register(ITransaction)
   */
  public void unregister(final ITransaction transaction) {
    m_transactions.remove(transaction);
  }

  @Override
  public ServerJobInput getInput() {
    return (ServerJobInput) super.getInput();
  }

  @Override
  public boolean cancel(final boolean interruptIfRunning) {
    if (isCancelled()) {
      return false;
    }

    // 1. Cancel all associated transactions (must be done before canceling the job to not exit transaction boundary).
    final Set<Boolean> success = new HashSet<>();
    for (final ITransaction transaction : m_transactions) {
      success.add(cancelTransactionSafe(transaction));
    }

    // 2. Cancel the job.
    success.add(super.cancel(interruptIfRunning));

    return Collections.singleton(true).equals(success);
  }

  /**
   * Cancels the given transaction without throwing an exception.
   *
   * @param transaction
   *          {@link ITransaction} to be cancelled.
   * @return <code>true</code> if successful cancelled.
   */
  protected boolean cancelTransactionSafe(final ITransaction transaction) {
    try {
      return transaction.cancel();
    }
    catch (final RuntimeException e) {
      LOG.error(String.format("Failed to cancel transaction [tx=%s]", transaction), e);
      return false;
    }
  }
}
