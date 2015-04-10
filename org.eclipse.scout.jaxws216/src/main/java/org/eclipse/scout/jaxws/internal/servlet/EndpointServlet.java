/*******************************************************************************
 * Copyright (c) 2011 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Daniel Wiehl (BSI Business Systems Integration AG) - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.jaxws.internal.servlet;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.ws.Binding;
import javax.xml.ws.http.HTTPBinding;

import org.eclipse.scout.commons.ConfigIniUtility;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.jaxws.JaxWs216Module;
import org.eclipse.scout.jaxws.service.IJaxWsEndpointService;
import org.eclipse.scout.rt.platform.BEANS;

import com.sun.xml.internal.ws.transport.http.HttpAdapter;

@SuppressWarnings("restriction")
public abstract class EndpointServlet extends HttpServlet {

  private static final long serialVersionUID = 1L;

  private static final IScoutLogger LOG = ScoutLogManager.getLogger(EndpointServlet.class);

  private final Map<String, ServletAdapter> m_urlAdapterMap = new HashMap<String, ServletAdapter>();
  private boolean m_publishStatusPage;

  @Override
  public void init() throws ServletException {
    super.init();
    installServletAdapters();
    publishStatusPage();
  }

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    handleRequest(request, response);
  }

  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    handleRequest(request, response);
  }

  @Override
  protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    handleRequest(request, response, HTTPBinding.class);
  }

  @Override
  protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    handleRequest(request, response, HTTPBinding.class);
  }

  protected void handleRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    handleRequest(request, response, null);
  }

  protected void handleRequest(HttpServletRequest request, HttpServletResponse response, Class<? extends Binding> bindingTypeFilter) throws ServletException, IOException {
    ServletAdapter adapter = resolveServletAdapter(request);
    if (adapter == null) {
      // No webservice endpoint found: return status page if installed.
      if (StringUtility.equalsIgnoreCase(request.getMethod(), "GET") && m_publishStatusPage) {
        IJaxWsEndpointService endpointService = BEANS.get(IJaxWsEndpointService.class);
        Collection<ServletAdapter> servletAdapters = m_urlAdapterMap.values();
        try {
          endpointService.onGetRequest(request, response, servletAdapters.toArray(new ServletAdapter[servletAdapters.size()]));
        }
        catch (Exception e) {
          LOG.error("Failed to provide JAX-WS status page", e);
          response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
      }
      else {
        response.sendError(HttpServletResponse.SC_NOT_FOUND);
      }
    }
    else {
      // This is a webservice request to an installed endpoint.
      if (bindingTypeFilter != null) {
        Binding candidateBinding = adapter.getEndpoint().getBinding();
        if (!bindingTypeFilter.isAssignableFrom(candidateBinding.getClass())) {
          LOG.error("Illegal binding in webservice request. Must be of the type " + bindingTypeFilter.getName());
          response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED, "Illegal binding in webservie request.");
          return;
        }
      }
      adapter.handle(getServletContext(), request, response);
    }
  }

  protected void installServletAdapters() {
    IJaxWsEndpointService endpointService = BEANS.get(IJaxWsEndpointService.class);
    for (ServletAdapter servletAdapter : endpointService.getServletAdapters()) {
      String urlPattern = servletAdapter.getValidPath();
      if (urlPattern.contains("*.")) {
        LOG.warn("Failed to install JAX-WS endpoint '" + servletAdapter.getAlias() + "' due to unsupported URL pattern");
        continue;
      }

      if (m_urlAdapterMap.containsKey(urlPattern)) {
        LOG.warn("Failed to install JAX-WS endpoint '" + servletAdapter.getAlias() + "' due to duplicate URL pattern '" + urlPattern + "'");
        continue;
      }

      m_urlAdapterMap.put(urlPattern, servletAdapter);
      LOG.info("Successfully installed JAX-WS endpoint '" + servletAdapter.getAlias() + "' on URL '" + urlPattern + "'");
    }
  }

  protected void publishStatusPage() {
    // do not use JAX-WS status page to display all endpoints together
    HttpAdapter.publishStatusPage = false;
    m_publishStatusPage = ConfigIniUtility.getPropertyBoolean(JaxWs216Module.PROP_PUBLISH_STATUS_PAGE, true);
  }

  protected ServletAdapter resolveServletAdapter(HttpServletRequest request) {
    String requestPath = request.getRequestURI().substring(request.getContextPath().length());
    ServletAdapter adapter = m_urlAdapterMap.get(requestPath);
    if (adapter != null) {
      return adapter;
    }
    for (ServletAdapter candidate : m_urlAdapterMap.values()) {
      if (requestPath.matches("^" + Pattern.quote(candidate.getValidPath()) + "[\\/\\?]?.*$")) {
        return candidate;
      }
    }
    return null;
  }
}
