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
package org.eclipse.scout.commons.job.interceptor;

import java.util.concurrent.Callable;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.job.IAsyncFuture;
import org.eclipse.scout.commons.job.IJob;
import org.eclipse.scout.commons.job.internal.JobExceptionTranslator;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;

/**
 * Processor to notify {@link IAsyncFuture} upon completion.
 * <p/>
 * This {@link Callable} is a processing object in the language of the design pattern 'chain-of-responsibility'.
 *
 * @param <R>
 *          the result type of the job's computation.
 * @since 5.0
 */
public class AsyncFutureNotifier<R> implements Callable<R> {

  protected static final IScoutLogger LOG = ScoutLogManager.getLogger(AsyncFutureNotifier.class);

  protected final Callable<R> m_next;
  protected final IAsyncFuture<R> m_asyncFuture;

  public AsyncFutureNotifier(final Callable<R> next, final IAsyncFuture<R> asyncFuture) {
    m_next = next;
    m_asyncFuture = asyncFuture;
  }

  @Override
  public R call() throws Exception {
    if (m_asyncFuture == null) {
      return m_next.call();
    }
    else {
      R result = null;
      ProcessingException error = null;
      try {
        result = m_next.call();
        handleSuccessSafe(result);
        return result;
      }
      catch (final Exception e) {
        error = JobExceptionTranslator.translate(e, IJob.CURRENT.get().getName());
        handleErrorSafe(error);
        throw error;
      }
      finally {
        handleDoneSafe(result, error);
      }
    }
  }

  /**
   * Is called to notify the {@link IAsyncFuture} about successful execution; must never throw an exception.
   */
  protected void handleSuccessSafe(final R result) {
    try {
      m_asyncFuture.onSuccess(result);
    }
    catch (final RuntimeException unexpected) {
      LOG.error("Unhandled exception while handling a job's 'SUCCESS' state.", unexpected);
    }
  }

  /**
   * Is called to notify the {@link IAsyncFuture} about failed execution; must never throw an exception.
   */
  protected void handleErrorSafe(final ProcessingException e) {
    try {
      m_asyncFuture.onError(e);
    }
    catch (final RuntimeException unexpected) {
      LOG.error("Unhandled exception while handling a job's 'ERROR' state.", unexpected);
    }
  }

  /**
   * Is called to notify the {@link IAsyncFuture} in case of success or error; must never throw an exception.
   */
  protected void handleDoneSafe(final R result, final ProcessingException e) {
    try {
      m_asyncFuture.onDone(result, e);
    }
    catch (final RuntimeException unexpected) {
      LOG.error("Unhandled exception while handling a job's 'DONE' state.", unexpected);
    }
  }
}
