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
package org.eclipse.scout.rt.platform.job.internal;

import java.util.concurrent.Callable;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.chain.callable.CallableChain;
import org.eclipse.scout.rt.platform.chain.callable.CallableChain.Chain;
import org.eclipse.scout.rt.platform.chain.callable.ICallableInterceptor;
import org.eclipse.scout.rt.platform.exception.DefaultExceptionTranslator;
import org.eclipse.scout.rt.platform.job.JobInput;

/**
 * Processor to control exception handling of uncaught exceptions during job execution.
 * <p>
 * Instances of this class are to be added to a {@link CallableChain} to participate in the execution of a
 * {@link Callable}.
 *
 * @since 5.1
 */
public class ExceptionProcessor<RESULT> implements ICallableInterceptor<RESULT> {

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
        m_input.getExceptionHandler().handle(BEANS.get(DefaultExceptionTranslator.class).unwrap(e));
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
