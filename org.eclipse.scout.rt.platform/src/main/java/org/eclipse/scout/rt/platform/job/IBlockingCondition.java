package org.eclipse.scout.rt.platform.job;

import java.util.concurrent.TimeUnit;

/**
 * Use this object to put the current thread into waiting mode until this condition falls. If getting blocked and the
 * job is configured for mutual exclusion, the job's mutex is released and passed to the next competing job.<br/>
 * This condition can be used across multiple threads to wait for the same condition; this condition is reusable upon
 * invalidation.
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
   * Blocks the calling thread until the <i>blocking-state</i> of this blocking condition is changed to
   * <code>false</code>, or if the thread is interrupted. Thereby, the current thread becomes disabled for thread
   * scheduling purposes and lies dormant. This method returns immediately, if this blocking condition is not armed at
   * the time of invocation.
   *
   * @throws JobException
   *           is thrown if the current thread is interrupted, or if being a mutex job and the mutex could not be
   *           acquired anew.<br/>
   *           <strong>If thrown and being a mutex job, the current thread is not synchronized with the mutex anymore
   *           and should terminate its work.</strong>
   */
  void waitFor();

  /**
   * Blocks the calling thread until the <i>blocking-state</i> of this blocking condition is changed to
   * <code>false</code>, or if the thread is interrupted, or the timeout elapses. Thereby, the current thread becomes
   * disabled for thread scheduling purposes and lies dormant. This method returns immediately, if this blocking
   * condition is not armed at the time of invocation.
   *
   * @param timeout
   *          the maximal time to wait.
   * @param unit
   *          unit of the given timeout.
   * @throws JobException
   *           is thrown if the current thread is interrupted, or the timeout elapsed, or if being a mutex job and the
   *           mutex could not be acquired anew.<br/>
   *           <strong>If thrown and being a mutex job, the current thread is not synchronized with the mutex anymore
   *           and should terminate its work.</strong>
   */
  void waitFor(long timeout, TimeUnit unit);

  /**
   * @return the name of this blocking condition.
   */
  String getName();
}
