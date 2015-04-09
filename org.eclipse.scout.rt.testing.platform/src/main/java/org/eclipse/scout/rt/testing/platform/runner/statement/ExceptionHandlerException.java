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
package org.eclipse.scout.rt.testing.platform.runner.statement;

import org.eclipse.scout.commons.exception.ProcessingException;

/**
 * {@code RuntimeException} thrown by {@code IExceptionHandler} installed in JUnit tests to not silently swallow
 * exceptions. This exception's cause is the swallowed {@code ProcessingException}, which is never <code>null</code>.
 */
public class ExceptionHandlerException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  public ExceptionHandlerException(final ProcessingException cause) {
    super(cause);
  }

  /**
   * Swallowed {@code ProcessingException}; is never <code>null</code>.
   */
  @Override
  public synchronized ProcessingException getCause() {
    return (ProcessingException) super.getCause();
  }
}
