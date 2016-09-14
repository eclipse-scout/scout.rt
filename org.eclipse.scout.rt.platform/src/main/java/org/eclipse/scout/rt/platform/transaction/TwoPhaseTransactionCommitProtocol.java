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
package org.eclipse.scout.rt.platform.transaction;

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
@SuppressWarnings("squid:S1181")
public class TwoPhaseTransactionCommitProtocol implements ITransactionCommitProtocol {

  private static final Logger LOG = LoggerFactory.getLogger(TwoPhaseTransactionCommitProtocol.class);

  @Override
  public void commitOrRollback(final ITransaction tx) {
    try {
      // Immediately rollback the TX in case of failures.
      if (tx.hasFailures()) {
        rollback(tx);
        return;
      }

      // Commit the XA transaction in 2PC-style.
      // Phase 1: Vote for final commit.
      try {
        if (!commitPhase1(tx)) {
          rollback(tx);
          return;
        }
      }
      catch (RuntimeException | Error e) {
        LOG.error("Unexpected error during commit of XA transaction [2PC-phase='voting', tx={}]", tx, e);
        tx.addFailure(e);
        rollback(tx);
        throw e;
      }

      // Phase 2: Commit the transaction.
      try {
        commitPhase2(tx);
      }
      catch (RuntimeException | Error e) {
        LOG.error("Unexpected error during commit of XA transaction [2PC-phase='commit', tx={}]", tx, e);
        tx.addFailure(e);
        rollback(tx);
        throw e;
      }
    }
    finally {
      release(tx);
    }
  }

  /**
   * @return <code>true</code> on success, or <code>false</code> on failure.
   * @throws RuntimeException
   *           if an error occurs during commit phase 1.
   * @see ITransaction#commitPhase1()
   */
  protected boolean commitPhase1(final ITransaction tx) {
    return tx.commitPhase1();
  }

  /**
   * @see ITransaction#commitPhase2()
   * @throws RuntimeException
   *           if an error occurs during commit phase 2.
   */
  protected void commitPhase2(final ITransaction tx) {
    tx.commitPhase2();
  }

  /**
   * @see ITransaction#rollback()
   */
  protected void rollback(final ITransaction tx) {
    tx.rollback();
  }

  /**
   * @see ITransaction#release()
   */
  protected void release(final ITransaction tx) {
    tx.release();
  }
}
