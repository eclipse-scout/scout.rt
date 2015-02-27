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

import java.security.AccessControlContext;
import java.security.AccessController;
import java.util.Locale;

import javax.security.auth.Subject;

import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.commons.nls.NlsLocale;

/**
 * Default implementation of {@link IJobInput}.
 *
 * @since 5.1
 */
public class JobInput implements IJobInput {

  private long m_id;
  private String m_name;
  private Subject m_subject;
  private Locale m_locale;
  private JobContext m_context;

  private JobInput() {
  }

  /**
   * Creates a copy of the given {@link IJobInput}.
   *
   * @param origin
   *          to be copied.
   */
  protected JobInput(final IJobInput origin) {
    m_id = origin.getId();
    m_name = origin.getName();
    m_subject = origin.getSubject();
    m_locale = origin.getLocale();
    m_context = JobContext.copy(origin.getContext());
  }

  /**
   * Creates a copy of the current {@link JobInput}.
   *
   * @return copy of the current {@link JobInput}.
   */
  public JobInput copy() {
    return new JobInput(this);
  }

  @Override
  public JobInput id(final long id) {
    m_id = id;
    return this;
  }

  @Override
  public JobInput name(final String name) {
    m_name = name;
    return this;
  }

  @Override
  public JobInput subject(final Subject subject) {
    m_subject = subject;
    return this;
  }

  @Override
  public JobInput locale(final Locale locale) {
    m_locale = locale;
    return this;
  }

  @Override
  public JobInput context(final JobContext context) {
    m_context = context;
    return this;
  }

  @Override
  public long getId() {
    return m_id;
  }

  @Override
  public String getName() {
    return m_name;
  }

  @Override
  public Subject getSubject() {
    return m_subject;
  }

  @Override
  public Locale getLocale() {
    return m_locale;
  }

  @Override
  public JobContext getContext() {
    return m_context;
  }

  @Override
  public String getIdentifier(final String defaultIdentifier) {
    if (m_id != 0L && StringUtility.hasText(m_name)) {
      return String.format("%s;%s", m_id, m_name);
    }
    else if (StringUtility.hasText(m_name)) {
      return m_name;
    }
    else if (m_id != 0L) {
      return String.valueOf(m_id);
    }
    else {
      return defaultIdentifier;
    }
  }

  /**
   * Creates a {@link IJobInput} that is only filled with the {@link JobContext} of the current thread, or if not
   * available, an empty one.
   */
  public static JobInput empty() {
    final JobInput empty = new JobInput();

    empty.context(JobContext.copy(JobContext.CURRENT.get()));

    return empty;
  }

  /**
   * Creates a {@link JobInput} filled with the defaults from the current calling context.
   * <ul>
   * <li>{@link IJobInput#getSubject()}: Subject associated with the current {@link AccessControlContext};</li>
   * <li>{@link IJobInput#getLocale()}: Locale associated with the current thread or <code>null</code> if not set;</li>
   * <li>{@link ServerJobInput#getContext()}: copy of the job-context associated with the current thread, or if not
   * available, an empty {@link JobContext};
   * </ul>
   */
  public static JobInput defaults() {
    final JobInput defaults = new JobInput();

    defaults.subject(Subject.getSubject(AccessController.getContext()));
    defaults.locale(NlsLocale.CURRENT.get());
    defaults.context(JobContext.copy(JobContext.CURRENT.get()));

    return defaults;
  }
}
