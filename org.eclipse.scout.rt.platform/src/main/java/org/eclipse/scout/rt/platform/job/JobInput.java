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

import java.util.Locale;
import java.util.concurrent.TimeUnit;

import javax.security.auth.Subject;

import org.eclipse.scout.commons.Assertions;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.commons.ToStringBuilder;
import org.eclipse.scout.rt.platform.Bean;
import org.eclipse.scout.rt.platform.OBJ;
import org.eclipse.scout.rt.platform.context.Context;

/**
 * Describes a job with context information to be applied onto the executing worker thread during the time of the job's
 * execution.
 * <p/>
 * The 'setter-methods' returns <code>this</code> in order to support for method chaining.
 *
 * @see Context
 * @see IJobManager
 * @since 5.1
 */
@Bean
public class JobInput {

  public static final String N_A = "n/a";

  /**
   * Indicates that an executable always commence execution regardless of how long it was waiting for its execution to
   * start.
   */
  public static final long INFINITE_EXPIRATION = 0;

  protected String m_id;
  protected String m_name;
  protected Object m_mutexObject;
  protected long m_expirationTime;
  protected boolean m_logOnError;
  protected Context m_context;

  protected JobInput() {
  }

  public String getId() {
    return m_id;
  }

  /**
   * Sets the <code>id</code> of a job; must not be set; must not be unique; is primarily used for logging purpose, to
   * decorate the worker thread's name and to identify the job's Future.
   */
  public JobInput id(final String id) {
    m_id = id;
    return this;
  }

  public String getName() {
    return m_name;
  }

  /**
   * Sets the name of a job; is used to decorate the worker thread's name and for logging purpose; must not be set.
   */
  public JobInput name(final String name) {
    m_name = name;
    return this;
  }

  public Object getMutex() {
    return m_mutexObject;
  }

  /**
   * Sets the mutex object (mutual exclusion) for the job. This is used to run the job in sequence among other jobs with
   * the same mutex object, so that no two such jobs are run in parallel at the same time.
   */
  public JobInput mutex(final Object mutexObject) {
    m_mutexObject = mutexObject;
    return this;
  }

  public long getExpirationTimeMillis() {
    return m_expirationTime;
  }

  /**
   * Sets the maximal expiration time, until the job must commence execution; if elapsed, the executable is cancelled
   * and never commence execution; is useful, if using a scheduling strategy which might queue scheduled executables
   * prior execution. By default, there is no expiration time set.
   *
   * @param time
   *          the maximal expiration time until an executable must commence execution.
   * @param timeUnit
   *          the time unit of the <code>time</code> argument.
   * @return this in order to support for method chaining
   */
  public JobInput expirationTime(final long time, final TimeUnit timeUnit) {
    m_expirationTime = timeUnit.toMillis(time);
    return this;
  }

  public Context getContext() {
    return m_context;
  }

  /**
   * Sets the {@link Context} to be set for the time of execution.
   */
  public JobInput context(final Context context) {
    m_context = Assertions.assertNotNull(context, "Context must not be null");
    return this;
  }

  public Subject getSubject() {
    return m_context.getSubject();
  }

  /**
   * Sets the Subject to execute the job under a particular user.
   */
  public JobInput subject(final Subject subject) {
    m_context.subject(subject);
    return this;
  }

  public Locale getLocale() {
    return m_context.getLocale();
  }

  /**
   * Sets the Locale to be set for the time of execution.
   */
  public JobInput locale(final Locale locale) {
    m_context.locale(locale);
    return this;
  }

  public boolean isLogOnError() {
    return m_logOnError;
  }

  /**
   * Instrument the job manager to log execution exceptions caused by this job; is <code>true</code> by default.
   */
  public JobInput logOnError(final boolean logOnError) {
    m_logOnError = logOnError;
    return this;
  }

  public PropertyMap getPropertyMap() {
    return m_context.getPropertyMap();
  }

  /***
   * @return the job's identifier consisting of the job's 'id' and 'name', or {@link JobInput#N_A} if not set.
   */
  public String getIdentifier() {
    final String identifier = StringUtility.join(":", m_id, m_name);
    return StringUtility.nvl(identifier, JobInput.N_A);
  }

  /**
   * @return name used to name the worker thread during the job's execution.
   */
  public String getThreadName() {
    return "scout-thread";
  }

  // === construction methods ===

  /**
   * Creates a shallow copy of the job-input represented by <code>this</code> context.
   */
  public JobInput copy() {
    final JobInput copy = OBJ.get(JobInput.class);
    copy.apply(this);
    return copy;
  }

  /**
   * Applies the given input values to <code>this</code> input.
   */
  protected void apply(final JobInput origin) {
    m_id = origin.m_id;
    m_name = origin.m_name;
    m_mutexObject = origin.m_mutexObject;
    m_expirationTime = origin.m_expirationTime;
    m_logOnError = origin.m_logOnError;
    m_context = origin.m_context.copy();
  }

  /**
   * Creates a {@link JobInput} with a "snapshot" of the current calling context.
   */
  public static JobInput defaults() {
    final JobInput defaults = OBJ.get(JobInput.class);
    defaults.expirationTime(INFINITE_EXPIRATION, TimeUnit.MILLISECONDS);
    defaults.logOnError(true);
    defaults.context(Context.defaults());
    return defaults;
  }

  /**
   * Creates an empty {@link JobInput} with <code>null</code> as preferred {@link Subject} and {@link Locale}. Preferred
   * means, that those values are not derived from other values, but must be set explicitly instead.
   */
  public static JobInput empty() {
    final JobInput empty = OBJ.get(JobInput.class);
    empty.expirationTime(INFINITE_EXPIRATION, TimeUnit.MILLISECONDS);
    empty.logOnError(true);
    empty.context(Context.empty());
    return empty;
  }

  @Override
  public String toString() {
    final ToStringBuilder builder = new ToStringBuilder(this);
    builder.attr("id", getId());
    builder.attr("name", getName());
    return builder.toString();
  }
}
