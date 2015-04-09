/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.testing.platform.runner.statement;

import org.junit.runners.model.Statement;

/**
 * Statement to re-throw a potential {@link ExceptionHandlerException}'s cause. That is an exception handled by the
 * {@code IExceptionHandler} installed in JUnit tests.
 *
 * @see ExceptionHandlerException
 * @see {@code IExceptionHandler} used in JUnit tests
 */
public class ThrowExceptionHandlerCauseStatement extends Statement {

  private final Statement m_delegate;

  public ThrowExceptionHandlerCauseStatement(final Statement delegate) {
    m_delegate = delegate;
  }

  @Override
  public void evaluate() throws Throwable {
    try {
      m_delegate.evaluate();
    }
    catch (final ExceptionHandlerException e) {
      throw e.getCause();
    }
  }
}
