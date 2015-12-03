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

/**
 * @since 3.4
 */
public interface ITransactionMember {

  String getMemberId();

  /**
   * the transaction member needs commit
   */
  boolean needsCommit();

  /**
   * Two-phase commit. Temporary commits the transaction member
   * <p>
   * 
   * @return true without any exception if the commit phase 1 was successful.
   *         <p>
   *         Subsequently there will be a call to {@link #commitPhase2()} or {@link #rollback()}
   */
  boolean commitPhase1();

  /**
   * commit phase 2 of the transaction member (commit phase 1 confirmation)
   */
  void commitPhase2();

  /**
   * rollback on the transaction member (commit phase 1 cancel and rollback)
   */
  void rollback();

  /**
   * release any resources allocated by the transaction member
   */
  void release();

  /**
   * When a transaction is canceled, it calls this method on all its members
   */
  void cancel();
}
