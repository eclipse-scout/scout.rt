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
package org.eclipse.scout.rt.server.commons.context;

import java.util.Locale;
import java.util.concurrent.Callable;

import javax.security.auth.Subject;
import javax.servlet.Servlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.scout.commons.ToStringBuilder;
import org.eclipse.scout.commons.nls.NlsLocale;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.context.IRunMonitor;
import org.eclipse.scout.rt.platform.context.RunContext;
import org.eclipse.scout.rt.platform.context.internal.InitThreadLocalCallable;
import org.eclipse.scout.rt.platform.job.PropertyMap;
import org.eclipse.scout.rt.server.commons.context.internal.CurrentHttpServletRequestLogCallable;
import org.eclipse.scout.rt.server.commons.servlet.IHttpServletRoundtrip;

/**
 * The <code>ServletRunContext</code> facilitates propagation of the {@link Servlet} state. This context is not intended
 * to be propagated across different threads.
 * <p/>
 * A context typically represents a "snapshot" of the current calling state. This class facilitates propagation of that
 * state.
 * <p/>
 * The 'setter-methods' returns <code>this</code> in order to support for method chaining. The context has the following
 * characteristics:
 * <ul>
 * <li>{@link IRunMonitor#CURRENT}</li>
 * <li>{@link Subject#getSubject(java.security.AccessControlContext)}</li>
 * <li>{@link NlsLocale#CURRENT}</li>
 * <li>{@link PropertyMap#CURRENT}</li>
 * <li>{@link IHttpServletRoundtrip#CURRENT}</li>
 * <li>{@link IHttpServletRoundtrip#CURRENT}</li>
 * </ul>
 *
 * @since 5.1
 * @see RunContext
 */
public class ServletRunContext extends RunContext {

  protected HttpServletRequest m_servletRequest;
  protected HttpServletResponse m_servletResponse;

  @Override
  protected <RESULT> Callable<RESULT> interceptCallable(final Callable<RESULT> next) {
    final Callable<RESULT> c4 = new CurrentHttpServletRequestLogCallable<>(next);
    final Callable<RESULT> c3 = new InitThreadLocalCallable<>(c4, IHttpServletRoundtrip.CURRENT_HTTP_SERVLET_RESPONSE, servletResponse());
    final Callable<RESULT> c2 = new InitThreadLocalCallable<>(c3, IHttpServletRoundtrip.CURRENT_HTTP_SERVLET_REQUEST, servletRequest());
    final Callable<RESULT> c1 = super.interceptCallable(c2);

    return c1;
  }

  @Override
  public ServletRunContext runMonitor(final IRunMonitor runMonitor) {
    super.runMonitor(runMonitor);
    return this;
  }

  @Override
  public ServletRunContext subject(final Subject subject) {
    super.subject(subject);
    return this;
  }

  @Override
  public ServletRunContext locale(final Locale locale) {
    super.locale(locale);
    return this;
  }

  public HttpServletRequest servletRequest() {
    return m_servletRequest;
  }

  public ServletRunContext servletRequest(final HttpServletRequest servletRequest) {
    m_servletRequest = servletRequest;
    return this;
  }

  public HttpServletResponse servletResponse() {
    return m_servletResponse;
  }

  public ServletRunContext servletResponse(final HttpServletResponse servletResponse) {
    m_servletResponse = servletResponse;
    return this;
  }

  @Override
  public String toString() {
    final ToStringBuilder builder = new ToStringBuilder(this);
    builder.attr("subject", subject());
    builder.attr("locale", locale());
    builder.ref("servletRequest", servletRequest());
    builder.ref("servletResponse", servletResponse());
    return builder.toString();
  }

  // === fill methods ===

  @Override
  protected void copyValues(final RunContext origin) {
    final ServletRunContext originRunContext = (ServletRunContext) origin;

    super.copyValues(originRunContext);
    m_servletRequest = originRunContext.m_servletRequest;
    m_servletResponse = originRunContext.m_servletResponse;
  }

  @Override
  protected void fillCurrentValues() {
    super.fillCurrentValues();
    m_servletRequest = IHttpServletRoundtrip.CURRENT_HTTP_SERVLET_REQUEST.get();
    m_servletResponse = IHttpServletRoundtrip.CURRENT_HTTP_SERVLET_RESPONSE.get();
  }

  @Override
  protected void fillEmptyValues() {
    super.fillEmptyValues();
    m_servletRequest = null;
    m_servletResponse = null;
  }

  @Override
  public ServletRunContext copy() {
    final ServletRunContext copy = BEANS.get(ServletRunContext.class);
    copy.copyValues(this);
    return copy;
  }

}
