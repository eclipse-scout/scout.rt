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
package org.eclipse.scout.rt.server.job;

import java.security.AccessControlContext;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import javax.security.auth.Subject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.scout.commons.Assertions;
import org.eclipse.scout.commons.Assertions.AssertionException;
import org.eclipse.scout.commons.ToStringBuilder;
import org.eclipse.scout.commons.job.IJobInput;
import org.eclipse.scout.commons.job.JobContext;
import org.eclipse.scout.commons.job.JobInput;
import org.eclipse.scout.rt.server.IServerSession;
import org.eclipse.scout.rt.server.commons.servletfilter.IHttpServletRoundtrip;
import org.eclipse.scout.rt.shared.ISession;
import org.eclipse.scout.rt.shared.ui.UserAgent;

/**
 * Implementation of {@link IJobInput} with some additional data used for server-side jobs.
 *
 * @since 5.1
 */
public class ServerJobInput extends JobInput {

  private IServerSession m_session;
  private boolean m_sessionRequired;
  private HttpServletRequest m_servletRequest;
  private HttpServletResponse m_servletResponse;
  private UserAgent m_userAgent;
  private boolean m_preferredUserAgentSet;
  private boolean m_transactional;

  private ServerJobInput(final JobInput origin) {
    super(origin);
  }

  /**
   * Creates a copy of the given {@link ServerJobInput}.
   */
  protected ServerJobInput(final ServerJobInput origin) {
    super(origin);
    m_session = origin.m_session;
    m_sessionRequired = origin.m_sessionRequired;
    m_servletRequest = origin.m_servletRequest;
    m_servletResponse = origin.m_servletResponse;
    m_userAgent = origin.m_userAgent;
    m_preferredUserAgentSet = origin.m_preferredUserAgentSet;
    m_transactional = origin.m_transactional;
  }

  /**
   * Creates a copy of the current {@link ServerJobInput}.
   */
  @Override
  public ServerJobInput copy() {
    return new ServerJobInput(this);
  }

  @Override
  public ServerJobInput id(final String id) {
    return (ServerJobInput) super.id(id);
  }

  @Override
  public ServerJobInput name(final String name) {
    return (ServerJobInput) super.name(name);
  }

  @Override
  public ServerJobInput expirationTime(final long time, final TimeUnit timeUnit) {
    return (ServerJobInput) super.expirationTime(time, timeUnit);
  }

  @Override
  public ServerJobInput subject(final Subject subject) {
    return (ServerJobInput) super.subject(subject);
  }

  @Override
  public ServerJobInput locale(final Locale locale) {
    return (ServerJobInput) super.locale(locale);
  }

  @Override
  public ServerJobInput context(final JobContext context) {
    return (ServerJobInput) super.context(context);
  }

  /**
   * @param session
   *          {@link IServerSession} to associate the executing job with; must not be <code>null</code>, unless
   *          {@link #sessionRequired(boolean)} is set explicitly to <code>false</code>.
   * @return {@link ServerJobInput} to be used as builder.
   * @see #sessionRequired(boolean)
   */
  public ServerJobInput session(final IServerSession session) {
    m_session = session;
    // do not update the Locale and UserAgent with derived values from the session, as not referring to real value-members but Thread-Locals instead.
    return this;
  }

  /**
   * @param sessionRequired
   *          <code>true</code> if the job requires to work on behalf of a session; is <code>true</code> by default.
   * @return {@link ServerJobInput} to be used as builder.
   * @see #session(IServerSession)
   */
  public ServerJobInput sessionRequired(final boolean sessionRequired) {
    m_sessionRequired = sessionRequired;
    return this;
  }

  /**
   * @param servletRequest
   *          {@link HttpServletRequest} which the job's execution thread is to be associated with.
   * @return {@link ServerJobInput} to be used as builder.
   */
  public ServerJobInput servletRequest(final HttpServletRequest servletRequest) {
    m_servletRequest = servletRequest;
    return this;
  }

  /**
   * @param servletResponse
   *          {@link HttpServletResponse} which the job's execution thread is to be associated with.
   * @return {@link ServerJobInput} to be used as builder.
   */
  public ServerJobInput servletResponse(final HttpServletResponse servletResponse) {
    m_servletResponse = servletResponse;
    return this;
  }

  /**
   * @param userAgent
   *          {@link UserAgent} which the job's execution thread is to be associated with.
   * @return {@link ServerJobInput} to be used as builder.
   */
  public ServerJobInput userAgent(final UserAgent userAgent) {
    return userAgent(userAgent, true); // set as preferred UserAgent.
  }

  /**
   * Sets the given {@link UserAgent} only if <code>preferred</code> or no preferred {@link UserAgent} is set yet.
   *
   * @return {@link ServerJobInput} to be used as builder.
   */
  protected ServerJobInput userAgent(final UserAgent userAgent, final boolean preferred) {
    if (preferred || !isPreferredUserAgentSet()) {
      m_userAgent = userAgent;
    }

    if (preferred) {
      m_preferredUserAgentSet = true;
    }
    return this;
  }

  /**
   * @param transactional
   *          <code>true</code> if the job should run on behalf of a transaction; is <code>true</code> by default.
   * @return {@link ServerJobInput} to be used as builder.
   * @see #isTransactional()
   */
  public ServerJobInput transactional(final boolean transactional) {
    m_transactional = transactional;
    return this;
  }

  /**
   * @return <code>true</code> if the {@link UserAgent} was set explicitly as preferred value.
   */
  protected boolean isPreferredUserAgentSet() {
    return m_preferredUserAgentSet;
  }

  /**
   * @see #session(IServerSession)
   * @see #sessionRequired(boolean)
   * @throws AssertionException
   *           if the session is <code>null</code> but required.
   */
  public IServerSession getSession() {
    if (isSessionRequired()) {
      Assertions.assertNotNull(m_session, "ServerSession must not be null");
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
   * @see #servletRequest(HttpServletRequest)
   */
  public HttpServletRequest getServletRequest() {
    return m_servletRequest;
  }

  /**
   * @see #servletResponse(HttpServletResponse)
   */
  public HttpServletResponse getServletResponse() {
    return m_servletResponse;
  }

  /**
   * @see #userAgent(UserAgent)
   */
  public UserAgent getUserAgent() {
    return m_userAgent;
  }

  /**
   * @see #transactional(boolean)
   */
  public boolean isTransactional() {
    return m_transactional;
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
    builder.ref("servletRequest", getUserAgent());
    builder.ref("servletResponse", getUserAgent());
    return builder.toString();
  }

  /**
   * Creates an empty {@link ServerJobInput} filled with an explicit <code>null</code>-Locale and <code>null</code>
   * -UserAgent as preferred values.
   *
   * @return {@link ServerJobInput}; requires a <code>not-null</code> {@link IServerSession} to be set.
   */
  public static ServerJobInput empty() {
    return new ServerJobInput(JobInput.empty()).sessionRequired(true).transactional(true).userAgent(null, true); // explicitly set null as preferred UserAgent.
  }

  /**
   * Creates a {@link ServerJobInput} filled with the defaults from the current calling context:
   * <ul>
   * <li>{@link Subject} which is associated with the current {@link AccessControlContext};</li>
   * <li>{@link JobContext} which is associated with the the current thread;</li>
   * <li>{@link Locale} which is associated with the current thread; is set as non-preferred value;</li>
   * <li>{@link UserAgent} which is associated with the current thread; is set as non-preferred value;</li>
   * <li>{@link IServerSession} which is associated with the current thread;</li>
   * <li>{@link HttpServletRequest} which is associated with the current thread;</li>
   * <li>{@link HttpServletResponse} which is associated with the current thread;</li>
   * </ul>
   *
   * @return {@link ServerJobInput}; requires a <code>not-null</code> {@link IServerSession} to be set.
   */
  public static ServerJobInput defaults() {
    final ServerJobInput defaults = new ServerJobInput(JobInput.defaults());
    defaults.userAgent(UserAgent.CURRENT.get(), false); // set as not-preferred UserAgent.
    defaults.servletRequest(IHttpServletRoundtrip.CURRENT_HTTP_SERVLET_REQUEST.get());
    defaults.servletResponse(IHttpServletRoundtrip.CURRENT_HTTP_SERVLET_RESPONSE.get());
    defaults.session((IServerSession) ISession.CURRENT.get()); // must be set after setting the Locale and the UserAgent because session-bound values have precedence.
    defaults.sessionRequired(true);
    defaults.transactional(true);
    return defaults;
  }
}
