package org.eclipse.scout.rt.client.job;

import org.eclipse.scout.commons.job.JobExecutionException;

/**
 * Use this object to put the current thread (must be the model thread) into waiting mode until this condition falls. If
 * getting blocked, the threads's model-mutex is released and passed to another competing and prospective model-thread.<br/>
 * This condition can be used across multiple model-threads to wait for the same condition; this condition is reusable
 * upon invalidation.
 * <p/>
 * The intrinsic lock used is provided by the {@link IBlockingCondition} instance itself and can be used for
 * synchronization outside the blocking condition.
 *
 * @since 5.1
 */
public interface IBlockingCondition {

  /**
   * @return <code>true</code> if this condition blocks if calling {@link #waitFor()}.
   */
  boolean isBlocking();

  /**
   * Invoke this method to change the blocking-state of this blocking condition. This method can be invoked from any
   * thread.
   * <p/>
   * If <code>true</code>, this condition will block subsequent calls on {@link #waitFor()}. If <code>false</code>, the
   * condition is invalidated, meaning that the blocking-state is set to <code>false</code> and any threads waiting for
   * this condition to fall are released.
   *
   * @param blocking
   *          <code>true</code> to arm this condition, or <code>false</code> to invalidate it and release all waiting
   *          threads.
   */
  void setBlocking(boolean blocking);

  /**
   * If the blocking-state of this condition is <code>true</code>, the calling thread is blocked until the
   * blocking-state is changed to <code>false</code>, or if the thread is {@link Thread#interrupt() interrupted}.<br/>
   * If being blocked, the model-mutex is released and passed to another competing and prospective model-thread.
   * Thereby, the current thread becomes disabled for thread scheduling purposes and lies dormant.
   * <p/>
   * <strong>Precondition: The calling thread must be the model-thread.</strong> <br/>
   *
   * @throws AssertionException
   *           is thrown if the current thread is not the model-thread.
   * @throws JobExecutionException
   *           is thrown if the current thread is interrupted while waiting for the blocking condition to fall, or if
   *           the current thread fails to re-acquire the model-mutex upon unblocking. However, the current
   *           thread is not synchronized with the model-mutex anymore and should terminate its work.</li>
   */
  void waitFor() throws JobExecutionException;
}
