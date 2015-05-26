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
package org.eclipse.scout.rt.server.jaxws.provider.context;

import java.security.Principal;
import java.util.Collections;
import java.util.Locale;
import java.util.concurrent.Callable;

import javax.security.auth.Subject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.handler.MessageContext;

import org.eclipse.scout.commons.ToStringBuilder;
import org.eclipse.scout.commons.nls.NlsLocale;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.context.IRunMonitor;
import org.eclipse.scout.rt.platform.context.RunContext;
import org.eclipse.scout.rt.platform.context.internal.InitThreadLocalCallable;
import org.eclipse.scout.rt.platform.job.PropertyMap;
import org.eclipse.scout.rt.server.commons.context.ServletRunContext;
import org.eclipse.scout.rt.server.commons.servlet.IHttpServletRoundtrip;

/**
 * The <code>JaxWsRunContext</code> facilitates propagation of the {@link JAX-WS} state like {@link WebServiceContext}.
 * <p/>
 * The 'setter-methods' returns <code>this</code> in order to support for method chaining. The context has the following
 * characteristics:
 * <ul>
 * <li>{@link IRunMonitor#CURRENT}</li>
 * <li>{@link Subject#getSubject(java.security.AccessControlContext)}</li>
 * <li>{@link NlsLocale#CURRENT}</li>
 * <li>{@link PropertyMap#CURRENT}</li>
 * <li>{@link IHttpServletRoundtrip#CURRENT_WEBSERVICE_CONTEXT}</li>
 * <li>{@link IHttpServletRoundtrip#CURRENT_WEBSERVICE_CONTEXT}</li>
 * <li>{@link JaxWsRunContext#CURRENT_WEBSERVICE_CONTEXT}</li>
 * </ul>
 *
 * @since 5.1
 * @see RunContext
 */
public class JaxWsRunContext extends ServletRunContext {

  /**
   * The {@link WebServiceContext} which is currently associated with the current thread.
   */
  public static final ThreadLocal<WebServiceContext> CURRENT_WEBSERVICE_CONTEXT = new ThreadLocal<>();

  protected WebServiceContext m_webServiceContext;

  @Override
  protected <RESULT> Callable<RESULT> interceptCallable(final Callable<RESULT> next) {
    final Callable<RESULT> c2 = new InitThreadLocalCallable<>(next, JaxWsRunContext.CURRENT_WEBSERVICE_CONTEXT, webServiceContext());
    final Callable<RESULT> c1 = super.interceptCallable(c2);
    return c1;
  }

  @Override
  public JaxWsRunContext runMonitor(final IRunMonitor runMonitor) {
    super.runMonitor(runMonitor);
    return this;
  }

  @Override
  public JaxWsRunContext subject(final Subject subject) {
    super.subject(subject);
    return this;
  }

  @Override
  public JaxWsRunContext locale(final Locale locale) {
    super.locale(locale);
    return this;
  }

  public WebServiceContext webServiceContext() {
    return m_webServiceContext;
  }

  /**
   * Sets the given {@link WebServiceContext}, its associated HTTP Servlet request and response, and its associated
   * Subject if present.
   */
  public JaxWsRunContext webServiceContext(final WebServiceContext webServiceContext) {
    m_webServiceContext = webServiceContext;
    m_servletRequest = (HttpServletRequest) m_webServiceContext.getMessageContext().get(MessageContext.SERVLET_REQUEST);
    m_servletResponse = (HttpServletResponse) m_webServiceContext.getMessageContext().get(MessageContext.SERVLET_RESPONSE);

    final Principal userPrincipal = m_webServiceContext.getUserPrincipal();
    if (userPrincipal != null) {
      m_subject.set(new Subject(true, Collections.singleton(userPrincipal), Collections.emptySet(), Collections.emptySet()), false);
    }
    return this;
  }

  @Override
  public String toString() {
    final ToStringBuilder builder = new ToStringBuilder(this);
    builder.ref("runMonitor", runMonitor());
    builder.attr("subject", subject());
    builder.attr("locale", locale());
    builder.ref("servletRequest", servletRequest());
    builder.ref("servletResponse", servletResponse());
    builder.ref("webServiceContext", webServiceContext());
    return builder.toString();
  }

  // === fill methods ===

  @Override
  protected void copyValues(final RunContext origin) {
    final JaxWsRunContext originRunContext = (JaxWsRunContext) origin;
    super.copyValues(originRunContext);
    m_webServiceContext = originRunContext.m_webServiceContext;
  }

  @Override
  protected void fillCurrentValues() {
    super.fillCurrentValues();
    m_webServiceContext = JaxWsRunContext.CURRENT_WEBSERVICE_CONTEXT.get();
  }

  @Override
  protected void fillEmptyValues() {
    super.fillEmptyValues();
    m_webServiceContext = null;
  }

  @Override
  public JaxWsRunContext copy() {
    final JaxWsRunContext copy = BEANS.get(JaxWsRunContext.class);
    copy.copyValues(this);
    return copy;
  }
}
