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

import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionHandler;
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
import org.eclipse.scout.rt.platform.config.PlatformConfigProperties.JobManagerKeepAliveTimeProperty;
import org.eclipse.scout.rt.platform.config.PlatformConfigProperties.JobManagerMaximumPoolSizeProperty;
import org.eclipse.scout.rt.platform.config.PlatformConfigProperties.JobManagerPrestartCoreThreadsProperty;
import org.eclipse.scout.rt.platform.context.RunContextRunner;
import org.eclipse.scout.rt.platform.context.RunMonitor;
import org.eclipse.scout.rt.platform.exception.DefaultRuntimeExceptionTranslator;
import org.eclipse.scout.rt.platform.exception.PlatformException;
import org.eclipse.scout.rt.platform.filter.IFilter;
import org.eclipse.scout.rt.platform.job.DoneEvent;
import org.eclipse.scout.rt.platform.job.ExecutionTrigger;
import org.eclipse.scout.rt.platform.job.FixedDelayScheduleBuilder;
import org.eclipse.scout.rt.platform.job.IBlockingCondition;
import org.eclipse.scout.rt.platform.job.IDoneHandler;
import org.eclipse.scout.rt.platform.job.IFuture;
import org.eclipse.scout.rt.platform.job.IJobListenerRegistration;
import org.eclipse.scout.rt.platform.job.IJobManager;
import org.eclipse.scout.rt.platform.job.JobInput;
import org.eclipse.scout.rt.platform.job.JobState;
import org.eclipse.scout.rt.platform.job.listener.IJobListener;
import org.eclipse.scout.rt.platform.job.listener.JobEvent;
import org.eclipse.scout.rt.platform.job.listener.JobEventData;
import org.eclipse.scout.rt.platform.job.listener.JobEventType;
import org.eclipse.scout.rt.platform.logger.DiagnosticContextValueProcessor;
import org.eclipse.scout.rt.platform.util.Assertions;
import org.eclipse.scout.rt.platform.util.ThreadLocalProcessor;
import org.eclipse.scout.rt.platform.util.concurrent.Callables;
import org.eclipse.scout.rt.platform.util.concurrent.IRunnable;
import org.eclipse.scout.rt.platform.visitor.IVisitor;
import org.quartz.Calendar;
import org.quartz.JobBuilder;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.impl.StdSchedulerFactory;
import org.quartz.simpl.RAMJobStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default implementation of {@link IJobManager}.
 * <p>
 * This job manager is based on Quartz {@link Scheduler} and {@link ThreadPoolExecutor}.
 * <p>
 * If a job is scheduled, it is submitted to {@link Scheduler Quartz Scheduler} in the form of a {@link Trigger}. Once
 * the job likes to commence execution, that trigger fires and invokes {@link QuartzExecutorJob}, which submits the job
 * to {@link ExecutorService} for execution.
 *
 * @since 5.1
 */
@ApplicationScoped
public class JobManager implements IJobManager, IPlatformListener {

  private static final Logger LOG = LoggerFactory.getLogger(JobManager.class);

  protected final ExecutorService m_executor;
  protected final Scheduler m_quartz;

  protected final FutureSet m_futures;
  protected final JobListeners m_listeners;

  public JobManager() {
    m_executor = createExecutor();
    m_quartz = createQuartzScheduler();
    m_listeners = BEANS.get(JobListeners.class);
    m_futures = BEANS.get(FutureSet.class);
    m_futures.init(this);

    installDurableExecutorJob();
  }

  @Override
  public IFuture<Void> schedule(final IRunnable runnable, final JobInput input) {
    return schedule(Callables.callable(runnable), ensureJobInputName(input, runnable.getClass().getName()));
  }

  @Override
  public final <RESULT> IFuture<RESULT> schedule(final Callable<RESULT> callable, final JobInput input) {
    Assertions.assertNotNull(input, "JobInput must not be null");
    Assertions.assertNotNull(input.getExecutionTrigger(), "ExecutionTrigger must not be null");

    // Create the Future to be given to the ExecutorService.
    final JobFutureTask<RESULT> futureTask = createJobFutureTask(callable, input);
    // Create the Quartz Trigger to fire once the job should commence execution.
    final Trigger trigger = createQuartzTrigger(futureTask);
    // Compute and set temporal values.
    futureTask.computeAndSetTemporalValues(trigger);
    // Register the Future in job manager.
    registerFuture(futureTask);

    // As of now, the Future is managed by job manager, and needs to be rejected in case of a scheduling error.

    try {
      futureTask.changeState(JobState.SCHEDULED);

      // Enter 'pending' state if about a delayed execution.
      if (futureTask.isDelayedExecution()) {
        futureTask.changeState(JobState.PENDING);
      }

      // Submit Quartz Trigger and install 'when-done' handler to remove the trigger from Quartz Scheduler once 'done'.
      m_quartz.scheduleJob(trigger);
      futureTask.whenDone(new IDoneHandler<RESULT>() {

        @Override
        public void onDone(final DoneEvent<RESULT> event) {
          try {
            m_quartz.unscheduleJob(trigger.getKey());
          }
          catch (final SchedulerException e) {
            LOG.error("Failed to remove Trigger from Quartz Scheduler [future={}]", futureTask, e);
          }
        }
      }, null);
    }
    catch (SchedulerException | RuntimeException e) {
      futureTask.reject();

      if (isShutdown()) {
        LOG.debug("Job rejected because the job manager is shutdown.");
      }
      else {
        throw BEANS.get(DefaultRuntimeExceptionTranslator.class).translate(e);
      }
    }

    return futureTask;
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
    try {
      return m_executor.isShutdown() || m_quartz.isShutdown();
    }
    catch (final SchedulerException e) {
      LOG.error("Failed to determine job manager shutdown status", e);
      return false;
    }
  }

  @Override
  public final void shutdown() {
    // Dispose Futures.
    m_futures.dispose();

    // Shutdown Quartz Scheduler.
    try {
      m_quartz.shutdown(false);
    }
    catch (final SchedulerException e) {
      LOG.error("Failed to shutdown Quartz Scheduler", e);
    }

    // Shutdown Java Executor Service.
    m_executor.shutdownNow();
    try {
      m_executor.awaitTermination(1, TimeUnit.MINUTES);
    }
    catch (final java.lang.InterruptedException e) {
      // NOOP
    }

    // Fire event that job manager was shutdown.
    fireEvent(new JobEvent(this, JobEventType.JOB_MANAGER_SHUTDOWN, new JobEventData()));
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

  @Override
  public Calendar getCalendar(final String calendarName) {
    try {
      return m_quartz.getCalendar(calendarName);
    }
    catch (final SchedulerException e) {
      throw BEANS.get(DefaultRuntimeExceptionTranslator.class).translate(e);
    }
  }

  @Override
  public void addCalendar(final String calendarName, final Calendar calendar, final boolean replaceIfPresent, final boolean updateExecutionTriggers) {
    try {
      m_quartz.addCalendar(calendarName, calendar, replaceIfPresent, updateExecutionTriggers);
    }
    catch (final SchedulerException e) {
      throw BEANS.get(DefaultRuntimeExceptionTranslator.class).translate(e);
    }
  }

  @Override
  public boolean removeCalendar(final String calendarName) {
    try {
      return m_quartz.deleteCalendar(calendarName);
    }
    catch (final SchedulerException e) {
      throw BEANS.get(DefaultRuntimeExceptionTranslator.class).translate(e);
    }
  }

  @Internal
  protected void fireEvent(final JobEvent eventToFire) {
    m_listeners.notifyListeners(eventToFire);
  }

  /**
   * Installs the durable {@link QuartzExecutorJob} in Quartz Scheduler, which is fired by Quartz trigger once a
   * {@link JobFutureTask} likes to commence execution. In turn, the associated {@link JobFutureTask} is given to
   * {@link ExecutorService} for asynchronous execution.
   * <p>
   * Durable means, that this job's existence is not bound to the existence of an associated trigger, which is typically
   * true when {@link JobManager} is idle because of no jobs are scheduled.
   */
  @Internal
  protected void installDurableExecutorJob() {
    try {
      Assertions.assertTrue(m_quartz.isStarted(), "Quartz Scheduler not started");
      m_quartz.addJob(JobBuilder.newJob(QuartzExecutorJob.class)
          .withIdentity(QuartzExecutorJob.IDENTITY)
          .setJobData(QuartzExecutorJob.newJobData(m_executor))
          .storeDurably() // not bound to the existence of triggers
          .build(), false);
    }
    catch (final SchedulerException e) {
      throw BEANS.get(DefaultRuntimeExceptionTranslator.class).translate(e);
    }
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
   * Creates the Quartz Trigger to execute the specified JobFutureTask.
   * <p>
   * Upon firing, {@link QuartzExecutorJob} is invoked with {@link JobFutureTask} and {@link IFutureRunner} as its
   * parameters. {@link QuartzExecutorJob} then first competes for an execution permit (if semaphore aware), and then
   * executes the specified {@link IFutureRunner} via {@link ExecutorService}.
   */
  @Internal
  protected Trigger createQuartzTrigger(final JobFutureTask<?> futureTask) {
    final ExecutionTrigger executionTrigger = futureTask.getJobInput().getExecutionTrigger();

    final IFutureRunner futureRunner;
    if (executionTrigger.getSchedule() instanceof FixedDelayScheduleBuilder) {
      futureRunner = new FixedDelayFutureRunner(m_quartz, futureTask);
    }
    else {
      futureRunner = new SerialFutureRunner(m_quartz, futureTask);
    }

    return TriggerBuilder.newTrigger()
        .withIdentity(futureTask.getTriggerIdentity())
        .withPriority(QuartzExecutorJob.computePriority(futureTask))
        .forJob(QuartzExecutorJob.IDENTITY)
        .usingJobData(QuartzExecutorJob.newTriggerData(futureTask, futureRunner))
        .startAt(executionTrigger.getStartTime())
        .endAt(executionTrigger.getEndTime())
        .withSchedule(executionTrigger.getSchedule())
        .modifiedByCalendar(executionTrigger.getCalendarName())
        .build();
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
   * Creates the Quartz Scheduler to submit triggers.
   */
  protected Scheduler createQuartzScheduler() {
    final Properties props = new Properties();
    props.put("org.quartz.scheduler.instanceName", String.format("scout.jobmanager.quartz.scheduler-%s", UUID.randomUUID())); // UUID as prefix to be unique
    props.put("org.quartz.scheduler.threadName", "scout-quartz-scheduler-thread");
    props.put("org.quartz.threadPool.threadNamePrefix", "scout-quartz-worker-thread");
    props.put("org.quartz.scheduler.skipUpdateCheck", true);
    props.put("org.quartz.threadPool.threadCount", "1"); // No sense for multiple threads, because the only installed Quartz Job (QuartzExecutorJob) disallows concurrent execution anyway (by design).
    props.put("org.quartz.jobStore.class", RAMJobStore.class.getName());

    try {
      final StdSchedulerFactory schedulerFactory = new StdSchedulerFactory();
      schedulerFactory.initialize(props);
      final Scheduler scheduler = schedulerFactory.getScheduler();
      scheduler.setJobFactory(new QuartzJobBeanFactory());
      scheduler.start();

      return scheduler;
    }
    catch (final SchedulerException e) {
      throw BEANS.get(DefaultRuntimeExceptionTranslator.class).translate(e);
    }
  }

  /**
   * Returns the internal Executor Service.
   */
  @Internal
  protected ExecutorService getExecutor() {
    return m_executor;
  }

  /**
   * Returns the internal Quartz Scheduler.
   */
  @Internal
  protected Scheduler getQuartz() {
    return m_quartz;
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
