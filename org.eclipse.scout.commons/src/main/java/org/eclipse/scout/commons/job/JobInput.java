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
import java.util.concurrent.TimeUnit;

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
  private long m_expirationTime = IJobInput.INFINITE_EXPIRATION;
  private Subject m_subject;
  private Locale m_locale;
  private boolean m_preferredLocaleSet;
  private JobContext m_context;

  private JobInput() {
  }

  /**
   * Creates a copy of the given {@link IJobInput}.
   */
  protected JobInput(final JobInput origin) {
    m_id = origin.m_id;
    m_name = origin.m_name;
    m_expirationTime = origin.m_expirationTime;
    m_subject = origin.m_subject;
    m_locale = origin.m_locale;
    m_preferredLocaleSet = origin.m_preferredLocaleSet;
    m_context = JobContext.copy(origin.m_context);
  }

  /**
   * Creates a copy of the current {@link JobInput}.
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
  public JobInput expirationTime(final long time, final TimeUnit timeUnit) {
    m_expirationTime = timeUnit.toMillis(time);
    return this;
  }

  @Override
  public JobInput subject(final Subject subject) {
    m_subject = subject;
    return this;
  }

  @Override
  public JobInput locale(final Locale locale) {
    return locale(locale, true); // set as preferred Locale.
  }

  /**
   * Sets the given {@link Locale} only if <code>preferred</code> or no preferred {@link Locale} is set yet.
   *
   * @return {@link JobInput} to be used as builder.
   */
  protected JobInput locale(final Locale locale, final boolean preferred) {
    if (preferred || !isPreferredLocaleSet()) {
      m_locale = locale;
    }

    if (preferred) {
      m_preferredLocaleSet = true;
    }
    return this;
  }

  /**
   * @return <code>true</code> if the {@link Locale} was set explicitly as preferred value.
   */
  protected boolean isPreferredLocaleSet() {
    return m_preferredLocaleSet;
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
  public long getExpirationTimeMillis() {
    return m_expirationTime;
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
    return m_context != null ? m_context : new JobContext();
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
   * Creates a {@link JobInput} filled with a <code>null</code>-Locale as preferred value.
   */
  public static JobInput empty() {
    return new JobInput().locale(null, true); // explicitly set null as preferred Locale.
  }

  /**
   * Creates a {@link JobInput} filled with the defaults from the current calling context:
   * <ul>
   * <li>{@link Subject} which is associated with the current {@link AccessControlContext};</li>
   * <li>{@link JobContext} which is associated with the the current thread;</li>
   * <li>{@link Locale} which is associated with the current thread; is set as non-preferred value;</li>
   * </ul>
   */
  public static JobInput defaults() {
    final JobInput defaults = new JobInput();
    defaults.subject(Subject.getSubject(AccessController.getContext()));
    defaults.locale(NlsLocale.CURRENT.get(), false); // set as not-preferred Locale.
    defaults.context(JobContext.copy(JobContext.CURRENT.get()));
    return defaults;
  }
}
