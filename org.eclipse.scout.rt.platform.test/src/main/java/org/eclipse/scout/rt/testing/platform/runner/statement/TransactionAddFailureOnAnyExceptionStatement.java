/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.testing.platform.runner.statement;

import org.eclipse.scout.rt.platform.transaction.ITransaction;
import org.junit.runners.model.Statement;

/**
 * Statement that adds a failure to the current {@link ITransaction} if the next {@link Statement} throws any
 * {@link Throwable}.
 *
 * @since 6.0
 */
public class TransactionAddFailureOnAnyExceptionStatement extends Statement {

  private final Statement m_next;

  public TransactionAddFailureOnAnyExceptionStatement(Statement next) {
    m_next = next;
  }

  @Override
  public void evaluate() throws Throwable {
    try {
      m_next.evaluate();
    }
    catch (Throwable t) {
      final ITransaction txn = ITransaction.CURRENT.get();
      if (txn != null) {
        txn.addFailure(new IllegalStateException("cancel transaction marker"));
      }
      throw t;
    }
  }
}
