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
package org.eclipse.scout.rt.client.job;

import java.security.AccessControlContext;
import java.util.Locale;

import javax.security.auth.Subject;

import org.eclipse.scout.commons.job.IJobInput;
import org.eclipse.scout.commons.job.JobContext;
import org.eclipse.scout.commons.job.JobInput;
import org.eclipse.scout.commons.nls.NlsLocale;
import org.eclipse.scout.rt.client.IClientSession;
import org.eclipse.scout.rt.shared.ISession;
import org.eclipse.scout.rt.shared.ui.UserAgent;

/**
 * Implementation of {@link IJobInput} with some additional data used for client-side jobs.
 *
 * @see JobInput
 * @since 5.1
 */
public class ClientJobInput extends JobInput {

  private IClientSession m_session;
  private UserAgent m_userAgent;

  private ClientJobInput(final IJobInput origin) {
    super(origin);
  }

  /**
   * Creates a copy of the given {@link ClientJobInput}.
   *
   * @param origin
   *          to be copied.
   */
  protected ClientJobInput(final ClientJobInput origin) {
    super(origin);
    m_session = origin.getSession();
    m_userAgent = origin.getUserAgent();
  }

  /**
   * Creates a copy of the current {@link ClientJobInput}.
   *
   * @return copy of the current {@link ClientJobInput}.
   */
  @Override
  public ClientJobInput copy() {
    return new ClientJobInput(this);
  }

  @Override
  public ClientJobInput id(final long id) {
    return (ClientJobInput) super.id(id);
  }

  @Override
  public ClientJobInput name(final String name) {
    return (ClientJobInput) super.name(name);
  }

  @Override
  public ClientJobInput subject(final Subject subject) {
    return (ClientJobInput) super.subject(subject);
  }

  @Override
  public ClientJobInput context(final JobContext context) {
    return (ClientJobInput) super.context(context);
  }

  /**
   * @param session
   *          {@link IClientSession} of behalf of which the job is to be executed; must not be <code>null</code>.
   * @return {@link IJobInput} to be used as builder.
   */
  public ClientJobInput session(final IClientSession session) {
    m_session = session;
    return this;
  }

  /**
   * @param userAgent
   *          {@link UserAgent} to describe the user agent used by the client; if set, the job's execution thread is
   *          associated with the given {@link UserAgent}.
   * @return {@link IJobInput} to be used as builder.
   */
  public ClientJobInput userAgent(final UserAgent userAgent) {
    m_userAgent = userAgent;
    return this;
  }

  /**
   * @return {@link IClientSession} of behalf of which the job is to be executed; must not be <code>null</code>.
   */
  public IClientSession getSession() {
    return m_session;
  }

  /**
   * @return {@link UserAgent} that describes the agent used by the client; must not be set.
   */
  public UserAgent getUserAgent() {
    return m_userAgent;
  }

  /**
   * Creates a {@link ClientJobInput} that is only filled with the job-context of the current thread, or if not
   * available, an empty one.
   */
  public static ClientJobInput empty() {
    return new ClientJobInput(JobInput.empty());
  }

  /**
   * Creates a {@link ClientJobInput} filled with the defaults from the current calling context.
   * <ul>
   * <li>{@link ClientJobInput#getSubject()}: Subject associated with the current {@link AccessControlContext};</li>
   * <li>{@link ClientJobInput#getLocale()}: the session's {@link Locale} if set, or the current thread's Locale if set,
   * or the JVM default otherwise;</li>
   * <li>{@link ClientJobInput#getContext()}: copy of the job-context associated with the current thread, or if not
   * available, an empty {@link JobContext};
   * <li>{@link ClientJobInput#getSession()}: session associated with the current thread;</li>
   * <li>{@link ServerJobInput#getUserAgent()}: {@link UserAgent} associated with the current thread;</li>
   * </ul>
   */
  public static ClientJobInput defaults() {
    final ClientJobInput defaults = new ClientJobInput(JobInput.defaults());

    defaults.session((IClientSession) ISession.CURRENT.get());
    defaults.locale(resolveDefaultLocale());
    defaults.userAgent(UserAgent.CURRENT.get());

    return defaults;
  }

  /**
   * @return the session's {@link Locale} if set, or the current thread's Locale if set, or the JVM default otherwise.
   */
  private static Locale resolveDefaultLocale() {
    final IClientSession currentSession = (IClientSession) ISession.CURRENT.get();
    final Locale sessionLocale = (currentSession != null ? currentSession.getLocale() : null);

    if (sessionLocale != null) {
      return sessionLocale; // 1st priority
    }
    else if (NlsLocale.CURRENT.get() != null) {
      return NlsLocale.CURRENT.get(); // 2nd priority
    }
    else {
      return Locale.getDefault(); // 3th priority
    }
  }
}
