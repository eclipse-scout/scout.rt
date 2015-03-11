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
import java.util.concurrent.TimeUnit;

import javax.security.auth.Subject;

import org.eclipse.scout.commons.Assertions;
import org.eclipse.scout.commons.Assertions.AssertionException;
import org.eclipse.scout.commons.ToStringBuilder;
import org.eclipse.scout.commons.job.IJobInput;
import org.eclipse.scout.commons.job.JobContext;
import org.eclipse.scout.commons.job.JobInput;
import org.eclipse.scout.rt.client.IClientSession;
import org.eclipse.scout.rt.client.session.ClientSessionProvider;
import org.eclipse.scout.rt.shared.ui.UserAgent;

/**
 * Implementation of {@link IJobInput} with some additional data used for client-side jobs.
 *
 * @see JobInput
 * @since 5.1
 */
public class ClientJobInput extends JobInput {

  private IClientSession m_session;
  private boolean m_sessionRequired;
  private UserAgent m_userAgent;
  private boolean m_preferredUserAgentSet;

  private ClientJobInput(final JobInput origin) {
    super(origin);
  }

  /**
   * Creates a copy of the given {@link ClientJobInput}.
   */
  protected ClientJobInput(final ClientJobInput origin) {
    super(origin);
    m_session = origin.m_session;
    m_sessionRequired = origin.m_sessionRequired;
    m_userAgent = origin.m_userAgent;
    m_preferredUserAgentSet = origin.m_preferredUserAgentSet;
  }

  /**
   * Creates a copy of the current {@link ClientJobInput}.
   */
  @Override
  public ClientJobInput copy() {
    return new ClientJobInput(this);
  }

  @Override
  public ClientJobInput id(final String id) {
    return (ClientJobInput) super.id(id);
  }

  @Override
  public ClientJobInput name(final String name) {
    return (ClientJobInput) super.name(name);
  }

  @Override
  public ClientJobInput expirationTime(final long time, final TimeUnit timeUnit) {
    return (ClientJobInput) super.expirationTime(time, timeUnit);
  }

  @Override
  public ClientJobInput subject(final Subject subject) {
    return (ClientJobInput) super.subject(subject);
  }

  @Override
  public ClientJobInput locale(final Locale locale) {
    return (ClientJobInput) super.locale(locale);
  }

  @Override
  public ClientJobInput context(final JobContext context) {
    return (ClientJobInput) super.context(context);
  }

  /**
   * @param session
   *          {@link IClientSession} to associate the executing job with; must not be <code>null</code>, unless
   *          {@link #sessionRequired(boolean)} is set explicitly to <code>false</code>.<br/>
   *          If set, the {@link Locale} and {@link UserAgent} are set accordingly, unless set explicitly as preferred
   *          values.
   * @return {@link ClientJobInput} to be used as builder.
   * @see #sessionRequired(boolean)
   */
  public ClientJobInput session(final IClientSession session) {
    m_session = session;

    if (session != null) {
      // Update the Locale with the session's Locale if not set explicitly yet.
      locale(session.getLocale(), false);

      // Update the UserAgent with the session's UserAgent if not set explicitly yet.
      userAgent(session.getUserAgent(), false);
    }

    return this;
  }

  /**
   * @param sessionRequired
   *          <code>true</code> if the job requires to work on behalf of a session; is <code>true</code> by default.
   * @return {@link ClientJobInput} to be used as builder.
   * @see #session(IClientSession)
   */
  public ClientJobInput sessionRequired(final boolean sessionRequired) {
    m_sessionRequired = sessionRequired;
    return this;
  }

  /**
   * @param userAgent
   *          {@link UserAgent} which the job's execution thread is to be associated with.
   * @return {@link ClientJobInput} to be used as builder.
   */
  public ClientJobInput userAgent(final UserAgent userAgent) {
    return userAgent(userAgent, true); // set as preferred Locale.
  }

  /**
   * Sets the given {@link UserAgent} only if <code>preferred</code> or no preferred {@link UserAgent} is set yet.
   *
   * @return {@link ClientJobInput} to be used as builder.
   */
  protected ClientJobInput userAgent(final UserAgent userAgent, final boolean preferred) {
    if (preferred || !isPreferredUserAgentSet()) {
      m_userAgent = userAgent;
    }

    if (preferred) {
      m_preferredUserAgentSet = true;
    }
    return this;
  }

  /**
   * @return <code>true</code> if the {@link UserAgent} was set explicitly as preferred value.
   */
  protected boolean isPreferredUserAgentSet() {
    return m_preferredUserAgentSet;
  }

  /**
   * @see #session(IClientSession)
   * @see #sessionRequired(boolean)
   * @throws AssertionException
   *           if the session is <code>null</code> but required.
   */
  public IClientSession getSession() {
    if (isSessionRequired()) {
      Assertions.assertNotNull(m_session, "ClientSession must not be null");
    }
    return m_session;
  }

  /**
   * @see #sessionRequired(boolean)
   */
  protected boolean isSessionRequired() {
    return m_sessionRequired;
  }

  /**
   * @see #userAgent(UserAgent)
   */
  public UserAgent getUserAgent() {
    return m_userAgent;
  }

  @Override
  public String toString() {
    final ToStringBuilder builder = new ToStringBuilder(this);
    builder.attr("id", getId());
    builder.attr("name", getName());
    builder.attr("subject", getSubject());
    builder.attr("locale", getLocale());
    builder.ref("session", getSession());
    builder.attr("sessionRequired", isSessionRequired());
    builder.attr("userAgent", getUserAgent());
    return builder.toString();
  }

  /**
   * Creates an empty {@link ClientJobInput} filled with an explicit <code>null</code>-Locale and <code>null</code>
   * -UserAgent as preferred values.
   *
   * @return {@link ClientJobInput}; requires a <code>not-null</code> {@link IClientSession} to be set.
   */
  public static ClientJobInput empty() {
    return new ClientJobInput(JobInput.empty()).sessionRequired(true).userAgent(null, true); // explicitly set null as preferred UserAgent.
  }

  /**
   * Creates a {@link ClientJobInput} filled with the defaults from the current calling context:
   * <ul>
   * <li>{@link Subject} which is associated with the current {@link AccessControlContext};</li>
   * <li>{@link JobContext} which is associated with the the current thread;</li>
   * <li>{@link Locale} which is associated with the current thread or the current session's Locale if applicable; is
   * set as non-preferred value, meaning that if setting the session explicitly, the Locale is derived as well;</li>
   * <li>{@link UserAgent} which is associated with the current thread or the current session's UserAgent if applicable;
   * is set as non-preferred value, meaning that if setting the session explicitly, the UserAgent is derived as well;</li>
   * <li>{@link IClientSession} which is associated with the current thread;</li>
   * </ul>
   *
   * @return {@link ClientJobInput}; requires a <code>not-null</code> {@link IClientSession} to be set.
   */
  public static ClientJobInput defaults() {
    final ClientJobInput defaults = new ClientJobInput(JobInput.defaults());
    defaults.userAgent(UserAgent.CURRENT.get(), false); // set as not-preferred UserAgent.
    defaults.session(ClientSessionProvider.currentSession()); // must be set after setting the Locale and the UserAgent because session-bound values have precedence.
    defaults.sessionRequired(true);
    return defaults;
  }
}
