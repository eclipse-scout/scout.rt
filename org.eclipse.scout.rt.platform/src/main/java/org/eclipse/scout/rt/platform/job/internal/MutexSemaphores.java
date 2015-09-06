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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;

import org.eclipse.scout.commons.Assertions;
import org.eclipse.scout.commons.annotations.Internal;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Bean;

/**
 * Provides a thread-safe implementation of a non-blocking 1-permit-per-mutex semaphore backed with a fair queue. For
 * each mutex object, a separate {@link MutexSemaphore} is created to hold the mutex state.
 *
 * @see MutexSemaphore
 * @since 5.1
 */
@Internal
@Bean
public class MutexSemaphores {

  private static final IScoutLogger LOG = ScoutLogManager.getLogger(MutexSemaphores.class);

  private static final boolean POSITION_TAIL = true;
  private static final boolean POSITION_HEAD = false;

  private final Map<Object, MutexSemaphore> m_mutexSemaphores = new ConcurrentHashMap<>();

  private volatile ExecutorService m_executor;

  /**
   * Initializes this class with the executor to schedule pending mutex taks once a mutex task of the same mutex-object
   * completes.
   */
  public void init(final ExecutorService executor) {
    m_executor = executor;
  }

  /**
   * @return <code>true</code> if the given task is a mutex task and currently owns the mutex.
   */
  public boolean isMutexOwner(final IMutexTask<?> task) {
    synchronized (task.getMutexObject()) {
      final MutexSemaphore mutexSemaphore = m_mutexSemaphores.get(task.getMutexObject());

      if (mutexSemaphore != null) {
        return mutexSemaphore.isMutexOwner(task);
      }
      return false;
    }
  }

  /**
   * Tries to acquire the mutex for the task's mutex-object. If not available at the time of invocation, the task is put
   * into the queue of pending tasks and will compete for the mutex once all queued tasks of that mutex-object
   * acquired/released the mutex.
   *
   * @param task
   *          the task to acquire the mutex for; must be a mutex task.
   * @return <code>true</code> if the mutex was acquired, <code>false</code> if being queued.
   */
  public boolean tryAcquireElseOfferTail(final IMutexTask<?> task) {
    return tryAcquireElseOffer(task, POSITION_TAIL);
  }

  /**
   * Tries to acquire the mutex for the task's mutex-object. If not available at the time of invocation, the task is put
   * into the queue of pending tasks and will compete for the mutex once being at first position in the queue.
   *
   * @param task
   *          the task to acquire the mutex for; must be a mutex task.
   * @param tail
   *          position where to add the task if the mutex cannot be acquired yet.
   * @return <code>true</code> if the mutex was acquired, <code>false</code> if being queued.
   */
  private boolean tryAcquireElseOffer(final IMutexTask<?> task, final boolean tail) {
    Assertions.assertTrue(task.isMutexTask(), "Task must be a mutex task [task=%s]", task);

    if (m_executor.isShutdown()) {
      task.cancel(true);
      return false;
    }

    final Object mutexObject = task.getMutexObject();

    synchronized (mutexObject) {
      MutexSemaphore mutexSemaphore = m_mutexSemaphores.get(mutexObject);
      if (mutexSemaphore == null) {
        mutexSemaphore = BEANS.get(MutexSemaphore.class);
        mutexSemaphore.init(mutexObject);
        m_mutexSemaphores.put(mutexObject, mutexSemaphore);
      }

      // The mutex acquisition must be within the global 'writeLock', so that the semaphore is not removed by a simultaneously completing mutex task.
      return mutexSemaphore.tryAcquireElseOffer(task, tail);
    }
  }

  /**
   * Blocks the calling thread until the given task acquired the task's mutex-object.
   *
   * @param task
   *          the task to acquire the mutex; must be a mutex task.
   * @throws ProcessingException
   *           is thrown if the current thread is interrupted while waiting for the mutex to become available, or upon
   *           shutdown of the job manager.
   */
  public void acquire(final IMutexTask<?> task) throws ProcessingException {
    Assertions.assertTrue(task.isMutexTask(), "Task must be a mutex task [task=%s]", task);
    final Object mutexObject = task.getMutexObject();
    final Object acquisitionLock = new Object();

    if (m_executor.isShutdown()) {
      task.cancel(true);
      throw new ProcessingException(String.format("Failed to acquire mutex because job manager is shutdown [task=%s]", task));
    }

    // Create the task to re-acquire the mutex. This task is queued to compete for the mutex anew.
    final MutexAcquisitionFutureTask mutexAcquisitionTask = new MutexAcquisitionFutureTask(mutexObject) {

      @Override
      protected void mutexAcquired() {
        synchronized (acquisitionLock) {
          synchronized (task.getMutexObject()) {
            final MutexSemaphore mutexSemaphore = m_mutexSemaphores.get(mutexObject);
            Assertions.assertNotNull(mutexSemaphore, "Inconsistency detected: Mutex semaphore must not be null when being the mutex owner");
            Assertions.assertTrue(mutexSemaphore.isMutexOwner(this), "Inconsistency detected: wrong mutex owner [expected=%s, actual=%s]", this, mutexSemaphore.getMutexOwner());

            if (isAwaitMutex()) {
              mutexSemaphore.replaceMutexOwner(this, task); // make the task the mutex-owner.
              acquisitionLock.notify();
            }
            else {
              passMutexToNextTask(this);
            }
          }
        }
      }
    };

    // Try to acquire the mutex, or wait for the mutex to become available.
    if (tryAcquireElseOffer(mutexAcquisitionTask, POSITION_HEAD)) {
      synchronized (task.getMutexObject()) {
        final MutexSemaphore mutexSemaphore = m_mutexSemaphores.get(mutexObject);
        Assertions.assertNotNull(mutexSemaphore, "Inconsistency detected: Mutex semaphore must not be null when being the mutex owner");
        Assertions.assertTrue(mutexSemaphore.isMutexOwner(mutexAcquisitionTask), "Inconsistency detected: wrong mutex owner [expected=%s, actual=%s]", mutexAcquisitionTask, mutexSemaphore.getMutexOwner());

        mutexSemaphore.replaceMutexOwner(mutexAcquisitionTask, task); // make the task the mutex-owner.
      }
    }
    else {
      synchronized (acquisitionLock) {
        while (!isMutexOwner(task)) {
          try {
            acquisitionLock.wait();
          }
          catch (final InterruptedException e) {
            mutexAcquisitionTask.stopAwaitMutex();

            Thread.currentThread().interrupt(); // Restore the interrupted status because cleared by catching InterruptedException.
            throw new ProcessingException(String.format("Interrupted while re-acquiring the mutex [%s]", task), e);
          }
        }
      }
    }
  }

  /**
   * Passes the tasks's mutex to the first task in the queue.
   *
   * @param task
   *          task which currently is the mutex-owner.
   * @return task which is the new mutex-owner, <code>null</code> if the queue was empty.
   */
  public IMutexTask<?> releaseAndPoll(final IMutexTask<?> task) {
    Assertions.assertTrue(task.isMutexTask(), "Task must be a mutex task [task=%s]", task);

    if (m_executor.isShutdown()) {
      return null;
    }

    final Object mutexObject = task.getMutexObject();

    synchronized (mutexObject) {
      final MutexSemaphore mutexSemaphore = m_mutexSemaphores.get(mutexObject);
      Assertions.assertNotNull(mutexSemaphore, "Inconsistency detected: Mutex semaphore must not be null when being the mutex owner");
      Assertions.assertTrue(mutexSemaphore.isMutexOwner(task), "Inconsistency detected: wrong mutex owner [expected=%s, actual=%s]", task, mutexSemaphore.getMutexOwner());

      final IMutexTask<?> nextTask = mutexSemaphore.releaseAndPoll();
      if (nextTask == null) {
        m_mutexSemaphores.remove(mutexObject);
      }

      return nextTask;
    }
  }

  /**
   * Use this method to pass the mutex to the next task in the queue.
   */
  public void passMutexToNextTask(final IMutexTask<?> currentMutexOwner) {
    final IMutexTask<?> nextTask = releaseAndPoll(currentMutexOwner);
    if (nextTask != null) {
      try {
        m_executor.execute(nextTask);
      }
      catch (final Throwable t) {
        LOG.error(String.format("Failed to execute mutex task [task=%s]", nextTask), t);
      }
    }
  }

  /**
   * Returns the current permit count for the given mutex-object. This is the mutex-owner plus all pending tasks; if
   * <code>0</code>, the mutex is not acquired.
   */
  public int getPermitCount(final Object mutexObject) {
    Assertions.assertNotNull(mutexObject, "Mutex object must not be null");

    synchronized (mutexObject) {
      final MutexSemaphore mutexSemaphore = m_mutexSemaphores.get(mutexObject);
      return (mutexSemaphore != null ? mutexSemaphore.getPermitCount() : 0);
    }
  }

  /**
   * Clears all queued tasks and mutex-owners.
   */
  void clear() {
    for (final MutexSemaphore mutexSemaphore : m_mutexSemaphores.values()) {
      synchronized (mutexSemaphore.getMutexObject()) {
        mutexSemaphore.clear();
      }
    }
    m_mutexSemaphores.clear();
  }
}
