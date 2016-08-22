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
