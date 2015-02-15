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
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.job.IFuture;
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
import org.eclipse.scout.rt.client.IClientSession;
import org.eclipse.scout.rt.client.job.internal.FutureTaskEx;
import org.eclipse.scout.rt.client.job.internal.MutexSemaphore;
import org.eclipse.scout.rt.client.job.internal.Task;

/**
 * Job manager to run jobs interacting with the client model on behalf of the model-thread. There is one
 * {@link ModelJobManager} per {@link IClientSession}.
 * <p/>
 * Within this manager, jobs are executed in sequence so that no more than one job will be active at any given time. If
 * a {@link ModelJob} gets blocked by entering a {@link IBlockingCondition}, the model-mutex will be released which
 * allows another model-job to run. When being unblocked, the job must compete for the model-mutex anew in order to
 * continue execution.
 *
 * @see {@link IClientSession#getModelJobManager()}
 * @see ModelJob
 * @see IBlockingCondition
 * @since 5.1
 */
public class ModelJobManager {

  private static final IScoutLogger LOG = ScoutLogManager.getLogger(ModelJobManager.class);

  protected static final String PROP_CORE_POOL_SIZE = "org.eclipse.scout.job.model.corePoolSize";
  protected static final int DEFAULT_CORE_POOL_SIZE = 0; // The number of threads to keep in the pool, even if they are idle; The default is 0 to have no worker-thread alive if idle to save resources if having inactive clients.

  protected static final String PROP_KEEP_ALIVE_TIME = "org.eclipse.scout.job.model.keepAliveTime";
  protected static final long DEFAULT_KEEP_ALIVE_TIME = 60L;

  protected final ExecutorService m_executor;

  protected final JobMap m_jobMap;
  protected final MutexSemaphore<Task<?>> m_mutexSemaphore;
  protected final Set<ModelJob> m_blockedJobs = new HashSet<>();

  public ModelJobManager() {
    m_jobMap = new JobMap();
    m_mutexSemaphore = Assertions.assertNotNull(createMutexSemaphore());
    m_executor = Assertions.assertNotNull(createExecutor());
  }

  /**
   * Runs the given job synchronously on behalf of the current model-thread. This call blocks the caller as long as the
   * given job is running.
   * <p/>
   * <strong>The calling thread must be the model-thread himself.</strong>
   * <p/>
   * Do not use this method directly. Use {@link ModelJob#runNow()} instead.
   *
   * @param job
   *          the {@link ModelJob} to be run.
   * @param callable
   *          the {@link Callable} to be executed.
   * @return the computed result.
   * @throws ProcessingException
   *           if the job throws an exception during execution.
   * @throws JobExecutionException
   *           if the job is already running or not called on behalf of the model-thread.
   */
  public <R> R runNow(final ModelJob<R> job, final Callable<R> callable) throws ProcessingException, JobExecutionException {
    Assertions.assertNotNull(callable);

    if (!isModelThread()) {
      throw new JobExecutionException(String.format("Wrong thread: The calling thread must be the model-thread to run model jobs in 'runNow' style. [thread=%s]", Thread.currentThread().getName()));
    }

    // Create a 'RunNow'-Future if not running yet.
    final RunNowFuture<R> future = m_jobMap.putIfAbsentElseReject(job, new IPutCallback<R, RunNowFuture<R>>() {

      @Override
      public RunNowFuture<R> onAbsent() {
        return new RunNowFuture<R>(Thread.currentThread());
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

  /**
   * Runs the given job asynchronously on behalf of the model-thread at the next reasonable opportunity. The caller of
   * this method continues to run in parallel.
   * <p/>
   * If the given job is rejected by the executor the time being scheduled, the job is <code>cancelled</code>. This
   * occurs if no more threads or queue slots are available, or upon shutdown of the executor.
   * <p/>
   * <strong>Do not wait for this job to complete if being a model-job yourself as this would cause a deadlock.</strong>
   * <p/>
   * Do not use this method directly. Use {@link ModelJob#schedule()} instead.
   *
   * @param job
   *          the {@link ModelJob} to be scheduled.
   * @param callable
   *          the {@link Callable} to be executed.
   * @return {@link Future} to wait for the job's completion or to cancel the job execution.
   * @throws JobExecutionException
   *           if the job is already running.
   */
  public <R> Future<R> schedule(final ModelJob<R> job, final Callable<R> callable) throws JobExecutionException {
    Assertions.assertNotNull(job);
    Assertions.assertNotNull(callable);

    // Schedule and register the job if not running yet (is unregistered in 'onAfter' or 'onReject').
    return m_jobMap.putIfAbsentElseReject(job, new IPutCallback<R, Future<R>>() {

      @Override
      public Future<R> onAbsent() {
        final Task<R> task = createModelTask(job, interceptCallable(callable));

        // Try to acquire the model-mutex if available at the time of invocation (is released in 'onAfter' or 'onReject').
        // Otherwise, the task is put into the queue of pending tasks and will compete for the model-mutex once all already queued tasks acquired/released the model-mutex.
        if (m_mutexSemaphore.tryAcquireElseOfferTail(task)) {
          task.schedule();
        }

        return task.getFuture();
      }
    });
  }

  /**
   * Creates a task representing the given {@link ModelJob} to be passed to the executor.
   *
   * @param job
   *          {@link ModelJob}.
   * @param callable
   *          {@link Callable} to be executed.
   * @return {@link Task} to be passed to the executor.
   */
  protected <R> Task<R> createModelTask(final ModelJob<R> job, final Callable<R> callable) {
    return new Task<R>(job.getName(), m_executor, m_mutexSemaphore) {

      @Override
      protected void onBefore() {
        m_mutexSemaphore.registerAsModelThread();
      }

      @Override
      protected R onCall() throws Exception {
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
          // Pass the model-mutex to the next queued job.
          final Task<?> nextTask = m_mutexSemaphore.pollElseRelease();
          if (nextTask != null) {
            nextTask.schedule();
          }
        }
      }

      @Override
      protected void onRejected() {
        final Future<R> future = getFuture();
        future.cancel(true); // to interrupt the submitter if waiting for the job to complete.
        m_jobMap.remove(future);

        // Pass the model-mutex to the next queued job.
        final Task<?> nextTask = m_mutexSemaphore.pollElseRelease();
        if (nextTask != null) {
          nextTask.schedule();
        }
      }
    };
  }

  /**
   * @return <code>true</code> if the given job is currently blocked because waiting for a {@link IBlockingCondition} to
   *         fall.
   */
  public boolean isBlocked(final ModelJob<?> modelJob) {
    return m_blockedJobs.contains(modelJob);
  }

  /**
   * @return <code>true</code> if the calling thread is the model-thread.
   */
  public boolean isModelThread() {
    return m_mutexSemaphore.isModelThread();
  }

  /**
   * @return <code>true</code> if the model-mutex is currently not acquired.
   */
  public boolean isIdle() {
    return m_mutexSemaphore.isIdle();
  }

  /**
   * Blocks the calling thread until the model-mutex gets available. Does not block if available at time of invocation.
   *
   * @param timeout
   *          the maximal time to wait for the model-mutex to become available.
   * @param unit
   *          unit of the given timeout.
   * @return <code>false</code> if the deadline has elapsed upon return, else <code>true</code>.
   * @throws InterruptedException
   * @see {@link #isIdle()}
   */
  public boolean waitForIdle(final long timeout, final TimeUnit unit) throws InterruptedException {
    return m_mutexSemaphore.waitForIdle(timeout, unit);
  }

  /**
   * Attempts to cancel execution of the given job.
   *
   * @param job
   *          the job to be canceled.
   * @param interruptIfRunning
   *          <code>true</code> if the thread executing this job should be interrupted; otherwise, in-progress jobs
   *          are allowed to complete.
   * @return <code>false</code> if the job could not be cancelled, typically because it has already completed normally;
   *         <code>true</code> otherwise.
   * @see IFuture#cancel(boolean)
   */
  public boolean cancel(final IJob<?> job, final boolean interruptIfRunning) {
    return m_jobMap.cancel(job, interruptIfRunning);
  }

  /**
   * @return <code>true</code> if the given job was cancelled before it completed normally.
   */
  public boolean isCanceled(final IJob<?> job) {
    return m_jobMap.isCancelled(job);
  }

  /**
   * @return {@link Future} associated with the given job; is <code>null</code> if not scheduled or already completed.
   */
  public Future<?> getFuture(final ModelJob<?> job) {
    return m_jobMap.getFuture(job);
  }

  /**
   * Interrupts a possible running job, rejects pending jobs and interrupts jobs waiting for a blocking condition to
   * fall. After having shutdown, this {@link ModelJobManager} cannot be used anymore.
   */
  public void shutdown() {
    m_executor.shutdownNow();

    final Set<Future<?>> futures = m_jobMap.clear();
    for (final Future<?> future : futures) {
      future.cancel(true); // to interrupt the submitter if waiting for the job to complete.
    }

    m_mutexSemaphore.clear();
  }

  /**
   * Creates a blocking condition to put a {@link ModelJob} into waiting mode and let another job acquire the
   * model-mutex. This condition can be used across multiple model-threads to wait for the same condition; this
   * condition is reusable upon signaling.
   *
   * @param name
   *          the name of the blocking condition; primarily used for debugging purpose.
   * @return {@link IBlockingCondition}.
   */
  public IBlockingCondition createBlockingCondition(final String name) {
    return new BlockingCondition(name);
  }

  /**
   * Creates a {@link MutexSemaphore} to manage acquisition of the model-mutex.
   */
  protected MutexSemaphore<Task<?>> createMutexSemaphore() {
    return new MutexSemaphore<>();
  }

  /**
   * Creates a {@link IProgressMonitor} for the given {@link ModelJob}.
   */
  public <R> IProgressMonitor createProgressMonitor(final ModelJob<R> job) {
    return new IProgressMonitor() {

      @Override
      public boolean isCancelled() {
        return isCanceled(job);
      }
    };
  }

  /**
   * Creates the {@link ExecutorService} to run model jobs within this {@link ModelJobManager} in mutual-exclusion
   * manner.
   */
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
   * To visit the running and all pending model jobs.
   *
   * @param visitor
   *          {@link IJobVisitor} called for each {@link ModelJob}.
   */
  public void visit(final IJobVisitor visitor) {
    m_jobMap.visit(visitor);
  }

  /**
   * This method can be used to intercept the concrete {@link Callable} given to the executor for execution. This method
   * is called just before executing the job. The default implementation simply returns the given {@link Callable}.
   *
   * @param callable
   *          {@link Callable} to decorate.
   * @return decorated {@link Callable} to be passed to an {@link ExecutorService};; must not be <code>null</code>.
   */
  protected <R> Callable<R> interceptCallable(final Callable<R> callable) {
    return callable;
  }

  /**
   * @see IBlockingCondition
   */
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

      final ModelJob<?> currentJob = ModelJob.get();

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
