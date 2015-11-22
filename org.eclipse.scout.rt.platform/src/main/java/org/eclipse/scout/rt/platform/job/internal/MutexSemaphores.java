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

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;

import org.eclipse.scout.commons.Assertions;
import org.eclipse.scout.commons.annotations.Internal;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.platform.Bean;
import org.eclipse.scout.rt.platform.job.internal.MutexSemaphore.Position;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

  private static final Logger LOG = LoggerFactory.getLogger(MutexSemaphores.class);

  private final ReadLock m_readLock;
  private final WriteLock m_writeLock;

  private final Map<Object, MutexSemaphore> m_mutexSemaphores = new HashMap<>();

  public MutexSemaphores() {
    final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    m_readLock = lock.readLock();
    m_writeLock = lock.writeLock();
  }

  /**
   * @return <code>true</code> if the given task is a mutex task and currently owns the mutex.
   */
  public boolean isMutexOwner(final JobFutureTask<?> task) {
    m_readLock.lock();
    try {
      final MutexSemaphore mutexSemaphore = m_mutexSemaphores.get(task.getMutexObject());
      if (mutexSemaphore != null) {
        return mutexSemaphore.isMutexOwner(task);
      }
      return false;
    }
    finally {
      m_readLock.unlock();
    }
  }

  /**
   * Blocks the calling thread until the mutex is acquired for the given task.
   *
   * @param mutexTask
   *          the task to acquire the mutex for.
   * @param queuePosition
   *          position in the queue of competing tasks.
   * @throws ProcessingException
   *           is thrown if the current thread is interrupted while waiting for the mutex to become available.
   */
  public void acquireMutex(final JobFutureTask<?> mutexTask, final Position queuePosition) {
    final Object acquisitionLock = new Object();
    final AtomicBoolean waitingForMutex = new AtomicBoolean(true);

    competeForMutex(mutexTask, queuePosition, new IMutexAcquiredCallback() {

      @Override
      public void onMutexAcquired() {
        synchronized (acquisitionLock) {
          if (waitingForMutex.get()) {
            acquisitionLock.notify();
          }
          else {
            passMutexToNextTask(mutexTask.getMutexObject(), mutexTask);
          }
        }
      }
    });

    // Block the current thread until the mutex is acquired.
    synchronized (acquisitionLock) {
      while (!isMutexOwner(mutexTask)) {
        try {
          acquisitionLock.wait();
        }
        catch (final InterruptedException e) {
          waitingForMutex.set(false);
          Thread.currentThread().interrupt(); // Restore the interrupted status because cleared by catching InterruptedException.
          throw new ProcessingException(String.format("Interrupted while re-acquiring the mutex [%s]", mutexTask), e);
        }
      }
    }
  }

  /**
   * Makes the given task to compete for its mutex. When acquired, the callback is invoked. The callback is invoked
   * immediately in the calling thread if the mutex was free.
   *
   * @param mutexTask
   *          the task to acquire the mutex for.
   * @param queuePosition
   *          position in the queue of competing tasks.
   * @param mutexAcquiredCallback
   *          the callback to be invoked once acquired the mutex.
   */
  public void competeForMutex(final JobFutureTask<?> mutexTask, final Position queuePosition, final IMutexAcquiredCallback mutexAcquiredCallback) {
    Assertions.assertTrue(mutexTask.getMutexObject() != null, "Task must have a mutex [task=%s]", mutexTask);
    final Object mutexObject = mutexTask.getMutexObject();

    // Create the task to compete for the mutex.
    final IMutexAcquisitionTask acquisitionTask = new IMutexAcquisitionTask() {

      @Override
      public void mutexAcquired() {
        getMutexSemaphore(mutexObject).replaceMutexOwner(this, mutexTask); // make the task the mutex-owner.
        mutexAcquiredCallback.onMutexAcquired();
      }

      @Override
      public String toString() {
        return String.format("Mutex acquisition for '%s'", mutexTask.getJobInput().getName());
      }
    };

    final boolean mutexFree;
    m_writeLock.lock();
    try {
      mutexFree = getMutexSemaphore(mutexObject).tryAcquireElseOffer(queuePosition, acquisitionTask);
    }
    finally {
      m_writeLock.unlock();
    }

    if (mutexFree) {
      getMutexSemaphore(mutexObject).replaceMutexOwner(acquisitionTask, mutexTask); // make the task the mutex-owner.
      mutexAcquiredCallback.onMutexAcquired();
    }
  }

  /**
   * Use this method to pass the mutex to the next task in the queue.
   */
  public void passMutexToNextTask(final Object mutexObject, final JobFutureTask<?> currentMutexOwner) {
    final IMutexAcquisitionTask nextTask;

    m_writeLock.lock();
    try {
      final MutexSemaphore mutexSemaphore = m_mutexSemaphores.get(mutexObject);
      Assertions.assertNotNull(mutexSemaphore, "Inconsistency detected: Mutex semaphore must not be null when being the mutex owner");
      Assertions.assertTrue(mutexSemaphore.isMutexOwner(currentMutexOwner), "Inconsistency detected: wrong mutex owner [expected=%s, actual=%s]", currentMutexOwner, mutexSemaphore.getMutexOwner());

      nextTask = mutexSemaphore.releaseAndPoll();
      if (nextTask == null) {
        m_mutexSemaphores.remove(mutexObject);
        return;
      }
    }
    finally {
      m_writeLock.unlock();
    }

    try {
      nextTask.mutexAcquired();
    }
    catch (final Throwable t) {
      LOG.error("Failed to pass mutex to next task [task={}]", nextTask, t);
    }
  }

  /**
   * Returns the mutex-semaphore for the given mutex-object.
   */
  private MutexSemaphore getMutexSemaphore(final Object mutexObject) {
    m_writeLock.lock();
    try {
      MutexSemaphore mutexSemaphore = m_mutexSemaphores.get(mutexObject);
      if (mutexSemaphore == null) {
        mutexSemaphore = new MutexSemaphore();
        m_mutexSemaphores.put(mutexObject, mutexSemaphore);
      }
      return mutexSemaphore;
    }
    finally {
      m_writeLock.unlock();
    }
  }

  /**
   * Returns the current permit count for the given mutex-object. This is the mutex-owner plus all pending tasks; if
   * <code>0</code>, the mutex is not acquired.
   */
  public int getPermitCount(final Object mutexObject) {
    Assertions.assertNotNull(mutexObject, "Mutex object must not be null");

    m_readLock.lock();
    try {
      final MutexSemaphore mutexSemaphore = m_mutexSemaphores.get(mutexObject);
      return (mutexSemaphore != null ? mutexSemaphore.getPermitCount() : 0);
    }
    finally {
      m_readLock.unlock();
    }
  }

  /**
   * Clears all queued tasks and mutex-owners.
   */
  void clear() {
    m_writeLock.lock();
    try {
      for (final MutexSemaphore mutexSemaphore : m_mutexSemaphores.values()) {
        mutexSemaphore.clear();
      }
      m_mutexSemaphores.clear();
    }
    finally {
      m_writeLock.unlock();
    }
  }
}
