package org.eclipse.scout.rt.platform.job;

/**
 * Use this object to put the current thread into waiting mode until this condition falls. If getting blocked and the
 * job is configured for mutual exclusion, the job's mutex is released and passed to the next competing job.<br/>
 * This condition can be used across multiple threads to wait for the same condition; this condition is reusable upon
 * invalidation.
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
   * blocking-state is changed to <code>false</code>, or if the thread is {@link Thread#interrupt() interrupted}.
   * Thereby, the current thread becomes disabled for thread scheduling purposes and lies dormant.
   *
   * @throws JobExecutionException
   *           is thrown if the current thread is interrupted while waiting for the blocking condition to fall, or if
   *           being a mutex job and the mutex cannot be acquired anew. However, the current thread is not synchronized
   *           with the mutex anymore and should terminate its work.
   */
  void waitFor() throws JobExecutionException;
}
