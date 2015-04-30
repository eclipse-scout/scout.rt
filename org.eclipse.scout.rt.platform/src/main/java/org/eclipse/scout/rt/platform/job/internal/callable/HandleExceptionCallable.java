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
package org.eclipse.scout.rt.platform.job.internal.callable;

import java.util.concurrent.Callable;

import org.eclipse.scout.commons.Assertions;
import org.eclipse.scout.commons.IChainable;
import org.eclipse.scout.commons.annotations.Internal;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.exception.ExceptionHandler;
import org.eclipse.scout.rt.platform.exception.ExceptionTranslator;
import org.eclipse.scout.rt.platform.job.JobInput;

/**
 * Processor to translate computing exceptions into {@link ProcessingException}s.
 *
 * @param <RESULT>
 *          the result type of the job's computation.
 * @since 5.1
 * @see <i>design pattern: chain of responsibility</i>
 */
public class HandleExceptionCallable<RESULT> implements Callable<RESULT>, IChainable<Callable<RESULT>> {

  @Internal
  protected final Callable<RESULT> m_next;
  @Internal
  protected final JobInput m_input;

  /**
   * Creates a processor to translate computing exceptions into {@link ProcessingException}s.
   *
   * @param next
   *          next processor in the chain; must not be <code>null</code>.
   * @param input
   *          input that describes the job.
   */
  public HandleExceptionCallable(final Callable<RESULT> next, final JobInput input) {
    m_input = input;
    m_next = Assertions.assertNotNull(next);
  }

  @Override
  public RESULT call() throws Exception {
    try {
      return m_next.call();
    }
    catch (final Throwable t) {
      // If logging is enabled for the current job, pass the exception to the ExceptionHandler. That is important if the job's result is not queried by the submitter, so that the exception is not swallowed silently.
      if (m_input.logOnError()) {
        try {
          BEANS.get(ExceptionHandler.class).handle(t);
        }
        catch (final Throwable unhandledThrowable) {
          // NOOP
        }
      }
      // Translate and propagate the exception.
      throw BEANS.get(ExceptionTranslator.class).translate(t);
    }
  }

  @Override
  public Callable<RESULT> getNext() {
    return m_next;
  }
}
