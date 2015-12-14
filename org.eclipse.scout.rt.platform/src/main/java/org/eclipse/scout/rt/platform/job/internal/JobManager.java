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

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.IPlatform;
import org.eclipse.scout.rt.platform.IPlatformListener;
import org.eclipse.scout.rt.platform.PlatformEvent;
import org.eclipse.scout.rt.platform.annotations.Internal;
import org.eclipse.scout.rt.platform.chain.callable.CallableChain;
import org.eclipse.scout.rt.platform.config.CONFIG;
import org.eclipse.scout.rt.platform.config.PlatformConfigProperties.JobManagerAllowCoreThreadTimeoutProperty;
import org.eclipse.scout.rt.platform.config.PlatformConfigProperties.JobManagerCorePoolSizeProperty;
import org.eclipse.scout.rt.platform.config.PlatformConfigProperties.JobManagerDispatcherThreadCountProperty;
import org.eclipse.scout.rt.platform.config.PlatformConfigProperties.JobManagerKeepAliveTimeProperty;
import org.eclipse.scout.rt.platform.config.PlatformConfigProperties.JobManagerMaximumPoolSizeProperty;
import org.eclipse.scout.rt.platform.context.RunContextRunner;
import org.eclipse.scout.rt.platform.context.RunMonitor;
import org.eclipse.scout.rt.platform.exception.PlatformException;
import org.eclipse.scout.rt.platform.filter.IFilter;
import org.eclipse.scout.rt.platform.job.IBlockingCondition;
import org.eclipse.scout.rt.platform.job.IFuture;
import org.eclipse.scout.rt.platform.job.IJobListenerRegistration;
import org.eclipse.scout.rt.platform.job.IJobManager;
import org.eclipse.scout.rt.platform.job.IMutex.QueuePosition;
import org.eclipse.scout.rt.platform.job.JobInput;
import org.eclipse.scout.rt.platform.job.JobState;
import org.eclipse.scout.rt.platform.job.listener.IJobListener;
import org.eclipse.scout.rt.platform.job.listener.JobEvent;
import org.eclipse.scout.rt.platform.job.listener.JobEventType;
import org.eclipse.scout.rt.platform.util.Assertions;
import org.eclipse.scout.rt.platform.util.ThreadLocalProcessor;
import org.eclipse.scout.rt.platform.util.concurrent.Callables;
import org.eclipse.scout.rt.platform.util.concurrent.IRunnable;
import org.eclipse.scout.rt.platform.visitor.IVisitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default implementation of {@link IJobManager}.
 * <p/>
 * This job manager is based on {@link ThreadPoolExecutor} and not {@link ScheduledThreadPoolExecutor} because
 * {@link ScheduledThreadPoolExecutor} is based on a fixed-size thread pool. That means, that once the
 * <code>core-pool-size</code> is exceeded, the creation of on-demand threads up to a <code>maximum-pool-size</code>
 * would not be supported. Instead, 'delayed scheduling' is implemented by {@link DelayedExecutor}.
 *
 * @since 5.1
 */
@ApplicationScoped
public class JobManager implements IJobManager, IPlatformListener {

  private static final Logger LOG = LoggerFactory.getLogger(JobManager.class);

  protected final ExecutorService m_executor;
  protected final FutureSet m_futures;
  protected final JobListeners m_listeners;
  private final DelayedExecutor m_delayedExecutor;

  public JobManager() {
    m_executor = Assertions.assertNotNull(createExecutor());
    m_delayedExecutor = new DelayedExecutor(m_executor, "internal-dispatcher", CONFIG.getPropertyValue(JobManagerDispatcherThreadCountProperty.class));
    m_listeners = BEANS.get(JobListeners.class);
    m_futures = BEANS.get(FutureSet.class);
    m_futures.init(this);
  }

  @Override
  public IFuture<Void> schedule(final IRunnable runnable, final JobInput input) {
    return schedule(Callables.callable(runnable), ensureJobInputName(input, runnable.getClass().getName()));
  }

  @Override
  public final <RESULT> IFuture<RESULT> schedule(final Callable<RESULT> callable, final JobInput input) {
    Assertions.assertNotNull(input, "'JobInput' must not be null");
    final JobFutureTask<RESULT> futureTask = createJobFutureTask(callable, input);

    switch (input.getSchedulingRule()) {
      case JobInput.SCHEDULING_RULE_SINGLE_EXECUTION:
        scheduleDelayed(futureTask, futureTask, input.getSchedulingDelay());
        break;
      case JobInput.SCHEDULING_RULE_PERIODIC_EXECUTION_AT_FIXED_RATE:
        scheduleDelayed(futureTask, new FixedRateRunnable(m_executor, m_delayedExecutor, futureTask, input.getPeriodicDelay()), input.getSchedulingDelay());
        break;
      case JobInput.SCHEDULING_RULE_PERIODIC_EXECUTION_WITH_FIXED_DELAY:
        scheduleDelayed(futureTask, new FixedDelayRunnable(m_executor, m_delayedExecutor, futureTask, input.getPeriodicDelay()), input.getSchedulingDelay());
        break;
      default:
        throw new UnsupportedOperationException("Unsupported scheduling rule");
    }

    return futureTask;
  }

  @Override
  public boolean isDone(final IFilter<IFuture<?>> filter) {
    return m_futures.matchesAll(filter, DonePromise.FUTURE_DONE_MATCHER);
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
  public boolean cancel(final IFilter<IFuture<?>> filter, final boolean interruptIfRunning) {
    return m_futures.cancel(filter, interruptIfRunning);
  }

  @Override
  public final void shutdown() {
    m_futures.dispose();
    m_executor.shutdownNow();
    try {
      m_executor.awaitTermination(1, TimeUnit.MINUTES);
    }
    catch (final java.lang.InterruptedException e) {
      // NOOP
    }

    fireEvent(new JobEvent(this, JobEventType.JOB_MANAGER_SHUTDOWN));
  }

  @Override
  public final void visit(final IFilter<IFuture<?>> filter, final IVisitor<IFuture<?>> visitor) {
    m_futures.visit(filter, visitor);
  }

  @Override
  public IJobListenerRegistration addListener(final IJobListener listener) {
    return addListener(null, listener);
  }

  @Override
  public IJobListenerRegistration addListener(final IFilter<JobEvent> filter, final IJobListener listener) {
    return m_listeners.add(filter, listener);
  }

  /**
   * Runs the given task asynchronously, but not before the given delay elapses.
   */
  @Internal
  protected void scheduleDelayed(final JobFutureTask<?> futureTask, final IRejectableRunnable runnable, final long schedulingDelay) {
    futureTask.changeState(JobState.SCHEDULED);

    if (schedulingDelay <= 0L) {
      competeForMutexAndExecute(futureTask, runnable);
    }
    else {
      futureTask.changeState(JobState.PENDING);
      m_delayedExecutor.schedule(new IRejectableRunnable() {

        @Override
        public void run() {
          competeForMutexAndExecute(futureTask, runnable);
        }

        @Override
        public void reject() {
          runnable.reject();
        }
      }, schedulingDelay, TimeUnit.MILLISECONDS);
    }
  }

  /**
   * Executes the given runnable, and if being a mutually exclusive task, acquires the mutex first.
   */
  private void competeForMutexAndExecute(final JobFutureTask<?> futureTask, final IRejectableRunnable runnable) {
    if (runnable instanceof JobFutureTask && ((JobFutureTask) runnable).getMutex() != null) {
      final JobFutureTask<?> mutexTask = (JobFutureTask) runnable;

      futureTask.changeState(JobState.WAITING_FOR_MUTEX);
      mutexTask.getMutex().compete(mutexTask, QueuePosition.TAIL, new IMutexAcquiredCallback() {

        @Override
        public void onMutexAcquired() {
          m_executor.execute(mutexTask);
        }
      });
    }
    else {
      m_executor.execute(runnable);
    }
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
  @Internal
  protected ExecutorService createExecutor() {
    final int corePoolSize = CONFIG.getPropertyValue(JobManagerCorePoolSizeProperty.class);
    final int maximumPoolSize = CONFIG.getPropertyValue(JobManagerMaximumPoolSizeProperty.class);
    final long keepAliveTime = CONFIG.getPropertyValue(JobManagerKeepAliveTimeProperty.class);
    final boolean allowCoreThreadTimeOut = CONFIG.getPropertyValue(JobManagerAllowCoreThreadTimeoutProperty.class);
    final int dispatcherThreadCount = CONFIG.getPropertyValue(JobManagerDispatcherThreadCountProperty.class);

    // Create the rejection handler.
    final RejectedExecutionHandler rejectHandler = new RejectedExecutionHandler() {

      @Override
      public void rejectedExecution(final Runnable runnable, final ThreadPoolExecutor executor) {
        if (executor.isShutdown()) {
          LOG.debug("Job rejected because the job manager is shutdown.");
        }
        else {
          LOG.error("Job rejected because no more threads or queue slots available. [runnable={}]", runnable);
        }

        if (runnable instanceof IRejectableRunnable) {
          ((IRejectableRunnable) runnable).reject();
        }
      }
    };

    final ThreadPoolExecutor executor = new ThreadPoolExecutor(corePoolSize + dispatcherThreadCount, maximumPoolSize, keepAliveTime, TimeUnit.SECONDS, new SynchronousQueue<Runnable>(), new NamedThreadFactory("scout-thread"), rejectHandler);
    executor.allowCoreThreadTimeOut(allowCoreThreadTimeOut);
    return executor;
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
        .add(new ThreadNameDecorator())
        .add(new RunContextRunner<RESULT>(input.getRunContext()))
        .add(new ExceptionProcessor<RESULT>(input)); // must following RunContextRunner to handle exception in proper RunContext
  }

  @Override
  public IBlockingCondition newBlockingCondition(final boolean blocking) {
    return new BlockingCondition(blocking);
  }

  @Internal
  protected void registerFuture(final JobFutureTask<?> future) {
    m_futures.add(future);
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

  // ==== IPlatformListener ==== //
  @Override
  public void stateChanged(final PlatformEvent event) throws PlatformException {
    if (event.getState() == IPlatform.State.PlatformStopping) {
      shutdown();
    }
  }
}
