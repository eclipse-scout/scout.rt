/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.platform.job.internal;

import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Predicate;

import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.IPlatform.State;
import org.eclipse.scout.rt.platform.IPlatformListener;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.PlatformEvent;
import org.eclipse.scout.rt.platform.chain.callable.CallableChain;
import org.eclipse.scout.rt.platform.config.CONFIG;
import org.eclipse.scout.rt.platform.config.PlatformConfigProperties.JobManagerAllowCoreThreadTimeoutProperty;
import org.eclipse.scout.rt.platform.config.PlatformConfigProperties.JobManagerCorePoolSizeProperty;
import org.eclipse.scout.rt.platform.config.PlatformConfigProperties.JobManagerKeepAliveTimeProperty;
import org.eclipse.scout.rt.platform.config.PlatformConfigProperties.JobManagerMaximumPoolSizeProperty;
import org.eclipse.scout.rt.platform.config.PlatformConfigProperties.JobManagerPrestartCoreThreadsProperty;
import org.eclipse.scout.rt.platform.context.RunContextRunner;
import org.eclipse.scout.rt.platform.context.RunMonitor;
import org.eclipse.scout.rt.platform.job.IBlockingCondition;
import org.eclipse.scout.rt.platform.job.IFuture;
import org.eclipse.scout.rt.platform.job.IJobManager;
import org.eclipse.scout.rt.platform.job.JobInput;
import org.eclipse.scout.rt.platform.job.JobState;
import org.eclipse.scout.rt.platform.job.internal.ExecutionSemaphore.QueuePosition;
import org.eclipse.scout.rt.platform.job.listener.IJobListener;
import org.eclipse.scout.rt.platform.job.listener.JobEvent;
import org.eclipse.scout.rt.platform.job.listener.JobEventData;
import org.eclipse.scout.rt.platform.job.listener.JobEventType;
import org.eclipse.scout.rt.platform.logger.DiagnosticContextValueProcessor;
import org.eclipse.scout.rt.platform.util.Assertions;
import org.eclipse.scout.rt.platform.util.Assertions.AssertionException;
import org.eclipse.scout.rt.platform.util.IRegistrationHandle;
import org.eclipse.scout.rt.platform.util.ThreadLocalProcessor;
import org.eclipse.scout.rt.platform.util.concurrent.Callables;
import org.eclipse.scout.rt.platform.util.concurrent.IRunnable;
import org.quartz.Trigger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default implementation of {@link IJobManager}.
 * <p>
 * This job manager is based on {@link ThreadPoolExecutor}, {@link DelayedExecutor} and Quartz {@link Trigger} to
 * compute firing times.
 * <p>
 * Jobs which run immediately and exactly one time are executed directly via {@link ExecutorService}. For all other
 * jobs, they are first queued via {@link DelayedExecutor}, and will commence execution once the trigger's first fire
 * time elapses. In turn, they are also given to {@link ExecutorService} for execution, so Quartz simply provides the
 * firing facility.
 *
 * @since 5.1
 */
@ApplicationScoped
public class JobManager implements IJobManager {

  private static final Logger LOG = LoggerFactory.getLogger(JobManager.class);

  protected final ExecutorService m_executor;
  protected final DelayedExecutor m_delayedExecutor;

  protected final FutureSet m_futures;
  protected final JobListeners m_listeners;

  protected final ReentrantReadWriteLock m_shutdownLock;
  protected volatile boolean m_shutdown;

  public JobManager() {
    m_executor = createExecutor();
    m_delayedExecutor = new DelayedExecutor(m_executor, "scout-scheduler-thread");
    m_listeners = BEANS.get(JobListeners.class);
    m_futures = BEANS.get(FutureSet.class);
    m_futures.init(this);
    m_shutdownLock = new ReentrantReadWriteLock();
  }

  @Override
  public final IFuture<Void> schedule(final IRunnable runnable, final JobInput input) {
    return schedule(Callables.callable(runnable), ensureJobInputName(input, runnable.getClass().getName()));
  }

  @Override
  public final <RESULT> IFuture<RESULT> schedule(final Callable<RESULT> callable, final JobInput input) {
    Assertions.assertNotNull(input, "JobInput must not be null");
    Assertions.assertFalse(isShutdown(), "{} not available because the platform has been shut down.", getClass().getSimpleName());

    final JobFutureTask<RESULT> futureTask = createJobFutureTask(callable, input);
    submit(futureTask);

    return futureTask;
  }

  /**
   * Submits the given future for asynchronous execution. Upon expiry of its fire time and the acquisition of a
   * potential execution permit, the future is given to {@link ExecutorService}. In turn, a worker thread is allocated
   * and execution commenced.
   */
  protected <RESULT> void submit(final JobFutureTask<RESULT> futureTask) {
    try {
      futureTask.changeState(JobState.SCHEDULED);

      // Schedule the task via ExecutorService, or delayed via DelayedExecutor.
      if (futureTask.isSingleExecution() && !futureTask.isDelayedExecution()) {
        competeForPermitAndExecute(futureTask, futureTask);
      }
      else {
        if (futureTask.isDelayedExecution()) {
          futureTask.changeState(JobState.PENDING);
        }

        m_delayedExecutor.schedule(() -> competeForPermitAndExecute(futureTask, new FutureRunner<>(JobManager.this, futureTask)), futureTask.getFirstFireTime());
      }
    }
    catch (final RuntimeException | Error e) { // NOSONAR
      futureTask.reject();
      throw e;
    }
  }

  /**
   * Competes for an execution permit (if semaphore aware) and executes the runnable via {@link ExecutorService}.
   */
  protected void competeForPermitAndExecute(final JobFutureTask<?> futureTask, final IRejectableRunnable futureRunner) {
    final ExecutionSemaphore executionSemaphore = futureTask.getExecutionSemaphore();
    if (executionSemaphore == null) {
      m_executor.execute(futureRunner);
    }
    else {
      futureTask.changeState(JobState.WAITING_FOR_PERMIT);
      executionSemaphore.compete(futureTask, QueuePosition.TAIL, () -> m_executor.execute(futureRunner));
    }
  }

  @Override
  public boolean isDone(final Predicate<IFuture<?>> filter) {
    return m_futures.matchesEvery(filter, CompletionPromise.FUTURE_DONE_MATCHER);
  }

  @Override
  public void awaitDone(final Predicate<IFuture<?>> filter, final long timeout, final TimeUnit unit) {
    try {
      m_futures.awaitDone(filter, timeout, unit);
    }
    catch (final TimeoutException e) {
      throw BEANS.get(JobExceptionTranslator.class).translateTimeoutException(e, "Failed to wait for jobs to complete because the maximal wait time elapsed", timeout, unit);
    }
    catch (final InterruptedException e) {
      Thread.currentThread().interrupt(); // Restore the interrupted status because cleared by catching InterruptedException.
      throw BEANS.get(JobExceptionTranslator.class).translateInterruptedException(e, "Interrupted while waiting for jobs to complete");
    }
  }

  @Override
  public void awaitFinished(final Predicate<IFuture<?>> filter, final long timeout, final TimeUnit unit) {
    try {
      m_futures.awaitFinished(filter, timeout, unit);
    }
    catch (final TimeoutException e) {
      throw BEANS.get(JobExceptionTranslator.class).translateTimeoutException(e, "Failed to wait for jobs to complete because the maximal wait time elapsed", timeout, unit);
    }
    catch (final InterruptedException e) {
      Thread.currentThread().interrupt(); // Restore the interrupted status because cleared by catching InterruptedException.
      throw BEANS.get(JobExceptionTranslator.class).translateInterruptedException(e, "Interrupted while waiting for jobs to complete");
    }
  }

  @Override
  public boolean cancel(final Predicate<IFuture<?>> filter, final boolean interruptIfRunning) {
    return m_futures.cancel(filter, interruptIfRunning);
  }

  @Override
  public boolean isShutdown() {
    return m_shutdown;
  }

  @Override
  public final void shutdown() {
    LOG.debug("JobManager shutting down.");
    m_shutdownLock.writeLock().lock();
    try {
      m_shutdown = true;
    }
    finally {
      m_shutdownLock.writeLock().unlock();
    }

    // Dispose Futures.
    m_futures.dispose();

    // Shutdown the Executor.
    shutdownExecutor(m_executor);

    // Fire event that job manager was shutdown.
    fireEvent(new JobEvent(this, JobEventType.JOB_MANAGER_SHUTDOWN, new JobEventData()));
  }

  @Override
  public Set<IFuture<?>> getFutures(final Predicate<IFuture<?>> filter) {
    return m_futures.values(filter);
  }

  @Override
  public IRegistrationHandle addListener(final IJobListener listener) {
    return addListener(null, listener);
  }

  @Override
  public IRegistrationHandle addListener(final Predicate<JobEvent> filter, final IJobListener listener) {
    return m_listeners.add(filter, listener);
  }

  protected void fireEvent(final JobEvent eventToFire) {
    m_listeners.notifyListeners(eventToFire);
  }

  /**
   * Creates the Future to be given to {@link ExecutorService} and registers it with this job manager.
   *
   * @param callable
   *          callable to be given to the executor for execution.
   * @param input
   *          input that describes the job to be executed.
   */
  protected <RESULT> JobFutureTask<RESULT> createJobFutureTask(final Callable<RESULT> callable, final JobInput input) {
    final RunMonitor runMonitor = Assertions.assertNotNull(input.getRunContext() != null ? input.getRunContext().getRunMonitor() : BEANS.get(RunMonitor.class), "'RunMonitor' required if providing a 'RunContext'");

    final JobInput inputCopy = ensureJobInputName(input, callable.getClass().getName());
    return new JobFutureTask<>(this, runMonitor, inputCopy, new CallableChain<>(), callable);
  }

  /**
   * Creates the executor to run jobs.
   */
  protected ExecutorService createExecutor() {
    final int corePoolSize = CONFIG.getPropertyValue(JobManagerCorePoolSizeProperty.class);
    final int maximumPoolSize = CONFIG.getPropertyValue(JobManagerMaximumPoolSizeProperty.class);
    final long keepAliveTime = CONFIG.getPropertyValue(JobManagerKeepAliveTimeProperty.class);
    final boolean allowCoreThreadTimeOut = CONFIG.getPropertyValue(JobManagerAllowCoreThreadTimeoutProperty.class);
    final boolean prestartCoreThreads = CONFIG.getPropertyValue(JobManagerPrestartCoreThreadsProperty.class);

    // Create the rejection handler.
    final RejectedExecutionHandler rejectHandler = (runnable, executor) -> {
      if (isShutdown()) {
        LOG.debug("Job rejected because the job manager is shutdown.");
      }
      else {
        // Do not propagate exception, because the caller is not the submitting thread.
        LOG.error("Job rejected because no more threads or queue slots available. [runnable={}]", runnable);
      }

      if (runnable instanceof IRejectableRunnable) {
        ((IRejectableRunnable) runnable).reject();
      }
    };

    final ThreadPoolExecutor executor = new ThreadPoolExecutor(corePoolSize, maximumPoolSize, keepAliveTime, TimeUnit.SECONDS, new SynchronousQueue<>(), new NamedThreadFactory("scout-thread"), rejectHandler);
    executor.allowCoreThreadTimeOut(allowCoreThreadTimeOut);
    if (prestartCoreThreads) {
      executor.prestartAllCoreThreads();
    }

    return executor;
  }

  /**
   * Returns the internal Executor Service.
   */
  public ExecutorService getExecutor() {
    return m_executor;
  }

  /**
   * Returns the internal delayed Executor Service.
   */
  protected DelayedExecutor getDelayedExecutor() {
    return m_delayedExecutor;
  }

  /**
   * Method invoked to shutdown the executor.
   */
  protected void shutdownExecutor(final ExecutorService executor) {
    executor.shutdownNow();
    try {
      LOG.info("Shutdown ExecutorService, await termination...");
      boolean terminated = executor.awaitTermination(1, TimeUnit.MINUTES);
      LOG.info("Shutdown ExecutorService, await termination returned terminated={}", terminated);
    }
    catch (final InterruptedException e) {
      // NOOP
    }
  }

  /**
   * Method invoked to contribute to the {@link CallableChain} which finally executes the {@link Callable}. Overwrite
   * this method to contribute some behavior to the execution of the {@link Callable}.
   * <p>
   * Contributions are plugged according to the design pattern: 'chain-of-responsibility'.<br/>
   * To contribute to the end of the chain (meaning that you are invoked <strong>after</strong> the contributions of
   * super classes and therefore can base on their contributed functionality), you can use constructions of the
   * following form:
   *
   * <pre>
   * this.interceptCallableChain(callableChain, future, runMonitor, input);
   * callableChain.addLast(new YourDecorator());
   * </pre>
   *
   * To be invoked <strong>before</strong> the super class contributions, you can use constructions of the following
   * form:
   *
   * <pre>
   * this.interceptCallableChain(callableChain, future, runMonitor, input);
   * callableChain.addFirst(new YourDecorator());
   * </pre>
   *
   * @param callableChain
   *          the chain to intercept the {@link Callable} before execution.
   */
  protected <RESULT> void interceptCallableChain(final CallableChain<RESULT> callableChain, final JobFutureTask<?> future, final RunMonitor runMonitor, final JobInput input) {
    callableChain
        .add(new CallableChainExceptionHandler<>())
        .add(new ThreadLocalProcessor<>(IFuture.CURRENT, future))
        .add(new ThreadLocalProcessor<>(RunMonitor.CURRENT, runMonitor))
        .add(BEANS.get(ThreadNameDecorator.class))
        .add(new DiagnosticContextValueProcessor(BEANS.get(JobNameContextValueProvider.class)))
        .add(new RunContextRunner<>(input.getRunContext()))
        .add(new ExceptionProcessor<>(input)); // must follow RunContextRunner to handle exception in proper RunContext
  }

  @Override
  public IBlockingCondition newBlockingCondition(final boolean blocking) {
    return new BlockingCondition(blocking);
  }

  /**
   * Registers the given future.
   *
   * @throws AssertionException
   *           if the job manager is shut down.
   */
  protected void registerFuture(final JobFutureTask<?> future) {
    m_shutdownLock.readLock().lock();
    try {
      Assertions.assertFalse(isShutdown(), "{} not available because the platform has been shut down.", getClass().getSimpleName());
      m_futures.add(future);
    }
    finally {
      m_shutdownLock.readLock().unlock();
    }
  }

  protected void unregisterFuture(final JobFutureTask<?> future) {
    m_futures.remove(future);
  }

  /**
   * Ensures that the given job input has a name set. If already set, the input is returned, or otherwise, a copy of the
   * input is returned with the given <code>defaultName</code> set as name.
   */
  protected JobInput ensureJobInputName(final JobInput input, final String defaultName) {
    if (input != null && input.getName() == null) {
      return input.copy().withName(defaultName);
    }
    return input;
  }

  /**
   * {@link IPlatformListener} to shutdown this job manager upon platform shutdown.
   */
  @Order(IJobManager.DESTROY_ORDER)
  public static class PlatformListener implements IPlatformListener {

    @Override
    public void stateChanged(final PlatformEvent event) {
      if (event.getState() == State.PlatformStopping) {
        BEANS.get(JobManager.class).shutdown();
      }
    }
  }
}
