/*******************************************************************************
 * Copyright (c) 2010-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.server.jaxws.provider.context;

import java.security.AccessController;
import java.util.Collections;

import javax.security.auth.Subject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.handler.MessageContext;

import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.context.CorrelationId;
import org.eclipse.scout.rt.platform.context.RunContext;
import org.eclipse.scout.rt.platform.context.RunContexts;
import org.eclipse.scout.rt.server.commons.servlet.IHttpServletRoundtrip;
import org.eclipse.scout.rt.server.commons.servlet.logging.ServletDiagnosticsProviderFactory;
import org.eclipse.scout.rt.server.jaxws.MessageContexts;
import org.eclipse.scout.rt.server.jaxws.implementor.JaxWsImplementorSpecifics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Lookup for JAX-WS {@link RunContext} in WS-EntryPoints.
 *
 * @since 6.1
 */
@ApplicationScoped
public class JaxWsRunContextLookup {

  private static final Logger LOG = LoggerFactory.getLogger(JaxWsRunContextLookup.class);

  private static final String RUNCONTEXT_MISSING_WARNING = ""
      + "No RunContext found in calling context: using empty RunContext. "
      + "Specify the RunContext in JAX-WS handler via 'MessageContexts.putRunContext(...)', "
      + "or use built-in authentication mechanism, "
      + "or provide it in Servlet filter via 'RunContext.CURRENT.set(...)'";

  /**
   * Looks up the {@link RunContext} from the given {@link WebServiceContext}.
   *
   * @return {@link RunContext}, is never <code>null</code>.
   */
  public RunContext lookup(final WebServiceContext webServiceContext) {
    final RunContext runContext = lookupRunContext(webServiceContext);
    final Subject subject = lookupSubject(webServiceContext, runContext);
    final String cid = lookupCorrelationId(webServiceContext, runContext);

    final MessageContext messageContext = webServiceContext.getMessageContext();
    final JaxWsImplementorSpecifics implementor = BEANS.get(JaxWsImplementorSpecifics.class);

    HttpServletRequest request = implementor.getServletRequest(messageContext);
    HttpServletResponse response = implementor.getServletResponse(messageContext);

    return runContext
        .withSubject(subject)
        .withCorrelationId(cid)
        .withThreadLocal(IWebServiceContext.CURRENT, webServiceContext)
        .withThreadLocal(IHttpServletRoundtrip.CURRENT_HTTP_SERVLET_REQUEST, request)
        .withThreadLocal(IHttpServletRoundtrip.CURRENT_HTTP_SERVLET_RESPONSE, response)
        .withDiagnostics(BEANS.get(ServletDiagnosticsProviderFactory.class).getProviders(request, response));
  }

  /**
   * Method invoked to look up the {@link RunContext}, and must not return <code>null</code>.
   */
  protected RunContext lookupRunContext(final WebServiceContext webServiceContext) {
    final RunContext runContext = MessageContexts.getRunContext(webServiceContext.getMessageContext());
    if (runContext != null) {
      return runContext;
    }

    if (RunContext.CURRENT.get() != null) {
      return RunContext.CURRENT.get().copy();
    }

    LOG.warn(RUNCONTEXT_MISSING_WARNING);
    return RunContexts.empty();
  }

  /**
   * Method invoked to look up the {@link Subject}, and may be <code>null</code>.
   */
  protected Subject lookupSubject(final WebServiceContext webServiceContext, final RunContext runContext) {
    final Subject subject = runContext.getSubject();
    if (subject != null) {
      return subject;
    }

    if (webServiceContext.getUserPrincipal() != null) {
      return new Subject(true, Collections.singleton(webServiceContext.getUserPrincipal()), Collections.emptySet(), Collections.emptySet());
    }

    return Subject.getSubject(AccessController.getContext());
  }

  /**
   * Method invoked to look up the Correlation ID.
   */
  protected String lookupCorrelationId(final WebServiceContext webServiceContext, final RunContext runContext) {
    final String cid = MessageContexts.getCorrelationId(webServiceContext.getMessageContext());
    if (cid != null) {
      return cid;
    }

    if (runContext.getCorrelationId() != null) {
      return runContext.getCorrelationId();
    }
    return BEANS.get(CorrelationId.class).newCorrelationId();
  }
}
