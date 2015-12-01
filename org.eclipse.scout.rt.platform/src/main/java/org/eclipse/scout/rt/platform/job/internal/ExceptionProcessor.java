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
package org.eclipse.scout.rt.platform.job.internal;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.chain.IInvocationInterceptor;
import org.eclipse.scout.rt.platform.chain.InvocationChain.Chain;
import org.eclipse.scout.rt.platform.exception.ExceptionTranslator;
import org.eclipse.scout.rt.platform.job.JobInput;

/**
 * Processor to control exception handling of uncaught exceptions during job execution.
 *
 * @since 5.1
 */
public class ExceptionProcessor<RESULT> implements IInvocationInterceptor<RESULT> {

  protected final JobInput m_input;

  public ExceptionProcessor(final JobInput input) {
    m_input = input;
  }

  @Override
  public RESULT intercept(final Chain<RESULT> chain) throws Exception {
    try {
      return chain.continueChain();
    }
    catch (final Exception | Error e) {
      if (m_input.getExceptionHandler() != null) {
        BEANS.get(m_input.getExceptionHandler()).handle(BEANS.get(ExceptionTranslator.class).translate(e));
      }

      if (!m_input.isSwallowException()) {
        throw e;
      }

      return null;
    }
  }

  @Override
  public boolean isEnabled() {
    return m_input.getExceptionHandler() != null || m_input.isSwallowException();
  }
}
