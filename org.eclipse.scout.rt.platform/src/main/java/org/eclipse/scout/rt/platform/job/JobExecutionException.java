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
package org.eclipse.scout.rt.platform.job;

import java.util.concurrent.CancellationException;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.scout.commons.exception.ProcessingException;

/**
 * This exception describes a problem while executing a job and is thrown in one of the following cases:
 * <ul>
 * <li>when a job cannot be accepted for execution. This may occur when no more threads or queue slots are available, or
 * upon shutdown of the job manager;</li>
 * <li>when a job cannot be accepted for execution because already running;</li>
 * <li>when waiting for the job to complete longer than the specified timeout;</li>
 * <li>when waiting for the job to complete and the current thread was interrupted;</li>
 * </ul>
 *
 * @since 5.1
 */
public class JobExecutionException extends ProcessingException {

  private static final long serialVersionUID = 1L;

  public JobExecutionException(final String message) {
    super(null, message);
  }

  public JobExecutionException(final String message, final Throwable cause) {
    super(null, message, cause);
  }

  /**
   * @return <code>true</code> to indicate that the result of a job could not be retrieved because being cancelled.
   */
  public boolean isCancellation() {
    return getStatus() != null && (getStatus().getException() instanceof CancellationException);
  }

  /**
   * @return <code>true</code> to indicate that the result of a job could not be retrieved because a timeout occurred
   *         while waiting for that job to complete.
   */
  public boolean isTimeout() {
    return getStatus() != null && (getStatus().getException() instanceof TimeoutException);
  }

  /**
   * @return <code>true</code> to indicate that a job was rejected from being executed either because no more threads or
   *         queue slots are available, or upon shutdown of the job manager, or if the job is already running.
   */
  public boolean isRejection() {
    return getStatus() != null && (getStatus().getException() instanceof RejectedExecutionException);
  }

  /**
   * Creates a {@link JobExecutionException} from the given {@link InterruptedException}.
   */
  public static JobExecutionException fromInterruptedException(final InterruptedException e, final String job) {
    return new JobExecutionException(String.format("Interrupted while waiting for the job to complete. [job=%s]", job), e);
  }

  /**
   * Creates a {@link JobExecutionException} from the given {@link TimeoutException}.
   */
  public static JobExecutionException fromTimeoutException(final TimeoutException e, final long timeout, final TimeUnit unit, final String job) {
    return new JobExecutionException(String.format("Failed to wait for the job to complete because it took longer than %sms [job=%s]", unit.toMillis(timeout), job), e);
  }
}
