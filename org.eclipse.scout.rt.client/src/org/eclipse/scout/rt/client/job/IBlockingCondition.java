package org.eclipse.scout.rt.client.job;

import org.eclipse.scout.commons.job.JobExecutionException;

/**
 * Use this object to put the current thread (must be the model thread) into waiting mode until this condition falls. If
 * entering the waiting mode, the threads's model-mutex is released and passed to another competing and prospective
 * model-thread.<br/>
 * This condition can be used across multiple model-threads to wait for the same condition; this condition is reusable
 * upon signaling.
 *
 * @since 5.0
 */
public interface IBlockingCondition {

  /**
   * Queries whether at least one thread is waiting for this condition to fall.
   *
   * @return <code>true</code> if there are waiting threads, <code>false</code> otherwise.
   */
  boolean hasWaitingThreads();

  /**
   * Causes the current model-thread to wait until it is {@link #signalAll() signaled} or {@link Thread#interrupt()
   * interrupted}.<br/>
   * Thereby, the model-mutex is released and passed to another competing and prospective model-thread. The current
   * thread becomes disabled for thread scheduling purposes and lies dormant until it is signaled or interrupted.
   * <p/>
   * <strong>Precondition: The calling thread must be the model-thread.</strong>
   *
   * @throws JobExecutionException
   *           is thrown if the calling thread is not the model-thread or because the waiting thread was interrupted
   *           (e.g. cancellation of the current job), or if the job fails to re-acquire the model-mutex upon
   *           {@link #signalAll()}; if thrown, the current thread is not synchronized with the model-mutex and
   *           therefore must terminate its work.
   * @see #signalAll()
   */
  void releaseMutexAndAwait() throws JobExecutionException;

  /**
   * Wakes up all former model-threads waiting for this condition to fall.<br/>
   * If any threads are waiting on this condition then they are all woken up. Each thread must re-acquire the
   * model-mutex before it can return from await to continue execution in the model-thread.
   * <p/>
   * <strong>The calling thread can be any thread.</strong>
   *
   * @see #releaseMutexAndAwait()
   */
  void signalAll();
}
