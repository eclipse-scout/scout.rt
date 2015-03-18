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
import org.eclipse.scout.rt.platform.context.Context;

/**
 * Describes a job with context information to be applied onto the executing worker thread during the time of the job's
 * execution.
 * <p/>
 * The 'setter-methods' returns <code>this</code> in order to support for method chaining. The context has the following
 * characteristics:
 *
 * @see Context
 * @see IJobManager
 * @since 5.1
 */
public class JobInput<CONTEXT extends Context> {

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
  protected CONTEXT m_context;

  protected JobInput() {
  }

  protected JobInput(final JobInput<?> origin) {
    m_id = origin.m_id;
    m_name = origin.m_name;
    m_mutexObject = origin.m_mutexObject;
    m_expirationTime = origin.m_expirationTime;
  }

  // === setter methods ===

  public String getId() {
    return m_id;
  }

  /**
   * Sets the <code>id</code> of a job; must not be set; must not be unique; is primarily used for logging purpose, to
   * decorate the worker thread's name and to identify the job's Future.
   */
  public JobInput setId(final String id) {
    m_id = id;
    return this;
  }

  public String getName() {
    return m_name;
  }

  /**
   * Sets the name of a job; is used to decorate the worker thread's name and for logging purpose; must not be set.
   */
  public JobInput setName(final String name) {
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
  public JobInput setMutex(final Object mutexObject) {
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
  public JobInput setExpirationTime(final long time, final TimeUnit timeUnit) {
    m_expirationTime = timeUnit.toMillis(time);
    return this;
  }

  public CONTEXT getContext() {
    return m_context;
  }

  /**
   * Sets the {@link Context} to be set for the time of execution.
   */
  public JobInput setContext(final CONTEXT context) {
    m_context = Assertions.assertNotNull(context, "Context must not be null");
    return this;
  }

  public Subject getSubject() {
    return m_context.getSubject();
  }

  /**
   * Sets the Subject to execute the job under a particular user.
   */
  public JobInput setSubject(final Subject subject) {
    m_context.setSubject(subject);
    return this;
  }

  public Locale getLocale() {
    return m_context.getLocale();
  }

  /**
   * Sets the Locale to be set for the time of execution.
   */
  public JobInput setLocale(final Locale locale) {
    m_context.setLocale(locale);
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
    return new JobInput<>(this).setContext(getContext().copy());
  }

  /**
   * Creates a job-input with a "snapshot" of the current calling context.
   */
  public static JobInput defaults() {
    final JobInput<Context> defaults = new JobInput<>();
    defaults.setExpirationTime(INFINITE_EXPIRATION, TimeUnit.MILLISECONDS);
    defaults.setContext(Context.defaults());
    return defaults;
  }

  /**
   * Creates an empty job-input with <code>null</code> as preferred Locale.
   */
  public static JobInput empty() {
    final JobInput<Context> empty = new JobInput<>();
    empty.setExpirationTime(INFINITE_EXPIRATION, TimeUnit.MILLISECONDS);
    empty.setContext(Context.empty());
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
