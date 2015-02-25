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

import javax.security.auth.Subject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.scout.commons.job.IJobInput;
import org.eclipse.scout.commons.job.JobContext;
import org.eclipse.scout.commons.job.JobInput;
import org.eclipse.scout.rt.server.IServerSession;
import org.eclipse.scout.rt.server.commons.servletfilter.IHttpServletRoundtrip;
import org.eclipse.scout.rt.shared.ISession;

/**
 * Implementation of {@link IJobInput} with some additional data used for server-side jobs.
 *
 * @since 5.1
 */
public class ServerJobInput extends JobInput {

  private IServerSession m_session;
  private HttpServletRequest m_servletRequest;
  private HttpServletResponse m_servletResponse;

  /**
   * Use {@link #empty()} or {@link #defaults()} to create a {@link ServerJobInput}.
   */
  private ServerJobInput(final IJobInput template) {
    super(template);
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
  public ServerJobInput context(final JobContext context) {
    return (ServerJobInput) super.context(context);
  }

  /**
   * @param session
   *          {@link IServerSession} of behalf of which the job is to be executed; must not be <code>null</code>.
   * @return {@link IJobInput} to be used as builder.
   */
  public ServerJobInput session(final IServerSession session) {
    m_session = session;
    return this;
  }

  /**
   * @param servletRequest
   *          {@link HttpServletRequest} of the ongoing HTTP Servlet call; if set, the job's execution thread is
   *          associated with that {@link HttpServletRequest}.
   * @return {@link IJobInput} to be used as builder.
   */
  public ServerJobInput servletRequest(final HttpServletRequest servletRequest) {
    m_servletRequest = servletRequest;
    return this;
  }

  /**
   * @param servletResponse
   *          {@link HttpServletResponse} of the ongoing HTTP Servlet call; if set, the job's execution thread is
   *          associated with that {@link HttpServletResponse}.
   * @return {@link IJobInput} to be used as builder.
   */
  public ServerJobInput servletResponse(final HttpServletResponse servletResponse) {
    m_servletResponse = servletResponse;
    return this;
  }

  /**
   * @return {@link IServerSession} of behalf of which the job is to be executed; must not be <code>null</code>.
   */
  public IServerSession getSession() {
    return m_session;
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
   * Creates a {@link ServerJobInput} that is only filled with the job-context of the current thread, or if not
   * available, an empty one.
   */
  public static ServerJobInput empty() {
    return new ServerJobInput(JobInput.empty());
  }

  /**
   * Creates a {@link ServerJobInput} filled with the defaults from the current calling context.
   * <ul>
   * <li>{@link ServerJobInput#getSubject()}: subject of the current {@link AccessControlContext};</li>
   * <li>{@link ServerJobInput#getSession()}: session associated with the current thread;</li>
   * <li>{@link ServerJobInput#getServletRequest()}: {@link HttpServletRequest} associated with the current thread;
   * <li>{@link ServerJobInput#getServletResponse()}: {@link HttpServletResponse} associated with the current thread;</li>
   * <li>{@link ServerJobInput#getContext()}: copy of the job-context associated with the current thread, or if not
   * available, an empty {@link JobContext};
   * </ul>
   */
  public static ServerJobInput defaults() {
    return new ServerJobInput(JobInput.defaults())
        .session((IServerSession) ISession.CURRENT.get())
        .servletRequest(IHttpServletRoundtrip.CURRENT_HTTP_SERVLET_REQUEST.get())
        .servletResponse(IHttpServletRoundtrip.CURRENT_HTTP_SERVLET_RESPONSE.get());
  }
}
