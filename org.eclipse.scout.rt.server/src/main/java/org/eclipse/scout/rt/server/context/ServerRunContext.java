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

import org.eclipse.scout.commons.Assertions;
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
 * Use this class to propagate server-side context.
 * <p/>
 * A context typically represents a "snapshot" of the current calling state. This class facilitates propagation of that
 * server state among different threads, or allows temporary state changes to be done for the time of executing some
 * code.
 * <p/>
 * Usage:</br>
 *
 * <pre>
 * ServerRunContext.fillCurrent().locale(Locale.US).subject(...).run(new IRunnable() {
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
  protected boolean m_sessionRequired;
  protected HttpServletRequest m_servletRequest;
  protected HttpServletResponse m_servletResponse;
  protected PreferredValue<UserAgent> m_userAgent = new PreferredValue<>(null, false);
  protected long m_transactionId;
  private boolean m_transactional;

  protected ServerRunContext() {
  }

  @Override
  public void validate() {
    super.validate();
    if (isSessionRequired()) {
      Assertions.assertNotNull(m_session, "ServerSession must not be null");
    }
  }

  @Override
  protected <RESULT> ICallable<RESULT> interceptCallable(final ICallable<RESULT> next) {
    final ITransaction tx = OBJ.get(ITransactionProvider.class).provide(m_transactionId);

    final ICallable<RESULT> c9 = new TwoPhaseTransactionBoundaryCallable<>(next, tx);
    final ICallable<RESULT> c8 = new InitThreadLocalCallable<>(c9, ITransaction.CURRENT, tx);
    final ICallable<RESULT> c7 = new InitThreadLocalCallable<>(c8, ScoutTexts.CURRENT, (getSession() != null ? getSession().getTexts() : ScoutTexts.CURRENT.get()));
    final ICallable<RESULT> c6 = new InitThreadLocalCallable<>(c7, UserAgent.CURRENT, getUserAgent());
    final ICallable<RESULT> c5 = new InitThreadLocalCallable<>(c6, ISession.CURRENT, getSession());
    final ICallable<RESULT> c4 = new InitThreadLocalCallable<>(c5, IHttpServletRoundtrip.CURRENT_HTTP_SERVLET_RESPONSE, getServletResponse());
    final ICallable<RESULT> c3 = new InitThreadLocalCallable<>(c4, IHttpServletRoundtrip.CURRENT_HTTP_SERVLET_REQUEST, getServletRequest());
    final ICallable<RESULT> c2 = new InitThreadLocalCallable<>(c3, OfflineState.CURRENT, OfflineState.CURRENT.get());
    final ICallable<RESULT> c1 = super.interceptCallable(c2);

    return c1;
  }

  public IServerSession getSession() {
    return m_session;
  }

  /**
   * Sets the session.<br/>
   * <strong>There are no other values derived from the given session, meaning that {@link Subject}, {@link Locale} and
   * {@link UserAgent} must be set accordingly.</strong>
   */
  public ServerRunContext session(final IServerSession session) {
    m_session = session;
    return this;
  }

  public boolean isSessionRequired() {
    return m_sessionRequired;
  }

  /**
   * Set to <code>false</code> if the context does not require a session. By default, a session is required.
   */
  public ServerRunContext sessionRequired(final boolean sessionRequired) {
    m_sessionRequired = sessionRequired;
    return this;
  }

  public HttpServletRequest getServletRequest() {
    return m_servletRequest;
  }

  public ServerRunContext servletRequest(final HttpServletRequest servletRequest) {
    m_servletRequest = servletRequest;
    return this;
  }

  public HttpServletResponse getServletResponse() {
    return m_servletResponse;
  }

  public ServerRunContext servletResponse(final HttpServletResponse servletResponse) {
    m_servletResponse = servletResponse;
    return this;
  }

  public UserAgent getUserAgent() {
    return m_userAgent.get();
  }

  public ServerRunContext userAgent(final UserAgent userAgent) {
    m_userAgent.set(userAgent, true);
    return this;
  }

  public long getTransactionId() {
    return m_transactionId;
  }

  public ServerRunContext transactionId(final long transactionId) {
    m_transactionId = transactionId;
    return this;
  }

  public boolean isTransactional() {
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
    builder.attr("subject", getSubject());
    builder.attr("locale", getLocale());
    builder.ref("session", getSession());
    builder.attr("sessionRequired", isSessionRequired());
    builder.attr("userAgent", getUserAgent());
    builder.ref("servletRequest", getServletRequest());
    builder.ref("servletResponse", getServletResponse());
    builder.ref("transactionId", getTransactionId());
    builder.ref("transactional", isTransactional());
    return builder.toString();
  }

  // === fill methods ===

  @Override
  protected void copyValues(final RunContext origin) {
    final ServerRunContext originSRC = (ServerRunContext) origin;

    super.copyValues(originSRC);
    m_session = originSRC.m_session;
    m_sessionRequired = originSRC.m_sessionRequired;
    m_userAgent = originSRC.m_userAgent;
    m_servletRequest = originSRC.m_servletRequest;
    m_servletResponse = originSRC.m_servletResponse;
    m_transactionId = originSRC.m_transactionId;
    m_transactional = originSRC.m_transactional;
  }

  @Override
  protected void fillCurrentValues() {
    super.fillCurrentValues();
    m_userAgent = new PreferredValue<>(UserAgent.CURRENT.get(), false);
    m_servletRequest = IHttpServletRoundtrip.CURRENT_HTTP_SERVLET_REQUEST.get();
    m_servletResponse = IHttpServletRoundtrip.CURRENT_HTTP_SERVLET_RESPONSE.get();
    m_transactionId = ITransaction.TX_ZERO_ID;
    m_transactional = true;
    m_sessionRequired = false;
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
    m_sessionRequired = false;
    session(null); // method call to derive other values.
  }

  // === construction methods ===

  @Override
  public ServerRunContext copy() {
    final ServerRunContext copy = OBJ.get(ServerRunContext.class);
    copy.copyValues(this);
    return copy;
  }

  /**
   * Creates a "snapshot" of the current calling server context.
   */
  public static ServerRunContext fillCurrent() {
    final ServerRunContext runContext = OBJ.get(ServerRunContext.class);
    runContext.fillCurrentValues();
    return runContext;
  }

  /**
   * Creates an empty {@link ClientContext} with <code>null</code> as preferred {@link Subject}, {@link Locale} and
   * {@link UserAgent}. Preferred means, that those values will not be derived from other values, but must be set
   * explicitly instead.
   */
  public static ServerRunContext fillEmpty() {
    final ServerRunContext runContext = OBJ.get(ServerRunContext.class);
    runContext.fillEmptyValues();
    return runContext;
  }
}
