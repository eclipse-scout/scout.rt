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

import org.eclipse.scout.rt.platform.util.Assertions;
import org.eclipse.scout.rt.platform.util.concurrent.ThreadInterruption;
import org.eclipse.scout.rt.platform.util.concurrent.ThreadInterruption.IRestorer;
import org.junit.runners.model.Statement;

/**
 * Clears the current thread's interrupted status during test execution.
 *
 * @since 6.1
 */
public class ClearThreadInterruptionStatusStatement extends Statement {

  private final Statement m_next;

  public ClearThreadInterruptionStatusStatement(final Statement next) {
    m_next = Assertions.assertNotNull(next, "next statement must not be null");
  }

  @Override
  public void evaluate() throws Throwable {
    final IRestorer interruption = ThreadInterruption.clear();
    try {
      m_next.evaluate();
    }
    finally {
      interruption.restore();
    }
  }
}
