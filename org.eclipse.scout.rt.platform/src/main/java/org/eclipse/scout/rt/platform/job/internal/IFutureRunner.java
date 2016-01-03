package org.eclipse.scout.rt.platform.job.internal;

import org.eclipse.scout.rt.platform.job.IExecutionSemaphore;

/**
 * Runner to control execution of a {@link JobFutureTask}.
 *
 * @since 5.2
 */
public interface IFutureRunner extends IRejectableRunnable {

  /**
   * Asks if this runner is ready to accept an execution via {@link #run()}. If <code>false</code>, {@link #run()} must
   * not be invoked, and it is forbidden to change the job's state, or to acquire an execution permit.
   */
  boolean accept();

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
