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

import org.eclipse.scout.commons.ICallable;
import org.eclipse.scout.commons.ToStringBuilder;
import org.eclipse.scout.commons.nls.NlsLocale;
import org.eclipse.scout.rt.platform.OBJ;
import org.eclipse.scout.rt.platform.context.InitThreadLocalCallable;
import org.eclipse.scout.rt.platform.context.PreferredValue;
import org.eclipse.scout.rt.platform.context.RunContext;
import org.eclipse.scout.rt.platform.job.PropertyMap;
import org.eclipse.scout.rt.server.IServerSession;
import org.eclipse.scout.rt.server.session.ServerSessionProvider;
import org.eclipse.scout.rt.server.transaction.ITransaction;
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
 * <li>{@link Subject}</li>
 * <li>{@link NlsLocale#CURRENT}</li>
 * <li>{@link PropertyMap#CURRENT}</li>
 * <li>{@link ISession#CURRENT}</li>
 * <li>{@link UserAgent#CURRENT}</li>
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
  protected PreferredValue<UserAgent> m_userAgent = new PreferredValue<>(null, false);
  protected long m_transactionId;
  protected TransactionScope m_transactionScope;

  @Override
  protected <RESULT> ICallable<RESULT> interceptCallable(final ICallable<RESULT> next) {
    final ICallable<RESULT> c6 = new TwoPhaseTransactionBoundaryCallable<>(next, transactionScope(), transactionId());
    final ICallable<RESULT> c5 = new InitThreadLocalCallable<>(c6, ScoutTexts.CURRENT, (session() != null ? session().getTexts() : ScoutTexts.CURRENT.get()));
    final ICallable<RESULT> c4 = new InitThreadLocalCallable<>(c5, UserAgent.CURRENT, userAgent());
    final ICallable<RESULT> c3 = new InitThreadLocalCallable<>(c4, ISession.CURRENT, session());
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

  public TransactionScope transactionScope() {
    return m_transactionScope;
  }

  /**
   * Sets the transaction scope to control in which transaction to run executables. By default, a new transaction is
   * started, and committed or rolled back upon completion.
   * <ul>
   * <li>Use {@link TransactionScope#REQUIRES_NEW} to run executables in a new transaction.</li>
   * <li>Use {@link TransactionScope#REQUIRED} to only start a new transaction if not running in a transaction yet.</li>
   * <li>Use {@link TransactionScope#MANDATORY} to enforce that the caller is already running in a transaction.
   * Otherwise, a {@link TransactionRequiredException} is thrown.</li>
   * </ul>
   */
  public ServerRunContext transactionScope(final TransactionScope transactionScope) {
    m_transactionScope = transactionScope;
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
    builder.attr("transactionId", transactionId());
    builder.attr("transactionScope", transactionScope());
    return builder.toString();
  }

  // === fill methods ===

  @Override
  protected void copyValues(final RunContext origin) {
    final ServerRunContext originRunContext = (ServerRunContext) origin;

    super.copyValues(originRunContext);
    m_session = originRunContext.m_session;
    m_userAgent = originRunContext.m_userAgent.copy();
    m_transactionId = originRunContext.m_transactionId;
    m_transactionScope = originRunContext.m_transactionScope;
  }

  @Override
  protected void fillCurrentValues() {
    super.fillCurrentValues();
    m_userAgent = new PreferredValue<>(UserAgent.CURRENT.get(), false);
    m_transactionId = ITransaction.TX_ZERO_ID;
    m_transactionScope = TransactionScope.REQUIRES_NEW;
    session(ServerSessionProvider.currentSession()); // method call to derive other values.
  }

  @Override
  protected void fillEmptyValues() {
    super.fillEmptyValues();
    m_userAgent = new PreferredValue<>(null, true); // null as preferred UserAgent
    m_transactionId = ITransaction.TX_ZERO_ID;
    m_transactionScope = TransactionScope.REQUIRES_NEW;
    session(null); // method call to derive other values.
  }

  @Override
  public ServerRunContext copy() {
    final ServerRunContext copy = OBJ.get(ServerRunContext.class);
    copy.copyValues(this);
    return copy;
  }
}
