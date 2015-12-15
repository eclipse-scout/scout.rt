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
package org.eclipse.scout.rt.platform.job.internal;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;

import org.eclipse.scout.rt.platform.Bean;
import org.eclipse.scout.rt.platform.job.IFuture;
import org.eclipse.scout.rt.platform.job.IMutex;
import org.eclipse.scout.rt.platform.job.JobInput;
import org.eclipse.scout.rt.platform.util.Assertions;
import org.eclipse.scout.rt.platform.util.Assertions.AssertionException;
import org.eclipse.scout.rt.platform.util.ToStringBuilder;
import org.eclipse.scout.rt.platform.util.concurrent.InterruptedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default implementation of {@link IMutex}.
 */
@Bean
public class Mutex implements IMutex {

  private static final Logger LOG = LoggerFactory.getLogger(Mutex.class);

  private final ReadLock m_readLock;
  private final WriteLock m_writeLock;

  private final Deque<AcquisitionTask> m_competitors;

  private volatile IFuture<?> m_mutexOwner;

  public Mutex() {
    m_competitors = new ArrayDeque<>();

    final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    m_readLock = lock.readLock();
    m_writeLock = lock.writeLock();
  }

  @Override
  public int getCompetitorCount() {
    m_readLock.lock();
    try {
      int count = m_competitors.size();
      if (m_mutexOwner != null) {
        count++;
      }
      return count;
    }
    finally {
      m_readLock.unlock();
    }
  }

  /**
   * Returns whether the given task is the current mutex owner.
   */
  @Override
  public boolean isMutexOwner(final IFuture<?> task) {
    m_readLock.lock();
    try {
      return m_mutexOwner == task;
    }
    finally {
      m_readLock.unlock();
    }
  }

  /**
   * Blocks the calling thread until the mutex can be acquired for the given task.
   *
   * @param task
   *          the task to acquire the mutex for.
   * @param queuePosition
   *          the position where to place the task in the queue of competing tasks if the mutex is not free at the time
   *          of invocation.
   * @throws InterruptedException
   *           if the current thread was interrupted while waiting.
   */
  protected void acquire(final IFuture<?> mutexTask, final QueuePosition queuePosition) {
    Assertions.assertSame(this, mutexTask.getJobInput().getMutex(), "Wrong mutex object [expected={}, actual={}]", this, mutexTask.getJobInput().getMutex());

    final Object acquisitionLock = new Object();
    final AtomicBoolean waitingForMutex = new AtomicBoolean(true);

    compete(mutexTask, queuePosition, new IMutexAcquiredCallback() {

      @Override
      public void onMutexAcquired() {
        synchronized (acquisitionLock) {
          if (waitingForMutex.get()) {
            acquisitionLock.notify();
          }
          else {
            release(mutexTask);
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
        catch (final java.lang.InterruptedException e) {
          Thread.currentThread().interrupt(); // Restore the interrupted status because cleared by catching InterruptedException.
          waitingForMutex.set(false);

          throw new InterruptedException("Interrupted while competing for the mutex")
              .withContextInfo("job", mutexTask.getJobInput().getName())
              .withContextInfo("mutex", mutexTask.getJobInput().getMutex());
        }
      }
    }
  }

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
  protected boolean compete(final IFuture<?> mutexTask, final QueuePosition queuePosition, final IMutexAcquiredCallback mutexAcquiredCallback) {
    Assertions.assertSame(this, mutexTask.getJobInput().getMutex(), "Wrong mutex object [expected={}, actual={}]", this, mutexTask.getJobInput().getMutex());

    boolean mutexFree;
    m_writeLock.lock();
    try {
      mutexFree = (m_mutexOwner == null && m_competitors.isEmpty());

      if (mutexFree) {
        m_mutexOwner = mutexTask;
      }
      else {
        switch (queuePosition) {
          case HEAD:
            m_competitors.offerFirst(new AcquisitionTask(mutexTask, mutexAcquiredCallback));
            break;
          case TAIL:
            m_competitors.offerLast(new AcquisitionTask(mutexTask, mutexAcquiredCallback));
            break;
          default:
            throw new IllegalArgumentException("illegal queue position");
        }
      }
    }
    finally {
      m_writeLock.unlock();
    }

    // Notify the new mutex owner about its mutex aquisition.
    if (mutexFree) {
      mutexAcquiredCallback.onMutexAcquired();
      return true;
    }
    else {
      return false;
    }
  }

  /**
   * Releases the mutex, and passes it to the next competing task.
   *
   * @param expectedMutexOwner
   *          the task which is currently owning the mutex, and is only used to do a consistency check against the
   *          actual mutex owner.
   */
  protected void release(final IFuture<?> mutexTask) {
    Assertions.assertSame(this, mutexTask.getJobInput().getMutex(), "Wrong mutex object [expected={}, actual={}]", this, mutexTask.getJobInput().getMutex());
    Assertions.assertSame(mutexTask, m_mutexOwner, "Task does not own the mutex  [mutexOwner={}, task={}]", m_mutexOwner, mutexTask);

    final AcquisitionTask acquisitionTask;
    m_writeLock.lock();
    try {
      acquisitionTask = m_competitors.poll();
      if (acquisitionTask == null) {
        m_mutexOwner = null;
      }
      else {
        m_mutexOwner = acquisitionTask.getCompetingTask();
      }
    }
    finally {
      m_writeLock.unlock();
    }

    if (acquisitionTask != null) {
      acquisitionTask.notifyMutexAcquired();
    }
  }

  @Override
  public String toString() {
    final ToStringBuilder builder = new ToStringBuilder(this);
    builder.attr("mutexOwner", m_mutexOwner);
    builder.attr("competitors", m_competitors);
    return builder.toString();
  }

  // ==== Helper classes ==== //

  protected static class AcquisitionTask {

    private final IFuture<?> m_competingTask;
    private final IMutexAcquiredCallback m_callback;

    public AcquisitionTask(final IFuture<?> competingTask, final IMutexAcquiredCallback callback) {
      m_competingTask = competingTask;
      m_callback = callback;
    }

    public IFuture<?> getCompetingTask() {
      return m_competingTask;
    }

    public void notifyMutexAcquired() {
      try {
        m_callback.onMutexAcquired();
      }
      catch (final RuntimeException e) {
        LOG.error("Failed to notify new mutex owner about mutex acquisition [task={}]", m_competingTask, e);
      }
    }
  }

  /**
   * Position in the queue of competing tasks.
   */
  protected static enum QueuePosition {
    HEAD, TAIL;
  }

  /**
   * Returns the {@link Mutex} of the given {@link JobInput}, or <code>null</code> if not set, or throws
   * {@link AssertionException} if not of type {@link Mutex}.
   */
  protected static Mutex getMutex(final JobInput input) {
    if (input.getMutex() == null) {
      return null;
    }

    Assertions.assertTrue(input.getMutex() instanceof Mutex, "Mutex object must be of type {} [mutex={}]", Mutex.class.getName(), input.getMutex().getClass().getName());
    return (Mutex) input.getMutex();
  }
}
