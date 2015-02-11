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

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.job.JobExecutionException;

/**
 * Utility to translate {@link Future} specific exceptions into {@link ProcessingException}s.
 *
 * @since 5.0
 */
public final class JobExceptionTranslator {

  private JobExceptionTranslator() {
  }

  /**
   * Translates the given {@link Exception} into a {@link ProcessingException}.
   */
  public static ProcessingException translate(final Exception e, final String jobName) {
    final ProcessingException pe;
    if (e instanceof ExecutionException) {
      final Throwable cause = e.getCause();
      pe = (cause instanceof ProcessingException ? (ProcessingException) cause : new ProcessingException(cause.getMessage(), cause));
    }
    else if (e instanceof CancellationException) {
      pe = new JobExecutionException(String.format("Failed to wait for the job to complete because it was canceled. [job=%s]", jobName), e);
    }
    else if (e instanceof InterruptedException) {
      pe = new JobExecutionException(String.format("Interrupted while waiting for the job to complete. [job=%s]", jobName), e);
    }
    else if (e instanceof ProcessingException) {
      pe = (ProcessingException) e;
    }
    else {
      pe = new ProcessingException(String.format("Unexpected error during job execution [job=%s]", jobName), e);
    }

    pe.addContextMessage("job=" + jobName);

    return pe;
  }

  /**
   * Translates the given {@link TimeoutException} into a {@link ProcessingException}.
   */
  public static ProcessingException translate(final TimeoutException e, final long timeout, final TimeUnit unit, final String jobName) {
    final JobExecutionException pe = new JobExecutionException(String.format("Failed to wait for the job to complete because it took longer than %sms [job=%s]", unit.toMillis(timeout), jobName), e);

    pe.addContextMessage("job=" + jobName);

    return pe;
  }
}
