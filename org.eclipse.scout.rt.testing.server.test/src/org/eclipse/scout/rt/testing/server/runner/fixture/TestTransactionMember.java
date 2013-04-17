/*******************************************************************************
 * Copyright (c) 2013 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.testing.server.runner.fixture;

import org.eclipse.scout.rt.server.transaction.AbstractTransactionMember;

/**
 * fixture implementation of {@link AbstractTransactionMember}
 */
public class TestTransactionMember extends AbstractTransactionMember {

  private int m_needsCommitMethodCallCount = 0;
  private int m_commitPhase1MethodCallCount = 0;
  private int m_commitPhase2MethodCallCount = 0;
  private int m_rollbackMethodCallCount = 0;
  private int m_releaseMethodCallCount = 0;

  /**
   * @param transactionMemberId
   */
  public TestTransactionMember(String transactionMemberId) {
    super(transactionMemberId);
  }

  @Override
  public final boolean needsCommit() {
    m_needsCommitMethodCallCount++;
    return execNeedsCommit();
  }

  protected boolean execNeedsCommit() {
    return true;
  }

  public int getNeedsCommitMethodCallCount() {
    return m_needsCommitMethodCallCount;
  }

  @Override
  public final boolean commitPhase1() {
    m_commitPhase1MethodCallCount++;
    return execCommitPhase1();
  }

  protected boolean execCommitPhase1() {
    return true;
  }

  public int getCommitPhase1MethodCallCount() {
    return m_commitPhase1MethodCallCount;
  }

  @Override
  public final void commitPhase2() {
    m_commitPhase2MethodCallCount++;
    execCommitPhase2();
  }

  protected void execCommitPhase2() {
  }

  public int getCommitPhase2MethodCallCount() {
    return m_commitPhase2MethodCallCount;
  }

  @Override
  public final void rollback() {
    m_rollbackMethodCallCount++;
    execRollback();
  }

  protected void execRollback() {
  }

  public int getRollbackMethodCallCount() {
    return m_rollbackMethodCallCount;
  }

  @Override
  public void release() {
    m_releaseMethodCallCount++;
    execRelease();
  }

  protected void execRelease() {
  }

  public int getReleaseMethodCallCount() {
    return m_releaseMethodCallCount;
  }
}
