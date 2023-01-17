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

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.exception.ExceptionHandler;
import org.eclipse.scout.rt.testing.platform.runner.JUnitExceptionHandler;
import org.junit.runners.model.Statement;

/**
 * Statement to re-throw the first exception handled by {@link JUnitExceptionHandler}.
 *
 * @see JUnitExceptionHandler
 */
public class ThrowHandledExceptionStatement extends Statement {

  private final Statement m_next;

  public ThrowHandledExceptionStatement(final Statement next) {
    m_next = next;
  }

  @Override
  public void evaluate() throws Throwable {
    m_next.evaluate();

    // Re-throw the first handled exception if 'JUnitExceptionHandler' is installed as exception handler.
    final ExceptionHandler exceptionHandler = BEANS.get(ExceptionHandler.class);
    if (exceptionHandler instanceof JUnitExceptionHandler) {
      ((JUnitExceptionHandler) exceptionHandler).throwOnError();
    }
  }
}
