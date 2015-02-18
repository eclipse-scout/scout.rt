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
package org.eclipse.scout.rt.client.job;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.job.IAsyncFuture;
import org.eclipse.scout.commons.job.IFuture;
import org.eclipse.scout.commons.job.IJob;
import org.eclipse.scout.commons.job.JobExecutionException;

/**
 * Job to interact with the client model.
 *
 * @see ModelJobWithResult
 * @see ModelJob
 * @see ModelJobManager
 * @since 5.1
 */
public interface IModelJob<RESULT> extends IJob<RESULT> {

  /**
   * Runs this job synchronously on behalf of the current model-thread. This call blocks the caller as long as this job
   * is running.
   * <p/>
   * <strong>The calling thread must be the model-thread himself.</strong>
   *
   * @return the computed result.
   * @throws ProcessingException
   *           if the job throws an exception during execution.
   * @throws JobExecutionException
   *           if the job is already running or not called on behalf of the model-thread.
   */
  @Override
  RESULT runNow() throws ProcessingException, JobExecutionException;

  /**
   * Runs this job asynchronously on behalf of the model-thread at the next reasonable opportunity. The caller of this
   * method continues to run in parallel.
   * <p/>
   * If the given job is rejected by the job manager the time being scheduled, the job is <code>cancelled</code>. This
   * occurs if no more threads or queue slots are available, or upon shutdown of the job manager.
   * <p/>
   * <strong>Do not wait for this job to complete if being a model-job yourself as this would cause a deadlock.</strong>
   *
   * @return {@link IFuture} to wait for the job's completion or to cancel the job execution.
   * @throws JobExecutionException
   *           if the job is already running.
   * @see #schedule(IAsyncFuture)
   */
  @Override
  IFuture<RESULT> schedule() throws JobExecutionException;

  /**
   * Runs this job asynchronously on behalf of the model-thread at the next reasonable opportunity. The caller of this
   * method continues to run in parallel.
   * <p/>
   * If the given job is rejected by the job manager the time being scheduled, the job is <code>cancelled</code>. This
   * occurs if no more threads or queue slots are available, or upon shutdown of the job manager.
   * <p/>
   * The given {@link IAsyncFuture} is called once the job completes successfully or terminates with an exception. The
   * {@link IFuture} returned allows to cancel the execution of this job or to also wait for the job to complete.
   *
   * @param asyncFuture
   *          {@link IAsyncFuture} to be notified about the job's completion or failure; is notified from from within
   *          the model-thread that executed the job; is not called if the job never started running.
   * @throws JobExecutionException
   *           if the job is already running.
   * @see #schedule()
   */
  @Override
  IFuture<RESULT> schedule(IAsyncFuture<RESULT> asyncFuture) throws JobExecutionException;

  /**
   * @return <code>true</code> if this job is blocked because waiting for a {@link IBlockingCondition} to fall.
   * @see IBlockingCondition
   */
  boolean isBlocked();
}
