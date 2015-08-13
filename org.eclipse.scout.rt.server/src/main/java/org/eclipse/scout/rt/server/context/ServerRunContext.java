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
import java.util.concurrent.Callable;

import javax.security.auth.Subject;

import org.eclipse.scout.commons.BooleanUtility;
import org.eclipse.scout.commons.ToStringBuilder;
import org.eclipse.scout.commons.nls.NlsLocale;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.context.RunContext;
import org.eclipse.scout.rt.platform.context.RunMonitor;
import org.eclipse.scout.rt.platform.context.internal.InitThreadLocalCallable;
import org.eclipse.scout.rt.platform.job.PropertyMap;
import org.eclipse.scout.rt.server.IServerSession;
import org.eclipse.scout.rt.server.clientnotification.ClientNotificationNodeId;
import org.eclipse.scout.rt.server.clientnotification.TransactionalClientNotificationCollector;
import org.eclipse.scout.rt.server.context.internal.CurrentSessionLogCallable;
import org.eclipse.scout.rt.server.context.internal.TwoPhaseTransactionBoundaryCallable;
import org.eclipse.scout.rt.server.session.ServerSessionProvider;
import org.eclipse.scout.rt.server.transaction.ITransaction;
import org.eclipse.scout.rt.server.transaction.TransactionRequiredException;
import org.eclipse.scout.rt.server.transaction.TransactionScope;
import org.eclipse.scout.rt.shared.ISession;
import org.eclipse.scout.rt.shared.OfflineState;
import org.eclipse.scout.rt.shared.ScoutTexts;
import org.eclipse.scout.rt.shared.ui.UserAgent;

/**
 * The <code>ServerRunContext</code> controls propagation of server-side state and sets the transaction boundaries. To
 * control transaction scope, configure the <code>ServerRunContext</code> with the respective {@link TransactionScope}.
 * <p/>
 * A context typically represents a "snapshot" of the current calling state. This class facilitates propagation of that
 * server state among different threads, or allows temporary state changes to be done for the time of executing some
 * code.
 * <p/>
 * A transaction scope controls in which transaction to run executables. By default, a new transaction is started, and
 * committed or rolled back upon completion.
 * <ul>
 * <li>Use {@link TransactionScope#REQUIRES_NEW} to run executables in a new transaction.</li>
 * <li>Use {@link TransactionScope#REQUIRED} to only start a new transaction if not running in a transaction yet.</li>
 * <li>Use {@link TransactionScope#MANDATORY} to enforce that the caller is already running in a transaction. Otherwise,
 * a {@link TransactionRequiredException} is thrown.</li>
 * </ul>
 * The 'setter-methods' returns <code>this</code> in order to support for method chaining. The context has the following
 * characteristics:
 * <ul>
 * <li>{@link RunMonitor#CURRENT}</li>
 * <li>{@link Subject#getSubject(java.security.AccessControlContext)}</li>
 * <li>{@link NlsLocale#CURRENT}</li>
 * <li>{@link PropertyMap#CURRENT}</li>
 * <li>{@link ISession#CURRENT}</li>
 * <li>{@link UserAgent#CURRENT}</li>
 * <li>{@link ClientNotificationNodeId#CURRENT}</li>
 * <li>{@link TransactionalClientNotificationCollector#CURRENT}</li>
 * <li>{@link ScoutTexts#CURRENT}</li>
 * <li>{@link ITransaction#CURRENT}</li>
 * <li>{@link OfflineState#CURRENT}</li>
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
  protected <RESULT> Callable<RESULT> interceptCallable(final Callable<RESULT> next) {
    final Callable<RESULT> c9 = new TwoPhaseTransactionBoundaryCallable<>(next, getTransaction(), m_transactionScope);
    final Callable<RESULT> c8 = new InitThreadLocalCallable<>(c9, ScoutTexts.CURRENT, (m_session != null ? m_session.getTexts() : ScoutTexts.CURRENT.get()));
    final Callable<RESULT> c7 = new InitThreadLocalCallable<>(c8, TransactionalClientNotificationCollector.CURRENT, m_transactionalClientNotificationCollector);
    final Callable<RESULT> c6 = new InitThreadLocalCallable<>(c7, ClientNotificationNodeId.CURRENT, m_notificationNodeId);
    final Callable<RESULT> c5 = new InitThreadLocalCallable<>(c6, UserAgent.CURRENT, m_userAgent);
    final Callable<RESULT> c4 = new CurrentSessionLogCallable<>(c5);
    final Callable<RESULT> c3 = new InitThreadLocalCallable<>(c4, ISession.CURRENT, m_session);
    final Callable<RESULT> c2 = new InitThreadLocalCallable<>(c3, OfflineState.CURRENT, m_offline);
    final Callable<RESULT> c1 = super.interceptCallable(c2);

    return c1;
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

  public IServerSession getSession() {
    return m_session;
  }

  /**
   * Sets the session.
   */
  public ServerRunContext withSession(final IServerSession session) {
    m_session = session;
    return this;
  }

  public UserAgent getUserAgent() {
    return m_userAgent;
  }

  public ServerRunContext withUserAgent(final UserAgent userAgent) {
    m_userAgent = userAgent;
    return this;
  }

  /**
   * The <code>id</code> of the 'client notification node' which triggered the ongoing service request.
   */
  public String getNotificationNodeId() {
    return m_notificationNodeId;
  }

  /**
   * Sets the <code>id</code> of the 'client notification node' which triggered the ongoing service request. If
   * transactional notifications are issued by the current or any nested transaction, those will not be published to
   * that client node, but included in the request's response instead (piggyback).
   * <p>
   * A transactional notification is only sent to clients once the transaction is committed successfully.
   */
  public ServerRunContext withNotificationNodeId(final String notificationNodeId) {
    m_notificationNodeId = notificationNodeId;
    return this;
  }

  /**
   * The collector for transactional client notifications issued by the current or any nested transaction, and are to be
   * included in the request's response upon successful commit (piggyback).
   */
  public TransactionalClientNotificationCollector getTransactionalClientNotificationCollector() {
    return m_transactionalClientNotificationCollector;
  }

  /**
   * Sets the collector for all transactional notifications which are issued by the current or any nested transaction,
   * and are to be included in the request's response upon successful commit (piggyback).
   * <p>
   * A transactional notification is only sent to clients once the transaction is committed successfully.
   */
  public ServerRunContext withTransactionalClientNotificationCollector(final TransactionalClientNotificationCollector collector) {
    m_transactionalClientNotificationCollector = collector;
    return this;
  }

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

  public ITransaction getTransaction() {
    return m_transaction;
  }

  /**
   * Sets the transaction to be used. Has only an effect, if transaction scope is set to
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
