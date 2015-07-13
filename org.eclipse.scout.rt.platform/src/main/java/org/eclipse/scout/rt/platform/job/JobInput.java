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

import java.util.concurrent.TimeUnit;

import org.eclipse.scout.commons.ToStringBuilder;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Bean;
import org.eclipse.scout.rt.platform.context.RunContext;
import org.eclipse.scout.rt.platform.context.RunMonitor;
import org.eclipse.scout.rt.platform.exception.ExceptionHandler;

/**
 * A <code>JobInput</code> contains information about a job like its name with execution instructions like 'serial
 * execution' or 'expiration', and tells the job manager in what {@link RunContext} to run the job.
 * <p/>
 * The 'setter-methods' return <code>this</code> in order to support for method chaining.
 *
 * @see RunContext
 * @since 5.1
 */
@Bean
public class JobInput {

  /**
   * Indicates that an executable always commence execution regardless of how long it was waiting for its execution to
   * start.
   */
  public static final long INFINITE_EXPIRATION = 0;

  protected String m_name;
  protected Object m_mutexObject;
  protected long m_expirationTime = INFINITE_EXPIRATION;
  protected boolean m_logOnError = true;
  protected String m_threadName = "scout-thread";
  protected RunContext m_runContext;

  public String name() {
    return m_name;
  }

  /**
   * Sets the name of a job. Optional. Is used to decorate the worker thread's name and for logging purposes.
   *
   * @param name
   *          the name
   * @param args
   *          arguments to be used in the name, e.g. <code>JobInput.name("load data [id=%s]", id)</code>
   */
  public JobInput name(final String name, final Object... args) {
    m_name = (name != null ? String.format(name, args) : null);
    return this;
  }

  /**
   * Returns the mutex object, if the job is to be run in sequence among other jobs with the same mutex object, or
   * <code>null</code> to run the job at the next reasonable opportunity.
   */
  public Object mutex() {
    return m_mutexObject;
  }

  /**
   * Sets the mutex object to run the job in sequence among other jobs with the same mutex object, so that no two such
   * jobs are run in parallel at the same time. By default, no mutex object is set, meaning the job is not executed in
   * mutually exclusive manner.
   */
  public JobInput mutex(final Object mutexObject) {
    m_mutexObject = mutexObject;
    return this;
  }

  public long expirationTimeMillis() {
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
  public JobInput expirationTime(final long time, final TimeUnit timeUnit) {
    m_expirationTime = timeUnit.toMillis(time);
    return this;
  }

  public RunContext runContext() {
    return m_runContext;
  }

  /**
   * Sets the {@link RunContext} to be applied during job execution. Also, the context's {@link RunMonitor} is
   * associated with the jobs's {@link IFuture}, meaning that cancellation requests to the {@link IFuture} or
   * {@link RunContext} are equivalent.
   * However, if no context is provided, the job manager ensures a {@link RunMonitor} to be installed, so that executing
   * code can always query the cancellation status by <code>RunMonitor.CURRENT.get().isCancelled()</code>.
   */
  public JobInput runContext(final RunContext runContext) {
    m_runContext = runContext;
    return this;
  }

  public boolean logOnError() {
    return m_logOnError;
  }

  /**
   * Instruments the job manager to log uncaught exceptions on behalf of the installed {@link ExceptionHandler}.
   * That is enabled by default, but might be disabled, if the caller handles exceptions himself by waiting for the job
   * to complete.
   */
  public JobInput logOnError(final boolean logOnError) {
    m_logOnError = logOnError;
    return this;
  }

  public String threadName() {
    return m_threadName;
  }

  /**
   * Sets the thread name of the worker thread that will execute the job.
   */
  public JobInput threadName(final String threadName) {
    m_threadName = threadName;
    return this;
  }

  public PropertyMap propertyMap() {
    return runContext().getPropertyMap();
  }

  public Object getProperty(final Object key) {
    return runContext().getProperty(key);
  }

  public JobInput withProperty(final Object key, final Object value) {
    runContext().withProperty(key, value);
    return this;
  }

  @Override
  public String toString() {
    final ToStringBuilder builder = new ToStringBuilder(this);
    builder.attr("name", name());
    builder.ref("mutexObject", mutex());
    builder.attr("expirationTime", expirationTimeMillis());
    builder.attr("logOnError", logOnError());
    builder.attr("threadName", threadName());
    builder.attr("runContext", runContext());

    return builder.toString();
  }

  /**
   * Creates a copy of <code>this</code> input.
   */
  public JobInput copy() {
    final JobInput copy = BEANS.get(JobInput.class);
    copy.name(m_name);
    copy.mutex(m_mutexObject);
    copy.expirationTime(m_expirationTime, TimeUnit.MILLISECONDS);
    copy.logOnError(m_logOnError);
    copy.threadName(m_threadName);
    copy.runContext(m_runContext != null ? m_runContext.copy() : null);
    return copy;
  }
}
