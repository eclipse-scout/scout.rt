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

import javax.security.auth.Subject;

/**
 * Describes a job to be executed with instruction details about the job's execution.<br/>
 * This interface is designed to be used as fluent builder to make the client code more readable.
 *
 * @since 5.1
 */
public interface IJobInput {

  /**
   * @param id
   *          <code>id</code> of a job; must not be unique; must not be set; is primarily used to identify the job's
   *          Future, e.g. for canceling that job without knowing its Future.
   * @return {@link IJobInput} to be used as builder.
   */
  IJobInput id(long id);

  /**
   * @param name
   *          name of a job; is used to decorate the name of the executing worker thread and for logging; must not be
   *          set.
   * @return {@link IJobInput} to be used as builder.
   */
  IJobInput name(String name);

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
   * @see #id(long)
   */
  long getId();

  /**
   * @see #name(String)
   */
  String getName();

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
   * Returns the identifier that consists of the job's <code>id</code> and name, or <code>defaultIdentifier</code> if
   * not set.
   */
  String getIdentifier(String defaultIdentifier);
}
