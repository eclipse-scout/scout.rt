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
import java.util.Map;

import javax.security.auth.Subject;

import org.eclipse.scout.commons.BooleanUtility;
import org.eclipse.scout.commons.ThreadLocalProcessor;
import org.eclipse.scout.commons.ToStringBuilder;
import org.eclipse.scout.commons.chain.InvocationChain;
import org.eclipse.scout.commons.logger.internal.slf4j.DiagnosticContextValueProcessor;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.context.RunContext;
import org.eclipse.scout.rt.platform.context.RunMonitor;
import org.eclipse.scout.rt.server.IServerSession;
import org.eclipse.scout.rt.server.ServiceTunnelServlet;
import org.eclipse.scout.rt.server.clientnotification.ClientNotificationNodeId;
import org.eclipse.scout.rt.server.clientnotification.TransactionalClientNotificationCollector;
import org.eclipse.scout.rt.server.session.ServerSessionProvider;
import org.eclipse.scout.rt.server.transaction.ITransaction;
import org.eclipse.scout.rt.server.transaction.TransactionRequiredException;
import org.eclipse.scout.rt.server.transaction.TransactionScope;
import org.eclipse.scout.rt.shared.ISession;
import org.eclipse.scout.rt.shared.OfflineState;
import org.eclipse.scout.rt.shared.ScoutTexts;
import org.eclipse.scout.rt.shared.logging.UserIdContextValueProvider;
import org.eclipse.scout.rt.shared.ui.UserAgent;

/**
 * The <code>ServerRunContext</code> controls propagation of server-side state and sets the transaction boundaries. To
 * control transaction scope, configure the <code>ServerRunContext</code> with the respective {@link TransactionScope}.
 * <p>
 * A context typically represents a "snapshot" of the current calling state. This class facilitates propagation of that
 * server state among different threads, or allows temporary state changes to be done for the time of executing some
 * code.
 * <p>
 * A transaction scope controls in which transaction to run executables. By default, a new transaction is started, and
 * committed or rolled back upon completion.
 * <ul>
 * <li>Use {@link TransactionScope#REQUIRES_NEW} to run executables in a new transaction.</li>
 * <li>Use {@link TransactionScope#REQUIRED} to only start a new transaction if not running in a transaction yet.</li>
 * <li>Use {@link TransactionScope#MANDATORY} to enforce that the caller is already running in a transaction. Otherwise,
 * a {@link TransactionRequiredException} is thrown.</li>
 * </ul>
 *
 * @since 5.1
 * @see RunContext
 */
public class ServerRunContext extends RunContext {

  protected IServerSession m_session;
  protected UserAgent m_userAgent;
  protected String m_notificationNodeId;
  protected TransactionalClientNotificationCollector m_transactionalClientNotificationCollector;
  protected TransactionScope m_transactionScope;
  protected ITransaction m_transaction;
  protected boolean m_offline;

  @Override
  protected <RESULT> void interceptInvocationChain(InvocationChain<RESULT> invocationChain) {
    super.interceptInvocationChain(invocationChain);

    invocationChain
        .add(new ThreadLocalProcessor<>(OfflineState.CURRENT, m_offline))
        .add(new ThreadLocalProcessor<>(ISession.CURRENT, m_session))
        .add(new DiagnosticContextValueProcessor<>(BEANS.get(UserIdContextValueProvider.class)))
        .add(new ThreadLocalProcessor<>(UserAgent.CURRENT, m_userAgent))
        .add(new ThreadLocalProcessor<>(ClientNotificationNodeId.CURRENT, m_notificationNodeId))
        .add(new ThreadLocalProcessor<>(TransactionalClientNotificationCollector.CURRENT, m_transactionalClientNotificationCollector))
        .add(new ThreadLocalProcessor<>(ScoutTexts.CURRENT, (m_session != null ? m_session.getTexts() : ScoutTexts.CURRENT.get())))
        .add(new TransactionProcessor<>(getTransaction(), m_transactionScope));
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
   * @see #withNotificationNodeId(String)
   */
  public String getNotificationNodeId() {
    return m_notificationNodeId;
  }

  /**
   * Associates this context with the given 'notification ID', meaning that any code running on behalf of this context
   * has that id set in {@link ClientNotificationNodeId#CURRENT} thread-local.
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
  public ServerRunContext withNotificationNodeId(final String notificationNodeId) {
    m_notificationNodeId = notificationNodeId;
    return this;
  }

  /**
   * @see #withTransactionalClientNotificationCollector(TransactionalClientNotificationCollector)
   */
  public TransactionalClientNotificationCollector getTransactionalClientNotificationCollector() {
    return m_transactionalClientNotificationCollector;
  }

  /**
   * Associates this context with the given {@link TransactionalClientNotificationCollector}, meaning that any code
   * running on behalf of this context has that collector set in
   * {@link TransactionalClientNotificationCollector#CURRENT} thread-local.
   * <p>
   * That collector is used to collect all transactional client notifications, which are to be published upon successful
   * commit of the associated transaction, and which are addressed to the client node which triggered processing (see
   * {@link #withNotificationNodeId(String)}). That way, transactional client notifications are not published
   * immediately upon successful commit, but included in the client's response instead (piggyback).
   * <p>
   * Typically, that collector is set by {@link ServiceTunnelServlet} for the processing of a service request.
   */
  public ServerRunContext withTransactionalClientNotificationCollector(final TransactionalClientNotificationCollector collector) {
    m_transactionalClientNotificationCollector = collector;
    return this;
  }

  /**
   * @see #withTransactionScope(TransactionScope)
   */
  public TransactionScope getTransactionScope() {
    return m_transactionScope;
  }

  /**
   * Sets the transaction scope to control in which transaction boundary to run the runnable. By default, a new
   * transaction is started, and committed or rolled back upon completion.
   * <ul>
   * <li>Use {@link TransactionScope#REQUIRES_NEW} to run in a new transaction.</li>
   * <li>Use {@link TransactionScope#REQUIRED} to only start a new transaction if there is no transaction set.</li>
   * <li>Use {@link TransactionScope#MANDATORY} to enforce running in the given transaction. Otherwise, a
   * {@link TransactionRequiredException} is thrown.</li>
   * </ul>
   */
  public ServerRunContext withTransactionScope(final TransactionScope transactionScope) {
    m_transactionScope = transactionScope;
    return this;
  }

  /**
   * @see #withTransaction(ITransaction)
   */
  public ITransaction getTransaction() {
    return m_transaction;
  }

  /**
   * Sets the transaction to be used to run the runnable. Has only an effect, if transaction scope is set to
   * {@link TransactionScope#REQUIRED} or {@link TransactionScope#MANDATORY}. Normally, this property should not be set
   * manually.
   */
  public ServerRunContext withTransaction(final ITransaction transaction) {
    m_transaction = transaction;
    return this;
  }

  public boolean isOffline() {
    return m_offline;
  }

  /**
   * Indicates to run in offline mode.
   */
  public ServerRunContext withOffline(final boolean offline) {
    m_offline = offline;
    return this;
  }

  @Override
  public String toString() {
    final ToStringBuilder builder = new ToStringBuilder(this);
    builder.ref("runMonitor", getRunMonitor());
    builder.attr("subject", getSubject());
    builder.attr("locale", getLocale());
    builder.ref("session", getSession());
    builder.attr("userAgent", getUserAgent());
    builder.attr("notificationNodeId", getNotificationNodeId());
    builder.ref("transactionalClientNotificationCollector", getTransactionalClientNotificationCollector());
    builder.ref("transaction", getTransaction());
    builder.attr("transactionScope", getTransactionScope());
    builder.attr("offline", isOffline());
    return builder.toString();
  }

  // === fill methods ===

  @Override
  protected void copyValues(final RunContext origin) {
    final ServerRunContext originRunContext = (ServerRunContext) origin;

    super.copyValues(originRunContext);
    m_session = originRunContext.m_session;
    m_userAgent = originRunContext.m_userAgent;
    m_transactionalClientNotificationCollector = originRunContext.m_transactionalClientNotificationCollector;
    m_notificationNodeId = originRunContext.m_notificationNodeId;
    m_transactionScope = originRunContext.m_transactionScope;
    m_transaction = originRunContext.m_transaction;
    m_offline = originRunContext.m_offline;
  }

  @Override
  protected void fillCurrentValues() {
    super.fillCurrentValues();
    m_userAgent = UserAgent.CURRENT.get();
    m_transactionalClientNotificationCollector = TransactionalClientNotificationCollector.CURRENT.get();
    m_notificationNodeId = ClientNotificationNodeId.CURRENT.get();
    m_transactionScope = TransactionScope.REQUIRES_NEW;
    m_transaction = ITransaction.CURRENT.get();
    m_offline = BooleanUtility.nvl(OfflineState.CURRENT.get(), false);
    m_session = ServerSessionProvider.currentSession();
  }

  @Override
  protected void fillEmptyValues() {
    super.fillEmptyValues();
    m_userAgent = null;
    m_transactionalClientNotificationCollector = new TransactionalClientNotificationCollector();
    m_notificationNodeId = null;
    m_transactionScope = TransactionScope.REQUIRES_NEW;
    m_transaction = null;
    m_offline = false;
    m_session = null;
  }

  @Override
  public ServerRunContext copy() {
    final ServerRunContext copy = BEANS.get(ServerRunContext.class);
    copy.copyValues(this);
    return copy;
  }
}
