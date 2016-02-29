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

import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.IPlatform.State;
import org.eclipse.scout.rt.platform.IPlatformListener;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.PlatformEvent;
import org.eclipse.scout.rt.platform.annotations.Internal;
import org.eclipse.scout.rt.platform.chain.callable.CallableChain;
import org.eclipse.scout.rt.platform.config.CONFIG;
import org.eclipse.scout.rt.platform.config.PlatformConfigProperties.JobManagerAllowCoreThreadTimeoutProperty;
import org.eclipse.scout.rt.platform.config.PlatformConfigProperties.JobManagerCorePoolSizeProperty;
import org.eclipse.scout.rt.platform.config.PlatformConfigProperties.JobManagerKeepAliveTimeProperty;
import org.eclipse.scout.rt.platform.config.PlatformConfigProperties.JobManagerMaximumPoolSizeProperty;
import org.eclipse.scout.rt.platform.config.PlatformConfigProperties.JobManagerPrestartCoreThreadsProperty;
import org.eclipse.scout.rt.platform.context.RunContextRunner;
import org.eclipse.scout.rt.platform.context.RunMonitor;
import org.eclipse.scout.rt.platform.filter.IFilter;
import org.eclipse.scout.rt.platform.job.IBlockingCondition;
import org.eclipse.scout.rt.platform.job.IFuture;
import org.eclipse.scout.rt.platform.job.IJobManager;
import org.eclipse.scout.rt.platform.job.JobInput;
import org.eclipse.scout.rt.platform.job.JobState;
import org.eclipse.scout.rt.platform.job.internal.ExecutionSemaphore.IPermitAcquiredCallback;
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

    // Create the Future to be given to the ExecutorService.
    final JobFutureTask<RESULT> futureTask = createJobFutureTask(callable, input);
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

        m_delayedExecutor.schedule(new Runnable() {

          @Override
          public void run() {
            competeForPermitAndExecute(futureTask, new FutureRunner<>(JobManager.this, futureTask));
          }
        }, futureTask.getFirstFireTime());
      }

      return futureTask;
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
      executionSemaphore.compete(futureTask, QueuePosition.TAIL, new IPermitAcquiredCallback() {

        @Override
        public void onPermitAcquired() {
          m_executor.execute(futureRunner);
        }
      });
    }
  }

  @Override
  public boolean isDone(final IFilter<IFuture<?>> filter) {
    return m_futures.matchesEvery(filter, CompletionPromise.FUTURE_DONE_MATCHER);
  }

  @Override
  public void awaitDone(final IFilter<IFuture<?>> filter, final long timeout, final TimeUnit unit) {
    try {
      m_futures.awaitDone(filter, timeout, unit);
    }
    catch (final java.util.concurrent.TimeoutException e) {
      throw BEANS.get(JobExceptionTranslator.class).translateTimeoutException(e, "Failed to wait for jobs to complete because the maximal wait time elapsed", timeout, unit);
    }
    catch (final java.lang.InterruptedException e) {
      Thread.currentThread().interrupt(); // Restore the interrupted status because cleared by catching InterruptedException.
      throw BEANS.get(JobExceptionTranslator.class).translateInterruptedException(e, "Interrupted while waiting for jobs to complete");
    }
  }

  @Override
  public void awaitFinished(final IFilter<IFuture<?>> filter, final long timeout, final TimeUnit unit) {
    try {
      m_futures.awaitFinished(filter, timeout, unit);
    }
    catch (final java.util.concurrent.TimeoutException e) {
      throw BEANS.get(JobExceptionTranslator.class).translateTimeoutException(e, "Failed to wait for jobs to complete because the maximal wait time elapsed", timeout, unit);
    }
    catch (final java.lang.InterruptedException e) {
      Thread.currentThread().interrupt(); // Restore the interrupted status because cleared by catching InterruptedException.
      throw BEANS.get(JobExceptionTranslator.class).translateInterruptedException(e, "Interrupted while waiting for jobs to complete");
    }
  }

  @Override
  public boolean cancel(final IFilter<IFuture<?>> filter, final boolean interruptIfRunning) {
    return m_futures.cancel(filter, interruptIfRunning);
  }

  @Override
  public boolean isShutdown() {
    return m_shutdown;
  }

  @Override
  public final void shutdown() {
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
  public Set<IFuture<?>> getFutures(final IFilter<IFuture<?>> filter) {
    return m_futures.values(filter);
  }

  @Override
  public IRegistrationHandle addListener(final IJobListener listener) {
    return addListener(null, listener);
  }

  @Override
  public IRegistrationHandle addListener(final IFilter<JobEvent> filter, final IJobListener listener) {
    return m_listeners.add(filter, listener);
  }

  @Internal
  protected void fireEvent(final JobEvent eventToFire) {
    m_listeners.notifyListeners(eventToFire);
  }

  /**
   * Creates the Future to interact with the executable.
   *
   * @param callable
   *          callable to be given to the executor for execution.
   * @param input
   *          input that describes the job to be executed.
   */
  @Internal
  protected <RESULT> JobFutureTask<RESULT> createJobFutureTask(final Callable<RESULT> callable, final JobInput input) {
    final RunMonitor runMonitor = Assertions.assertNotNull(input.getRunContext() != null ? input.getRunContext().getRunMonitor() : BEANS.get(RunMonitor.class), "'RunMonitor' required if providing a 'RunContext'");

    final JobInput inputCopy = ensureJobInputName(input, callable.getClass().getName());
    return new JobFutureTask<>(this, runMonitor, inputCopy, new CallableChain<RESULT>(), callable);
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
    final RejectedExecutionHandler rejectHandler = new RejectedExecutionHandler() {

      @Override
      public void rejectedExecution(final Runnable runnable, final ThreadPoolExecutor executor) {
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
      }
    };

    final ThreadPoolExecutor executor = new ThreadPoolExecutor(corePoolSize, maximumPoolSize, keepAliveTime, TimeUnit.SECONDS, new SynchronousQueue<Runnable>(), new NamedThreadFactory("scout-thread"), rejectHandler);
    executor.allowCoreThreadTimeOut(allowCoreThreadTimeOut);
    if (prestartCoreThreads) {
      executor.prestartAllCoreThreads();
    }

    return executor;
  }

  /**
   * Returns the internal Executor Service.
   */
  @Internal
  protected ExecutorService getExecutor() {
    return m_executor;
  }

  /**
   * Returns the internal delayed Executor Service.
   */
  @Internal
  protected DelayedExecutor getDelayedExecutor() {
    return m_delayedExecutor;
  }

  /**
   * Method invoked to shutdown the executor.
   */
  protected void shutdownExecutor(final ExecutorService executor) {
    executor.shutdownNow();
    try {
      executor.awaitTermination(1, TimeUnit.MINUTES);
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
        .add(new ThreadLocalProcessor<>(IFuture.CURRENT, future))
        .add(new ThreadLocalProcessor<>(RunMonitor.CURRENT, runMonitor))
        .add(BEANS.get(ThreadNameDecorator.class))
        .add(new DiagnosticContextValueProcessor(BEANS.get(JobNameContextValueProvider.class)))
        .add(new RunContextRunner<RESULT>(input.getRunContext()))
        .add(new ExceptionProcessor<RESULT>(input)); // must follow RunContextRunner to handle exception in proper RunContext
  }

  @Override
  public IBlockingCondition newBlockingCondition(final boolean blocking) {
    return new BlockingCondition(blocking);
  }

  /**
   * Registers the given future.
   *
   * @throws AssertionException
   *           if the job manager is shut donw.
   */
  @Internal
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

  @Internal
  protected void unregisterFuture(final JobFutureTask<?> future) {
    m_futures.remove(future);
  }

  /**
   * Ensures that the given job input has a name set. If already set, the input is returned, or otherwise, a copy of the
   * input is returned with the given <code>defaultName</code> set as name.
   */
  @Internal
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
      if (State.PlatformStopping.equals(event.getState())) {
        BEANS.get(JobManager.class).shutdown();
      }
    }
  }
}
