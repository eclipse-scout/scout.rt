/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.platform.job;

import java.util.concurrent.TimeUnit;

import org.eclipse.scout.rt.platform.util.concurrent.ThreadInterruptedError;
import org.eclipse.scout.rt.platform.util.concurrent.TimedOutError;

/**
 * A blocking condition allows a thread to wait for a condition to become <code>true</code>. That is similar to the Java
 * Object's 'wait/notify' mechanism, but with some additional functionality regarding semaphore aware jobs. If a
 * semaphore aware job enters a blocking condition, it releases ownership of the permit, which allows another job of
 * that same semaphore to commence execution. Upon the condition becomes <code>true</code>, the job then must compete
 * for a permit anew.
 * <p>
 * A condition can be used across multiple threads to wait for the same condition. Also, a condition is reusable upon
 * invalidation. And finally, a condition can be used even if not running within a job.
 * <p>
 * A blocking condition is often used by model jobs to wait for something to happen, but to allow another model job to
 * run while waiting. A typical use case would be to wait for a MessageBox to be closed.
 *
 * @since 5.1
 * @see Jobs#newBlockingCondition(boolean)
 */
public interface IBlockingCondition {

  /**
   * Returns <code>true</code> if this condition is in <em>blocking state</em>, meaning that calls to
   * {@link #waitFor(String...)} or {@link #waitFor(long, TimeUnit, String...)} block the calling thread.
   */
  boolean isBlocking();

  /**
   * Invoke to change the <em>blocking state</em> of this blocking condition. This method can be invoked from any
   * thread.
   * <p>
   * If <code>true</code>, this condition will block subsequent calls on {@link #waitFor(String...)} or
   * {@link #waitFor(long, TimeUnit, String...)}. If <code>false</code>, the condition is invalidated, meaning that the
   * <em>blocking state</em> is set to <code>false</code> and any thread waiting for this condition to fall is released
   * asynchronously.
   *
   * @param blocking
   *          <code>true</code> to arm this condition, or <code>false</code> to invalidate it and release all waiting
   *          threads.
   */
  void setBlocking(boolean blocking);

  /**
   * Waits if necessary for the <em>blocking state</em> of this blocking condition to be unblocked, or until the current
   * thread is interrupted. While waiting, the current thread is disabled for thread scheduling purposes and lies
   * dormant. This method returns immediately, if this blocking condition is not blocking at the time of invocation.
   * <p>
   * If invoked from a semaphore aware job, the job's permit is released and passed to the next competing job while
   * waiting.
   * <p>
   * If the current thread's interrupted status is set when it enters this method, or it is interrupted while waiting,
   * then {@link ThreadInterruptedError} is thrown with the thread's interrupted status still set. Additionally for
   * semaphore aware jobs, this method returns with the semaphore permit re-acquired.
   *
   * @param executionHints
   *          optional execution hints to be associated with the current {@link IFuture} for the time of waiting; has no
   *          effect if not running on behalf of a job.
   * @throws ThreadInterruptedError
   *           if the current thread's interrupted status is set when it enters this method, or if it is interrupted
   *           while waiting. The thread's interrupted status is still set. Additionally for semaphore aware jobs, this
   *           method returns with the semaphore permit re-acquired. However, this condition may still be in
   *           <em>blocking state</em>.
   */
  void waitFor(String... executionHints);

  /**
   * Waits if necessary for at most the given time for the <em>blocking state</em> of this blocking condition to be
   * unblocked, or until the current thread is interrupted, or until the timeout elapses. While waiting, the current
   * thread is disabled for thread scheduling purposes and lies dormant. This method returns immediately, if this
   * blocking condition is not blocking at the time of invocation.
   * <p>
   * If invoked from a semaphore aware job, the job's permit is released and passed to the next competing job while
   * waiting.
   * <p>
   * If the current thread's interrupted status is set when it enters this method, or it is interrupted while waiting,
   * then {@link ThreadInterruptedError} is thrown with the thread's interrupted status still set. Additionally for
   * semaphore aware jobs, when it finally returns from this method the semaphore permit will be re-acquired.
   * <p>
   * If the timeout elapses, then {@link TimedOutError} is thrown. Additionally for semaphore aware jobs, this method
   * returns with the semaphore permit re-acquired.
   *
   * @param timeout
   *          the maximal time to wait.
   * @param unit
   *          unit of the given timeout.
   * @param executionHints
   *          optional execution hints to be associated with the current {@link IFuture} for the time of waiting; has no
   *          effect if not running on behalf of a job.
   * @throws ThreadInterruptedError
   *           if the current thread's interrupted status is set when it enters this method, or if it is interrupted
   *           while waiting. The thread's interrupted status is still set. Additionally for semaphore aware jobs, this
   *           method returns with the semaphore permit re-acquired. However, this condition may still be in
   *           <em>blocking state</em>.
   * @throws TimedOutError
   *           if the wait timed out.<br/>
   *           For semaphore aware jobs, this method returns with the semaphore permit re-acquired. However, this
   *           condition may still be in <em>blocking state</em>.
   */
  void waitFor(long timeout, TimeUnit unit, String... executionHints);
}
