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
package org.eclipse.scout.commons.job;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

import javax.security.auth.Subject;

/**
 * Describes a job to be executed with instruction details about the job's execution.<br/>
 * This interface is designed to be used as fluent builder to make the client code more readable.
 *
 * @since 5.1
 */
public interface IJobInput {

  String ANONYMOUS_IDENTIFIER = "anonymous";

  /**
   * Indicates that an executable always commence execution regardless of how long it was waiting for its execution to
   * start.
   */
  long INFINITE_EXPIRATION = 0;

  /**
   * @param id
   *          <code>id</code> of a job; must not be unique; must not be set; is primarily used to identify the job's
   *          Future, e.g. for canceling that job without knowing its Future.
   * @return {@link IJobInput} to be used as builder.
   */
  IJobInput id(String id);

  /**
   * @param name
   *          name of a job; is used to decorate the name of the executing worker thread and for logging; must not be
   *          set.
   * @return {@link IJobInput} to be used as builder.
   */
  IJobInput name(String name);

  /**
   * Sets the maximal expiration time, until a {@link IExecutable} must commence execution; if elapsed, the executable
   * is discarded and never commence execution; is useful, if using a job manager which, according to its scheduling
   * rules, might queue scheduled executables prior execution. By default, there is no expiration time used.
   *
   * @param time
   *          the maximal expiration time until an executable must commence execution.
   * @param timeUnit
   *          the time unit of the <code>time</code> argument.
   * @return {@link IJobInput} to be used as builder.
   */
  IJobInput expirationTime(long time, TimeUnit timeUnit);

  /**
   * @param subject
   *          {@link Subject} of behalf of which the job is to be executed; must not be set.
   * @return {@link IJobInput} to be used as builder.
   */
  IJobInput subject(Subject subject);

  /**
   * @param locale
   *          {@link Locale} which the job's execution thread is to be associated with.
   * @return {@link IJobInput} to be used as builder.
   */
  IJobInput locale(Locale locale);

  /**
   * @param context
   *          {@link JobContext} to propagate data along with nested jobs; must not be set.
   * @return {@link IJobInput} to be used as builder.
   */
  IJobInput context(JobContext context);

  /**
   * @see #id(String)
   */
  String getId();

  /**
   * @see #name(String)
   */
  String getName();

  /**
   * @return the maximal expiration time until an executable must commence execution or if elapsed, is discarded; if
   *         {@link #INFINITE_EXPIRATION}, the executable will always be executed.
   */
  long getExpirationTimeMillis();

  /**
   * @see #subject(Subject)
   */
  Subject getSubject();

  /**
   * @see #locale(Locale)
   */
  Locale getLocale();

  /**
   * @return {@link JobContext} as set or an empty context if not set.
   * @see #context(JobContext)
   */
  JobContext getContext();

  /**
   * Returns the identifier that consists of the job's <code>id</code> and <code>name</code>, or
   * {@link #ANONYMOUS_IDENTIFIER} if unknown.
   */
  String getIdentifier();
}
