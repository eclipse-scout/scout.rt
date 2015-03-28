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
package org.eclipse.scout.rt.server.context;

import java.util.Locale;

import javax.security.auth.Subject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.scout.commons.ICallable;
import org.eclipse.scout.commons.ToStringBuilder;
import org.eclipse.scout.commons.nls.NlsLocale;
import org.eclipse.scout.rt.platform.OBJ;
import org.eclipse.scout.rt.platform.context.InitThreadLocalCallable;
import org.eclipse.scout.rt.platform.context.PreferredValue;
import org.eclipse.scout.rt.platform.context.RunContext;
import org.eclipse.scout.rt.platform.job.PropertyMap;
import org.eclipse.scout.rt.server.IServerSession;
import org.eclipse.scout.rt.server.commons.servletfilter.IHttpServletRoundtrip;
import org.eclipse.scout.rt.server.session.ServerSessionProvider;
import org.eclipse.scout.rt.server.transaction.ITransaction;
import org.eclipse.scout.rt.server.transaction.ITransactionProvider;
import org.eclipse.scout.rt.shared.ISession;
import org.eclipse.scout.rt.shared.OfflineState;
import org.eclipse.scout.rt.shared.ScoutTexts;
import org.eclipse.scout.rt.shared.ui.UserAgent;

/**
 * Use this class to propagate server-side context and to run code in a new transaction.
 * <p/>
 * A context typically represents a "snapshot" of the current calling state. This class facilitates propagation of that
 * server state among different threads, or allows temporary state changes to be done for the time of executing some
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
 * <li>{@link ITransaction#CURRENT}</li>
 * <li>{@link IHttpServletRoundtrip#CURRENT}</li>
 * <li>{@link IHttpServletRoundtrip#CURRENT}</li>
 * <li>{@link OfflineState#CURRENT}</li>
 * </ul>
 *
 * @since 5.1
 * @see RunContext
 */
public class ServerRunContext extends RunContext {

  protected IServerSession m_session;
  protected HttpServletRequest m_servletRequest;
  protected HttpServletResponse m_servletResponse;
  protected PreferredValue<UserAgent> m_userAgent = new PreferredValue<>(null, false);
  protected long m_transactionId;
  private boolean m_transactional;

  @Override
  protected <RESULT> ICallable<RESULT> interceptCallable(final ICallable<RESULT> next) {
    final ITransaction tx = OBJ.get(ITransactionProvider.class).provide(m_transactionId);

    final ICallable<RESULT> c9 = new TwoPhaseTransactionBoundaryCallable<>(next, tx);
    final ICallable<RESULT> c8 = new InitThreadLocalCallable<>(c9, ITransaction.CURRENT, tx);
    final ICallable<RESULT> c7 = new InitThreadLocalCallable<>(c8, ScoutTexts.CURRENT, (session() != null ? session().getTexts() : ScoutTexts.CURRENT.get()));
    final ICallable<RESULT> c6 = new InitThreadLocalCallable<>(c7, UserAgent.CURRENT, userAgent());
    final ICallable<RESULT> c5 = new InitThreadLocalCallable<>(c6, ISession.CURRENT, session());
    final ICallable<RESULT> c4 = new InitThreadLocalCallable<>(c5, IHttpServletRoundtrip.CURRENT_HTTP_SERVLET_RESPONSE, servletResponse());
    final ICallable<RESULT> c3 = new InitThreadLocalCallable<>(c4, IHttpServletRoundtrip.CURRENT_HTTP_SERVLET_REQUEST, servletRequest());
    final ICallable<RESULT> c2 = new InitThreadLocalCallable<>(c3, OfflineState.CURRENT, OfflineState.CURRENT.get());
    final ICallable<RESULT> c1 = super.interceptCallable(c2);

    return c1;
  }

  public IServerSession session() {
    return m_session;
  }

  /**
   * Sets the session with its {@link Subject} if not set yet. The session's {@link Locale} and {@link UserAgent} are
   * not set.
   */
  public ServerRunContext session(final IServerSession session) {
    m_session = session;
    if (session != null) {
      m_subject.set(session.getSubject(), false);
    }
    return this;
  }

  public HttpServletRequest servletRequest() {
    return m_servletRequest;
  }

  public ServerRunContext servletRequest(final HttpServletRequest servletRequest) {
    m_servletRequest = servletRequest;
    return this;
  }

  public HttpServletResponse servletResponse() {
    return m_servletResponse;
  }

  public ServerRunContext servletResponse(final HttpServletResponse servletResponse) {
    m_servletResponse = servletResponse;
    return this;
  }

  public UserAgent userAgent() {
    return m_userAgent.get();
  }

  public ServerRunContext userAgent(final UserAgent userAgent) {
    m_userAgent.set(userAgent, true);
    return this;
  }

  public long transactionId() {
    return m_transactionId;
  }

  public ServerRunContext transactionId(final long transactionId) {
    m_transactionId = transactionId;
    return this;
  }

  public boolean transactional() {
    return m_transactional;
  }

  public ServerRunContext transactional(final boolean transactional) {
    m_transactional = transactional;
    return this;
  }

  @Override
  public ServerRunContext subject(final Subject subject) {
    return (ServerRunContext) super.subject(subject);
  }

  @Override
  public ServerRunContext locale(final Locale locale) {
    return (ServerRunContext) super.locale(locale);
  }

  @Override
  public String toString() {
    final ToStringBuilder builder = new ToStringBuilder(this);
    builder.attr("subject", subject());
    builder.attr("locale", locale());
    builder.ref("session", session());
    builder.attr("userAgent", userAgent());
    builder.ref("servletRequest", servletRequest());
    builder.ref("servletResponse", servletResponse());
    builder.ref("transactionId", transactionId());
    builder.ref("transactional", transactional());
    return builder.toString();
  }

  // === fill methods ===

  @Override
  protected void copyValues(final RunContext origin) {
    final ServerRunContext originRunContext = (ServerRunContext) origin;

    super.copyValues(originRunContext);
    m_session = originRunContext.m_session;
    m_userAgent = originRunContext.m_userAgent.copy();
    m_servletRequest = originRunContext.m_servletRequest;
    m_servletResponse = originRunContext.m_servletResponse;
    m_transactionId = originRunContext.m_transactionId;
    m_transactional = originRunContext.m_transactional;
  }

  @Override
  protected void fillCurrentValues() {
    super.fillCurrentValues();
    m_userAgent = new PreferredValue<>(UserAgent.CURRENT.get(), false);
    m_servletRequest = IHttpServletRoundtrip.CURRENT_HTTP_SERVLET_REQUEST.get();
    m_servletResponse = IHttpServletRoundtrip.CURRENT_HTTP_SERVLET_RESPONSE.get();
    m_transactionId = ITransaction.TX_ZERO_ID;
    m_transactional = true;
    session(ServerSessionProvider.currentSession()); // method call to derive other values.
  }

  @Override
  protected void fillEmptyValues() {
    super.fillEmptyValues();
    m_userAgent = new PreferredValue<>(null, true); // null as preferred UserAgent
    m_servletRequest = null;
    m_servletResponse = null;
    m_transactionId = ITransaction.TX_ZERO_ID;
    m_transactional = true;
    session(null); // method call to derive other values.
  }

  @Override
  public ServerRunContext copy() {
    final ServerRunContext copy = OBJ.get(ServerRunContext.class);
    copy.copyValues(this);
    return copy;
  }
}
