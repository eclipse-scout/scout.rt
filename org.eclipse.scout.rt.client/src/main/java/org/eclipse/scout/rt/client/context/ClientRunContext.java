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

import javax.security.auth.Subject;

import org.eclipse.scout.commons.Assertions;
import org.eclipse.scout.commons.ICallable;
import org.eclipse.scout.commons.ToStringBuilder;
import org.eclipse.scout.commons.nls.NlsLocale;
import org.eclipse.scout.rt.client.IClientSession;
import org.eclipse.scout.rt.client.session.ClientSessionProvider;
import org.eclipse.scout.rt.platform.OBJ;
import org.eclipse.scout.rt.platform.context.InitThreadLocalCallable;
import org.eclipse.scout.rt.platform.context.PreferredValue;
import org.eclipse.scout.rt.platform.context.RunContext;
import org.eclipse.scout.rt.platform.job.PropertyMap;
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
 * ClientRunContext.fillCurrent().locale(Locale.US).subject(...).run(new IRunnable() {
 * 
 *   &#064;Override
 *   public void run() throws Exception {
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
 * @see RunContext
 */
public class ClientRunContext extends RunContext {

  protected IClientSession m_session;
  protected boolean m_sessionRequired;
  protected PreferredValue<UserAgent> m_userAgent = new PreferredValue<>(null, false);

  protected ClientRunContext() {
  }

  @Override
  protected <RESULT> ICallable<RESULT> interceptCallable(final ICallable<RESULT> next) {
    final ICallable<RESULT> c4 = new InitThreadLocalCallable<>(next, ScoutTexts.CURRENT, (getSession() != null ? getSession().getTexts() : ScoutTexts.CURRENT.get()));
    final ICallable<RESULT> c3 = new InitThreadLocalCallable<>(c4, UserAgent.CURRENT, getUserAgent());
    final ICallable<RESULT> c2 = new InitThreadLocalCallable<>(c3, ISession.CURRENT, getSession());
    final ICallable<RESULT> c1 = super.interceptCallable(c2);

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
   * Set the session and its {@link Locale}, {@link UserAgent} and {@link Subject} as derived values. Those derived
   * values are only set if not explicitly set yet.
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

  public boolean isSessionRequired() {
    return m_sessionRequired;
  }

  /**
   * Set to <code>false</code> if the context does not require a session. By default, a session is required.
   */
  public ClientRunContext sessionRequired(final boolean sessionRequired) {
    m_sessionRequired = sessionRequired;
    return this;
  }

  public UserAgent getUserAgent() {
    return m_userAgent.get();
  }

  public ClientRunContext userAgent(final UserAgent userAgent) {
    m_userAgent.set(userAgent, true);
    return this;
  }

  @Override
  public ClientRunContext subject(final Subject subject) {
    return (ClientRunContext) super.subject(subject);
  }

  @Override
  public ClientRunContext locale(final Locale locale) {
    return (ClientRunContext) super.locale(locale);
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

  // === fill methods ===

  @Override
  protected void copyValues(final RunContext origin) {
    final ClientRunContext originCRC = (ClientRunContext) origin;

    super.copyValues(originCRC);
    m_userAgent = originCRC.m_userAgent;
    m_sessionRequired = originCRC.m_sessionRequired;
    m_session = originCRC.m_session;
  }

  @Override
  protected void fillCurrentValues() {
    super.fillCurrentValues();
    m_userAgent = new PreferredValue<>(UserAgent.CURRENT.get(), false);
    m_sessionRequired = false;
    session(ClientSessionProvider.currentSession()); // method call to derive other values.
  }

  @Override
  protected void fillEmptyValues() {
    super.fillEmptyValues();
    m_userAgent = new PreferredValue<>(null, true); // null as preferred UserAgent
    m_sessionRequired = false;
    session(null); // method call to derive other values.
  }

  // === construction methods ===

  @Override
  public ClientRunContext copy() {
    final ClientRunContext copy = OBJ.get(ClientRunContext.class);
    copy.copyValues(this);
    return copy;
  }

  /**
   * Creates a "snapshot" of the current calling client context.
   */
  public static ClientRunContext fillCurrent() {
    final ClientRunContext runContext = OBJ.get(ClientRunContext.class);
    runContext.fillCurrentValues();
    return runContext;
  }

  /**
   * Creates an empty {@link ClientRunContext} with <code>null</code> as preferred {@link Subject}, {@link Locale} and
   * {@link UserAgent}. Preferred means, that those values will not be derived from other values, e.g. when setting the
   * session, but must be set explicitly instead.
   */
  public static ClientRunContext fillEmpty() {
    final ClientRunContext runContext = OBJ.get(ClientRunContext.class);
    runContext.fillEmptyValues();
    return runContext;
  }
}
