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

import org.eclipse.scout.rt.platform.annotations.Internal;
import org.eclipse.scout.rt.server.transaction.ITransaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of the atomic commitment protocol <code>2-phase-commit-protocol (2PC)</code> according to the XA
 * specification (eXtended Architecture).
 * <p>
 * That protocol commits the transaction consistently over all involved transaction members like relational databases,
 * message queues, webservice consumers and so on.
 *
 * @since 5.1
 */
public class TwoPhaseTransactionCommitProtocol implements ITransactionCommitProtocol {

  private static final Logger LOG = LoggerFactory.getLogger(TwoPhaseTransactionCommitProtocol.class);

  @Override
  public void commitOrRollback(final ITransaction tx) {
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
      LOG.error("Failed to commit XA transaction [2PC-phase='voting', tx={}]", tx, e);
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
      LOG.error("Failed to commit XA transaction [2PC-phase='commit', tx={}]", tx, e);
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
      LOG.error("Failed to rollback XA transaction [tx={}]", tx, e);
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
      LOG.error("Failed to release XA transaction members [tx={}]", tx, e);
      return false;
    }
  }
}
