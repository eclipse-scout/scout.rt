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

import javax.security.auth.Subject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.scout.commons.job.IJobInput;
import org.eclipse.scout.commons.job.JobContext;
import org.eclipse.scout.commons.job.JobInput;
import org.eclipse.scout.commons.nls.NlsLocale;
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

  private ServerJobInput(final IJobInput origin) {
    super(origin);
  }

  /**
   * Creates a copy of the given {@link ServerJobInput}.
   *
   * @param origin
   *          to be copied.
   */
  protected ServerJobInput(final ServerJobInput origin) {
    super(origin);
    m_session = origin.getSession();
    m_sessionRequired = origin.isSessionRequired();
    m_servletRequest = origin.getServletRequest();
    m_servletResponse = origin.getServletResponse();
    m_userAgent = origin.getUserAgent();
  }

  /**
   * Creates a copy of the current {@link ServerJobInput}.
   *
   * @return copy of the current {@link ServerJobInput}.
   */
  @Override
  public ServerJobInput copy() {
    return new ServerJobInput(this);
  }

  @Override
  public ServerJobInput id(final long id) {
    return (ServerJobInput) super.id(id);
  }

  @Override
  public ServerJobInput name(final String name) {
    return (ServerJobInput) super.name(name);
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
   *          {@link IServerSession} of behalf of which the job is to be executed; must not be <code>null</code> if
   *          <code>sessionRequired=true (default)</code>.
   * @return {@link IJobInput} to be used as builder.
   */
  public ServerJobInput session(final IServerSession session) {
    m_session = session;
    return this;
  }

  /**
   * @param sessionRequired
   *          <code>true</code> if the job requires to work on behalf of a session; by default, this property is
   *          <code>true</code> and should only be changed with caution.
   * @return {@link IJobInput} to be used as builder.
   */
  public ServerJobInput sessionRequired(final boolean sessionRequired) {
    m_sessionRequired = sessionRequired;
    return this;
  }

  /**
   * @param servletRequest
   *          {@link HttpServletRequest} of the ongoing HTTP Servlet call; if set, the job's execution thread is
   *          associated with the given {@link HttpServletRequest}.
   * @return {@link IJobInput} to be used as builder.
   */
  public ServerJobInput servletRequest(final HttpServletRequest servletRequest) {
    m_servletRequest = servletRequest;
    return this;
  }

  /**
   * @param servletResponse
   *          {@link HttpServletResponse} of the ongoing HTTP Servlet call; if set, the job's execution thread is
   *          associated with the given {@link HttpServletResponse}.
   * @return {@link IJobInput} to be used as builder.
   */
  public ServerJobInput servletResponse(final HttpServletResponse servletResponse) {
    m_servletResponse = servletResponse;
    return this;
  }

  /**
   * @param userAgent
   *          {@link UserAgent} to describe the user agent used by the client; if set, the job's execution thread is
   *          associated with the given {@link UserAgent}; must not be set for background-jobs.
   * @return {@link IJobInput} to be used as builder.
   */
  public ServerJobInput userAgent(final UserAgent userAgent) {
    m_userAgent = userAgent;
    return this;
  }

  /**
   * @return {@link IServerSession} of behalf of which the job is to be executed; must not be <code>null</code> if
   *         <code>sessionRequired=true (default)</code>.
   */
  public IServerSession getSession() {
    return m_session;
  }

  /**
   * @return <code>true</code> if the job manager asserts to have a session provided, either explicitly or by the
   *         current calling context; is <code>true</code> by default.
   */
  public boolean isSessionRequired() {
    return m_sessionRequired;
  }

  /**
   * @return {@link HttpServletRequest} of the ongoing HTTP Servlet call.
   */
  public HttpServletRequest getServletRequest() {
    return m_servletRequest;
  }

  /**
   * @return {@link HttpServletResponse} of the ongoing HTTP Servlet call.
   */
  public HttpServletResponse getServletResponse() {
    return m_servletResponse;
  }

  /**
   * @return {@link UserAgent} that describes the agent used by the client; must not be set, e.g. for background-jobs.
   */
  public UserAgent getUserAgent() {
    return m_userAgent;
  }

  /**
   * Creates a {@link ServerJobInput} that is only filled with the job-context of the current thread, or if not
   * available, an empty one.
   */
  public static ServerJobInput empty() {
    return new ServerJobInput(JobInput.empty()).sessionRequired(true);
  }

  /**
   * Creates a {@link ServerJobInput} filled with the defaults from the current calling context.
   * <ul>
   * <li>{@link ServerJobInput#getSubject()}: Subject associated with the current {@link AccessControlContext};</li>
   * <li>{@link ServerJobInput#getLocale()}: Locale associated with the current thread - in case of a
   * client-server-request, this is typically the Locale provided with the request unless not overwritten explicitly by
   * a dependent job; if not set on the current thread, the Locale is not set;</li>
   * <li>{@link ServerJobInput#getContext()}: copy of the job-context associated with the current thread, or if not
   * available, an empty {@link JobContext};
   * <li>{@link ServerJobInput#getSession()}: session associated with the current thread;</li>
   * <li>{@link ServerJobInput#getServletRequest()}: {@link HttpServletRequest} associated with the current thread;
   * <li>{@link ServerJobInput#getServletResponse()}: {@link HttpServletResponse} associated with the current thread;</li>
   * <li>{@link ServerJobInput#getUserAgent()}: {@link UserAgent} associated with the current thread;</li>
   * </ul>
   */
  public static ServerJobInput defaults() {
    final ServerJobInput defaults = new ServerJobInput(JobInput.defaults());

    defaults.session((IServerSession) ISession.CURRENT.get());
    defaults.sessionRequired(true);
    defaults.servletRequest(IHttpServletRoundtrip.CURRENT_HTTP_SERVLET_REQUEST.get());
    defaults.servletResponse(IHttpServletRoundtrip.CURRENT_HTTP_SERVLET_RESPONSE.get());
    defaults.locale(NlsLocale.CURRENT.get());
    defaults.userAgent(UserAgent.CURRENT.get());

    return defaults;
  }
}
