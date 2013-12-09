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

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.server.services.common.jdbc.AbstractSqlTransactionMember;
import org.eclipse.scout.rt.server.transaction.internal.ActiveTransactionRegistry;
import org.eclipse.scout.rt.shared.services.common.processing.IServerProcessingCancelService;
import org.eclipse.scout.rt.shared.servicetunnel.ServiceTunnelRequest;

/**
 * Whenever a remote service call is handled by the ServiceTunnelServlet it is dispatched to a
 * DefaultTransactionDelegate that runs a ITransaction as a ServerJob.
 * That transaction does {@link ActiveTransactionRegistry#register(ITransaction)} /
 * {@link ActiveTransactionRegistry#unregister(ITransaction)} with the requestSequence as the
 * {@link ITransaction#getTransactionSequence()} sequence. Resources such as jdbc connections take part on the
 * transaction as {@link ITransactionMember}s.
 * Whenever a sql statement is run, it registers/unregisters on the
 * {@link AbstractSqlTransactionMember#registerActiveStatement(java.sql.Statement)} /
 * {@link AbstractSqlTransactionMember#unregisterActiveStatement(java.sql.Statement)}.
 * Thus canceling a {@link ITransaction#cancel()} also cancels all its members {@link ITransactionMember#cancel()} and
 * that cancels the (potentially) running statement.
 * A canceled transaction can only do a rollback and does not accept new members.
 * 
 * @since 3.4
 */
public interface ITransaction {

  /**
   * @return transaction sequence
   *         is either 0L for non-cancellable transactions or the {@link ServiceTunnelRequest#getRequestSequence()} for
   *         backend calls
   *         from the ui. This number is used when cancelling a transaction by
   *         {@link IServerProcessingCancelService#cancel(long)}
   */
  long getTransactionSequence();

  /**
   * register the member (even if the transaction is canceled)
   * 
   * @throws ProcessingException
   *           with an {@link InterruptedException} when the transaction is canceled
   */
  void registerMember(ITransactionMember member) throws ProcessingException;

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
  boolean commitPhase1() throws ProcessingException;

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

  /**
   * an external process tries to cancel the transaction
   * 
   * @return true if cancel was successful and transaction was in fact canceled, false otherwise
   */
  boolean cancel();

  boolean isCancelled();

}
