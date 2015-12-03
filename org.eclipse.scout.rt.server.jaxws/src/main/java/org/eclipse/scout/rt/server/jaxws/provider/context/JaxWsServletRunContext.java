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
package org.eclipse.scout.rt.server.jaxws.provider.context;

import java.security.Principal;
import java.util.Collections;
import java.util.Locale;
import java.util.Map;

import javax.security.auth.Subject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.handler.MessageContext;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.chain.callable.CallableChain;
import org.eclipse.scout.rt.platform.context.RunContext;
import org.eclipse.scout.rt.platform.context.RunMonitor;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.platform.util.ThreadLocalProcessor;
import org.eclipse.scout.rt.platform.util.ToStringBuilder;
import org.eclipse.scout.rt.server.commons.context.ServletRunContext;

/**
 * The <code>JaxWsServletRunContext</code> facilitates propagation of the {@link JAX-WS} state like
 * {@link WebServiceContext}.
 *
 * @since 5.1
 * @see ServletRunContext
 */
public class JaxWsServletRunContext extends ServletRunContext {

  /**
   * The {@link WebServiceContext} which is currently associated with the current thread.
   */
  public static final ThreadLocal<WebServiceContext> CURRENT_WEBSERVICE_CONTEXT = new ThreadLocal<>();

  protected WebServiceContext m_webServiceContext;

  @Override
  protected <RESULT> void interceptCallableChain(CallableChain<RESULT> callableChain) {
    super.interceptCallableChain(callableChain);

    callableChain.add(new ThreadLocalProcessor<>(JaxWsServletRunContext.CURRENT_WEBSERVICE_CONTEXT, m_webServiceContext));
  }

  @Override
  public JaxWsServletRunContext withRunMonitor(final RunMonitor runMonitor) {
    super.withRunMonitor(runMonitor);
    return this;
  }

  @Override
  public JaxWsServletRunContext withSubject(final Subject subject) {
    super.withSubject(subject);
    return this;
  }

  @Override
  public JaxWsServletRunContext withLocale(final Locale locale) {
    super.withLocale(locale);
    return this;
  }

  @Override
  public JaxWsServletRunContext withProperty(final Object key, final Object value) {
    super.withProperty(key, value);
    return this;
  }

  @Override
  public JaxWsServletRunContext withProperties(final Map<?, ?> properties) {
    super.withProperties(properties);
    return this;
  }

  @Override
  public JaxWsServletRunContext withIdentifier(String id) {
    super.withIdentifier(id);
    return this;
  }

  /**
   * @see #withWebServiceContext(WebServiceContext)
   */
  public WebServiceContext getWebServiceContext() {
    return m_webServiceContext;
  }

  /**
   * Associates this context with the given {@link WebServiceContext}, meaning that any code running on behalf of this
   * context has that {@link WebServiceContext} set in {@link JaxWsServletRunContext#CURRENT_WEBSERVICE_CONTEXT}
   * thread-local.
   * <p>
   * Also, the associated {@link HttpServletRequest} , {@link HttpServletResponse} and {@link Subject} (if applicable)
   * is set.
   */
  public JaxWsServletRunContext withWebServiceContext(final WebServiceContext webServiceContext) {
    m_webServiceContext = webServiceContext;
    m_servletRequest = (HttpServletRequest) m_webServiceContext.getMessageContext().get(MessageContext.SERVLET_REQUEST);
    m_servletResponse = (HttpServletResponse) m_webServiceContext.getMessageContext().get(MessageContext.SERVLET_RESPONSE);

    if (m_subject == null) {
      final Principal userPrincipal = m_webServiceContext.getUserPrincipal();
      if (userPrincipal != null) {
        m_subject = new Subject(true, Collections.singleton(userPrincipal), Collections.emptySet(), Collections.emptySet());
      }
    }
    return this;
  }

  @Override
  public String toString() {
    final ToStringBuilder builder = new ToStringBuilder(this);
    builder.ref("runMonitor", getRunMonitor());
    builder.attr("subject", getSubject());
    builder.attr("locale", getLocale());
    builder.attr("ids", CollectionUtility.format(getIdentifiers()));
    builder.ref("servletRequest", getServletRequest());
    builder.ref("servletResponse", getServletResponse());
    builder.ref("webServiceContext", getWebServiceContext());
    return builder.toString();
  }

  // === fill methods ===

  @Override
  protected void copyValues(final RunContext origin) {
    final JaxWsServletRunContext originRunContext = (JaxWsServletRunContext) origin;
    super.copyValues(originRunContext);
    m_webServiceContext = originRunContext.m_webServiceContext;
  }

  @Override
  protected void fillCurrentValues() {
    super.fillCurrentValues();
    m_webServiceContext = JaxWsServletRunContext.CURRENT_WEBSERVICE_CONTEXT.get();
  }

  @Override
  protected void fillEmptyValues() {
    throw new UnsupportedOperationException(); // not supported to not loose context information accidentally (e.g. the authenticated subject)
  }

  @Override
  public JaxWsServletRunContext copy() {
    final JaxWsServletRunContext copy = BEANS.get(JaxWsServletRunContext.class);
    copy.copyValues(this);
    return copy;
  }
}
