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
package org.eclipse.scout.rt.server.job.interceptor;

import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.job.IJob;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.server.transaction.ITransaction;

/**
 * Delegator for {@link ITransaction} that forwards to the respective {@link ITransaction} methods without propagating
 * exceptions. Instead, a <code>boolean</code> return value informs about the underlying operation's success.
 * 
 * @since 5.1
 */
class TransactionSafeDelegator {

  private static final IScoutLogger LOG = ScoutLogManager.getLogger(TransactionSafeDelegator.class);

  private final ITransaction m_delegate;

  /**
   * Creates a delegator for {@link ITransaction} that forwards to the respective {@link ITransaction} methods without
   * propagating exceptions.
   *
   * @param delegate
   *          the {@link ITransaction} to delegate to.
   */
  public TransactionSafeDelegator(final ITransaction delegate) {
    m_delegate = delegate;
  }

  /**
   * @return <code>true</code> on success or <code>false</code> on failure.
   * @see ITransaction#commitPhase1()
   */
  public boolean commitPhase1() {
    try {
      return m_delegate.commitPhase1();
    }
    catch (ProcessingException | RuntimeException e) {
      LOG.error(String.format("Failed to commit XA transaction [2PC-phase='voting', reason=%s, job=%s, tx=%s]", toReason(e), IJob.CURRENT.get().getName(), m_delegate), e);
      return false;
    }
  }

  /**
   * @return <code>true</code> on success or <code>false</code> on failure.
   * @see ITransaction#commitPhase2()
   */
  public boolean commitPhase2() {
    try {
      m_delegate.commitPhase2();
      return true;
    }
    catch (final RuntimeException e) {
      LOG.error(String.format("Failed to commit XA transaction [2PC-phase='commit', reason=%s, job=%s, tx=%s]", toReason(e), IJob.CURRENT.get().getName(), m_delegate), e);
      return false;
    }
  }

  /**
   * @return <code>true</code> on success or <code>false</code> on failure.
   * @see ITransaction#rollback()
   */
  public boolean rollback() {
    try {
      m_delegate.rollback();
      return true;
    }
    catch (final RuntimeException e) {
      LOG.error(String.format("Failed to rollback XA transaction [reason=%s, job=%s, tx=%s]", toReason(e), IJob.CURRENT.get().getName(), m_delegate), e);
      return false;
    }
  }

  /**
   * @return <code>true</code> on success or <code>false</code> on failure.
   * @see ITransaction#release()
   */
  public boolean release() {
    try {
      m_delegate.release();
      return true;
    }
    catch (final RuntimeException e) {
      LOG.error(String.format("Failed to release XA transaction members [reason=%s, job=%s, tx=%s]", toReason(e), IJob.CURRENT.get().getName(), m_delegate), e);
      return false;
    }
  }

  /**
   * @return the exceptions message; is not <code>null</code>.
   */
  private String toReason(final Exception e) {
    return StringUtility.hasText(e.getMessage()) ? e.getMessage() : "n/a";
  }
}
