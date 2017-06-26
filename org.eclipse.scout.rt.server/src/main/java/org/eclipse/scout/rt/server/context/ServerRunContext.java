/*******************************************************************************
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
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
import java.util.Map;

import javax.security.auth.Subject;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.chain.callable.CallableChain;
import org.eclipse.scout.rt.platform.context.RunContext;
import org.eclipse.scout.rt.platform.context.RunMonitor;
import org.eclipse.scout.rt.platform.logger.DiagnosticContextValueProcessor;
import org.eclipse.scout.rt.platform.transaction.ITransaction;
import org.eclipse.scout.rt.platform.transaction.ITransactionMember;
import org.eclipse.scout.rt.platform.transaction.TransactionScope;
import org.eclipse.scout.rt.platform.util.ThreadLocalProcessor;
import org.eclipse.scout.rt.platform.util.ToStringBuilder;
import org.eclipse.scout.rt.server.IServerSession;
import org.eclipse.scout.rt.server.ServiceTunnelServlet;
import org.eclipse.scout.rt.server.clientnotification.ClientNotificationCollector;
import org.eclipse.scout.rt.server.clientnotification.IClientNodeId;
import org.eclipse.scout.rt.server.session.ServerSessionProvider;
import org.eclipse.scout.rt.shared.ISession;
import org.eclipse.scout.rt.shared.logging.UserIdContextValueProvider;
import org.eclipse.scout.rt.shared.session.ScoutSessionIdContextValueProvider;
import org.eclipse.scout.rt.shared.ui.UserAgent;

/**
 * The <code>ServerRunContext</code> controls propagation of server-side state.
 * <p>
 * A context typically represents a "snapshot" of the current calling state. This class facilitates propagation of that
 * server state among different threads, or allows temporary state changes to be done for the time of executing some
 * code.
 * <p>
 * By default, {@link ServerRunContext} is configured with {@link TransactionScope#REQUIRES_NEW}, so that code is always
 * executed in a new transaction.
 *
 * @since 5.1
 * @see RunContext
 */
public class ServerRunContext extends RunContext {

  protected IServerSession m_session;
  protected UserAgent m_userAgent;
  protected String m_clientNodeId;
  protected ClientNotificationCollector m_clientNotificationCollector = new ClientNotificationCollector();

  @Override
  protected <RESULT> void interceptCallableChain(final CallableChain<RESULT> callableChain) {
    callableChain
        .add(new ThreadLocalProcessor<>(ISession.CURRENT, m_session))
        .add(new DiagnosticContextValueProcessor(BEANS.get(UserIdContextValueProvider.class)))
        .add(new DiagnosticContextValueProcessor(BEANS.get(ScoutSessionIdContextValueProvider.class)))
        .add(new ThreadLocalProcessor<>(UserAgent.CURRENT, m_userAgent))
        .add(new ThreadLocalProcessor<>(IClientNodeId.CURRENT, m_clientNodeId))
        .add(new ThreadLocalProcessor<>(ClientNotificationCollector.CURRENT, m_clientNotificationCollector));
  }

  @Override
  public ServerRunContext withRunMonitor(final RunMonitor runMonitor) {
    super.withRunMonitor(runMonitor);
    return this;
  }

  @Override
  public ServerRunContext withSubject(final Subject subject) {
    super.withSubject(subject);
    return this;
  }

  @Override
  public ServerRunContext withLocale(final Locale locale) {
    super.withLocale(locale);
    return this;
  }

  @Override
  public ServerRunContext withCorrelationId(final String correlationId) {
    super.withCorrelationId(correlationId);
    return this;
  }

  @Override
  public ServerRunContext withTransactionScope(final TransactionScope transactionScope) {
    super.withTransactionScope(transactionScope);
    return this;
  }

  @Override
  public ServerRunContext withTransaction(final ITransaction transaction) {
    super.withTransaction(transaction);
    return this;
  }

  @Override
  public ServerRunContext withTransactionMember(final ITransactionMember transactionMember) {
    super.withTransactionMember(transactionMember);
    return this;
  }

  @Override
  public ServerRunContext withoutTransactionMembers() {
    super.withoutTransactionMembers();
    return this;
  }

  @Override
  public <THREAD_LOCAL> RunContext withThreadLocal(final ThreadLocal<THREAD_LOCAL> threadLocal, final THREAD_LOCAL value) {
    super.withThreadLocal(threadLocal, value);
    return this;
  }

  @Override
  public ServerRunContext withProperty(final Object key, final Object value) {
    super.withProperty(key, value);
    return this;
  }

  @Override
  public ServerRunContext withProperties(final Map<?, ?> properties) {
    super.withProperties(properties);
    return this;
  }

  /**
   * @see #withSession(IServerSession)
   */
  public IServerSession getSession() {
    return m_session;
  }

  /**
   * Associates this context with the given {@link IServerSession}, meaning that any code running on behalf of this
   * context has that {@link ISession} set in {@link ISession#CURRENT} thread-local.
   */
  public ServerRunContext withSession(final IServerSession session) {
    m_session = session;
    return this;
  }

  /**
   * @see #withUserAgent(UserAgent)
   */
  public UserAgent getUserAgent() {
    return m_userAgent;
  }

  /**
   * Associates this context with the given {@link UserAgent}, meaning that any code running on behalf of this context
   * has that {@link UserAgent} set in {@link UserAgent#CURRENT} thread-local.
   */
  public ServerRunContext withUserAgent(final UserAgent userAgent) {
    m_userAgent = userAgent;
    return this;
  }

  /**
   * @see #withClientNodeId(String)
   */
  public String getClientNodeId() {
    return m_clientNodeId;
  }

  /**
   * Associates this context with the given 'client node ID', meaning that any code running on behalf of this context
   * has that id set in {@link IClientNodeId#CURRENT} thread-local.
   * <p>
   * Every client node (that is every UI server node) has its unique 'node ID' which is included with every
   * 'client-server' request, and is mainly used to publish client notifications. If transactional client notifications
   * are issued by code running on behalf of this context, those will not be published to that client node, but included
   * in the request's response instead (piggyback).
   * <p>
   * However, transactional notifications are only sent to clients upon successful commit of the transaction.
   * <p>
   * Typically, this node ID is set by {@link ServiceTunnelServlet} for the processing of a service request.
   */
  public ServerRunContext withClientNodeId(final String clientNodeId) {
    m_clientNodeId = clientNodeId;
    return this;
  }

  /**
   * @see #withClientNotificationCollector(ClientNotificationCollector)
   */
  public ClientNotificationCollector getClientNotificationCollector() {
    return m_clientNotificationCollector;
  }

  /**
   * Associates this context with the given {@link ClientNotificationCollector}, meaning that any code running on behalf
   * of this context has that collector set in {@link ClientNotificationCollector#CURRENT} thread-local.
   * <p>
   * That collector is used to collect all transactional client notifications, which are to be published upon successful
   * commit of the associated transaction, and which are addressed to the client node which triggered processing (see
   * {@link #withClientNodeId(String)}). That way, transactional client notifications are not published immediately upon
   * successful commit, but included in the client's response instead (piggyback).
   * <p>
   * Typically, that collector is set by {@link ServiceTunnelServlet} for the processing of a service request.
   */
  public ServerRunContext withClientNotificationCollector(final ClientNotificationCollector collector) {
    m_clientNotificationCollector = collector;
    return this;
  }

  @Override
  protected void interceptToStringBuilder(final ToStringBuilder builder) {
    super.interceptToStringBuilder(builder
        .ref("session", getSession())
        .attr("userAgent", getUserAgent())
        .attr("clientNodeId", getClientNodeId())
        .ref("transactionalClientNotificationCollector", getClientNotificationCollector()));
  }

  @Override
  protected void copyValues(final RunContext runContext) {
    super.copyValues(runContext);

    final ServerRunContext origin = (ServerRunContext) runContext;
    m_session = origin.m_session;
    m_userAgent = origin.m_userAgent;
    m_clientNotificationCollector = origin.m_clientNotificationCollector;
    m_clientNodeId = origin.m_clientNodeId;
  }

  @Override
  protected void fillCurrentValues() {
    super.fillCurrentValues();

    m_userAgent = UserAgent.CURRENT.get();
    m_session = ServerSessionProvider.currentSession();
    m_clientNotificationCollector = ClientNotificationCollector.CURRENT.get();
    m_clientNodeId = IClientNodeId.CURRENT.get();
  }

  @Override
  public ServerRunContext copy() {
    final ServerRunContext copy = BEANS.get(ServerRunContext.class);
    copy.copyValues(this);
    return copy;
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T> T getAdapter(final Class<T> type) {
    if (ISession.class.isAssignableFrom(type)) {
      return (T) m_session;
    }
    return null;
  }
}
