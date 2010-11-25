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

/**
 * @since Build 183
 */
public interface ITransaction {

  void registerResource(ITransactionMember resource);

  ITransactionMember getMember(String resourceId);

  ITransactionMember[] getMembers();

  void unregisterMember(ITransactionMember member);

  boolean hasFailures();

  Throwable[] getFailures();

  void addFailure(Throwable t);

  /**
   * Two-phase commit
   * <p>
   * Temporary commits the transaction members
   * <p>
   * 
   * @return true without any exception iff the commit phase 1 was successful on all members.
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
   * release any resources allocated by the transaction members
   */
  void release();

}
