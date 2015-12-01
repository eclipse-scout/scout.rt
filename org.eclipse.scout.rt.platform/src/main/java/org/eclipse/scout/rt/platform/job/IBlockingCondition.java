package org.eclipse.scout.rt.platform.job;

import java.util.concurrent.TimeUnit;

import org.eclipse.scout.rt.platform.exception.ProcessingException;

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
   * <p>
   * <strong>If this method returns with an exception, and if this is a mutually exclusive job, the current thread is
   * not synchronized with the mutex anymore and should terminate its work.</strong>
   *
   * @param executionHints
   *          optional execution hints to be associated with the current {@link IFuture} for the time of blocking the
   *          current thread; has no effect if not running in a job.
   * @throws ProcessingException
   *           <ul>
   *           <li>if this thread was interrupted while waiting for this condition to fall; see
   *           {@link ProcessingException#isInterruption()}</li>
   *           <li>if being a mutually exclusive job and the mutex could not be acquired anew upon unblocking</li>
   *           </ul>
   */
  void waitFor(String... executionHints);

  /**
   * Blocks the calling thread until the <i>blocking-state</i> of this blocking condition is changed to
   * <code>false</code>, or if the thread is interrupted, or the timeout elapses. Thereby, the current thread becomes
   * disabled for thread scheduling purposes and lies dormant. This method returns immediately, if this blocking
   * condition is not armed at the time of invocation.
   * <p>
   * <strong>If this method returns with an exception or the timeout elapses, and if this is a mutually exclusive job,
   * the current thread is not synchronized with the mutex anymore and should terminate its work.</strong>
   *
   * @param timeout
   *          the maximal time to wait.
   * @param unit
   *          unit of the given timeout.
   * @param executionHints
   *          optional execution hints to be associated with the current {@link IFuture} for the time of blocking the
   *          current thread; has no effect if not running in a job.
   * @return <code>false</code> if the deadline has elapsed upon return, else <code>true</code>.
   * @throws ProcessingException
   *           <ul>
   *           <li>if this thread was interrupted while waiting for this condition to fall; see
   *           {@link ProcessingException#isInterruption()}</li>
   *           <li>if being a mutually exclusive job and the mutex could not be acquired anew upon unblocking</li>
   *           </ul>
   */
  boolean waitFor(long timeout, TimeUnit unit, String... executionHints);

  /**
   * @return the name of this blocking condition.
   */
  String getName();
}
