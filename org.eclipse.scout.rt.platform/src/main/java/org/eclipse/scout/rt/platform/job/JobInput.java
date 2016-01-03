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
package org.eclipse.scout.rt.platform.job;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Bean;
import org.eclipse.scout.rt.platform.context.RunContext;
import org.eclipse.scout.rt.platform.context.RunMonitor;
import org.eclipse.scout.rt.platform.exception.ExceptionHandler;
import org.eclipse.scout.rt.platform.util.Assertions;
import org.eclipse.scout.rt.platform.util.Assertions.AssertionException;
import org.eclipse.scout.rt.platform.util.ToStringBuilder;
import org.slf4j.helpers.MessageFormatter;

/**
 * A <code>JobInput</code> contains information about a job like its name with execution instructions for the job
 * manager to run the job.
 * <p>
 * The 'setter-methods' return <code>this</code> in order to support for method chaining.
 *
 * @see RunContext
 * @since 5.1
 */
@Bean
public class JobInput {

  public static final long EXPIRE_NEVER = 0;

  protected String m_name;
  protected ISchedulingSemaphore m_schedulingSemaphore;
  protected long m_expirationTime = EXPIRE_NEVER;
  protected String m_threadName = "scout-thread";
  protected RunContext m_runContext;
  protected ExecutionTrigger m_executionTrigger = Jobs.newExecutionTrigger();

  protected Class<? extends ExceptionHandler> m_exceptionHandler = ExceptionHandler.class;
  protected boolean m_swallowException = false;

  protected Set<String> m_executionHints = new HashSet<>();

  public String getName() {
    return m_name;
  }

  public ExecutionTrigger getExecutionTrigger() {
    return m_executionTrigger;
  }

  /**
   * Sets the trigger to define the schedule upon which the job will commence execution. If not set, the job will
   * commence execution immediately after being scheduled, and will execute exactly once.
   * <p>
   * The trigger mechanism is provided by Quartz Scheduler, meaning that you can profit from the powerful Quartz
   * schedule capabilities.
   * <p>
   * For more information, see <a href="http://www.quartz-scheduler.org">http://www.quartz-scheduler.org</a>.
   * <p>
   * Use the static factory method {@link Jobs#newExecutionTrigger()} to get an instance:
   *
   * <pre>
   *
   * Jobs.newInput()
   *     .withName("job")
   *     .withExecutionTrigger(<strong>Jobs.newExecutionTrigger()</strong>
   *         .withSchedule(FixedDelayScheduleBuilder.repeatForever(1, TimeUnit.SECONDS))
   *         .withStartIn(10, TimeUnit.SECONDS));
   *
   * or
   *
   * Jobs.newInput()
   *     .withName("job")
   *     .withExecutionTrigger(<strong>Jobs.newExecutionTrigger()</strong>
   *         .withSchedule(SimpleScheduleBuilder.repeatSecondlyForever())
   *         .withStartIn(10, TimeUnit.SECONDS));
   * </pre>
   */
  public JobInput withExecutionTrigger(final ExecutionTrigger executionTrigger) {
    Assertions.assertNotNull(executionTrigger, "ExecutionTrigger must not be null");
    m_executionTrigger = executionTrigger;
    return this;
  }

  /**
   * Sets the name of the job, which is used to name the worker thread and for logging purpose.
   * <p>
   * Optionally, <em>formatting anchors</em> in the form of {} pairs can be used in the name, which will be replaced by
   * the respective argument.
   *
   * @param name
   *          the name with support for <em>formatting anchors</em> in the form of {} pairs.
   * @param valueArgs
   *          optional arguments to substitute <em>formatting anchors</em> in the name.
   */
  public JobInput withName(final String name, final Object... args) {
    m_name = MessageFormatter.arrayFormat(name, args).getMessage();
    return this;
  }

  /**
   * Returns the scheduling semaphore which this job is assigned to, or <code>null</code> if there is no maximal
   * concurrency restriction for this job.
   * <p>
   * With a semaphore in place, this job only commences execution, once a permit is free or gets available. If free, the
   * job commences execution immediately at the next reasonable opportunity, unless no worker thread is available.
   */
  public ISchedulingSemaphore getSchedulingSemaphore() {
    return m_schedulingSemaphore;
  }

  /**
   * Sets the scheduling semaphore to control the job's maximal concurrently level.
   * <p>
   * With a semaphore in place, this job only commences execution, once a permit is free or gets available. If free, the
   * job commences execution immediately at the next reasonable opportunity, unless no worker thread is available.
   * <p>
   * A semaphore initialized to <code>one</code> allows to run jobs in a mutually exclusive manner, and a semaphore
   * initialized to <code>zero</code> to run no job at all. The number of total permits available can be changed at any
   * time, which allows to adapt the maximal concurrency level to some dynamic criteria like time of day or system load.
   * However, once calling {@link #seal()}, the number of permits cannot be changed anymore, and any attempts will
   * result in an {@link AssertionException}.
   *
   * @see Jobs#newSchedulingSemaphore()
   */
  public JobInput withSchedulingSemaphore(final ISchedulingSemaphore schedulingSemaphore) {
    m_schedulingSemaphore = schedulingSemaphore;
    return this;
  }

  public long getExpirationTimeMillis() {
    return m_expirationTime;
  }

  /**
   * Sets the maximal expiration time upon which the job must commence execution. If elapsed, the job is cancelled and
   * does not commence execution.
   * <p>
   * For a job that executes once, the expiration is evaluated just before it commences execution. For a job with a
   * repeating schedule, it is evaluated before every single execution.
   * <p>
   * In contrast, the trigger's end time specifies the time at which the trigger will no longer fire. However, if fired,
   * the job may or may not be executed at this time, which depends whether having to compete for an execution permit
   * first.
   * <p>
   * By default, a job never expires.
   *
   * @param time
   *          the maximal expiration time until the job must commence execution.
   * @param timeUnit
   *          the time unit of the <code>time</code> argument.
   */
  public JobInput withExpirationTime(final long time, final TimeUnit timeUnit) {
    m_expirationTime = timeUnit.toMillis(time);
    return this;
  }

  public RunContext getRunContext() {
    return m_runContext;
  }

  /**
   * Sets the {@link RunContext} to be installed during job execution. Also, the context's {@link RunMonitor} is
   * associated with the jobs's {@link IFuture}, meaning that cancellation requests to the {@link IFuture} or
   * {@link RunContext} are equivalent. However, if no context is provided, the job manager ensures a {@link RunMonitor}
   * to be installed, so that executing code can always query the cancellation status via
   * <code>RunMonitor.CURRENT.get().isCancelled()</code> .
   */
  public JobInput withRunContext(final RunContext runContext) {
    m_runContext = runContext;
    return this;
  }

  public Class<? extends ExceptionHandler> getExceptionHandler() {
    return m_exceptionHandler;
  }

  public boolean isSwallowException() {
    return m_swallowException;
  }

  /**
   * Controls the handling of uncaught exceptions.
   * <p>
   * By default, an uncaught exception is handled by {@link ExceptionHandler} bean and then propagated to the submitter,
   * unless the submitter is not waiting for the job to complete via {@link IFuture#awaitDoneAndGet()}.
   * <p>
   * If running a periodic job with <code>swallowException=true</code>, the job will continue periodic execution upon an
   * uncaught exception. If set to <code>false</code>, the execution would exit.
   *
   * @param exceptionHandler
   *          optional handler to handle an uncaught exception, or <code>null</code> to not handle the exception. By
   *          default, {@link ExceptionHandler} bean is used.
   * @param swallowException
   *          <code>true</code> to swallow an uncaught exception, meaning that the exception is not propagated to the
   *          submitter. By default, exceptions are not swallowed and propagated to the submitter.
   */
  public JobInput withExceptionHandling(final Class<? extends ExceptionHandler> exceptionHandler, final boolean swallowException) {
    m_exceptionHandler = exceptionHandler;
    m_swallowException = swallowException;
    return this;
  }

  public String getThreadName() {
    return m_threadName;
  }

  /**
   * Sets the thread name of the worker thread that will execute the job.
   */
  public JobInput withThreadName(final String threadName) {
    m_threadName = threadName;
    return this;
  }

  public Set<String> getExecutionHints() {
    return m_executionHints;
  }

  /**
   * Associates the job with an execution hint, which can be evaluated by filters like when listening to job lifecycle
   * events, or when waiting for job completion, or when cancelling jobs, or by the job manager.
   * <p>
   * A job may have multiple hints associated, and hints are not propagated to nested jobs.
   */
  public JobInput withExecutionHint(final String hint) {
    m_executionHints.add(hint);
    return this;
  }

  /**
   * Conditionally associates the job with an execution hint. Unlike {@link #withExecutionHint(String)}, this method
   * sets the hint only if the condition is <code>true</code>, and is used to ease fluent usage of {@link JobInput}.
   *
   * @param hint
   *          the execution hint to be set.
   * @param setExecutionHint
   *          <code>true</code> to set the execution hint, or <code>false</code> otherwise.
   * @see #withExecutionHint(String)
   */
  public JobInput withExecutionHint(final String hint, final boolean setExecutionHint) {
    if (setExecutionHint) {
      m_executionHints.add(hint);
    }
    return this;
  }

  @Override
  public String toString() {
    final ToStringBuilder builder = new ToStringBuilder(this);
    builder.attr("name", m_name);
    builder.ref("schedulingSemaphore", m_schedulingSemaphore);
    builder.attr("expirationTime", m_expirationTime);
    builder.attr("exceptionHandler", m_exceptionHandler);
    builder.attr("swallowException", m_swallowException);
    builder.attr("threadName", m_threadName);
    builder.attr("executionTrigger", m_executionTrigger);
    builder.attr("runContext", m_runContext);
    builder.attr("executionHints", m_executionHints);

    return builder.toString();
  }

  /**
   * Creates a copy of <code>this</code> input.
   */
  public JobInput copy() {
    final JobInput copy = BEANS.get(JobInput.class);
    copy.m_name = m_name;
    copy.m_schedulingSemaphore = m_schedulingSemaphore;
    copy.m_expirationTime = m_expirationTime;
    copy.m_exceptionHandler = m_exceptionHandler;
    copy.m_swallowException = m_swallowException;
    copy.m_threadName = m_threadName;
    copy.m_runContext = (m_runContext != null ? m_runContext.copy() : null);
    copy.m_executionTrigger = m_executionTrigger.copy();
    copy.m_executionHints = new HashSet<>(m_executionHints);

    return copy;
  }
}
