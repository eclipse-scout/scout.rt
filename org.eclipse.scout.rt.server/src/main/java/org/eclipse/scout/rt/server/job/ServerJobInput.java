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

import org.eclipse.scout.commons.Assertions;
import org.eclipse.scout.commons.ToStringBuilder;
import org.eclipse.scout.rt.platform.OBJ;
import org.eclipse.scout.rt.platform.context.Context;
import org.eclipse.scout.rt.platform.job.IJobManager;
import org.eclipse.scout.rt.platform.job.JobInput;
import org.eclipse.scout.rt.server.IServerSession;
import org.eclipse.scout.rt.server.context.ServerContext;
import org.eclipse.scout.rt.shared.ui.UserAgent;

/**
 * Describes a server-side job with context information to be applied onto the executing worker thread during the time
 * of the job's execution.
 * <p/>
 * The 'setter-methods' returns <code>this</code> in order to support for method chaining.
 *
 * @see ServerContext
 * @see ServerJobs
 * @see IJobManager
 * @since 5.1
 */
public class ServerJobInput extends JobInput {

  protected ServerJobInput() {
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
  public ServerJobInput mutex(final Object mutexObject) {
    return (ServerJobInput) super.mutex(mutexObject);
  }

  @Override
  public ServerJobInput expirationTime(final long time, final TimeUnit timeUnit) {
    return (ServerJobInput) super.expirationTime(time, timeUnit);
  }

  @Override
  public ServerContext getContext() {
    return (ServerContext) super.getContext();
  }

  @Override
  public ServerJobInput context(final Context context) {
    Assertions.assertTrue(context instanceof ServerContext, "Wrong context type [expected=%s, actual=%s]", ServerContext.class.getName(), context);
    return (ServerJobInput) super.context(context);
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
  public ServerJobInput logOnError(final boolean logOnError) {
    return (ServerJobInput) super.logOnError(logOnError);
  }

  public IServerSession getSession() {
    return getContext().getSession();
  }

  /**
   * Sets the session.<br/>
   * <strong>There are no other values derived from the given session, meaning that {@link Subject}, {@link Locale} and
   * {@link UserAgent} must be set accordingly.</strong>
   */
  public ServerJobInput session(final IServerSession session) {
    getContext().session(session);
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
  public ServerJobInput sessionRequired(final boolean sessionRequired) {
    getContext().sessionRequired(sessionRequired);
    return this;
  }

  public HttpServletRequest getServletRequest() {
    return getContext().getServletRequest();
  }

  /**
   * Sets the HTTP ServletRequest to be set for the time of execution.
   */
  public ServerJobInput servletRequest(final HttpServletRequest servletRequest) {
    getContext().servletRequest(servletRequest);
    return this;
  }

  public HttpServletResponse getServletResponse() {
    return getContext().getServletResponse();
  }

  /**
   * Sets the HTTP ServletResponse to be set for the time of execution.
   */
  public ServerJobInput servletResponse(final HttpServletResponse servletResponse) {
    getContext().servletResponse(servletResponse);
    return this;
  }

  public UserAgent getUserAgent() {
    return getContext().getUserAgent();
  }

  /**
   * Sets the UserAgent to be set for the time of execution.
   */
  public ServerJobInput userAgent(final UserAgent userAgent) {
    getContext().userAgent(userAgent);
    return this;
  }

  public long getTransactionId() {
    return getContext().getTransactionId();
  }

  /**
   * Sets the transaction-ID to be set for the time of execution.
   */
  public ServerJobInput transactionId(final long transactionId) {
    getContext().transactionId(transactionId);
    return this;
  }

  public boolean isTransactional() {
    return getContext().isTransactional();
  }

  /**
   * Sets whether this job should run in a separate transaction.
   */
  public ServerJobInput transactional(final boolean transactional) {
    getContext().transactional(transactional);
    return this;
  }

  @Override
  public String getThreadName() {
    return "scout-server-thread";
  }

  // === construction methods ===

  @Override
  public ServerJobInput copy() {
    final ServerJobInput copy = OBJ.get(ServerJobInput.class);
    copy.apply(this);
    return copy;
  }

  /**
   * Creates a {@link ServerJobInput} with a "snapshot" of the current calling context. This input requires a session to
   * be set.
   */
  public static ServerJobInput defaults() {
    final ServerJobInput defaults = OBJ.get(ServerJobInput.class);
    defaults.apply(JobInput.defaults());
    defaults.context(ServerContext.defaults());
    defaults.sessionRequired(true);
    return defaults;
  }

  /**
   * Creates an empty {@link ServerJobInput} with <code>null</code> as preferred {@link Subject}, {@link Locale} and
   * {@link UserAgent}. Preferred means, that those values are not derived from other values, but must be set explicitly
   * instead. This input requires a session to be set.
   */
  public static ServerJobInput empty() {
    final ServerJobInput empty = OBJ.get(ServerJobInput.class);
    empty.apply(JobInput.empty());
    empty.context(ServerContext.empty());
    empty.sessionRequired(true);
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
