/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client.job;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.scout.commons.Assertions;
import org.eclipse.scout.commons.ConfigIniUtility;
import org.eclipse.scout.commons.annotations.Internal;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.job.IJob;
import org.eclipse.scout.commons.job.IJobVisitor;
import org.eclipse.scout.commons.job.IProgressMonitor;
import org.eclipse.scout.commons.job.JobExecutionException;
import org.eclipse.scout.commons.job.NamedThreadFactory;
import org.eclipse.scout.commons.job.interceptor.ExceptionTranslator;
import org.eclipse.scout.commons.job.internal.JobMap;
import org.eclipse.scout.commons.job.internal.JobMap.IPutCallback;
import org.eclipse.scout.commons.job.internal.RunNowFuture;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.job.internal.FutureTaskEx;
import org.eclipse.scout.rt.client.job.internal.MutexSemaphore;
import org.eclipse.scout.rt.client.job.internal.Task;

/**
 * Default implementation of {@link IModelJobManager}.
 *
 * @since 5.1
 */
public class ModelJobManager implements IModelJobManager {

  private static final IScoutLogger LOG = ScoutLogManager.getLogger(ModelJobManager.class);

  @Internal
  protected static final String PROP_CORE_POOL_SIZE = "org.eclipse.scout.job.model.corePoolSize";
  @Internal
  protected static final int DEFAULT_CORE_POOL_SIZE = 0; // The number of threads to keep in the pool, even if they are idle; The default is 0 to have no worker-thread alive if idle to save resources if having inactive clients.
  @Internal
  protected static final String PROP_KEEP_ALIVE_TIME = "org.eclipse.scout.job.model.keepAliveTime";
  @Internal
  protected static final long DEFAULT_KEEP_ALIVE_TIME = 60L;

  @Internal
  protected final ExecutorService m_executor;
  @Internal
  protected final JobMap m_jobMap;
  @Internal
  protected final MutexSemaphore<Task<?>> m_mutexSemaphore;
  @Internal
  protected final Set<IModelJob> m_blockedJobs = new HashSet<>();

  public ModelJobManager() {
    m_jobMap = new JobMap();
    m_mutexSemaphore = Assertions.assertNotNull(createMutexSemaphore());
    m_executor = Assertions.assertNotNull(createExecutor());
  }

  @Override
  public final <RESULT> RESULT runNow(final IModelJob<RESULT> job, final Callable<RESULT> callable) throws ProcessingException, JobExecutionException {
    Assertions.assertNotNull(callable);

    if (!isModelThread()) {
      throw new JobExecutionException(String.format("Wrong thread: The calling thread must be the model-thread to run model jobs in 'runNow' style. [thread=%s]", Thread.currentThread().getName()));
    }

    // Create a 'RunNow'-Future if not running yet.
    final RunNowFuture<RESULT> future = m_jobMap.putIfAbsentElseReject(job, new IPutCallback<RESULT, RunNowFuture<RESULT>>() {

      @Override
      public RunNowFuture<RESULT> onAbsent() {
        return new RunNowFuture<RESULT>(Thread.currentThread());
      }
    });

    // Run the future on behalf of the current model-thread.
    try {
      return interceptCallable(callable).call();
    }
    catch (final Exception e) {
      throw ExceptionTranslator.translate(e);
    }
    finally {
      m_jobMap.remove(future);
    }
  }

  @Override
  public final <RESULT> Future<RESULT> schedule(final IModelJob<RESULT> job, final Callable<RESULT> callable) throws JobExecutionException {
    Assertions.assertNotNull(job);
    Assertions.assertNotNull(callable);

    // Schedule and register the job if not running yet (is unregistered in 'onAfter' or 'onReject').
    return m_jobMap.putIfAbsentElseReject(job, new IPutCallback<RESULT, Future<RESULT>>() {

      @Override
      public Future<RESULT> onAbsent() {
        final Task<RESULT> task = createModelTask(job, interceptCallable(callable));

        if (m_mutexSemaphore.tryAcquireElseOfferTail(task)) {
          task.schedule();
        }

        return task.getFuture();
      }
    });
  }

  /**
   * Creates a task representing the given {@link IModelJob} to be passed to the executor.
   *
   * @param job
   *          {@link IModelJob}.
   * @param callable
   *          {@link Callable} to be executed.
   * @return {@link Task} to be passed to the executor.
   */
  @Internal
  protected <RESULT> Task<RESULT> createModelTask(final IModelJob<RESULT> job, final Callable<RESULT> callable) {
    return new Task<RESULT>(job.getName(), m_executor, m_mutexSemaphore) {

      @Override
      protected void onBefore() {
        m_mutexSemaphore.registerAsModelThread();
      }

      @Override
      protected RESULT onCall() throws Exception {
        return callable.call();
      }

      @Override
      protected void onAfter() {
        m_jobMap.remove(getFuture());

        // Check, if this job was interrupted while waiting for a blocking condition to fall.
        final boolean interruptedWhileBlocking = m_blockedJobs.remove(job);

        if (interruptedWhileBlocking) {
          // NOOP: This job does not own the mutex and therefore must not pass the mutex to the next queued job.
        }
        else {
          final Task<?> nextTask = m_mutexSemaphore.pollElseRelease();
          if (nextTask != null) {
            nextTask.schedule();
          }
        }
      }

      @Override
      protected void onRejected() {
        final Future<RESULT> future = getFuture();
        future.cancel(true); // to interrupt the submitter if waiting for the job to complete.
        m_jobMap.remove(future);

        final Task<?> nextTask = m_mutexSemaphore.pollElseRelease();
        if (nextTask != null) {
          nextTask.schedule();
        }
      }
    };
  }

  @Override
  public final boolean isBlocked(final IModelJob<?> modelJob) {
    return m_blockedJobs.contains(modelJob);
  }

  @Override
  public final boolean isModelThread() {
    return m_mutexSemaphore.isModelThread();
  }

  @Override
  public final boolean isIdle() {
    return m_mutexSemaphore.isIdle();
  }

  @Override
  public final boolean waitForIdle(final long timeout, final TimeUnit unit) throws InterruptedException {
    return m_mutexSemaphore.waitForIdle(timeout, unit);
  }

  @Override
  public final boolean cancel(final IJob<?> job, final boolean interruptIfRunning) {
    return m_jobMap.cancel(job, interruptIfRunning);
  }

  @Override
  public final boolean isCanceled(final IJob<?> job) {
    return m_jobMap.isCancelled(job);
  }

  @Override
  public final Future<?> getFuture(final IModelJob<?> job) {
    return m_jobMap.getFuture(job);
  }

  @Override
  public final void shutdown() {
    m_executor.shutdownNow();

    final Set<Future<?>> futures = m_jobMap.clear();
    for (final Future<?> future : futures) {
      future.cancel(true); // to interrupt the submitter if waiting for the job to complete.
    }

    m_mutexSemaphore.clear();
  }

  @Override
  public final void visit(final IJobVisitor visitor) {
    m_jobMap.visit(visitor);
  }

  @Override
  public IBlockingCondition createBlockingCondition(final String name) {
    return new BlockingCondition(name);
  }

  @Override
  public <RESULT> IProgressMonitor createProgressMonitor(final IModelJob<RESULT> job) {
    return new IProgressMonitor() {

      @Override
      public boolean isCancelled() {
        return isCanceled(job);
      }
    };
  }

  /**
   * Creates a {@link MutexSemaphore} to manage acquisition of the model-mutex.
   */
  @Internal
  protected MutexSemaphore<Task<?>> createMutexSemaphore() {
    return new MutexSemaphore<>();
  }

  /**
   * Creates the {@link ExecutorService} to run model jobs within this {@link ModelJobManager} in mutual-exclusion
   * manner.
   */
  @Internal
  protected ExecutorService createExecutor() {
    final int corePoolSize = ConfigIniUtility.getPropertyInt(PROP_CORE_POOL_SIZE, DEFAULT_CORE_POOL_SIZE);
    final long keepAliveTime = ConfigIniUtility.getPropertyLong(PROP_KEEP_ALIVE_TIME, DEFAULT_KEEP_ALIVE_TIME);

    final RejectedExecutionHandler rejectionHandler = new RejectedExecutionHandler() {

      @Override
      public void rejectedExecution(final Runnable runnable, final ThreadPoolExecutor executor) {
        handleJobRejected((FutureTaskEx) runnable);
      }
    };

    return new ThreadPoolExecutor(corePoolSize, Integer.MAX_VALUE, keepAliveTime, TimeUnit.SECONDS, new SynchronousQueue<Runnable>(), new NamedThreadFactory("scout-model"), rejectionHandler);
  }

  /**
   * Method invoked if a job was rejected from being scheduled.<br/>
   *
   * @param futureTask
   *          rejected {@link Future}.
   */
  @Internal
  protected void handleJobRejected(final FutureTaskEx<?> futureTask) {
    futureTask.reject();

    // Do not throw a 'RejectedExecutionException' because the invoker will not be the job's submitter if this task was queued.
    if (m_executor.isShutdown()) {
      LOG.debug("Job rejected because the job manager is shutdown.");
    }
    else {
      LOG.error("Job rejected because no more threads or queue slots available.");
    }
  }

  /**
   * This method can be used to intercept the concrete {@link Callable} given to the executor for execution. This method
   * is called just before executing the job. The default implementation simply returns the given {@link Callable}.
   *
   * @param callable
   *          {@link Callable} to decorate.
   * @return decorated {@link Callable} to be passed to an {@link ExecutorService};; must not be <code>null</code>.
   */
  protected <RESULT> Callable<RESULT> interceptCallable(final Callable<RESULT> callable) {
    return callable;
  }

  /**
   * @see IBlockingCondition
   */
  @Internal
  protected class BlockingCondition implements IBlockingCondition {

    private final AtomicBoolean m_blocking = new AtomicBoolean(); // true if at least one thread is waiting for the condition to fall.
    private final String m_name;

    public BlockingCondition(final String name) {
      m_name = Assertions.assertNotNull(name);
    }

    @Override
    public boolean hasWaitingThreads() {
      return m_blocking.get();
    }

    @Override
    public void releaseMutexAndAwait() throws JobExecutionException {
      if (!isModelThread()) {
        throw new JobExecutionException(String.format("Wrong thread: A job can only be blocked on behalf of the model thread. [thread=%s]", Thread.currentThread().getName()));
      }

      final IModelJob<?> currentJob = (IModelJob<?>) IJob.CURRENT.get();

      // Pass the model-mutex to the next queued job or release the mutex.
      final Task<?> nextTask = m_mutexSemaphore.pollElseRelease();
      if (nextTask != null) {
        nextTask.schedule();
      }

      // [mutex] The following code is not synchronized with the model-mutex anymore.

      // Block the calling thread until the blocking condition falls (IBlockingCondition#signalAll).
      synchronized (m_blocking) {
        m_blocking.set(true);
        m_blockedJobs.add(currentJob);

        while (m_blocking.get()) { // spurious-wakeup safe
          try {
            m_blocking.wait();
          }
          catch (final InterruptedException e) {
            throw new JobExecutionException(String.format("Interrupted while waiting for a blocking condition to fall. [bc=%s, job=%s]", m_name, currentJob.getName()), e);
          }
        }
        m_blockedJobs.remove(currentJob); // do not put into a 'finally'-block to not pass the mutex to the next job if being interrupted.
      }

      // [re-acquire] phase 1: Compete for the model-mutex anew.
      final CountDownLatch mutexReAcquiredLatch = new CountDownLatch(1);
      final AtomicBoolean rejectedByExecutor = new AtomicBoolean(false);

      final Task<Void> task = new Task<Void>(currentJob.getName(), m_executor, m_mutexSemaphore) {

        @Override
        protected Void onCall() throws Exception {
          rejectedByExecutor.set(false);
          mutexReAcquiredLatch.countDown(); // simply release the blocking thread.
          return null;
        }

        @Override
        protected void onRejected() {
          rejectedByExecutor.set(true);
          mutexReAcquiredLatch.countDown(); // simply release the blocking thread.
        }
      };
      boolean acquired = m_mutexSemaphore.tryAcquireElseOfferHead(task);

      // [re-acquire] phase 2: If not being the mutex-owner yet, wait until having re-acquired the model-mutex.
      while (!acquired && !m_executor.isShutdown()) {
        try {
          mutexReAcquiredLatch.await();
          acquired = true;
        }
        catch (final InterruptedException e) {
          // Ignore thread interruptions to not enter an inconsistent state of having multiple concurrent model-threads.
          LOG.warn(String.format("Ignored thread's interruption while waiting for the model-mutex to be re-acquired. [bc=%s, job=%s]", m_name, currentJob.getName()), e);
        }
      }

      // Check if the model-mutex could be acquired successfully.
      if (rejectedByExecutor.get()) {
        getFuture(currentJob).cancel(true); // to interrupt the submitter if waiting for the job to complete.
        throw JobExecutionException.newRejectedJobExecutionException("Failed to re-acquire the model-mutex because being rejected by the executor. Maybe there are no more threads or queue slots available, or the executor was shutdown. [bc=%s, job=%s, shutdown=%s]", m_name, currentJob.getName(), m_executor.isShutdown());
      }
      else {
        // [mutex] The following code is synchronized with the model-mutex anew.
        m_mutexSemaphore.registerAsModelThread();
      }
    }

    @Override
    public void signalAll() {
      synchronized (m_blocking) {
        m_blocking.set(false);
        m_blocking.notifyAll();
      }
    }
  }
}
