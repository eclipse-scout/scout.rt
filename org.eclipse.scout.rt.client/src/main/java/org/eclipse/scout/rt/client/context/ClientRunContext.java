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

import org.eclipse.scout.commons.PreferredValue;
import org.eclipse.scout.commons.ToStringBuilder;
import org.eclipse.scout.commons.nls.NlsLocale;
import org.eclipse.scout.rt.client.IClientSession;
import org.eclipse.scout.rt.client.session.ClientSessionProvider;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.context.IRunMonitor;
import org.eclipse.scout.rt.platform.context.RunContext;
import org.eclipse.scout.rt.platform.context.internal.InitThreadLocalCallable;
import org.eclipse.scout.rt.platform.job.PropertyMap;
import org.eclipse.scout.rt.shared.ISession;
import org.eclipse.scout.rt.shared.ScoutTexts;
import org.eclipse.scout.rt.shared.ui.UserAgent;

/**
 * Use this class to propagate client-side context.
 * <p/>
 * A context typically represents a "snapshot" of the current calling state. This class facilitates propagation of that
 * client state among different threads, or allows temporary state changes to be done for the time of executing some
 * code.
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
 * @see ClientRunContexts
 * @see RunContext
 */
public class ClientRunContext extends RunContext {

  protected IClientSession m_session;
  protected PreferredValue<UserAgent> m_userAgent = new PreferredValue<>(null, false);

  @Override
  public ClientRunContext runMonitor(IRunMonitor parentRunMonitor, IRunMonitor runMonitor) {
    super.runMonitor(parentRunMonitor, runMonitor);
    return this;
  }

  @Override
  protected <RESULT> Callable<RESULT> interceptCallable(final Callable<RESULT> next) {
    final Callable<RESULT> c4 = new InitThreadLocalCallable<>(next, ScoutTexts.CURRENT, (session() != null ? session().getTexts() : ScoutTexts.CURRENT.get()));
    final Callable<RESULT> c3 = new InitThreadLocalCallable<>(c4, UserAgent.CURRENT, userAgent());
    final Callable<RESULT> c2 = new InitThreadLocalCallable<>(c3, ISession.CURRENT, session());
    final Callable<RESULT> c1 = super.interceptCallable(c2);

    return c1;
  }

  public IClientSession session() {
    return m_session;
  }

  /**
   * Set the session with its {@link Locale}, {@link UserAgent} and {@link Subject} if not set yet.
   */
  public ClientRunContext session(final IClientSession session) {
    m_session = session;
    if (session != null) {
      m_locale.set(session.getLocale(), false);
      m_userAgent.set(session.getUserAgent(), false);
      m_subject.set(session.getSubject(), false);
    }
    return this;
  }

  public UserAgent userAgent() {
    return m_userAgent.get();
  }

  public ClientRunContext userAgent(final UserAgent userAgent) {
    m_userAgent.set(userAgent, true);
    return this;
  }

  @Override
  public ClientRunContext subject(final Subject subject) {
    super.subject(subject);
    return this;
  }

  @Override
  public ClientRunContext locale(final Locale locale) {
    super.locale(locale);
    return this;
  }

  @Override
  public String toString() {
    final ToStringBuilder builder = new ToStringBuilder(this);
    builder.attr("subject", subject());
    builder.attr("locale", locale());
    builder.ref("session", session());
    builder.attr("userAgent", userAgent());
    return builder.toString();
  }

  // === fill methods ===

  @Override
  protected void copyValues(final RunContext origin) {
    final ClientRunContext originRunContext = (ClientRunContext) origin;

    super.copyValues(originRunContext);
    m_userAgent = originRunContext.m_userAgent.copy();
    m_session = originRunContext.m_session;
  }

  @Override
  protected void fillCurrentValues() {
    super.fillCurrentValues();
    m_userAgent = new PreferredValue<>(UserAgent.CURRENT.get(), false);
    session(ClientSessionProvider.currentSession()); // method call to derive other values.
  }

  @Override
  protected void fillEmptyValues() {
    super.fillEmptyValues();
    m_userAgent = new PreferredValue<>(null, true); // null as preferred UserAgent
    session(null); // method call to derive other values.
  }

  @Override
  public ClientRunContext copy() {
    final ClientRunContext copy = BEANS.get(ClientRunContext.class);
    copy.copyValues(this);
    return copy;
  }

}
