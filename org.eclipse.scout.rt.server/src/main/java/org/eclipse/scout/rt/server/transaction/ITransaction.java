/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.server.transaction;

import org.eclipse.scout.commons.ICancellable;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.platform.Bean;
import org.eclipse.scout.rt.platform.context.RunMonitor;

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
   * register the member (even if the transaction is canceled)
   *
   * @throws ProcessingException
   *           with an {@link InterruptedException} when the transaction is canceled
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
   * Two-phase commit
   * <p>
   * Temporary commits the transaction members
   * <p>
   *
   * @return true without any exception if the commit phase 1 was successful on all members.
   *         <p>
   *         Subsequently there will be a call to {@link #commitPhase2()} or {@link #rollback()}
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
