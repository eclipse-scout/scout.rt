/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.server.session.context;

import java.util.Collection;
import java.util.Locale;
import java.util.Map;

import javax.security.auth.Subject;

import org.eclipse.scout.rt.dataobject.id.NodeId;
import org.eclipse.scout.rt.platform.Replace;
import org.eclipse.scout.rt.platform.chain.callable.CallableChain;
import org.eclipse.scout.rt.platform.context.RunContext;
import org.eclipse.scout.rt.platform.context.RunMonitor;
import org.eclipse.scout.rt.platform.logger.DiagnosticContextValueProcessor.IDiagnosticContextValueProvider;
import org.eclipse.scout.rt.platform.transaction.ITransaction;
import org.eclipse.scout.rt.platform.transaction.ITransactionMember;
import org.eclipse.scout.rt.platform.transaction.TransactionScope;
import org.eclipse.scout.rt.platform.util.ThreadLocalProcessor;
import org.eclipse.scout.rt.platform.util.ToStringBuilder;
import org.eclipse.scout.rt.server.clientnotification.ClientNotificationCollector;
import org.eclipse.scout.rt.server.context.ServerRunContext;
import org.eclipse.scout.rt.server.session.IServerSession;
import org.eclipse.scout.rt.server.session.ServerSessionProvider;
import org.eclipse.scout.rt.shared.session.ISession;
import org.eclipse.scout.rt.shared.ui.UserAgent;

import io.opentelemetry.context.Context;

/**
 * {@link ServerRunContext} extended with support for {@link IServerSession}.
 */
@Replace
public class ServerSessionRunContext extends ServerRunContext {

  protected IServerSession m_session;

  @Override
  protected <RESULT> void interceptCallableChain(CallableChain<RESULT> callableChain) {
    callableChain.add(new ThreadLocalProcessor<>(ISession.CURRENT, m_session));
    super.interceptCallableChain(callableChain);
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
  public ServerSessionRunContext withSession(final IServerSession session) {
    m_session = session;
    return this;
  }

  @Override
  public ServerSessionRunContext withRunMonitor(final RunMonitor runMonitor) {
    super.withRunMonitor(runMonitor);
    return this;
  }

  @Override
  public ServerSessionRunContext withSubject(final Subject subject) {
    super.withSubject(subject);
    return this;
  }

  @Override
  public ServerSessionRunContext withLocale(final Locale locale) {
    super.withLocale(locale);
    return this;
  }

  @Override
  public ServerSessionRunContext withCorrelationId(final String correlationId) {
    super.withCorrelationId(correlationId);
    return this;
  }

  @Override
  public ServerSessionRunContext withOpenTelemetryContext(Context context) {
    super.withOpenTelemetryContext(context);
    return this;
  }

  @Override
  public ServerSessionRunContext withTransactionScope(final TransactionScope transactionScope) {
    super.withTransactionScope(transactionScope);
    return this;
  }

  @Override
  public ServerSessionRunContext withTransaction(final ITransaction transaction) {
    super.withTransaction(transaction);
    return this;
  }

  @Override
  public ServerSessionRunContext withTransactionMember(final ITransactionMember transactionMember) {
    super.withTransactionMember(transactionMember);
    return this;
  }

  @Override
  public ServerSessionRunContext withoutTransactionMembers() {
    super.withoutTransactionMembers();
    return this;
  }

  @Override
  public <THREAD_LOCAL> ServerSessionRunContext withThreadLocal(final ThreadLocal<THREAD_LOCAL> threadLocal, final THREAD_LOCAL value) {
    super.withThreadLocal(threadLocal, value);
    return this;
  }

  @Override
  public ServerSessionRunContext withDiagnostic(IDiagnosticContextValueProvider provider) {
    super.withDiagnostic(provider);
    return this;
  }

  @Override
  public ServerSessionRunContext withDiagnostics(Collection<? extends IDiagnosticContextValueProvider> diagnosticContextValueProviders) {
    super.withDiagnostics(diagnosticContextValueProviders);
    return this;
  }

  @Override
  public ServerSessionRunContext withProperty(final Object key, final Object value) {
    super.withProperty(key, value);
    return this;
  }

  @Override
  public ServerSessionRunContext withProperties(final Map<?, ?> properties) {
    super.withProperties(properties);
    return this;
  }

  @Override
  public ServerSessionRunContext withClientNotificationCollector(final ClientNotificationCollector collector) {
    super.withClientNotificationCollector(collector);
    return this;
  }

  @Override
  public ServerSessionRunContext withClientNodeId(NodeId clientNodeId) {
    super.withClientNodeId(clientNodeId);
    return this;
  }

  @Override
  public ServerSessionRunContext withUserAgent(final UserAgent userAgent) {
    super.withUserAgent(userAgent);
    return this;
  }

  @Override
  protected void interceptToStringBuilder(final ToStringBuilder builder) {
    super.interceptToStringBuilder(builder
        .ref("session", getSession()));
  }

  @Override
  protected void copyValues(final RunContext runContext) {
    super.copyValues(runContext);

    final ServerSessionRunContext origin = (ServerSessionRunContext) runContext;
    m_session = origin.m_session;
  }

  @Override
  protected void fillCurrentValues() {
    super.fillCurrentValues();

    m_session = ServerSessionProvider.currentSession();
  }

  @Override
  public ServerSessionRunContext copy() {
    return (ServerSessionRunContext) super.copy();
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
