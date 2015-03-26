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

import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.eclipse.scout.commons.Assertions;
import org.eclipse.scout.commons.Assertions.AssertionException;
import org.eclipse.scout.commons.Callables;
import org.eclipse.scout.commons.ConfigIniUtility;
import org.eclipse.scout.commons.ICallable;
import org.eclipse.scout.commons.IExecutable;
import org.eclipse.scout.commons.IRunnable;
import org.eclipse.scout.commons.IVisitor;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.commons.ToStringBuilder;
import org.eclipse.scout.commons.annotations.Internal;
import org.eclipse.scout.commons.filter.Filters;
import org.eclipse.scout.commons.filter.IFilter;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.context.RunContext;
import org.eclipse.scout.rt.platform.job.IBlockingCondition;
import org.eclipse.scout.rt.platform.job.IFuture;
import org.eclipse.scout.rt.platform.job.IJobManager;
import org.eclipse.scout.rt.platform.job.IProgressMonitor;
import org.eclipse.scout.rt.platform.job.JobExecutionException;
import org.eclipse.scout.rt.platform.job.JobInput;
import org.eclipse.scout.rt.platform.job.Jobs;
import org.eclipse.scout.rt.platform.job.internal.callable.ApplyRunContextCallable;
import org.eclipse.scout.rt.platform.job.internal.callable.HandleExceptionCallable;
import org.eclipse.scout.rt.platform.job.internal.callable.ThreadNameDecorator;
import org.eclipse.scout.rt.platform.job.internal.future.IFutureTask;
import org.eclipse.scout.rt.platform.job.internal.future.JobFutureTask;
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

  protected static final String PROP_CORE_POOL_SIZE = "org.eclipse.scout.job.corePoolSize";
  protected static final int DEFAULT_CORE_POOL_SIZE = 10; // The number of threads to keep in the pool, even if they are idle;

  protected static final String PROP_MAXIMUM_POOL_SIZE = "org.eclipse.scout.job.maximumPoolSize";
  protected static final int DEFAULT_MAXIMUM_POOL_SIZE = Integer.MAX_VALUE; // The maximal number of threads to be created once the core-pool-size is exceeded.

  protected static final String PROP_KEEP_ALIVE_TIME = "org.eclipse.scout.job.keepAliveTime"; // The time limit for which threads, which are created upon exceeding the 'core-pool-size' limit, may remain idle before being terminated.
  protected static final long DEFAULT_KEEP_ALIVE_TIME = 60; // seconds

  protected static final String PROP_ALLOW_CORE_THREAD_TIME_OUT = "org.eclipse.scout.job.allowCoreThreadTimeOut"; // Specifies whether threads of the core-pool should be terminated after being idle for longer than 'keepAliveTime'.
  protected static final boolean DEFAULT_ALLOW_CORE_THREAD_TIME_OUT = false;

  protected static final String PROP_DISPATCHER_THREAD_COUNT = "org.eclipse.scout.job.dispatcherThreadCount";
  protected static final int DEFAULT_DISPATCHER_THREAD_COUNT = 1; // The number of dispatcher threads to be used to dispatch delayed jobs, meaning jobs scheduled with a delay or periodic jobs.

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
    m_listeners = new JobListeners();
    m_delayedExecutor = new DelayedExecutor(m_executor, "delayed-job-dispatcher", ConfigIniUtility.getPropertyInt(PROP_DISPATCHER_THREAD_COUNT, DEFAULT_DISPATCHER_THREAD_COUNT));

    addListener(Jobs.newEventFilter().eventTypes(JobEventType.SCHEDULED, JobEventType.DONE, JobEventType.BLOCKED, JobEventType.UNBLOCKED, JobEventType.SHUTDOWN), m_futures);
  }

  @Override
  public final <RESULT> IFuture<RESULT> schedule(final IExecutable<RESULT> executable, final JobInput input) {
    final JobFutureTask<RESULT> futureTask = createJobFutureTask(executable, input, false);

    if (!futureTask.isMutexTask() || m_mutexSemaphores.tryAcquireElseOfferTail(futureTask)) {
      m_executor.execute(futureTask);
    }

    return futureTask;
  }

  @Override
  public final <RESULT> IFuture<RESULT> schedule(final IExecutable<RESULT> executable, final long delay, final TimeUnit delayUnit, final JobInput input) {
    final JobFutureTask<RESULT> futureTask = createJobFutureTask(executable, input, false);

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
    final JobFutureTask<Void> futureTask = createJobFutureTask(runnable, input, true);
    Assertions.assertFalse(futureTask.isMutexTask(), "Mutual exclusion is not supported for periodic jobs");

    m_delayedExecutor.schedule(new FixedRateRunnable(m_delayedExecutor, futureTask, period, unit), initialDelay, unit);

    return futureTask;
  }

  @Override
  public final IFuture<Void> scheduleWithFixedDelay(final IRunnable runnable, final long initialDelay, final long delay, final TimeUnit unit, final JobInput input) {
    final JobFutureTask<Void> futureTask = createJobFutureTask(runnable, input, true);
    Assertions.assertFalse(futureTask.isMutexTask(), "Mutual exclusion is not supported for periodic jobs");

    m_delayedExecutor.schedule(new FixedDelayRunnable(m_delayedExecutor, futureTask, delay, unit), initialDelay, unit);

    return futureTask;
  }

  @Override
  public boolean isDone(final IFilter<IFuture<?>> filter) {
    return m_futures.isDone(filter);
  }

  @Override
  public boolean awaitDone(final IFilter<IFuture<?>> filter, final long timeout, final TimeUnit unit) throws InterruptedException {
    return m_futures.awaitDone(filter, timeout, unit);
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
   * @param executable
   *          executable to be given to the executor for execution.
   * @param input
   *          input that describes the job to be executed.
   * @param periodic
   *          <code>true</code> if this is a periodic action, <code>false</code> if executed only once.
   */
  @Internal
  protected <RESULT> JobFutureTask<RESULT> createJobFutureTask(final IExecutable<RESULT> executable, JobInput input, final boolean periodic) {
    validate(input);

    // Ensure a job name to be set.
    if (input.getName() == null) {
      input = input.copy().name(executable.getClass().getName());
    }

    // Create the Callable to be given to the executor.
    final ICallable<RESULT> callable = interceptCallable(Callables.callable(executable), input);

    // Create the Future to be returned to the caller.
    final JobFutureTask<RESULT> futureTask = new JobFutureTask<RESULT>(input, periodic, m_mutexSemaphores, callable) {

      @Override
      protected void postConstruct() {
        m_futures.add(this);
        m_listeners.fireEvent(new JobEvent(JobManager.this, JobEventType.SCHEDULED, this));
      }

      @Override
      protected void rejected() {
        cancel(true); // to interrupt the submitter if waiting for the job to complete.
        m_futures.remove(this);
        m_listeners.fireEvent(new JobEvent(JobManager.this, JobEventType.REJECTED, this));

        if (isMutexTask()) {
          m_mutexSemaphores.passMutexToNextTask(this);
        }
      }

      @Override
      protected void beforeExecute() {
        // Check, if the Future is expired and therefore should not be executed. The FutureTask ensures that 'cancelled' Futures do not commence execution.
        if (isExpired()) {
          cancel(true);
        }

        m_listeners.fireEvent(new JobEvent(JobManager.this, JobEventType.ABOUT_TO_RUN, this));

        IFuture.CURRENT.set(this);
        IProgressMonitor.CURRENT.set(getProgressMonitor());
      }

      @Override
      protected void afterExecute() {
        IProgressMonitor.CURRENT.remove();
        IFuture.CURRENT.remove();

        if (isPeriodic() && !isDone()) {
          // NOOP: periodic action which is not finished yet but re-scheduled for a next execution.
          return;
        }

        m_futures.remove(this);
        m_listeners.fireEvent(new JobEvent(JobManager.this, JobEventType.DONE, this));

        if (isMutexTask() && isMutexOwner()) { // the current task is not the mutex owner if being interrupted while waiting for a blocking condition to fall.
          m_mutexSemaphores.passMutexToNextTask(this);
        }
      }
    };

    return interceptFuture(futureTask);
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
    final int corePoolSize = ConfigIniUtility.getPropertyInt(PROP_CORE_POOL_SIZE, DEFAULT_CORE_POOL_SIZE);
    final int maximumPoolSize = ConfigIniUtility.getPropertyInt(PROP_MAXIMUM_POOL_SIZE, DEFAULT_MAXIMUM_POOL_SIZE);
    final long keepAliveTime = ConfigIniUtility.getPropertyLong(PROP_KEEP_ALIVE_TIME, DEFAULT_KEEP_ALIVE_TIME);
    final boolean allowCoreThreadTimeOut = ConfigIniUtility.getPropertyBoolean(PROP_ALLOW_CORE_THREAD_TIME_OUT, DEFAULT_ALLOW_CORE_THREAD_TIME_OUT);
    final int dispatcherThreadCount = Assertions.assertGreater(ConfigIniUtility.getPropertyInt(PROP_DISPATCHER_THREAD_COUNT, DEFAULT_DISPATCHER_THREAD_COUNT), 0);

    // Create the rejection handler.
    final RejectedExecutionHandler rejectHandler = new RejectedExecutionHandler() {

      @Override
      public void rejectedExecution(final Runnable runnable, final ThreadPoolExecutor executor) {
        if (m_executor.isShutdown()) {
          LOG.debug("Job rejected because the job manager is shutdown.");
        }
        else {
          LOG.error("Job rejected because no more threads or queue slots available.");
        }

        if (runnable instanceof IFutureTask) {
          ((IFutureTask<?>) runnable).reject();
        }
      }
    };

    final ThreadPoolExecutor executor = new ThreadPoolExecutor(corePoolSize + dispatcherThreadCount, maximumPoolSize, keepAliveTime, TimeUnit.SECONDS, new SynchronousQueue<Runnable>(), new NamedThreadFactory("scout-thread"), rejectHandler);
    executor.allowCoreThreadTimeOut(allowCoreThreadTimeOut);
    return executor;
  }

  /**
   * Overwrite this method to contribute some behavior to the {@link ICallable} given to the executor for execution.
   * <p/>
   * Contributions are plugged according to the design pattern: 'chain-of-responsibility' - it is easiest to read the
   * chain from 'bottom-to-top'.
   * <p/>
   * To contribute on top of the chain (meaning that you are invoked <strong>after</strong> the contributions of super
   * classes and therefore can base on their contributed functionality), you can use constructions of the following
   * form:
   * <p/>
   * <code>
   *   ICallable c2 = new YourInterceptor2(<strong>next</strong>); // executed 3th<br/>
   *   ICallable c1 = new YourInterceptor1(c2); // executed 2nd<br/>
   *   ICallable head = <i>super.interceptCallable(c1)</i>; // executed 1st<br/>
   *   return head;
   * </code>
   * </p>
   * To be invoked <strong>before</strong> the super class contributions, you can use constructions of the following
   * form:
   * <p/>
   * <code>
   *   ICallable c2 = <i>super.interceptCallable(<strong>next</strong>)</i>; // executed 3th<br/>
   *   ICallable c1 = new YourInterceptor2(c2); // executed 2nd<br/>
   *   ICallable head = new YourInterceptor1(c1); // executed 1st<br/>
   *   return head;
   * </code>
   *
   * @param next
   *          subsequent chain element which is typically the {@link ICallable} to be executed.
   * @param input
   *          describes the job to be executed.
   * @return the head of the chain to be invoked first.
   */
  protected <RESULT> ICallable<RESULT> interceptCallable(final ICallable<RESULT> next, final JobInput input) {
    final ICallable<RESULT> c3 = new ApplyRunContextCallable<RESULT>(next, input.getRunContext());
    final ICallable<RESULT> c2 = new ThreadNameDecorator<RESULT>(c3, input.getThreadName(), input.getIdentifier());
    final ICallable<RESULT> c1 = new HandleExceptionCallable<>(c2, input);

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

  /**
   * Method invoked to validate the given input. The default implementation ensures the input not to be
   * <code>null</code> and calls {@link RunContext#validate}.
   *
   * @throws AssertionException
   *           thrown if the input is not valid.
   */
  protected void validate(final JobInput input) {
    Assertions.assertNotNull(input, "'JobInput' must not be null");
    Assertions.assertNotNull(input.getRunContext(), "'RunContext' must not be null");
    input.getRunContext().validate();
  }

  @Override
  public IBlockingCondition createBlockingCondition(final String name, final boolean blocking) {
    return new BlockingCondition(name, blocking);
  }

  /**
   * @see IBlockingCondition
   */
  @Internal
  protected class BlockingCondition implements IBlockingCondition {

    private volatile boolean m_blocking;
    private final String m_name;

    protected BlockingCondition(final String name, final boolean blocking) {
      m_name = StringUtility.nvl(name, "n/a");
      m_blocking = blocking;
    }

    @Override
    public String getName() {
      return m_name;
    }

    @Override
    public boolean isBlocking() {
      return m_blocking;
    }

    @Override
    public void setBlocking(final boolean blocking) {
      if (m_blocking != blocking) {
        synchronized (BlockingCondition.this) {
          if (m_blocking != blocking) {
            m_blocking = blocking;
            if (!blocking) {
              BlockingCondition.this.notifyAll();
            }
          }
        }
      }
    }

    @Override
    public void waitFor() throws JobExecutionException {
      // Get the current FutureTask. If not available, this blocking condition is used from a thread not managed by this job manager.
      final JobFutureTask<?> currentTask = (JobFutureTask<?>) (IFuture.CURRENT.get() != null ? IFuture.CURRENT.get() : null);

      synchronized (BlockingCondition.this) {
        if (!m_blocking) {
          return; // the blocking condition is not armed yet.
        }

        try {
          if (currentTask != null) {
            currentTask.setBlocked(true);
            m_listeners.fireEvent(new JobEvent(JobManager.this, JobEventType.BLOCKED, currentTask, this));

            // Pass the mutex to next task if being a mutex task.
            if (currentTask.isMutexTask()) {
              Assertions.assertTrue(currentTask.isMutexOwner(), "Unexpected inconsistency: Current FutureTask must be mutex owner [task=%s, thread=%s]", currentTask, Thread.currentThread().getName());
              m_mutexSemaphores.passMutexToNextTask(currentTask);
            }
          }

          // Block the calling thread until the blocking condition falls.
          while (m_blocking) {
            try {
              BlockingCondition.this.wait();
            }
            catch (final InterruptedException e) {
              Thread.currentThread().interrupt(); // Restore the interrupted status because cleared by catching InterruptedException.
              throw new JobExecutionException(String.format("Interrupted while waiting for a blocking condition to fall. [blockingCondition=%s, thread=%s]", m_name, Thread.currentThread().getName()), e);
            }
          }
        }
        finally {
          if (currentTask != null) {
            currentTask.setBlocked(false);
            m_listeners.fireEvent(new JobEvent(JobManager.this, JobEventType.UNBLOCKED, currentTask, this));
          }
        }
      }

      if (currentTask != null) {
        // Acquire the mutex anew if being a mutex task.
        if (currentTask.isMutexTask()) {
          m_mutexSemaphores.acquire(currentTask); // Wait until acquired the mutex anew.
        }
        m_listeners.fireEvent(new JobEvent(JobManager.this, JobEventType.RESUMED, currentTask, this));
      }
    }

    @Override
    public String toString() {
      final ToStringBuilder builder = new ToStringBuilder(this);
      builder.attr("name", m_name);
      builder.attr("blocking", m_blocking);
      return builder.toString();
    }
  }
}
