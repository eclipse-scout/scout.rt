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

import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.platform.context.ICancellable;

/**
 * Represents a {@link Future} to interact with the associated job or to wait for the job to complete and to query it's
 * computation result. Exceptions thrown during the job's execution are propagated in the form of a
 * {@link ProcessingException}, technical exceptions like timeout or interruption in the form of a {@link JobException}.
 *
 * @see Future
 * @since 5.1
 */
public interface IFuture<RESULT> extends ICancellable {

  /**
   * The {@link IFuture} which is currently associated with the current thread.
   */
  ThreadLocal<IFuture<?>> CURRENT = new ThreadLocal<>();

  /**
   * @return {@link JobInput} the job was instrumented with.
   */
  JobInput getJobInput();

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
  @Override
  boolean cancel(boolean interruptIfRunning);

  /**
   * @return <code>true</code> if the associated job was cancelled before it completed normally.
   */
  @Override
  boolean isCancelled();

  /**
   * @return <code>true</code> if the associated job completed either normally, by an exception or was canceled.
   */
  boolean isDone();

  /**
   * @return <code>true</code> if the associated job is waiting for a blocking condition to fall.
   * @see IBlockingCondition
   */
  boolean isBlocked();

  /**
   * @return <code>true</code> if this is a periodic job, or <code>false</code> if executed only once.
   */
  boolean isPeriodic();

  /**
   * Blocks the calling thread until the job gets cancelled or completed to return its execution result. This call
   * returns immediately if the job is not running anymore or was cancelled. Use {@link #isCancelled()} to query the
   * job's cancellation status.
   *
   * @return the computed result.
   * @throws ProcessingException
   *           is thrown if an exception is thrown during the job's execution.
   * @throws JobException
   *           is thrown if this thread was interrupted while waiting for the job to complete; see
   *           {@link JobException#isInterruption()}
   */
  RESULT awaitDoneAndGet() throws ProcessingException;

  /**
   * Blocks the calling thread until the job gets cancelled or completed to return its execution result. This call
   * returns immediately if the job is not running anymore or was cancelled. Use {@link #isCancelled()} to query the
   * job's cancellation status.
   *
   * @param timeout
   *          the maximal time to wait for the job to complete.
   * @param unit
   *          unit of the timeout.
   * @return the computed result.
   * @throws ProcessingException
   *           is thrown if an exception is thrown during the job's execution.
   * @throws JobException
   *           is thrown in the following cases:
   *           <ul>
   *           <li>this thread was interrupted while waiting for the job to complete;<br/>
   *           see {@link JobException#isInterruption()};</li>
   *           <li>the job did not return within the timeout specified;<br/>
   *           see {@link JobException#isTimeout()};</li>
   *           </ul>
   */
  RESULT awaitDoneAndGet(long timeout, TimeUnit unit) throws ProcessingException;

  /**
   * Registers the given <code>callback</code> to be notified once the Future enters 'done' state. That is once the
   * associated job completes successfully or with an exception, or was cancelled. Thereby, the callback is invoked in
   * any thread with no {@code RunContext} set. If the job is already in 'done' state when the callback is registered,
   * the callback is invoked immediately.
   * <p/>
   * The following code snippet illustrates its usage:
   *
   * <pre>
   * <code>
   * Jobs.schedule(new IRunnable() {
   * 
   *   &#064;Override
   *   public void run() throws Exception {
   *     // do some work
   *   }
   * })<strong>
   * .whenDone(new IDoneCallback&lt;Void&gt;() {
   * 
   *   &#064;Override
   *   public void onDone(DoneEvent&lt;Void&gt; event) {
   *     // invoked once the job completes
   *   }
   * })</strong>;
   * </code>
   * </pre>
   */
  void whenDone(IDoneCallback<RESULT> callback);
}
