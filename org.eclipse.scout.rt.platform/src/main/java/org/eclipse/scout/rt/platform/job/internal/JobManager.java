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
package org.eclipse.scout.rt.platform.job.internal;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.eclipse.scout.commons.Assertions;
import org.eclipse.scout.commons.Callables;
import org.eclipse.scout.commons.IRunnable;
import org.eclipse.scout.commons.IVisitor;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.commons.annotations.Internal;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.filter.Filters;
import org.eclipse.scout.commons.filter.IFilter;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.config.CONFIG;
import org.eclipse.scout.rt.platform.config.PlatformConfigProperties.JobManagerAllowCoreThreadTimeoutProperty;
import org.eclipse.scout.rt.platform.config.PlatformConfigProperties.JobManagerCorePoolSizeProperty;
import org.eclipse.scout.rt.platform.config.PlatformConfigProperties.JobManagerDispatcherThreadCountProperty;
import org.eclipse.scout.rt.platform.config.PlatformConfigProperties.JobManagerKeepAliveTimeProperty;
import org.eclipse.scout.rt.platform.config.PlatformConfigProperties.JobManagerMaximumPoolSizeProperty;
import org.eclipse.scout.rt.platform.job.IBlockingCondition;
import org.eclipse.scout.rt.platform.job.IFuture;
import org.eclipse.scout.rt.platform.job.IJobManager;
import org.eclipse.scout.rt.platform.job.JobInput;
import org.eclipse.scout.rt.platform.job.Jobs;
import org.eclipse.scout.rt.platform.job.internal.callable.HandleExceptionCallable;
import org.eclipse.scout.rt.platform.job.internal.callable.RunContextCallable;
import org.eclipse.scout.rt.platform.job.internal.callable.ThreadNameDecorator;
import org.eclipse.scout.rt.platform.job.listener.IJobListener;
import org.eclipse.scout.rt.platform.job.listener.JobEvent;
import org.eclipse.scout.rt.platform.job.listener.JobEventType;

/**
 * Default implementation of {@link IJobManager}.
 * <p/>
 * This job manager is not based on {@link ScheduledThreadPoolExecutor} due to its fixed-size thread pool. That means,
 * that once the <code>core-pool-size</code> is exceeded, the creation of on-demand threads up to a
 * <code>maximum-pool-size</code> would not be supported. Instead, 'delayed scheduling' is implemented by
 * {@link DelayedExecutor}.
 *
 * @since 5.1
 */
@ApplicationScoped
public class JobManager implements IJobManager {

  private static final IScoutLogger LOG = ScoutLogManager.getLogger(JobManager.class);

  @Internal
  protected final ExecutorService m_executor;
  @Internal
  protected final MutexSemaphores m_mutexSemaphores;
  @Internal
  protected final FutureSet m_futures;
  @Internal
  protected final JobListeners m_listeners;
  @Internal
  private final DelayedExecutor m_delayedExecutor;

  public JobManager() {
    m_executor = Assertions.assertNotNull(createExecutor());
    m_futures = new FutureSet();
    m_mutexSemaphores = Assertions.assertNotNull(createMutexSemaphores(m_executor));
    m_listeners = Assertions.assertNotNull(createJobListeners(m_executor));
    m_delayedExecutor = new DelayedExecutor(m_executor, "internal-dispatcher", CONFIG.getPropertyValue(JobManagerDispatcherThreadCountProperty.class));

    addListener(Jobs.newEventFilter().andMatchEventTypes(JobEventType.SCHEDULED, JobEventType.DONE, JobEventType.BLOCKED, JobEventType.UNBLOCKED, JobEventType.SHUTDOWN), m_futures);
  }

  @Override
  public final <RESULT> IFuture<RESULT> schedule(final Callable<RESULT> callable, final JobInput input) {
    final JobFutureTask<RESULT> futureTask = createJobFutureTask(callable, input, false);

    if (!futureTask.isMutexTask() || m_mutexSemaphores.tryAcquireElseOfferTail(futureTask)) {
      m_executor.execute(futureTask);
    }

    return futureTask;
  }

  @Override
  public final <RESULT> IFuture<RESULT> schedule(final Callable<RESULT> callable, final long delay, final TimeUnit delayUnit, final JobInput input) {
    final JobFutureTask<RESULT> futureTask = createJobFutureTask(callable, input, false);

    m_delayedExecutor.schedule(new Runnable() {

      @Override
      public void run() {
        if (!futureTask.isMutexTask() || m_mutexSemaphores.tryAcquireElseOfferTail(futureTask)) {
          futureTask.run();
        }
      }
    }, delay, delayUnit);

    return futureTask;
  }

  @Override
  public final IFuture<Void> scheduleAtFixedRate(final IRunnable runnable, final long initialDelay, final long period, final TimeUnit unit, final JobInput input) {
    final JobFutureTask<Void> futureTask = createJobFutureTask(Callables.callable(runnable), input, true);
    Assertions.assertFalse(futureTask.isMutexTask(), "Mutual exclusion is not supported for periodic jobs");

    m_delayedExecutor.schedule(new FixedRateRunnable(m_delayedExecutor, futureTask, period, unit), initialDelay, unit);

    return futureTask;
  }

  @Override
  public final IFuture<Void> scheduleWithFixedDelay(final IRunnable runnable, final long initialDelay, final long delay, final TimeUnit unit, final JobInput input) {
    final JobFutureTask<Void> futureTask = createJobFutureTask(Callables.callable(runnable), input, true);
    Assertions.assertFalse(futureTask.isMutexTask(), "Mutual exclusion is not supported for periodic jobs");

    m_delayedExecutor.schedule(new FixedDelayRunnable(m_delayedExecutor, futureTask, delay, unit), initialDelay, unit);

    return futureTask;
  }

  @Override
  public boolean isDone(final IFilter<IFuture<?>> filter) {
    return m_futures.isDone(filter);
  }

  @Override
  public boolean awaitDone(final IFilter<IFuture<?>> filter, final long timeout, final TimeUnit unit) throws ProcessingException {
    try {
      return m_futures.awaitDone(filter, timeout, unit);
    }
    catch (final InterruptedException e) {
      throw new ProcessingException("Interrupted while waiting for jobs to complete", e);
    }
  }

  @Override
  public boolean cancel(final IFilter<IFuture<?>> filter, final boolean interruptIfRunning) {
    return m_futures.cancel(filter, interruptIfRunning);
  }

  @Override
  public final void shutdown() {
    m_futures.clear();
    m_mutexSemaphores.clear();
    m_executor.shutdownNow();
    try {
      m_executor.awaitTermination(1, TimeUnit.MINUTES);
    }
    catch (final InterruptedException e) {
      // NOOP
    }
    m_listeners.fireEvent(new JobEvent(this, JobEventType.SHUTDOWN, null));
  }

  @Override
  public final void visit(final IFilter<IFuture<?>> filter, final IVisitor<IFuture<?>> visitor) {
    m_futures.visit(filter, visitor);
  }

  @Override
  public IJobListener addListener(final IFilter<JobEvent> filter, final IJobListener listener) {
    return m_listeners.add(listener, Filters.alwaysFilterIfNull(filter));
  }

  @Override
  public void removeListener(final IJobListener listener) {
    m_listeners.remove(listener);
  }

  /**
   * Creates the Future to interact with the executable once being executed.
   *
   * @param callable
   *          callable to be given to the executor for execution.
   * @param input
   *          input that describes the job to be executed.
   * @param periodic
   *          <code>true</code> if this is a periodic action, <code>false</code> if executed only once.
   */
  @Internal
  protected <RESULT> JobFutureTask<RESULT> createJobFutureTask(final Callable<RESULT> callable, final JobInput input, final boolean periodic) {
    Assertions.assertNotNull(input, "'JobInput' must not be null");
    if (input.runContext() != null) {
      Assertions.assertNotNull(input.runContext().runMonitor(), "'RunMonitor' required if providing a 'RunContext'");
    }

    // Ensure a job name to be set.
    final JobInput inputCopy = input.copy().name(StringUtility.nvl(input.name(), callable.getClass().getName()));

    // Create the Future to be returned to the caller.
    return interceptFuture(JobFutureTask.create(this, inputCopy, periodic, interceptCallable(callable, inputCopy)));
  }

  /**
   * Creates a semaphore to manage acquisition of the mutex objects.
   */
  @Internal
  protected MutexSemaphores createMutexSemaphores(final ExecutorService executor) {
    return new MutexSemaphores(executor);
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

        if (runnable instanceof IRejectable) {
          ((IRejectable) runnable).reject();
        }
      }
    };

    final ThreadPoolExecutor executor = new ThreadPoolExecutor(corePoolSize + dispatcherThreadCount, maximumPoolSize, keepAliveTime, TimeUnit.SECONDS, new SynchronousQueue<Runnable>(), new NamedThreadFactory("scout-thread"), rejectHandler);
    executor.allowCoreThreadTimeOut(allowCoreThreadTimeOut);
    return executor;
  }

  /**
   * Method invoked to create the manager to register and notify job listeners.
   */
  @Internal
  protected JobListeners createJobListeners(final ExecutorService executor) {
    return new JobListeners(executor);
  }

  /**
   * Overwrite this method to contribute some behavior to the {@link Callable} given to the executor for execution.
   * <p/>
   * Contributions are plugged according to the design pattern: 'chain-of-responsibility' - it is easiest to read the
   * chain from 'bottom-to-top'.
   * <p/>
   * To contribute on top of the chain (meaning that you are invoked <strong>after</strong> the contributions of super
   * classes and therefore can base on their contributed functionality), you can use constructions of the following
   * form:
   * <p/>
   * <code>
   *   Callable c2 = new YourInterceptor2(<strong>next</strong>); // executed 3th<br/>
   *   Callable c1 = new YourInterceptor1(c2); // executed 2nd<br/>
   *   Callable head = <i>super.interceptCallable(c1)</i>; // executed 1st<br/>
   *   return head;
   * </code>
   * </p>
   * To be invoked <strong>before</strong> the super class contributions, you can use constructions of the following
   * form:
   * <p/>
   * <code>
   *   Callable c2 = <i>super.interceptCallable(<strong>next</strong>)</i>; // executed 3th<br/>
   *   Callable c1 = new YourInterceptor2(c2); // executed 2nd<br/>
   *   Callable head = new YourInterceptor1(c1); // executed 1st<br/>
   *   return head;
   * </code>
   *
   * @param next
   *          subsequent chain element which is typically the {@link Callable} to be executed.
   * @param input
   *          describes the job to be executed.
   * @return the head of the chain to be invoked first.
   */
  protected <RESULT> Callable<RESULT> interceptCallable(final Callable<RESULT> next, final JobInput input) {
    final Callable<RESULT> c3 = new RunContextCallable<RESULT>(next, input.runContext());
    final Callable<RESULT> c2 = new ThreadNameDecorator<RESULT>(c3, input.threadName(), input.name());
    final Callable<RESULT> c1 = new HandleExceptionCallable<>(c2, input);

    return c1;
  }

  /**
   * Overwrite this method to adapt the Future representing a job to be executed.<br/>
   * The default implementation simply returns the given future.
   *
   * @param future
   *          Future to be adapted.
   * @return adapted Future.
   */
  protected <RESULT> JobFutureTask<RESULT> interceptFuture(final JobFutureTask<RESULT> future) {
    return future;
  }

  @Override
  public IBlockingCondition createBlockingCondition(final String name, final boolean blocking) {
    return new BlockingCondition(name, blocking, this);
  }

  @Internal
  protected MutexSemaphores getMutexSemaphores() {
    return m_mutexSemaphores;
  }

  @Internal
  protected void fireEvent(final JobEvent eventToFire) {
    m_listeners.fireEvent(eventToFire);
  }

  @Internal
  protected void registerFuture(final IFuture<?> future) {
    m_futures.add(future);
  }

  @Internal
  protected void unregisterFuture(final IFuture<?> future) {
    m_futures.remove(future);
  }

  /**
   * Passes the mutex to the next pending task, unless the given task is not a mutex task, or not the mutex owner.
   */
  @Internal
  protected void passMutexIfMutexOwner(final JobFutureTask<?> task) {
    if (task.isMutexTask() && task.isMutexOwner()) {
      m_mutexSemaphores.passMutexToNextTask(task);
    }
  }

  @Internal
  protected boolean isMutexOwner(final JobFutureTask<?> task) {
    return m_mutexSemaphores.isMutexOwner(task);
  }
}
