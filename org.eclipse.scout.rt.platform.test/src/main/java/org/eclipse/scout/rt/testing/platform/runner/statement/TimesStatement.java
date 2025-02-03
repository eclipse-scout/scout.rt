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
import org.eclipse.scout.rt.testing.platform.runner.Times;
import org.junit.runners.model.Statement;

/**
 * Statement to execute a test multiple times.
 *
 * @see Times
 * @since 5.1
 */
public class TimesStatement extends Statement {

  private final Statement m_next;
  private final long m_times;

  /**
   * Creates a statement to execute a test multiple times.
   *
   * @param next
   *     next {@link Statement} to be executed.
   * @param annotation
   *     {@link Times}-annotation to read the number of executions.
   */
  public TimesStatement(final Statement next, final Times annotation) {
    m_next = Assertions.assertNotNull(next, "next statement must not be null");
    m_times = (annotation != null ? annotation.value() : 1);
  }

  @Override
  public void evaluate() throws Throwable {
    for (int i = 0; i < m_times; i++) {
      m_next.evaluate();
    }
  }
}
