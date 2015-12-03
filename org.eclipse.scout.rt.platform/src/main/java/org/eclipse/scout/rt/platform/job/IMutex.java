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

import org.eclipse.scout.rt.platform.Bean;
import org.eclipse.scout.rt.platform.exception.ProcessingException;
import org.eclipse.scout.rt.platform.job.internal.IMutexAcquiredCallback;

/**
 * Represents a mutex object to achieve mutual exclusion among jobs of the same mutex object. Mutual exclusion means,
 * that at any given time, there is only one job active for the same mutex.
 *
 * @since 5.2
 */
@Bean
public interface IMutex {

  /**
   * Returns whether the given task is the current mutex owner.
   */
  boolean isMutexOwner(IFuture<?> task);

  /**
   * Returns the number of tasks currently competing for the mutex. That is the task currently owning the mutex, plus
   * all tasks waiting for the mutex to become available.
   */
  int getCompetitorCount();

  /**
   * Blocks the calling thread until the mutex can be acquired for the given task.
   *
   * @param task
   *          the task to acquire the mutex for.
   * @param queuePosition
   *          the position where to place the task in the queue of competing tasks if the mutex is not free at the time
   *          of invocation.
   * @throws ProcessingException
   *           is thrown if the current thread is interrupted while waiting for the mutex to become available.
   */
  void acquire(IFuture<?> task, QueuePosition queuePosition);

  /**
   * Makes the given task to compete for this mutex. Upon mutex acquisition, the given callback is invoked. The callback
   * is invoked immediately and on behalf of the calling thread, if being available at the time of invocation.
   * Otherwise, this method returns immediately.
   *
   * @param task
   *          the task to acquire the mutex for.
   * @param queuePosition
   *          the position where to place the task in the queue of competing tasks if the mutex is not free at the time
   *          of invocation.
   * @param mutexAcquiredCallback
   *          the callback to be invoked once the given task acquired the mutex.
   * @return <code>true</code> if the mutex was free and was acquired immediately, or <code>false</code> otherwise.
   */
  boolean compete(IFuture<?> task, QueuePosition queuePosition, IMutexAcquiredCallback mutexAcquiredCallback);

  /**
   * Releases the mutex, and passes it to the next competing task.
   *
   * @param expectedMutexOwner
   *          the task which is currently owning the mutex, and is only used to do a consistency check against the
   *          actual mutex owner.
   */
  void release(IFuture<?> expectedMutexOwner);

  /**
   * Position in the queue of competing tasks.
   */
  enum QueuePosition {
    HEAD, TAIL;
  }
}
