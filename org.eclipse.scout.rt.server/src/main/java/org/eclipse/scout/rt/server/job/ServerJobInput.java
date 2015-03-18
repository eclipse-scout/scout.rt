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

import java.util.Locale;
import java.util.concurrent.TimeUnit;

import javax.security.auth.Subject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.core.runtime.jobs.IJobManager;
import org.eclipse.scout.commons.ToStringBuilder;
import org.eclipse.scout.rt.platform.job.JobInput;
import org.eclipse.scout.rt.server.IServerSession;
import org.eclipse.scout.rt.server.context.ServerContext;
import org.eclipse.scout.rt.shared.ui.UserAgent;

/**
 * Describes a server-side job with context information to be applied onto the executing worker thread during the time
 * of the job's execution.
 * <p/>
 * The 'setter-methods' returns <code>this</code> in order to support for method chaining. The context has the following
 * characteristics:
 *
 * @see ServerContext
 * @see ServerJobs
 * @see IJobManager
 * @since 5.1
 */
public class ServerJobInput extends JobInput<ServerContext> {

  protected ServerJobInput(final JobInput origin) {
    super(origin);
  }

  @Override
  public ServerJobInput setId(final String id) {
    return (ServerJobInput) super.setId(id);
  }

  @Override
  public ServerJobInput setName(final String name) {
    return (ServerJobInput) super.setName(name);
  }

  @Override
  public ServerJobInput setMutex(final Object mutexObject) {
    return (ServerJobInput) super.setMutex(mutexObject);
  }

  @Override
  public ServerJobInput setExpirationTime(final long time, final TimeUnit timeUnit) {
    return (ServerJobInput) super.setExpirationTime(time, timeUnit);
  }

  @Override
  public ServerJobInput setContext(final ServerContext context) {
    return (ServerJobInput) super.setContext(context);
  }

  @Override
  public ServerJobInput setSubject(final Subject subject) {
    return (ServerJobInput) super.setSubject(subject);
  }

  @Override
  public ServerJobInput setLocale(final Locale locale) {
    return (ServerJobInput) super.setLocale(locale);
  }

  public IServerSession getSession() {
    return getContext().getSession();
  }

  /**
   * Sets the session. There are no other values derived from the given session.
   */
  public ServerJobInput setSession(final IServerSession session) {
    getContext().setSession(session);
    return this;
  }

  public boolean isSessionRequired() {
    return getContext().isSessionRequired();
  }

  /**
   * @param sessionRequired
   *          <code>false</code> if the context does not require a session. By default, a session is required.
   * @return <code>this</code> in order to support for method chaining
   */
  public ServerJobInput setSessionRequired(final boolean sessionRequired) {
    getContext().setSessionRequired(sessionRequired);
    return this;
  }

  public HttpServletRequest getServletRequest() {
    return getContext().getServletRequest();
  }

  /**
   * Sets the HTTP ServletRequest to be set for the time of execution.
   */
  public ServerJobInput setServletRequest(final HttpServletRequest servletRequest) {
    getContext().setServletRequest(servletRequest);
    return this;
  }

  public HttpServletResponse getServletResponse() {
    return getContext().getServletResponse();
  }

  /**
   * Sets the HTTP ServletResponse to be set for the time of execution.
   */
  public ServerJobInput setServletResponse(final HttpServletResponse servletResponse) {
    getContext().setServletResponse(servletResponse);
    return this;
  }

  public UserAgent getUserAgent() {
    return getContext().getUserAgent();
  }

  /**
   * Sets the UserAgent to be set for the time of execution.
   */
  public ServerJobInput setUserAgent(final UserAgent userAgent) {
    getContext().setUserAgent(userAgent);
    return this;
  }

  public long getTransactionId() {
    return getContext().getTransactionId();
  }

  /**
   * Sets the transaction-ID to be set for the time of execution.
   */
  public ServerJobInput setTransactionId(final long transactionId) {
    getContext().setTransactionId(transactionId);
    return this;
  }

  public boolean isTransactional() {
    return getContext().isTransactional();
  }

  /**
   * Sets whether this job should run in a separate transaction.
   */
  public ServerJobInput setTransactional(final boolean transactional) {
    getContext().setTransactional(transactional);
    return this;
  }

  @Override
  public String getThreadName() {
    return "scout-server-thread";
  }

  // === construction methods ===

  @Override
  public ServerJobInput copy() {
    return new ServerJobInput(this).setContext(getContext().copy());
  }

  public static ServerJobInput defaults() {
    final ServerJobInput defaults = new ServerJobInput(JobInput.defaults());
    defaults.setContext(ServerContext.defaults());
    defaults.setSessionRequired(true);
    return defaults;
  }

  public static ServerJobInput empty() {
    final ServerJobInput empty = new ServerJobInput(JobInput.empty());
    empty.setContext(ServerContext.empty());
    empty.setSessionRequired(true);
    return empty;
  }

  @Override
  public String toString() {
    final ToStringBuilder builder = new ToStringBuilder(this);
    builder.attr("id", getId());
    builder.attr("name", getName());
    builder.ref("session", getSession());
    return builder.toString();
  }
}
