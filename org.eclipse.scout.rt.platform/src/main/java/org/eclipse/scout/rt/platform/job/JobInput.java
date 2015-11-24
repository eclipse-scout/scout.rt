/*******************************************************************************
 * Copyright (c) 2015 BSI Business Systems Integration AG.
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

import org.eclipse.scout.commons.Assertions;
import org.eclipse.scout.commons.ToStringBuilder;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Bean;
import org.eclipse.scout.rt.platform.context.RunContext;
import org.eclipse.scout.rt.platform.context.RunMonitor;
import org.eclipse.scout.rt.platform.exception.ExceptionHandler;

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

  /**
   * Indicates to execute a job exactly one time.
   */
  public static final int SCHEDULING_RULE_SINGLE_EXECUTION = 1 << 0;
  /**
   * Indicates to execute a job periodically with a fixed delay.
   */
  public static final int SCHEDULING_RULE_PERIODIC_EXECUTION_WITH_FIXED_DELAY = 1 << 1;
  /**
   * Indicates to execute a job periodically at a fixed rate.
   */
  public static final int SCHEDULING_RULE_PERIODIC_EXECUTION_AT_FIXED_RATE = 1 << 2;
  /**
   * Indicates that an executable always should commence execution regardless of how long it was waiting for its
   * execution to start.
   */
  public static final long INFINITE_EXPIRATION = 0;

  protected String m_name;
  protected IMutex m_mutex;
  protected long m_expirationTime = INFINITE_EXPIRATION;
  protected boolean m_logOnError = true;
  protected String m_threadName = "scout-thread";
  protected RunContext m_runContext;
  protected long m_schedulingDelay;
  protected long m_periodicDelay;
  protected int m_schedulingRule = SCHEDULING_RULE_SINGLE_EXECUTION;

  protected Set<Object> m_executionHints = new HashSet<>();

  public String getName() {
    return m_name;
  }

  /**
   * Instruments the job manager to delay the execution of the job until the delay elapsed. For periodic jobs, this is
   * the initial delay to start with the periodic execution.
   *
   * @param delay
   *          the delay to delay the execution.
   * @param unit
   *          the time unit of the <code>period</code> argument.
   */
  public JobInput withSchedulingDelay(final long delay, final TimeUnit unit) {
    m_schedulingDelay = unit.toMillis(delay);
    return this;
  }

  /**
   * Returns the scheduling delay [millis] to indicate, that the job should commence execution only after the delay
   * elapsed.
   */
  public long getSchedulingDelay() {
    return m_schedulingDelay;
  }

  /**
   * A periodic delay is only set for periodic jobs. That are jobs with a scheduling rule 'at-fixed-rate' or
   * 'with-fixed-delay'.
   * <p>
   * Returns the rate for 'at-fixed-rate' jobs, or the delay for 'with-fixed-delay' jobs. The delay is given in
   * milliseconds, and is ignored for one-time executing jobs. The delay is used by the job manager to reschedule a
   * periodic job.
   */
  public long getPeriodicDelay() {
    return m_periodicDelay;
  }

  /**
   * Instruments the job manager to run the job periodically at a fixed rate, until being cancelled, or the job throws
   * an exception, or the job manager is shutdown. Also, periodic jobs do not return a result to the caller.
   * <p>
   * The term 'at fixed rate' means, that the job is run consequently at that rate. The first execution starts
   * immediately, unless configured to run with an initial delay as set via {@link #withSchedulingDelay(long, TimeUnit)}
   * . The second execution is after 'initialDelay' plus one period, the third execution after 'initialDelay' plus 2
   * periods, and so on.
   * <p>
   * If an execution 'A' takes longer than the <code>period</code>, the subsequent execution 'B' is delayed and starts
   * immediately after execution 'A' completes. In such a case, all subsequent executions are shifted by the delay of
   * execution 'A'. In other words, the clock to trigger subsequent executions is reset to the start time of execution
   * 'B'.
   *
   * @param period
   *          the period between successive runs.
   * @param unit
   *          the time unit of the <code>period</code> argument.
   */
  public JobInput withPeriodicExecutionAtFixedRate(final long period, final TimeUnit unit) {
    Assertions.assertTrue(m_schedulingRule == SCHEDULING_RULE_SINGLE_EXECUTION || m_schedulingRule == SCHEDULING_RULE_PERIODIC_EXECUTION_AT_FIXED_RATE, "Periodic scheduling rule already set");
    m_schedulingRule = SCHEDULING_RULE_PERIODIC_EXECUTION_AT_FIXED_RATE;
    m_periodicDelay = unit.toMillis(period);
    return this;
  }

  /**
   * Instruments the job manager to run the job periodically with a fixed delay, until being cancelled, or the job
   * throws an exception, or the job manager is shutdown. Also, periodic jobs do not return a result to the caller.
   * <p>
   * The term 'with fixed delay' means, that there is a fixed delay between the termination of one execution and the
   * commencement of the next.
   *
   * @param period
   *          the delay between successive runs.
   * @param unit
   *          the time unit of the <code>delay</code> argument.
   */
  public JobInput withPeriodicExecutionWithFixedDelay(final long delay, final TimeUnit unit) {
    Assertions.assertTrue(m_schedulingRule == SCHEDULING_RULE_SINGLE_EXECUTION || m_schedulingRule == SCHEDULING_RULE_PERIODIC_EXECUTION_WITH_FIXED_DELAY, "Periodic scheduling rule already set");
    m_schedulingRule = SCHEDULING_RULE_PERIODIC_EXECUTION_WITH_FIXED_DELAY;
    m_periodicDelay = unit.toMillis(delay);
    return this;
  }

  /**
   * Sets the name of a job. Optional. Is used to decorate the worker thread's name and for logging purposes.
   *
   * @param name
   *          the name
   * @param args
   *          arguments to be used in the name, e.g. <code>JobInput.name("load data [id=%s]", id)</code>
   */
  public JobInput withName(final String name, final Object... args) {
    m_name = (name != null ? String.format(name, args) : null);
    return this;
  }

  /**
   * Returns the mutex object, if the job is to be run in sequence among other jobs with the same mutex object, or
   * <code>null</code> to run the job at the next reasonable opportunity.
   */
  public IMutex getMutex() {
    return m_mutex;
  }

  /**
   * Sets the mutex object to run the job in sequence among other jobs with the same mutex object, so that no two such
   * jobs are run in parallel at the same time. By default, no mutex object is set, meaning the job is not executed in
   * mutually exclusive manner.
   *
   * @see Jobs#newMutex()
   */
  public JobInput withMutex(final IMutex mutex) {
    m_mutex = mutex;
    return this;
  }

  public long getExpirationTimeMillis() {
    return m_expirationTime;
  }

  /**
   * Sets the maximal expiration time, until the job must commence execution. If elapsed, the executable is cancelled
   * and never commence execution. This is useful when using a scheduling strategy which might queue scheduled
   * executables prior execution. By default, there is no expiration time set.
   *
   * @param time
   *          the maximal expiration time until an executable must commence execution.
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
   * to be installed, so that executing code can always query the cancellation status by
   * <code>RunMonitor.CURRENT.get().isCancelled()</code> .
   */
  public JobInput withRunContext(final RunContext runContext) {
    m_runContext = runContext;
    return this;
  }

  public boolean isLogOnError() {
    return m_logOnError;
  }

  /**
   * Instruments the job manager to log uncaught exceptions on behalf of the installed {@link ExceptionHandler}. This
   * behavior is enabled by default, but might be disabled, if the caller handles exceptions himself by waiting for the
   * job to complete.
   */
  public JobInput withLogOnError(final boolean logOnError) {
    m_logOnError = logOnError;
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

  public Set<Object> getExecutionHints() {
    return m_executionHints;
  }

  /**
   * Associates the job with an execution hint, which can be evaluated by filters like when listening to job lifecycle
   * events, or when waiting for job completion, or by the job manager.
   */
  public JobInput withExecutionHint(final Object hint) {
    m_executionHints.add(hint);
    return this;
  }

  /**
   * Returns the scheduling rule to run the job, and is one of {@link #SCHEDULING_RULE_SINGLE_EXECUTION}, or
   * {@link #SCHEDULING_RULE_PERIODIC_EXECUTION_AT_FIXED_RATE}, or
   * {@link #SCHEDULING_RULE_PERIODIC_EXECUTION_WITH_FIXED_DELAY}.
   */
  public int getSchedulingRule() {
    return m_schedulingRule;
  }

  @Override
  public String toString() {
    final ToStringBuilder builder = new ToStringBuilder(this);
    builder.attr("name", m_name);
    builder.ref("mutex", m_mutex);
    builder.attr("expirationTime", m_expirationTime);
    builder.attr("logOnError", m_logOnError);
    builder.attr("threadName", m_threadName);
    builder.attr("schedulingRule", m_schedulingRule);
    builder.attr("schedulingDelay", m_schedulingDelay);
    builder.attr("periodicDelay", m_periodicDelay);
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
    copy.m_mutex = m_mutex;
    copy.m_expirationTime = m_expirationTime;
    copy.m_logOnError = m_logOnError;
    copy.m_threadName = m_threadName;
    copy.m_runContext = (m_runContext != null ? m_runContext.copy() : null);
    copy.m_schedulingDelay = m_schedulingDelay;
    copy.m_periodicDelay = m_periodicDelay;
    copy.m_schedulingRule = m_schedulingRule;
    copy.m_executionHints = new HashSet<>(m_executionHints);

    return copy;
  }
}
