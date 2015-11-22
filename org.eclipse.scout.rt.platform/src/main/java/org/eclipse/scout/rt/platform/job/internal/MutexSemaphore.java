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
package org.eclipse.scout.rt.platform.job.internal;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.Semaphore;

import org.eclipse.scout.commons.ToStringBuilder;
import org.eclipse.scout.commons.annotations.Internal;
import org.eclipse.scout.rt.platform.Bean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents a 1-based semaphore for tasks to gain exclusive access to a mutual exclusive object (mutex). In contrast
 * to the Java {@link Semaphore}, this semaphore maintains an internal, fair queue for tasks which cannot acquire the
 * semaphore the time requesting exclusive access, but without blocking the submitter of the task. Once all preceding
 * task acquired and in turn released the semaphore, that task becomes the mutex owner of the semaphore.
 * <p>
 * This class is not thread-safe, meaning that the caller is responsible for proper synchronization.
 *
 * @see MutexSemaphores
 * @since 5.1
 */
@Bean
@Internal
public class MutexSemaphore {

  private static final Logger LOG = LoggerFactory.getLogger(MutexSemaphore.class);

  private volatile Object m_mutexObject;
  private final Deque<IMutexAcquisitionTask> m_pendingQueue;

  private volatile int m_permits;
  private volatile Object m_mutexOwner;

  public MutexSemaphore() {
    m_permits = 0;
    m_pendingQueue = new ArrayDeque<>();
  }

  protected void init(final Object mutexObject) {
    m_mutexObject = mutexObject;
  }

  protected Object getMutexObject() {
    return m_mutexObject;
  }

  protected int getPermitCount() {
    return m_permits;
  }

  protected boolean isMutexOwner(final Object task) {
    return m_mutexOwner == task;
  }

  protected Object getMutexOwner() {
    return m_mutexOwner;
  }

  /**
   * Tries to acquire the mutex, either immediately if free, or puts the acquisition task into the queue of competing
   * tasks.
   *
   * @param position
   *          position in the queue of competing tasks.
   * @param task
   *          the mutex acquisition task to acquire the mutex for.
   * @return <code>true</code> if the mutex was free and was acquired for the given acquisition task.
   */
  protected boolean tryAcquireElseOffer(final Position position, final IMutexAcquisitionTask task) {
    if (m_permits++ == 0) {
      m_mutexOwner = task;
      return true;
    }
    else {
      switch (position) {
        case HEAD:
          m_pendingQueue.offerFirst(task);
          break;
        case TAIL:
          m_pendingQueue.offerLast(task);
          break;
        default:
          throw new IllegalArgumentException();
      }
      return false;
    }
  }

  protected void replaceMutexOwner(final IMutexAcquisitionTask currentMutexOwner, final JobFutureTask<?> newMutexOwner) {
    if (!isMutexOwner(currentMutexOwner)) {
      LOG.error("Unexpected inconsistency: current task must be mutex-owner [currentTask={}, newMutexOwner={}, currentMutexOwner={}]", new Object[]{currentMutexOwner, newMutexOwner, getMutexOwner()});
    }
    m_mutexOwner = newMutexOwner;
  }

  protected IMutexAcquisitionTask releaseAndPoll() {
    final IMutexAcquisitionTask mutexOwner = m_pendingQueue.poll();
    m_mutexOwner = mutexOwner;

    m_permits--;

    if (m_permits < 0) {
      LOG.error("Unexpected inconsistency while releasing mutex: permit count must not be '0'.", m_permits);
      m_permits = 0;
    }

    return mutexOwner;
  }

  protected void clear() {
    m_permits = 0;
    m_pendingQueue.clear();
    m_mutexOwner = null;
  }

  @Override
  public String toString() {
    final ToStringBuilder builder = new ToStringBuilder(this);
    builder.attr("mutexOwner", m_mutexOwner);
    builder.attr("permits", m_permits);
    builder.attr("pendingQueue", m_pendingQueue);
    return builder.toString();
  }

  public static enum Position {
    HEAD, TAIL;
  }
}
