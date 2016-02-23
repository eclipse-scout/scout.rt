/*******************************************************************************
 * Copyright (c) 2014-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.ui.html;

import java.util.Locale;
import java.util.Map;

import javax.security.auth.Subject;

import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.chain.callable.CallableChain;
import org.eclipse.scout.rt.platform.context.RunContext;
import org.eclipse.scout.rt.platform.context.RunMonitor;
import org.eclipse.scout.rt.platform.logger.DiagnosticContextValueProcessor;
import org.eclipse.scout.rt.platform.logger.DiagnosticContextValueProcessor.IDiagnosticContextValueProvider;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.platform.util.ThreadLocalProcessor;
import org.eclipse.scout.rt.platform.util.ToStringBuilder;
import org.eclipse.scout.rt.ui.html.json.JsonRequest;
import org.slf4j.MDC;

/**
 * The <code>UiRunContext</code> controls propagation of UI state.
 * <p>
 * A context typically represents a "snapshot" of the current calling state. This class facilitates propagation of that
 * state among different threads, or allows temporary state changes to be done for the time of executing some code.
 *
 * @since 5.2
 * @see RunContext
 */
public class UiRunContext extends RunContext {

  protected IUiSession m_session;
  protected JsonRequest m_jsonRequest;

  @Override
  protected <RESULT> void interceptCallableChain(final CallableChain<RESULT> callableChain) {
    super.interceptCallableChain(callableChain);

    callableChain
        .add(new ThreadLocalProcessor<>(IUiSession.CURRENT, m_session))
        .add(new ThreadLocalProcessor<>(JsonRequest.CURRENT, m_jsonRequest))
        .add(new DiagnosticContextValueProcessor(BEANS.get(UiSessionIdContextValueProvider.class)));
  }

  @Override
  public UiRunContext withRunMonitor(final RunMonitor runMonitor) {
    super.withRunMonitor(runMonitor);
    return this;
  }

  @Override
  public UiRunContext withSubject(final Subject subject) {
    super.withSubject(subject);
    return this;
  }

  @Override
  public UiRunContext withLocale(final Locale locale) {
    super.withLocale(locale);
    return this;
  }

  @Override
  public UiRunContext withCorrelationId(final String correlationId) {
    super.withCorrelationId(correlationId);
    return this;
  }

  @Override
  public UiRunContext withProperty(final Object key, final Object value) {
    super.withProperty(key, value);
    return this;
  }

  @Override
  public UiRunContext withProperties(final Map<?, ?> properties) {
    super.withProperties(properties);
    return this;
  }

  @Override
  public UiRunContext withIdentifier(String id) {
    super.withIdentifier(id);
    return this;
  }

  /**
   * @see #withSession(IUiSession)
   */
  public IUiSession getSession() {
    return m_session;
  }

  /**
   * Associates this context with the given {@link IUiSession}, meaning that any code running on behalf of this context
   * has that {@link IUiSession} set in {@link IUiSession#CURRENT} thread-local.
   */
  public UiRunContext withSession(final IUiSession session) {
    m_session = session;
    return this;
  }

  /**
   * @see #withJsonRequest(JsonRequest)
   */
  public JsonRequest getJsonRequest() {
    return m_jsonRequest;
  }

  /**
   * Associates this context with the given {@link JsonRequest}, meaning that any code running on behalf of this context
   * has that {@link JsonRequest} set in {@link JsonRequest#CURRENT} thread-local.
   */
  public UiRunContext withJsonRequest(final JsonRequest jsonRequest) {
    m_jsonRequest = jsonRequest;
    return this;
  }

  @Override
  public String toString() {
    final ToStringBuilder builder = new ToStringBuilder(this);
    builder.ref("runMonitor", getRunMonitor());
    builder.attr("subject", getSubject());
    builder.attr("locale", getLocale());
    builder.attr("ids", CollectionUtility.format(getIdentifiers()));
    builder.ref("session", getSession());
    builder.ref("jsonRequest", getJsonRequest());
    return builder.toString();
  }

  // === fill methods ===

  @Override
  protected void copyValues(final RunContext origin) {
    final UiRunContext originRunContext = (UiRunContext) origin;

    super.copyValues(originRunContext);
    m_session = originRunContext.m_session;
    m_jsonRequest = originRunContext.m_jsonRequest;
  }

  @Override
  protected void fillCurrentValues() {
    super.fillCurrentValues();
    m_session = IUiSession.CURRENT.get();
    m_jsonRequest = JsonRequest.CURRENT.get();
  }

  @Override
  protected void fillEmptyValues() {
    throw new UnsupportedOperationException(); // not supported to not loose context information accidentally (e.g. the authenticated subject)
  }

  @Override
  public UiRunContext copy() {
    final UiRunContext copy = BEANS.get(UiRunContext.class);
    copy.copyValues(this);
    return copy;
  }

  /**
   * This class provides the {@link IUiSession#getUiSessionId()} to be set into the <code>diagnostic context map</code>
   * for logging purpose.
   *
   * @see #KEY
   * @see DiagnosticContextValueProcessor
   * @see MDC
   */
  @ApplicationScoped
  public static class UiSessionIdContextValueProvider implements IDiagnosticContextValueProvider {

    public static final String KEY = "scout.ui.session.id";

    @Override
    public String key() {
      return KEY;
    }

    @Override
    public String value() {
      final IUiSession uiSession = UiSession.CURRENT.get();
      return uiSession != null ? uiSession.getUiSessionId() : null;
    }
  }
}
