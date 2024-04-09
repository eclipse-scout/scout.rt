/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.platform.transaction;

import java.util.function.Function;

import org.eclipse.scout.rt.platform.Bean;
import org.eclipse.scout.rt.platform.context.RunMonitor;
import org.eclipse.scout.rt.platform.util.concurrent.FutureCancelledError;
import org.eclipse.scout.rt.platform.util.concurrent.ICancellable;

/**
 * Represents a transaction which multiple transaction members can participate for consistent commit or rollback.
 * <p>
 * Cancellation is done using {@link RunMonitor#cancel(boolean)} on {@link RunMonitor#CURRENT}, which cancels all its
 * associated members. A cancelled transaction does not accept any new members.
 *
 * @since 3.4
 */
@Bean
public interface ITransaction extends ICancellable {

  /**
   * @throws TransactionCancelledError
   *           if {@link #isCancelled()}
   * @since 9.0
   */
  default void checkCancelled() {
    if (isCancelled()) {
      throw new TransactionCancelledError();
    }
  }

  /**
   * The {@link ITransaction} which is currently associated with the current thread.
   */
  ThreadLocal<ITransaction> CURRENT = new ThreadLocal<>();

  /**
   * Registers the given transaction member to participate in the <code>2-phase-commit-protocol (2PC)</code>.
   *
   * @throws FutureCancelledError
   *           if the transaction is cancelled.
   */
  void registerMember(ITransactionMember member);

  /**
   * Produces and registers the given transaction member using the given mapping function, but only if not registered
   * yet. This allows the member to participate in the <code>2-phase-commit-protocol (2PC)</code>.
   *
   * @return transaction member registered, or which was produced by the given mapping function.
   */
  <TRANSACTION_MEMBER extends ITransactionMember> TRANSACTION_MEMBER registerMemberIfAbsent(String memberId, Function<String, TRANSACTION_MEMBER> producer);

  /**
   * Produces and registers the given transaction member using the given mapping function, but only if not registered
   * yet. If the transaction has been cancelled, the <code>producer</code> will not be invoked and null will be
   * returned. This allows the member to participate in the <code>2-phase-commit-protocol (2PC)</code>.
   *
   * @return transaction member registered, or which was produced by the given mapping function, or null in case the
   *         transaction has been cancelled.
   */
  <TRANSACTION_MEMBER extends ITransactionMember> TRANSACTION_MEMBER registerMemberIfAbsentAndNotCancelled(String memberId, Function<String, TRANSACTION_MEMBER> producer);

  /**
   * Returns the transaction member of the given member id, or <code>null</code> if not registered.
   */
  ITransactionMember getMember(String memberId);

  /**
   * Returns all transaction members which participate in the <code>2-phase-commit-protocol (2PC)</code>.
   */
  ITransactionMember[] getMembers();

  /**
   * Unregisters the given transaction member from participating in the <code>2-phase-commit-protocol (2PC)</code>.
   */
  void unregisterMember(ITransactionMember member);

  /**
   * Unregisters the given transaction member from participating in the <code>2-phase-commit-protocol (2PC)</code>.
   */
  void unregisterMember(String memberId);

  /**
   * Indicates whether there are some failures associated with this transaction.
   */
  boolean hasFailures();

  /**
   * Returns the failures associated with this transaction.
   */
  Throwable[] getFailures();

  /**
   * Associates this transaction with the given {@link Throwable}, which typically results in a later rollback of this
   * transaction. This method has no effect if already registered.
   */
  void addFailure(Throwable t);

  /**
   * Tries to temporarily commit the transaction on all transaction members by invoking
   * {@link ITransactionMember#commitPhase1()} on any member.
   *
   * @return <code>true</code> if the commit phase 1 was successful on all members.
   * @throws FutureCancelledError
   *           if the transaction is cancelled.
   */
  boolean commitPhase1();

  /**
   * Instructs every transaction member to commit by invoking {@link ITransactionMember#commitPhase2()}. This method
   * must only be invoked upon successful 'commit phase 1' invocation.
   */
  void commitPhase2();

  /**
   * Instructs every transaction member to roll back by invoking {@link ITransactionMember#rollback()}.
   */
  void rollback();

  /**
   * Releases all transaction members by invoking {@link ITransactionMember#release()}.
   */
  void release();
}
