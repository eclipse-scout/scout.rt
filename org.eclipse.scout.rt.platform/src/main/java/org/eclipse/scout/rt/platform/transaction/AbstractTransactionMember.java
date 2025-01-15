/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.platform.transaction;

/**
 * Use this base class when implementing custom transaction members.
 *
 * @since 3.8
 */
public abstract class AbstractTransactionMember implements ITransactionMember {
  private final String m_transactionMemberId;

  public AbstractTransactionMember(String transactionMemberId) {
    m_transactionMemberId = transactionMemberId;
  }

  @Override
  public String getMemberId() {
    return m_transactionMemberId;
  }

  @Override
  public boolean needsCommit() {
    return false;
  }

  @Override
  public boolean commitPhase1() {
    return true;
  }

  @Override
  public void commitPhase2() {
  }

  @Override
  public void rollback() {
  }

  @Override
  public void release() {
  }

  @Override
  public void cancel() {
  }
}
