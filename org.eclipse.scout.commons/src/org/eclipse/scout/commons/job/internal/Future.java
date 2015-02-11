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

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.scout.commons.Assertions;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.job.IFuture;
import org.eclipse.scout.commons.job.JobExecutionException;

/**
 * Default implementation of {@link IFuture}.
 *
 * @see java.util.concurrent.Future
 * @since 5.0
 */
public class Future<R> implements IFuture<R> {

  private final java.util.concurrent.Future<R> m_delegate;
  private final String m_jobName;

  public Future(final java.util.concurrent.Future<R> delegate, final String jobName) {
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
  public R get() throws ProcessingException, JobExecutionException {
    try {
      return m_delegate.get();
    }
    catch (ExecutionException | InterruptedException | RuntimeException e) {
      throw JobExceptionTranslator.translate(e, m_jobName);
    }
  }

  @Override
  public R get(final long timeout, final TimeUnit unit) throws ProcessingException, JobExecutionException {
    try {
      return m_delegate.get(timeout, unit);
    }
    catch (final TimeoutException e) {
      throw JobExceptionTranslator.translate(e, timeout, unit, m_jobName);
    }
    catch (ExecutionException | InterruptedException | RuntimeException e) {
      throw JobExceptionTranslator.translate(e, m_jobName);
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
