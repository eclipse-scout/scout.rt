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
package org.eclipse.scout.rt.client.context;

import java.util.Locale;
import java.util.concurrent.Callable;

import javax.security.auth.Subject;

import org.eclipse.scout.commons.Assertions;
import org.eclipse.scout.commons.ToStringBuilder;
import org.eclipse.scout.commons.nls.NlsLocale;
import org.eclipse.scout.rt.client.IClientSession;
import org.eclipse.scout.rt.client.session.ClientSessionProvider;
import org.eclipse.scout.rt.platform.OBJ;
import org.eclipse.scout.rt.platform.context.Context;
import org.eclipse.scout.rt.platform.context.PreferredValue;
import org.eclipse.scout.rt.platform.job.PropertyMap;
import org.eclipse.scout.rt.platform.job.internal.callable.InitThreadLocalCallable;
import org.eclipse.scout.rt.shared.ISession;
import org.eclipse.scout.rt.shared.ScoutTexts;
import org.eclipse.scout.rt.shared.ui.UserAgent;

/**
 * A context typically represents a "snapshot" of the current calling state. This class facilitates propagation of that
 * client state among different threads, or allows temporary state changes to be done for the time of executing some
 * code.
 * <p/>
 * Usage:</br>
 *
 * <pre>
 * ClientContext.defaults().setLocale(Locale.US).setSubject(...).invoke(new Callable&lt;Void&gt;() {
 * 
 *   &#064;Override
 *   public void call() throws Exception {
 *      // run code on behalf of the new context
 *   }
 * });
 * </pre>
 * <p/>
 * The 'setter-methods' returns <code>this</code> in order to support for method chaining. The context has the following
 * characteristics:
 * <ul>
 * <li>{@link Subject}</li>
 * <li>{@link NlsLocale#CURRENT}</li>
 * <li>{@link PropertyMap#CURRENT}</li>
 * <li>{@link ISession#CURRENT}</li>
 * <li>{@link UserAgent#CURRENT}</li>
 * <li>{@link ScoutTexts#CURRENT}</li>
 * </ul>
 *
 * @since 5.1
 * @see Context
 */
public class ClientContext extends Context {

  protected IClientSession m_session;
  protected boolean m_sessionRequired;
  protected PreferredValue<UserAgent> m_userAgent = new PreferredValue<>(null, false);

  protected ClientContext() {
  }

  @Override
  protected <RESULT> Callable<RESULT> interceptCallable(final Callable<RESULT> next) {
    final Callable<RESULT> c4 = new InitThreadLocalCallable<>(next, ScoutTexts.CURRENT, (getSession() != null ? getSession().getTexts() : ScoutTexts.CURRENT.get()));
    final Callable<RESULT> c3 = new InitThreadLocalCallable<>(c4, UserAgent.CURRENT, getUserAgent());
    final Callable<RESULT> c2 = new InitThreadLocalCallable<>(c3, ISession.CURRENT, getSession());
    final Callable<RESULT> c1 = super.interceptCallable(c2);

    return c1;
  }

  @Override
  public void validate() {
    super.validate();
    if (isSessionRequired()) {
      Assertions.assertNotNull(m_session, "ClientSession must not be null");
    }
  }

  public IClientSession getSession() {
    return m_session;
  }

  /**
   * Set the session and its Locale and UserAgent as derived values.
   */
  public ClientContext session(final IClientSession session) {
    m_session = session;
    if (session != null) {
      m_locale.set(session.getLocale(), false);
      m_userAgent.set(session.getUserAgent(), false);
      m_subject.set(session.getSubject(), false);
    }
    return this;
  }

  public boolean isSessionRequired() {
    return m_sessionRequired;
  }

  /**
   * Set to <code>false</code> if the context does not require a session. By default, a session is required.
   */
  public ClientContext sessionRequired(final boolean sessionRequired) {
    m_sessionRequired = sessionRequired;
    return this;
  }

  public UserAgent getUserAgent() {
    return m_userAgent.get();
  }

  public ClientContext userAgent(final UserAgent userAgent) {
    m_userAgent.set(userAgent, true);
    return this;
  }

  @Override
  public ClientContext subject(final Subject subject) {
    return (ClientContext) super.subject(subject);
  }

  @Override
  public ClientContext locale(final Locale locale) {
    return (ClientContext) super.locale(locale);
  }

  // === construction methods ===

  @Override
  public ClientContext copy() {
    final ClientContext copy = OBJ.get(ClientContext.class);
    copy.apply(this);
    return copy;
  }

  /**
   * Applies the given context-values to <code>this</code> context.
   */
  protected void apply(final ClientContext origin) {
    super.apply(origin);
    m_userAgent = origin.m_userAgent;
    m_sessionRequired = origin.m_sessionRequired;
    m_session = origin.m_session;
  }

  /**
   * Creates a "snapshot" of the current calling client context.
   */
  public static ClientContext defaults() {
    final ClientContext defaults = OBJ.get(ClientContext.class);
    defaults.apply(Context.defaults());
    defaults.m_userAgent = new PreferredValue<>(UserAgent.CURRENT.get(), false);
    defaults.sessionRequired(false);
    defaults.session(ClientSessionProvider.currentSession());
    return defaults;
  }

  /**
   * Creates an empty client context with <code>null</code> as preferred Locale and UserAgent.
   */
  public static ClientContext empty() {
    final ClientContext empty = OBJ.get(ClientContext.class);
    empty.apply(Context.empty());
    empty.m_userAgent = new PreferredValue<>(null, true);
    empty.sessionRequired(false);
    empty.session(null);
    return empty;
  }

  @Override
  public String toString() {
    final ToStringBuilder builder = new ToStringBuilder(this);
    builder.attr("subject", getSubject());
    builder.attr("locale", getLocale());
    builder.ref("session", getSession());
    builder.attr("sessionRequired", isSessionRequired());
    builder.attr("userAgent", getUserAgent());
    return builder.toString();
  }
}
