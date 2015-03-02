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

import java.util.concurrent.TimeUnit;

/**
 * Scheduler to execute jobs periodically.
 *
 * @since 5.1
 */
public interface IScheduler<INPUT extends IJobInput> {

  /**
   * Periodically runs the given job on behalf of a worker thread.<br/>
   * The first execution is after the given <code>initialDelay</code>, the second after <code>initialDelay+period</code>
   * , the third after <code>initialDelay+period+period</code> and so on. If an execution takes longer than the
   * <code>period</code>, the subsequent execution is delayed and starts only once the current execution completed. If
   * any execution throws an exception, subsequent executions are suppressed. Otherwise, the job only terminates via
   * cancellation or termination of the job manager.
   *
   * @param runnable
   *          the runnable to be executed periodically.
   * @param initialDelay
   *          the time to delay first run.
   * @param period
   *          the period between successive runs.
   * @param unit
   *          the time unit of the <code>initialDelay</code> and <code>period</code> arguments.
   * @param input
   *          gives the runnable a semantic meaning and contains instructions about its execution.
   * @return {@link IFuture} to cancel the periodic action.
   * @throws JobExecutionException
   *           if the job is rejected by the job manager because no more threads or queue slots are available, or upon
   *           shutdown of the job manager.
   */
  IFuture<Void> scheduleAtFixedRate(IRunnable runnable, long initialDelay, long period, TimeUnit unit, INPUT input) throws JobExecutionException;

  /**
   * Periodically runs the given job on behalf of a worker thread.<br/>
   * The first execution is after the given <code>initialDelay</code>, and subsequently with the given
   * <code>delay</code> between the termination of one execution and the commencement of the next. If any execution
   * throws an exception, subsequent executions are suppressed. Otherwise, the job will only terminate
   * via cancellation or termination of the the job manager.
   *
   * @param runnable
   *          the runnable to be executed periodically.
   * @param initialDelay
   *          the time to delay first run.
   * @param delay
   *          the fixed delay between successive runs.
   * @param unit
   *          the time unit of the <code>initialDelay</code> and <code>period</code> arguments.
   * @param input
   *          gives the runnable a semantic meaning and contains instructions about its execution.
   * @return {@link IFuture} to cancel the periodic action.
   * @throws JobExecutionException
   *           if the job is rejected by the job manager because no more threads or queue slots are available, or upon
   *           shutdown of the job manager.
   */
  IFuture<Void> scheduleWithFixedDelay(IRunnable runnable, long initialDelay, long delay, TimeUnit unit, INPUT input) throws JobExecutionException;
}
