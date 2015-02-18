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
package org.eclipse.scout.commons.job.internal;

import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.scout.commons.Assertions;
import org.eclipse.scout.commons.annotations.Internal;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.job.IFuture;
import org.eclipse.scout.commons.job.JobExecutionException;
import org.eclipse.scout.commons.job.interceptor.ExceptionTranslator;

/**
 * Default implementation of {@link IFuture}.
 *
 * @see java.util.concurrent.Future
 * @since 5.1
 */
@Internal
public class Future<RESULT> implements IFuture<RESULT> {

  private final java.util.concurrent.Future<RESULT> m_delegate;
  private final String m_jobName;

  public Future(final java.util.concurrent.Future<RESULT> delegate, final String jobName) {
    m_delegate = Assertions.assertNotNull(delegate);
    m_jobName = Assertions.assertNotNull(jobName);
  }

  @Override
  public boolean cancel(final boolean mayInterruptIfRunning) {
    return m_delegate.cancel(mayInterruptIfRunning);
  }

  @Override
  public boolean isCancelled() {
    return m_delegate.isCancelled();
  }

  @Override
  public boolean isDone() {
    return m_delegate.isDone();
  }

  @Override
  public RESULT get() throws ProcessingException, JobExecutionException {
    try {
      return m_delegate.get();
    }
    catch (final ExecutionException e) {
      throw ExceptionTranslator.translate(e.getCause());
    }
    catch (final CancellationException e) {
      throw ExceptionTranslator.translateCancellationException(e, m_jobName);
    }
    catch (final InterruptedException e) {
      throw ExceptionTranslator.translateInterruptedException(e, m_jobName);
    }
    catch (final RuntimeException e) {
      throw ExceptionTranslator.translate(e);
    }
  }

  @Override
  public RESULT get(final long timeout, final TimeUnit unit) throws ProcessingException, JobExecutionException {
    try {
      return m_delegate.get(timeout, unit);
    }
    catch (final ExecutionException e) {
      throw ExceptionTranslator.translate(e.getCause());
    }
    catch (final CancellationException e) {
      throw ExceptionTranslator.translateCancellationException(e, m_jobName);
    }
    catch (final InterruptedException e) {
      throw ExceptionTranslator.translateInterruptedException(e, m_jobName);
    }
    catch (final TimeoutException e) {
      throw ExceptionTranslator.translateTimeoutException(e, timeout, unit, m_jobName);
    }
    catch (final RuntimeException e) {
      throw ExceptionTranslator.translate(e);
    }
  }

  @Override
  public int hashCode() {
    return m_delegate.hashCode(); // equality is based on the underlying Future.
  }

  @Override
  public boolean equals(final Object obj) {
    return m_delegate.equals(obj); // equality is based on the underlying Future.
  }
}
