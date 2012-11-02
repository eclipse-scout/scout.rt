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
package org.eclipse.scout.rt.testing.shared.services.common.exceptionhandler;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.junit.runners.model.Statement;

/**
 * JUnit Statement that unpacks a wrapped {@link ProcessingException}.
 * 
 * @see WrappingProcessingRuntimeExceptionHandlerService
 */
public class ProcessingRuntimeExceptionUnwrappingStatement extends Statement {

  private final Statement m_delegate;

  public ProcessingRuntimeExceptionUnwrappingStatement(Statement delegate) {
    m_delegate = delegate;
  }

  @Override
  public void evaluate() throws Throwable {
    try {
      m_delegate.evaluate();
    }
    catch (WrappedProcessingRuntimeException e) {
      throw e.getCause();
    }
  }
}
