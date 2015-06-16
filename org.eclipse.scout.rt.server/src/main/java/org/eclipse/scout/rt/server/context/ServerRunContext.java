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

import org.eclipse.scout.commons.BooleanUtility;
import org.eclipse.scout.commons.ToStringBuilder;
import org.eclipse.scout.commons.nls.NlsLocale;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.context.RunContext;
import org.eclipse.scout.rt.platform.context.RunMonitor;
import org.eclipse.scout.rt.platform.context.internal.InitThreadLocalCallable;
import org.eclipse.scout.rt.platform.job.PropertyMap;
import org.eclipse.scout.rt.server.IServerSession;
import org.eclipse.scout.rt.server.clientnotification.ClientNotificationContainer;
import org.eclipse.scout.rt.server.clientnotification.ClientNotificationNodeId;
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
 * <li>{@link ClientNotificationContainer#CURRENT}</li>
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
  protected ClientNotificationContainer m_txNotificationContainer;
  protected TransactionScope m_transactionScope;
  protected ITransaction m_transaction;
  protected boolean m_offline;

  @Override
  protected <RESULT> Callable<RESULT> interceptCallable(final Callable<RESULT> next) {
    final Callable<RESULT> c9 = new TwoPhaseTransactionBoundaryCallable<>(next, transaction(), transactionScope());
    final Callable<RESULT> c8 = new InitThreadLocalCallable<>(c9, ScoutTexts.CURRENT, (session() != null ? session().getTexts() : ScoutTexts.CURRENT.get()));
    final Callable<RESULT> c7 = new InitThreadLocalCallable<>(c8, ClientNotificationContainer.CURRENT, txNotificationContainer());
    final Callable<RESULT> c6 = new InitThreadLocalCallable<>(c7, ClientNotificationNodeId.CURRENT, notificationNodeId());
    final Callable<RESULT> c5 = new InitThreadLocalCallable<>(c6, UserAgent.CURRENT, userAgent());
    final Callable<RESULT> c4 = new CurrentSessionLogCallable<>(c5);
    final Callable<RESULT> c3 = new InitThreadLocalCallable<>(c4, ISession.CURRENT, session());
    final Callable<RESULT> c2 = new InitThreadLocalCallable<>(c3, OfflineState.CURRENT, offline());
    final Callable<RESULT> c1 = super.interceptCallable(c2);

    return c1;
  }

  @Override
  public ServerRunContext runMonitor(final RunMonitor runMonitor) {
    super.runMonitor(runMonitor);
    return this;
  }

  @Override
  public ServerRunContext subject(final Subject subject) {
    super.subject(subject);
    return this;
  }

  @Override
  public ServerRunContext locale(final Locale locale) {
    super.locale(locale);
    return this;
  }

  public IServerSession session() {
    return m_session;
  }

  /**
   * Sets the session.
   *
   * @param applySessionProperties
   *          <code>true</code> to apply session properties like {@link Locale}, {@link Subject} and {@link UserAgent}.
   */
  public ServerRunContext session(final IServerSession session, final boolean applySessionProperties) {
    m_session = session;

    if (applySessionProperties) {
      m_subject = (session != null ? session.getSubject() : null);
    }

    return this;
  }

  public UserAgent userAgent() {
    return m_userAgent;
  }

  public ServerRunContext userAgent(final UserAgent userAgent) {
    m_userAgent = userAgent;
    return this;
  }

  public String notificationNodeId() {
    return m_notificationNodeId;
  }

  /**
   * The id of the notification node. This id is on the run context to make use of transactional piggy back
   * notifications during a remote request. The notificationNodeId on the context will be excluded from sending
   * notifications.
   *
   * @param notificationNodeId
   * @return
   */
  public ServerRunContext notificationNodeId(final String notificationNodeId) {
    m_notificationNodeId = notificationNodeId;
    return this;
  }

  public ServerRunContext txNotificationContainer(ClientNotificationContainer txNotificationContainer) {
    m_txNotificationContainer = txNotificationContainer;
    return this;
  }

  public ClientNotificationContainer txNotificationContainer() {
    return m_txNotificationContainer;
  }

  public TransactionScope transactionScope() {
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
  public ServerRunContext transactionScope(final TransactionScope transactionScope) {
    m_transactionScope = transactionScope;
    return this;
  }

  public ITransaction transaction() {
    return m_transaction;
  }

  /**
   * Sets the transaction to be used. Has only an effect, if transaction scope is set to
   * {@link TransactionScope#REQUIRED} or {@link TransactionScope#MANDATORY}. Normally, this property should not be set
   * manually.
   */
  public ServerRunContext transaction(final ITransaction transaction) {
    m_transaction = transaction;
    return this;
  }

  public boolean offline() {
    return m_offline;
  }

  /**
   * Indicates to run in offline mode.
   */
  public ServerRunContext offline(final boolean offline) {
    m_offline = offline;
    return this;
  }

  @Override
  public String toString() {
    final ToStringBuilder builder = new ToStringBuilder(this);
    builder.ref("runMonitor", runMonitor());
    builder.attr("subject", subject());
    builder.attr("locale", locale());
    builder.ref("session", session());
    builder.attr("userAgent", userAgent());
    builder.attr("notificationId", notificationNodeId());
    builder.attr("txNotificationContainer", txNotificationContainer());
    builder.ref("transaction", transaction());
    builder.attr("transactionScope", transactionScope());
    builder.attr("offline", offline());
    return builder.toString();
  }

  // === fill methods ===

  @Override
  protected void copyValues(final RunContext origin) {
    final ServerRunContext originRunContext = (ServerRunContext) origin;

    super.copyValues(originRunContext);
    m_session = originRunContext.m_session;
    m_userAgent = originRunContext.m_userAgent;
    m_txNotificationContainer = originRunContext.m_txNotificationContainer;
    m_notificationNodeId = originRunContext.m_notificationNodeId;
    m_transactionScope = originRunContext.m_transactionScope;
    m_transaction = originRunContext.m_transaction;
    m_offline = originRunContext.m_offline;
  }

  @Override
  protected void fillCurrentValues() {
    super.fillCurrentValues();
    m_userAgent = UserAgent.CURRENT.get();
    m_txNotificationContainer = ClientNotificationContainer.CURRENT.get();
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
    m_txNotificationContainer = null;
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
