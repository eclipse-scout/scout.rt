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
package org.eclipse.scout.rt.client.context;

import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Callable;

import javax.security.auth.Subject;

import org.eclipse.scout.commons.ToStringBuilder;
import org.eclipse.scout.commons.nls.NlsLocale;
import org.eclipse.scout.rt.client.IClientSession;
import org.eclipse.scout.rt.client.context.internal.CurrentSessionLogCallable;
import org.eclipse.scout.rt.client.session.ClientSessionProvider;
import org.eclipse.scout.rt.client.ui.desktop.IDesktop;
import org.eclipse.scout.rt.client.ui.desktop.outline.IOutline;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.context.RunContext;
import org.eclipse.scout.rt.platform.context.RunMonitor;
import org.eclipse.scout.rt.platform.context.internal.InitThreadLocalCallable;
import org.eclipse.scout.rt.platform.job.PropertyMap;
import org.eclipse.scout.rt.shared.ISession;
import org.eclipse.scout.rt.shared.ScoutTexts;
import org.eclipse.scout.rt.shared.ui.UserAgent;

/**
 * Use this class to propagate client-side context.
 * <p/>
 * A context typically represents a "snapshot" of the current calling state. This class facilitates propagation of that
 * client state among different threads, or allows temporary state changes to be done for the time of executing some
 * code.
 * <p/>
 * The 'setter-methods' returns <code>this</code> in order to support for method chaining. The context has the following
 * characteristics:
 * <ul>
 * <li>{@link RunMonitor#CURRENT}</li>
 * <li>{@link Subject#getSubject(java.security.AccessControlContext)}</li>
 * <li>{@link NlsLocale#CURRENT}</li>
 * <li>{@link PropertyMap#CURRENT}</li>
 * <li>{@link ISession#CURRENT}</li>
 * <li>{@link UserAgent#CURRENT}</li>
 * <li>{@link ScoutTexts#CURRENT}</li>
 * <li>{@link IDesktop#CURRENT}</li>
 * <li>{@link IOutline#CURRENT}</li>
 * <li>{@link IForm#CURRENT}</li>
 * </ul>
 *
 * @since 5.1
 * @see ClientRunContexts
 * @see RunContext
 */
public class ClientRunContext extends RunContext {

  protected IClientSession m_session;
  protected UserAgent m_userAgent;
  protected IForm m_form;
  protected IOutline m_outline;
  protected IDesktop m_desktop;

  @Override
  protected <RESULT> Callable<RESULT> interceptCallable(final Callable<RESULT> next) {
    final Callable<RESULT> c8 = new InitThreadLocalCallable<>(next, IForm.CURRENT, m_form);
    final Callable<RESULT> c7 = new InitThreadLocalCallable<>(c8, IOutline.CURRENT, m_outline);
    final Callable<RESULT> c6 = new InitThreadLocalCallable<>(c7, IDesktop.CURRENT, m_desktop);
    final Callable<RESULT> c5 = new InitThreadLocalCallable<>(c6, ScoutTexts.CURRENT, (m_session != null ? m_session.getTexts() : ScoutTexts.CURRENT.get()));
    final Callable<RESULT> c4 = new InitThreadLocalCallable<>(c5, UserAgent.CURRENT, m_userAgent);
    final Callable<RESULT> c3 = new CurrentSessionLogCallable<>(c4);
    final Callable<RESULT> c2 = new InitThreadLocalCallable<>(c3, ISession.CURRENT, m_session);
    final Callable<RESULT> c1 = super.interceptCallable(c2);

    return c1;
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
  public ClientRunContext withProperty(final Object key, final Object value) {
    super.withProperty(key, value);
    return this;
  }

  @Override
  public ClientRunContext withProperties(final Map<?, ?> properties) {
    super.withProperties(properties);
    return this;
  }

  public IClientSession getSession() {
    return m_session;
  }

  /**
   * Sets the session.
   *
   * @param applySessionProperties
   *          <code>true</code> to apply session properties like {@link Locale}, {@link Subject} and {@link UserAgent}.
   */
  public ClientRunContext withSession(final IClientSession session, final boolean applySessionProperties) {
    m_session = session;

    if (applySessionProperties) {
      m_locale = (session != null ? session.getLocale() : null);
      m_userAgent = (session != null ? session.getUserAgent() : null);
      m_subject = (session != null ? session.getSubject() : null);
    }

    return this;
  }

  public UserAgent getUserAgent() {
    return m_userAgent;
  }

  public ClientRunContext withUserAgent(final UserAgent userAgent) {
    m_userAgent = userAgent;
    return this;
  }

  /**
   * Returns the {@link IForm} which is associated with this {@link ClientRunContext}, or <code>null</code> if not
   * set.
   */
  public IForm getForm() {
    return m_form;
  }

  /**
   * Associates this {@link ClientRunContext} with a {@link IForm}. Typically, that information is set by the UI facade
   * when dispatching a request from UI.
   */
  public ClientRunContext withForm(final IForm form) {
    m_form = form;
    return this;
  }

  /**
   * Returns the {@link IOutline} which is associated with this {@link ClientRunContext}, or <code>null</code> if not
   * set.
   */
  public IOutline getOutline() {
    return m_outline;
  }

  /**
   * Associates this {@link ClientRunContext} with a {@link IOutline}. Typically, that information is set by the UI
   * facade when dispatching a request from UI.
   */
  public ClientRunContext withOutline(final IOutline outline) {
    m_outline = outline;
    return this;
  }

  /**
   * Returns the {@link IDesktop} which is associated with this {@link ClientRunContext}, or <code>null</code> if not
   * set.
   */
  public IDesktop getDesktop() {
    return m_desktop;
  }

  /**
   * Associates this {@link ClientRunContext} with a {@link IDesktop}. Typically, that information is set by the UI
   * facade when dispatching a request from UI.
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
    m_userAgent = UserAgent.CURRENT.get();
    m_session = ClientSessionProvider.currentSession();
    m_desktop = resolveCurrentDesktop();
    m_outline = IOutline.CURRENT.get();
    m_form = IForm.CURRENT.get();
  }

  @Override
  protected void fillEmptyValues() {
    super.fillEmptyValues();
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

    final IClientSession session = (IClientSession) ISession.CURRENT.get();
    if (session != null) {
      return session.getDesktopElseVirtualDesktop();
    }
    else {
      return null;
    }
  }
}
