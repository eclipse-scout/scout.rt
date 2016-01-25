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
  protected IExecutionSemaphore m_executionSemaphore;
  protected long m_expirationTime = EXPIRE_NEVER;
  protected String m_threadName = "scout-thread";
  protected RunContext m_runContext;
  protected ExecutionTrigger m_executionTrigger = Jobs.newExecutionTrigger();

  protected ExceptionHandler m_exceptionHandler = BEANS.get(ExceptionHandler.class);
  protected boolean m_swallowException = false;

  protected Set<String> m_executionHints = new HashSet<>();

  public String getName() {
    return m_name;
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
   * Returns the execution semaphore which this job is assigned to, or <code>null</code> if there is no maximal
   * concurrency restriction for this job.
   * <p>
   * With a semaphore in place, this job only commences execution, once a permit is free or gets available. If free, the
   * job commences execution immediately at the next reasonable opportunity, unless no worker thread is available.
   */
  public IExecutionSemaphore getExecutionSemaphore() {
    return m_executionSemaphore;
  }

  /**
   * Sets the execution semaphore to control the maximal concurrently level among jobs assigned to the same semaphore.
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
   * @see Jobs#newExecutionSemaphore(int)
   */
  public JobInput withExecutionSemaphore(final IExecutionSemaphore executionSemaphore) {
    m_executionSemaphore = executionSemaphore;
    return this;
  }

  public long getExpirationTimeMillis() {
    return m_expirationTime;
  }

  /**
   * Sets the maximal expiration time upon which the job must commence execution. If elapsed, the job is cancelled and
   * does not commence execution. By default, a job never expires.
   * <p>
   * For a job that executes once, the expiration is evaluated just before it commences execution. For a job with a
   * repeating schedule, it is evaluated before every single execution.
   * <p>
   * In contrast, the trigger's end time specifies the time at which the trigger will no longer fire. However, if fired,
   * the job may not be executed immediately at this time, which depends on whether having to compete for an execution
   * permit first. So the end time may already have expired once commencing execution. In contrast, the expiration time
   * is evaluated just before starting execution.
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
   * Sets the {@link RunContext} to be installed during job execution. The {@link RunMonitor} associated with the
   * context will be used as the job's monitor, meaning that cancellation requests to the job future or the context's
   * monitor are equivalent. If no context is given, the job manager ensures a monitor to be installed, so that
   * executing code can always query its cancellation status via <code><RunMonitor.CURRENT.get().isCancelled()</code>.
   */
  public JobInput withRunContext(final RunContext runContext) {
    m_runContext = runContext;
    return this;
  }

  public ExceptionHandler getExceptionHandler() {
    return m_exceptionHandler;
  }

  public boolean isSwallowException() {
    return m_swallowException;
  }

  /**
   * Controls how to deal with uncaught exceptions.
   * <p>
   * By default, an uncaught exception is handled by {@link ExceptionHandler} bean and then propagated to the submitter,
   * unless the submitter is not waiting for the job to complete via {@link IFuture#awaitDoneAndGet()}.
   * <p>
   * This method expects two arguments: an optional exception handler, and a boolean flag indicating whether to swallow
   * exceptions. 'Swallow' is independent of the specified exception handler, and indicates whether an exception should
   * be propagated to the submitter, or swallowed otherwise.
   * <p>
   * If running a repetitive job with swallowing set to <code>true</code>, the job will continue its repetitive
   * execution upon an uncaught exception. If set to <code>false</code>, the execution would exit.
   *
   * @param exceptionHandler
   *          optional handler to handle an uncaught exception, or <code>null</code> to not handle the exception. By
   *          default, {@link ExceptionHandler} bean is used.
   * @param swallowException
   *          <code>true</code> to swallow an uncaught exception, meaning that the exception is not propagated to the
   *          submitter. By default, exceptions are not swallowed and propagated to the submitter.
   */
  public JobInput withExceptionHandling(final ExceptionHandler exceptionHandler, final boolean swallowException) {
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
   * Associates the job with an execution hint.
   * <p>
   * An execution hint is simply a marker to mark a job, and can be evaluated by filters to select jobs, e.g. to listen
   * for job lifecycle events for some particular jobs, or to wait for some particular jobs to complete, or to cancel
   * some particular jobs. A job may have multiple hints associated. Further, hints can be registered directly on the
   * future via {@link IFuture#addExecutionHint(String)}, or removed via {@link IFuture#removeExecutionHint(String)}.
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
    builder.ref("executionSemaphore", m_executionSemaphore);
    builder.attr("expirationTime", m_expirationTime);
    builder.ref("exceptionHandler", m_exceptionHandler);
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
    copy.m_executionSemaphore = m_executionSemaphore;
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
