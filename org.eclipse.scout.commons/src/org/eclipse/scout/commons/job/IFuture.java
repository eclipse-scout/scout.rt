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
package org.eclipse.scout.commons.job;

import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.scout.commons.exception.ProcessingException;

/**
 * Represents a {@link java.util.concurrent.Future} and translates exceptions that might occur while waiting for a job
 * to complete into {@link JobExecutionException}s.
 *
 * @since 5.0
 * @see java.util.concurrent.Future
 * @see CancellationException
 * @see InterruptedException
 * @see TimeoutException
 * @see ExecutionException
 */
public interface IFuture<R> {

  /**
   * Attempts to cancel the execution of the associated job. This attempt will be ignored if the job has already
   * completed or was cancelled. If not running yet, the job will never run. If the job has already started, then the
   * <code>interruptIfRunning</code> parameter determines whether the thread executing this job should be interrupted in
   * an attempt to stop the job.
   *
   * @param interruptIfRunning
   *          <code>true</code> if the thread executing this job should be interrupted; otherwise, in-progress jobs are
   *          allowed to complete.
   * @return <code>false</code> if the job could not be cancelled, typically because it has already completed normally.
   */
  boolean cancel(boolean interruptIfRunning);

  /**
   * @return <code>true</code> if the associated job was cancelled before it completed normally.
   */
  boolean isCancelled();

  /**
   * @return <code>true</code> if the associated job completed either normally, by an exception or was canceled.
   */
  boolean isDone();

  /**
   * Blocks the calling thread until the job completed to return its execution result. This call returns immediately if
   * the job is not running anymore due to normal completion.
   *
   * @return the computed result.
   * @throws ProcessingException
   *           is thrown if an exception is thrown during the job's execution.
   * @throws JobExecutionException
   *           is thrown in the following cases:
   *           <ul>
   *           <li>the job was canceled or rejected;<br/>
   *           see {@link JobExecutionException#isCancellation()};</li>
   *           <li>this thread was interrupted while waiting for the job to complete;<br/>
   *           see {@link JobExecutionException#isInterruption()};</li>
   *           </ul>
   */
  R get() throws ProcessingException, JobExecutionException;

  /**
   * Blocks the calling thread until the job completed to return its execution result. This call returns immediately if
   * the job is not running anymore due to normal completion.
   *
   * @param timeout
   *          the maximal time to block the current thread to wait for the job to complete.
   * @param unit
   *          {@link TimeUnit} to describe the <code>timeout</code>.
   * @return the computed result.
   * @throws ProcessingException
   *           is thrown if an exception is thrown during the job's execution.
   * @throws JobExecutionException
   *           is thrown in the following cases:
   *           <ul>
   *           <li>the job was canceled or rejected;<br/>
   *           see {@link JobExecutionException#isCancellation()};</li>
   *           <li>this thread was interrupted while waiting for the job to complete;<br/>
   *           see {@link JobExecutionException#isInterruption()};</li>
   *           <li>the job did not return within the timeout specified;<br/>
   *           see {@link JobExecutionException#isTimeout()};</li>
   *           </ul>
   */
  R get(long timeout, TimeUnit unit) throws ProcessingException, JobExecutionException;
}
