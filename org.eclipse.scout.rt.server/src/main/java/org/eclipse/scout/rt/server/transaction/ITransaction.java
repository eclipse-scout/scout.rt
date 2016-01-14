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
package org.eclipse.scout.rt.server.transaction;

import org.eclipse.scout.rt.platform.Bean;
import org.eclipse.scout.rt.platform.context.RunMonitor;
import org.eclipse.scout.rt.platform.util.concurrent.CancellationRuntimeException;
import org.eclipse.scout.rt.platform.util.concurrent.ICancellable;

/**
 * Represents a transaction which multiple transaction members can participate for consistent commit or rollback.
 * <p>
 * Cancelling is done using {@link RunMonitor#cancel(boolean)} on {@link RunMonitor#CURRENT}, which in turn cancels all
 * its associated members and (potentially) running SQL statements. A cancelled transaction does not accept any new
 * members.
 * <p>
 * Whenever for example a SQL statement is run, it registers/unregisters on the
 * {@link AbstractSqlTransactionMember#registerActiveStatement(java.sql.Statement)} /
 * {@link AbstractSqlTransactionMember#unregisterActiveStatement(java.sql.Statement)}.
 * <p>
 *
 * @since 3.4
 */
@Bean
public interface ITransaction extends ICancellable {

  /**
   * The {@link ITransaction} which is currently associated with the current thread.
   */
  ThreadLocal<ITransaction> CURRENT = new ThreadLocal<>();

  /**
   * Registers the given {@link ITransactionMember}.
   *
   * @throws CancellationRuntimeException
   *           if the transaction is cancelled.
   */
  void registerMember(ITransactionMember member);

  ITransactionMember getMember(String memberId);

  ITransactionMember[] getMembers();

  void unregisterMember(ITransactionMember member);

  void unregisterMember(String memberId);

  boolean hasFailures();

  Throwable[] getFailures();

  void addFailure(Throwable t);

  /**
   * Tries to temporarily commit the transaction on all transaction members.
   *
   * @return <code>true</code> without if the commit phase 1 was successful on all members.
   * @throws CancellationRuntimeException
   *           if the transaction is cancelled.
   */
  boolean commitPhase1();

  /**
   * commit phase 2 of the transaction members (commit phase 1 confirmation)
   */
  void commitPhase2();

  /**
   * rollback on the transaction members (commit phase 1 cancel and rollback)
   */
  void rollback();

  /**
   * release any members allocated by the transaction members
   */
  void release();
}
