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
package org.eclipse.scout.rt.client.context;

import java.util.Locale;
import java.util.Map;

import javax.security.auth.Subject;

import org.eclipse.scout.rt.client.IClientSession;
import org.eclipse.scout.rt.client.session.ClientSessionProvider;
import org.eclipse.scout.rt.client.ui.IDisplayParent;
import org.eclipse.scout.rt.client.ui.desktop.IDesktop;
import org.eclipse.scout.rt.client.ui.desktop.outline.IOutline;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.chain.callable.CallableChain;
import org.eclipse.scout.rt.platform.context.RunContext;
import org.eclipse.scout.rt.platform.context.RunMonitor;
import org.eclipse.scout.rt.platform.logger.DiagnosticContextValueProcessor;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.platform.util.ThreadLocalProcessor;
import org.eclipse.scout.rt.platform.util.ToStringBuilder;
import org.eclipse.scout.rt.shared.ISession;
import org.eclipse.scout.rt.shared.ScoutTexts;
import org.eclipse.scout.rt.shared.logging.UserIdContextValueProvider;
import org.eclipse.scout.rt.shared.session.ScoutSessionIdContextValueProvider;
import org.eclipse.scout.rt.shared.ui.UserAgent;

/**
 * Use this class to propagate client-side context.
 * <p>
 * A context typically represents a "snapshot" of the current calling state. This class facilitates propagation of that
 * client state among different threads, or allows temporary state changes to be done for the time of executing some
 * code.
 *
 * @since 5.1
 * @see ClientRunContexts
 * @see RunContext
 */
public class ClientRunContext extends RunContext {

  /**
   * Identifier used in {@link RunContext#withIdentifier(String)} to mark a run context as client run context
   */
  public static final String CLIENT_RUN_CONTEXT_IDENTIFIER = "Client";

  protected IClientSession m_session;
  protected UserAgent m_userAgent;
  protected IForm m_form;
  protected IOutline m_outline;
  protected IDesktop m_desktop;

  @Override
  protected <RESULT> void interceptCallableChain(final CallableChain<RESULT> callableChain) {
    super.interceptCallableChain(callableChain);

    callableChain
        .add(new ThreadLocalProcessor<>(ISession.CURRENT, m_session))
        .add(new DiagnosticContextValueProcessor(BEANS.get(UserIdContextValueProvider.class)))
        .add(new DiagnosticContextValueProcessor(BEANS.get(ScoutSessionIdContextValueProvider.class)))
        .add(new ThreadLocalProcessor<>(UserAgent.CURRENT, m_userAgent))
        .add(new ThreadLocalProcessor<>(ScoutTexts.CURRENT, (m_session != null ? m_session.getTexts() : ScoutTexts.CURRENT.get())))
        .add(new ThreadLocalProcessor<>(IDesktop.CURRENT, m_desktop))
        .add(new ThreadLocalProcessor<>(IOutline.CURRENT, m_outline))
        .add(new ThreadLocalProcessor<>(IForm.CURRENT, m_form));
  }

  @Override
  public ClientRunContext withRunMonitor(final RunMonitor runMonitor) {
    super.withRunMonitor(runMonitor);
    return this;
  }

  @Override
  public ClientRunContext withSubject(final Subject subject) {
    super.withSubject(subject);
    return this;
  }

  @Override
  public ClientRunContext withLocale(final Locale locale) {
    super.withLocale(locale);
    return this;
  }

  @Override
  public ClientRunContext withCorrelationId(final String correlationId) {
    super.withCorrelationId(correlationId);
    return this;
  }

  @Override
  public ClientRunContext withIdentifier(final String id) {
    super.withIdentifier(id);
    return this;
  }

  @Override
  public ClientRunContext withProperty(final Object key, final Object value) {
    super.withProperty(key, value);
    return this;
  }

  @Override
  public ClientRunContext withProperties(final Map<?, ?> properties) {
    super.withProperties(properties);
    return this;
  }

  /**
   * @see #withSession(IClientSession, boolean)
   */
  public IClientSession getSession() {
    return m_session;
  }

  /**
   * Associates this context with the given {@link IClientSession}, meaning that any code running on behalf of this
   * context has that {@link ISession} set in {@link ISession#CURRENT} thread-local.
   *
   * @param applySessionProperties
   *          <code>true</code> to apply session properties like {@link Locale}, {@link Subject}, {@link UserAgent} and
   *          {@link IDesktop} to this context.
   */
  public ClientRunContext withSession(final IClientSession session, final boolean applySessionProperties) {
    m_session = session;

    if (applySessionProperties) {
      m_locale = (session != null ? session.getLocale() : null);
      m_userAgent = (session != null ? session.getUserAgent() : null);
      m_subject = (session != null ? session.getSubject() : null);
      m_desktop = (session != null ? session.getDesktopElseVirtualDesktop() : null);
    }
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
  public ClientRunContext withUserAgent(final UserAgent userAgent) {
    m_userAgent = userAgent;
    return this;
  }

  /**
   * Returns the {@link IForm} which is associated with this context, or <code>null</code> if not set.
   */
  public IForm getForm() {
    return m_form;
  }

  /**
   * Associates this context with the given {@link IForm}, meaning that any code running on behalf of this context has
   * that {@link IForm} set in {@link IForm#CURRENT} thread-local.
   * <p>
   * That information is mainly used to determine the current calling model context, e.g. when opening a message-box to
   * associate it with the proper {@link IDisplayParent}.
   * <p>
   * Typically, that information is set by the UI facade when dispatching a request from UI, or when constructing UI
   * model elements.
   */
  public ClientRunContext withForm(final IForm form) {
    m_form = form;
    return this;
  }

  /**
   * Returns the {@link IOutline} which is associated with this context, or <code>null</code> if not set.
   */
  public IOutline getOutline() {
    return m_outline;
  }

  /**
   * Associates this context with the given {@link IOutline}, meaning that any code running on behalf of this context
   * has that {@link IOutline} set in {@link IOutline#CURRENT} thread-local.
   * <p>
   * That information is mainly used to determine the current calling model context, e.g. when opening a message-box to
   * associate it with the proper {@link IDisplayParent}.
   * <p>
   * Typically, that information is set by the UI facade when dispatching a request from UI, or when constructing UI
   * model elements.
   * <p>
   * If unsetForm is true the form on this runContext is set to null
   */
  public ClientRunContext withOutline(final IOutline outline, boolean unsetForm) {
    if (unsetForm) {
      m_form = null;
    }
    m_outline = outline;
    return this;
  }

  /**
   * Returns the {@link IDesktop} which is associated with this context, or <code>null</code> if not set.
   */
  public IDesktop getDesktop() {
    return m_desktop;
  }

  /**
   * Associates this context with the given {@link IDesktop}, meaning that any code running on behalf of this context
   * has that {@link IDesktop} set in {@link IDesktop#CURRENT} thread-local.
   * <p>
   * That information is mainly used to determine the current calling model context, e.g. when opening a message-box to
   * associate it with the proper {@link IDisplayParent}.
   * <p>
   * Typically, that information is set by the UI facade when dispatching a request from UI, or when constructing UI
   * model elements.
   */
  public ClientRunContext withDesktop(final IDesktop desktop) {
    m_desktop = desktop;
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
    builder.attr("userAgent", getUserAgent());
    builder.ref("desktop", getDesktop());
    builder.ref("outline", getOutline());
    builder.ref("form", getForm());
    return builder.toString();
  }

  // === fill methods ===

  @Override
  protected void copyValues(final RunContext origin) {
    final ClientRunContext originRunContext = (ClientRunContext) origin;

    super.copyValues(originRunContext);
    m_userAgent = originRunContext.m_userAgent;
    m_session = originRunContext.m_session;
    m_desktop = originRunContext.m_desktop;
    m_outline = originRunContext.m_outline;
    m_form = originRunContext.m_form;
  }

  @Override
  protected void fillCurrentValues() {
    super.fillCurrentValues();
    m_identifiers.push(CLIENT_RUN_CONTEXT_IDENTIFIER);
    m_userAgent = UserAgent.CURRENT.get();
    m_session = ClientSessionProvider.currentSession();
    m_desktop = resolveCurrentDesktop();
    m_outline = IOutline.CURRENT.get();
    m_form = IForm.CURRENT.get();
  }

  @Override
  protected void fillEmptyValues() {
    super.fillEmptyValues();
    m_identifiers.push(CLIENT_RUN_CONTEXT_IDENTIFIER);
    m_userAgent = null;
    m_session = null;
    m_desktop = null;
    m_outline = null;
    m_form = null;
  }

  @Override
  public ClientRunContext copy() {
    final ClientRunContext copy = BEANS.get(ClientRunContext.class);
    copy.copyValues(this);
    return copy;
  }

  /**
   * Resolves the {@link IDesktop} form current calling context.
   */
  protected IDesktop resolveCurrentDesktop() {
    final IDesktop desktop = IDesktop.CURRENT.get();
    if (desktop != null) {
      return desktop;
    }

    final ISession currentSession = ISession.CURRENT.get();
    if (currentSession instanceof IClientSession) {
      return ((IClientSession) currentSession).getDesktopElseVirtualDesktop();
    }
    return null;
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T> T getAdapter(final Class<T> type) {
    if (ISession.class.isAssignableFrom(type)) {
      return (T) m_session;
    }
    else {
      return null;
    }
  }
}
