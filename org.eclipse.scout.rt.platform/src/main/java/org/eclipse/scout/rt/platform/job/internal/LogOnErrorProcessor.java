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

import java.util.concurrent.Callable;

import org.eclipse.scout.commons.chain.IInvocationInterceptor;
import org.eclipse.scout.commons.chain.InvocationChain;
import org.eclipse.scout.commons.chain.InvocationChain.Chain;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.exception.ExceptionHandler;
import org.eclipse.scout.rt.platform.exception.ExceptionTranslator;
import org.eclipse.scout.rt.platform.job.JobInput;

/**
 * Processor to log uncaught exceptions during job execution.
 * <p>
 * Instances of this class are to be added to a {@link InvocationChain} to participate in the execution of a
 * {@link Callable}.
 *
 * @since 5.1
 */
public class LogOnErrorProcessor<RESULT> implements IInvocationInterceptor<RESULT> {

  protected final JobInput m_input;

  public LogOnErrorProcessor(final JobInput input) {
    m_input = input;
  }

  @Override
  public RESULT intercept(final Chain<RESULT> chain) throws Exception {
    try {
      return chain.continueChain();
    }
    catch (final Throwable t) {
      try {
        BEANS.get(ExceptionHandler.class).handle(t);
      }
      catch (final Throwable unhandledThrowable) {
        // NOOP: ExceptionHandler is not expected to throw an Exception; However, this catch-block is for safety purpose.
      }

      // Propagate the exception.
      throw BEANS.get(ExceptionTranslator.class).translate(t);
    }
  }

  @Override
  public boolean isEnabled() {
    return m_input.isLogOnError();
  }
}
