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

import java.util.Locale;
import java.util.concurrent.TimeUnit;

import javax.security.auth.Subject;

import org.eclipse.scout.commons.Assertions;
import org.eclipse.scout.commons.ToStringBuilder;
import org.eclipse.scout.rt.client.IClientSession;
import org.eclipse.scout.rt.client.context.ClientRunContext;
import org.eclipse.scout.rt.platform.OBJ;
import org.eclipse.scout.rt.platform.context.RunContext;
import org.eclipse.scout.rt.platform.job.IJobManager;
import org.eclipse.scout.rt.platform.job.JobInput;
import org.eclipse.scout.rt.shared.ISession;
import org.eclipse.scout.rt.shared.ui.UserAgent;

/**
 * Describes a client-side job with context information to be applied onto the executing worker thread during the time
 * of the job's execution.
 * <p/>
 * The 'setter-methods' returns <code>this</code> in order to support for method chaining.
 *
 * @see ClientRunContext
 * @see ClientJobs
 * @see IJobManager
 * @since 5.1
 */
public class ClientJobInput extends JobInput {

  protected ClientJobInput() {
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
  public ClientJobInput mutex(final Object mutexObject) {
    Assertions.assertFalse(mutexObject instanceof ISession, "The session cannot be used as mutex object to not interfere with model jobs");
    return (ClientJobInput) super.mutex(mutexObject);
  }

  @Override
  public ClientJobInput expirationTime(final long time, final TimeUnit timeUnit) {
    return (ClientJobInput) super.expirationTime(time, timeUnit);
  }

  @Override
  public ClientRunContext getRunContext() {
    return (ClientRunContext) super.getRunContext();
  }

  @Override
  public ClientJobInput runContext(final RunContext clientRunContext) {
    Assertions.assertTrue(clientRunContext instanceof ClientRunContext, "Wrong 'RunContext' type [expected=%s, actual=%s]", ClientRunContext.class.getName(), clientRunContext);
    return (ClientJobInput) super.runContext(clientRunContext);
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
  public ClientJobInput logOnError(final boolean logOnError) {
    return (ClientJobInput) super.logOnError(logOnError);
  }

  public IClientSession getSession() {
    return getRunContext().getSession();
  }

  /**
   * Set the session and its Locale and UserAgent as derived values.
   */
  public ClientJobInput session(final IClientSession session) {
    getRunContext().session(session);
    return this;
  }

  public boolean isSessionRequired() {
    return getRunContext().isSessionRequired();
  }

  /**
   * Set to <code>false</code> if the context does not require a session. By default, a session is required.
   */
  public ClientJobInput sessionRequired(final boolean sessionRequired) {
    getRunContext().sessionRequired(sessionRequired);
    return this;
  }

  public UserAgent getUserAgent() {
    return getRunContext().getUserAgent();
  }

  public ClientJobInput userAgent(final UserAgent userAgent) {
    getRunContext().userAgent(userAgent);
    return this;
  }

  @Override
  public String getThreadName() {
    return "scout-client-thread";
  }

  @Override
  public String toString() {
    final ToStringBuilder builder = new ToStringBuilder(this);
    builder.attr("id", getId());
    builder.attr("name", getName());
    builder.ref("session", getSession());
    return builder.toString();
  }

  // === fill methods ===

  @Override
  protected void fillCurrentValues() {
    super.fillCurrentValues();
    m_runContext = ClientRunContext.fillCurrent();
    sessionRequired(true); // client jobs require a session by default
  }

  @Override
  protected void fillEmptyValues() {
    super.fillEmptyValues();
    m_runContext = ClientRunContext.fillEmpty();
    sessionRequired(true); // client jobs require a session by default
  }

  // === construction methods ===

  @Override
  public ClientJobInput copy() {
    final ClientJobInput copy = OBJ.get(ClientJobInput.class);
    copy.copyValues(this);
    return copy;
  }

  /**
   * Creates a {@link ClientJobInput} with a "snapshot" of the current calling {@link RunContext}. This input requires a
   * session to be set.
   */
  public static ClientJobInput fillCurrent() {
    final ClientJobInput jobInput = OBJ.get(ClientJobInput.class);
    jobInput.fillCurrentValues();
    return jobInput;
  }

  /**
   * Creates an empty {@link ClientJobInput} with <code>null</code> as preferred {@link Subject}, {@link Locale} and
   * {@link UserAgent}. Preferred means, that those values will not be derived from other values, e.g. when setting the
   * session, but must be set explicitly instead.
   */
  public static ClientJobInput fillEmpty() {
    final ClientJobInput jobInput = OBJ.get(ClientJobInput.class);
    jobInput.fillEmptyValues();
    return jobInput;
  }
}
