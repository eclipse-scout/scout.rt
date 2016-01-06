package org.eclipse.scout.rt.platform.job.internal;

import org.eclipse.scout.rt.platform.job.IExecutionSemaphore;

/**
 * Runner to control execution of a {@link JobFutureTask}.
 *
 * @since 5.2
 */
public interface IFutureRunner extends IRejectableRunnable {

  /**
   * Invoke in the Quartz worker thread before starting execution. If this method returns with <code>true</code>, the
   * caller must either invoke {@link #run()} or {@link #reject()}. If returning <code>false</code>, {@link #run()} must
   * not be invoked, and it is forbidden to change the job's state, or to acquire an execution permit.
   */
  boolean beforeExecute();

  /**
   * Invoke to run the associated {@link JobFutureTask}.
   * <p>
   * <em>The following invariants must apply:</em>
   * <ul>
   * <li>{@link #accept()} must have returned with <code>true</code>;</li>
   * <li>if the job is assigned to an {@link IExecutionSemaphore}, it must be a permit owner;</li>
   * </ul>
   */
  @Override
  void run();

  /**
   * Returns the associated Future.
   */
  JobFutureTask<?> getFutureTask();
}
