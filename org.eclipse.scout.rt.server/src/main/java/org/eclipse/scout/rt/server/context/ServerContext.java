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
import java.util.concurrent.Callable;

import javax.security.auth.Subject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.scout.commons.Assertions;
import org.eclipse.scout.commons.ToStringBuilder;
import org.eclipse.scout.commons.nls.NlsLocale;
import org.eclipse.scout.rt.platform.OBJ;
import org.eclipse.scout.rt.platform.context.Context;
import org.eclipse.scout.rt.platform.context.PreferredValue;
import org.eclipse.scout.rt.platform.job.PropertyMap;
import org.eclipse.scout.rt.platform.job.internal.callable.InitThreadLocalCallable;
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
 * ServerContext.defaults().setLocale(Locale.US).setSubject(...).invoke(new Callable&lt;Void&gt;() {
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
 * <li>{@link ITransaction#CURRENT}</li>
 * <li>{@link IHttpServletRoundtrip#CURRENT}</li>
 * <li>{@link IHttpServletRoundtrip#CURRENT}</li>
 * <li>{@link OfflineState#CURRENT}</li>
 * </ul>
 *
 * @since 5.1
 * @see Context
 */
public class ServerContext extends Context {

  protected IServerSession m_session;
  protected boolean m_sessionRequired;
  protected HttpServletRequest m_servletRequest;
  protected HttpServletResponse m_servletResponse;
  protected PreferredValue<UserAgent> m_userAgent = new PreferredValue<>(null, false);
  protected long m_transactionId;
  private boolean m_transactional;

  protected ServerContext() {
  }

  @Override
  protected void beforeInvoke() {
    super.beforeInvoke();
    if (isSessionRequired()) {
      Assertions.assertNotNull(m_session, "ServerSession must not be null");
    }
  }

  @Override
  protected <RESULT> Callable<RESULT> interceptCallable(final Callable<RESULT> next) {
    final ITransaction tx = OBJ.get(ITransactionProvider.class).provide(m_transactionId);

    final Callable<RESULT> c9 = new TwoPhaseTransactionBoundaryCallable<>(next, tx);
    final Callable<RESULT> c8 = new InitThreadLocalCallable<>(c9, ITransaction.CURRENT, tx);
    final Callable<RESULT> c7 = new InitThreadLocalCallable<>(c8, ScoutTexts.CURRENT, (getSession() != null ? getSession().getTexts() : ScoutTexts.CURRENT.get()));
    final Callable<RESULT> c6 = new InitThreadLocalCallable<>(c7, UserAgent.CURRENT, getUserAgent());
    final Callable<RESULT> c5 = new InitThreadLocalCallable<>(c6, ISession.CURRENT, getSession());
    final Callable<RESULT> c4 = new InitThreadLocalCallable<>(c5, IHttpServletRoundtrip.CURRENT_HTTP_SERVLET_RESPONSE, getServletResponse());
    final Callable<RESULT> c3 = new InitThreadLocalCallable<>(c4, IHttpServletRoundtrip.CURRENT_HTTP_SERVLET_REQUEST, getServletRequest());
    final Callable<RESULT> c2 = new InitThreadLocalCallable<>(c3, OfflineState.CURRENT, OfflineState.CURRENT.get());
    final Callable<RESULT> c1 = super.interceptCallable(c2);

    return c1;
  }

  public IServerSession getSession() {
    return m_session;
  }

  /**
   * Sets the session. There are no other values derived from the given session.
   */
  public ServerContext setSession(final IServerSession session) {
    m_session = session;
    return this;
  }

  public boolean isSessionRequired() {
    return m_sessionRequired;
  }

  /**
   * Set to <code>false</code> if the context does not require a session. By default, a session is required.
   */
  public ServerContext setSessionRequired(final boolean sessionRequired) {
    m_sessionRequired = sessionRequired;
    return this;
  }

  public HttpServletRequest getServletRequest() {
    return m_servletRequest;
  }

  public ServerContext setServletRequest(final HttpServletRequest servletRequest) {
    m_servletRequest = servletRequest;
    return this;
  }

  public HttpServletResponse getServletResponse() {
    return m_servletResponse;
  }

  public ServerContext setServletResponse(final HttpServletResponse servletResponse) {
    m_servletResponse = servletResponse;
    return this;
  }

  public UserAgent getUserAgent() {
    return m_userAgent.get();
  }

  public ServerContext setUserAgent(final UserAgent userAgent) {
    m_userAgent.set(userAgent, true);
    return this;
  }

  public long getTransactionId() {
    return m_transactionId;
  }

  public ServerContext setTransactionId(final long transactionId) {
    m_transactionId = transactionId;
    return this;
  }

  public boolean isTransactional() {
    return m_transactional;
  }

  public ServerContext setTransactional(final boolean transactional) {
    m_transactional = transactional;
    return this;
  }

  @Override
  public ServerContext setSubject(final Subject subject) {
    return (ServerContext) super.setSubject(subject);
  }

  @Override
  public ServerContext setLocale(final Locale locale) {
    return (ServerContext) super.setLocale(locale);
  }

  // === construction methods ===

  @Override
  public ServerContext copy() {
    final ServerContext copy = OBJ.get(ServerContext.class);
    copy.apply(this);
    return copy;
  }

  /**
   * Applies the given context-values to <code>this</code> context.
   */
  protected void apply(final ServerContext origin) {
    super.apply(origin);
    m_session = origin.m_session;
    m_sessionRequired = origin.m_sessionRequired;
    m_userAgent = origin.m_userAgent;
    m_servletRequest = origin.m_servletRequest;
    m_servletResponse = origin.m_servletResponse;
    m_transactionId = origin.m_transactionId;
    m_transactional = origin.m_transactional;
  }

  /**
   * Creates a "snapshot" of the current calling server context.
   */
  public static ServerContext defaults() {
    final ServerContext defaults = OBJ.get(ServerContext.class);
    defaults.apply(Context.defaults());
    defaults.m_userAgent = new PreferredValue<>(UserAgent.CURRENT.get(), false);
    defaults.setServletRequest(IHttpServletRoundtrip.CURRENT_HTTP_SERVLET_REQUEST.get());
    defaults.setServletResponse(IHttpServletRoundtrip.CURRENT_HTTP_SERVLET_RESPONSE.get());
    defaults.setTransactionId(ITransaction.TX_ZERO_ID);
    defaults.setTransactional(true);
    defaults.setSessionRequired(false);
    defaults.setSession(ServerSessionProvider.currentSession());
    return defaults;
  }

  /**
   * Creates an empty server context with <code>null</code> as preferred Locale and UserAgent.
   */
  public static ServerContext empty() {
    final ServerContext empty = OBJ.get(ServerContext.class);
    empty.apply(Context.empty());
    empty.m_userAgent = new PreferredValue<>(null, true);
    empty.setServletRequest(null);
    empty.setServletResponse(null);
    empty.setTransactionId(ITransaction.TX_ZERO_ID);
    empty.setTransactional(true);
    empty.setSessionRequired(false);
    empty.setSession(null);
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
    builder.ref("servletRequest", getServletRequest());
    builder.ref("servletResponse", getServletResponse());
    builder.ref("transactionId", getTransactionId());
    builder.ref("transactional", isTransactional());
    return builder.toString();
  }
}
