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
import org.eclipse.scout.rt.client.context.ClientContext;
import org.eclipse.scout.rt.platform.OBJ;
import org.eclipse.scout.rt.platform.context.Context;
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
 * @see ClientContext
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
  public ClientContext getContext() {
    return (ClientContext) super.getContext();
  }

  @Override
  public ClientJobInput context(final Context context) {
    Assertions.assertTrue(context instanceof ClientContext, "Wrong context type [expected=%s, actual=%s]", ClientContext.class.getName(), context);
    return (ClientJobInput) super.context(context);
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
    return getContext().getSession();
  }

  /**
   * Set the session and its Locale and UserAgent as derived values.
   */
  public ClientJobInput session(final IClientSession session) {
    getContext().session(session);
    return this;
  }

  public boolean isSessionRequired() {
    return getContext().isSessionRequired();
  }

  /**
   * Set to <code>false</code> if the context does not require a session. By default, a session is required.
   */
  public ClientJobInput sessionRequired(final boolean sessionRequired) {
    getContext().sessionRequired(sessionRequired);
    return this;
  }

  public UserAgent getUserAgent() {
    return getContext().getUserAgent();
  }

  public ClientJobInput userAgent(final UserAgent userAgent) {
    getContext().userAgent(userAgent);
    return this;
  }

  @Override
  public String getThreadName() {
    return "scout-client-thread";
  }

  // === construction methods ===

  @Override
  public ClientJobInput copy() {
    final ClientJobInput copy = OBJ.get(ClientJobInput.class);
    copy.apply(this);
    return copy;
  }

  /**
   * Creates a {@link ClientJobInput} with a "snapshot" of the current calling context. This input requires a session to
   * be set.
   */
  public static ClientJobInput defaults() {
    final ClientJobInput defaults = OBJ.get(ClientJobInput.class);
    defaults.apply(JobInput.defaults());
    defaults.context(ClientContext.defaults());
    defaults.sessionRequired(true);
    return defaults;
  }

  /**
   * Creates an empty {@link ClientJobInput} with <code>null</code> as preferred {@link Subject}, {@link Locale} and
   * {@link UserAgent}. Preferred means, that those values will not be derived from other values, e.g. when setting the
   * session, but must be set explicitly instead.
   */
  public static ClientJobInput empty() {
    final ClientJobInput empty = OBJ.get(ClientJobInput.class);
    empty.apply(JobInput.empty());
    empty.context(ClientContext.empty());
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
