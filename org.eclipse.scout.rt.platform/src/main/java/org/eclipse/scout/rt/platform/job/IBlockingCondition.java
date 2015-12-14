/*******************************************************************************
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.platform.job;

import java.util.concurrent.TimeUnit;

import org.eclipse.scout.rt.platform.util.concurrent.InterruptedException;
import org.eclipse.scout.rt.platform.util.concurrent.TimeoutException;

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
   * @return <code>true</code> if this condition blocks if calling {@link #waitFor(String...)} or
   *         {@link #waitFor(long, TimeUnit, String...)}.
   */
  boolean isBlocking();

  /**
   * Invoke to change the blocking-state of this blocking condition. This method can be invoked from any thread.
   * <p>
   * If <code>true</code>, this condition will block subsequent calls on {@link #waitFor(String...)} or
   * {@link #waitFor(long, TimeUnit, String...)}. If <code>false</code>, the condition is invalidated, meaning that the
   * blocking-state is set to <code>false</code> and any thread waiting for this condition to fall is released.
   *
   * @param blocking
   *          <code>true</code> to arm this condition, or <code>false</code> to invalidate it and release all waiting
   *          threads.
   */
  void setBlocking(boolean blocking);

  /**
   * Waits if necessary for the blocking state of this blocking condition to become <em>unblocked</em>. Thereto, the
   * current thread becomes disabled for thread scheduling purposes and lies dormant. This method returns immediately,
   * if this blocking condition is not <em>blocking</em> at the time of invocation.
   *
   * @param executionHints
   *          optional execution hints to be associated with the current {@link IFuture} for the time of waiting; has no
   *          effect if not running on behalf of a job.
   * @throws InterruptedException
   *           if the current thread was interrupted while waiting. But, even if not waiting anymore, the blocking
   *           condition might still be in blocking state. Also, if being a mutually exclusive job, the thread did not
   *           acquire the mutex, meaning that the thread should terminate its work or waiting anew for the condition to
   *           fall.
   */
  void waitFor(String... executionHints);

  /**
   * Waits if necessary for at most the given time for the blocking state of this blocking condition to become
   * <em>unblocked</em>. Thereto, the current thread becomes disabled for thread scheduling purposes and lies dormant.
   * This method returns immediately, if this blocking condition is not <em>blocking</em> at the time of invocation.
   *
   * @param timeout
   *          the maximal time to wait.
   * @param unit
   *          unit of the given timeout.
   * @param executionHints
   *          optional execution hints to be associated with the current {@link IFuture} for the time of waiting; has no
   *          effect if not running on behalf of a job.
   * @throws InterruptedException
   *           if the current thread was interrupted while waiting. But, even if not waiting anymore, the blocking
   *           condition might still be in blocking state. Also, if being a mutually exclusive job, the thread did not
   *           acquire the mutex, meaning that the thread should terminate its work or waiting anew for the condition to
   *           fall.
   * @throws TimeoutException
   *           if the wait timed out. But, even if not waiting anymore, the blocking condition might still be in
   *           blocking state. Also, if being a mutually exclusive job, the thread did not acquire the mutex, meaning
   *           that the thread should terminate its work or waiting anew for the condition to fall.
   */
  void waitFor(long timeout, TimeUnit unit, String... executionHints);
}
