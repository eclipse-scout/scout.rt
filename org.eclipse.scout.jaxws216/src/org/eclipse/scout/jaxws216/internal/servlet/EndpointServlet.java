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
package org.eclipse.scout.jaxws216.internal.servlet;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.ws.Binding;
import javax.xml.ws.handler.Handler;
import javax.xml.ws.http.HTTPBinding;

import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.scout.commons.BooleanUtility;
import org.eclipse.scout.commons.CompareUtility;
import org.eclipse.scout.commons.FileUtility;
import org.eclipse.scout.commons.IOUtility;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.commons.TypeCastUtility;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.jaxws216.Activator;
import org.eclipse.scout.jaxws216.handler.internal.IScoutTransactionHandlerWrapper;
import org.eclipse.scout.jaxws216.security.provider.IAuthenticationHandler;
import org.osgi.framework.Bundle;

import com.sun.xml.internal.ws.transport.http.HttpAdapter;

public abstract class EndpointServlet extends HttpServlet {

  public static final String JAXWS_RI_ADAPTERS = "org.eclipse.scout.jaxws216.adapters";
  public static final String JAXWS_RI_SERVICES_PLACEHOLDER = "#jaxws-services#";

  private static final long serialVersionUID = 1L;

  private static final IScoutLogger LOG = ScoutLogManager.getLogger(EndpointServlet.class);

  private final Map<String, ServletAdapter> m_urlAdapterMap = new HashMap<String, ServletAdapter>();
  private boolean m_publishStatusPage;

  private Bundle m_resourceBundle;
  private String m_resourcePath;

  @Override
  public void init() throws ServletException {
    super.init();

    installResourceBundle();
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

  protected void handleRequest(HttpServletRequest request, HttpServletResponse response) {
    handleRequest(request, response, null);
  }

  protected void handleRequest(HttpServletRequest request, HttpServletResponse response, Class<? extends Binding> bindingTypeFilter) {
    try {
      ServletAdapter adapter = resolveServletAdapter(request);
      if (adapter == null) {
        // status page
        if (request.getMethod().equals("GET") && m_publishStatusPage) {
          writeStaticResource(request, response);
          return;
        }
        response.sendError(HttpServletResponse.SC_NOT_FOUND);
        return;
      }

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
    catch (Throwable t) {
      LOG.error("webservice request failed", t);
      try {
        response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      }
      catch (IOException e) {
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      }
    }
  }

  protected ServletAdapter[] getServletAdapters() {
    Object adapters = getServletContext().getAttribute(EndpointServlet.JAXWS_RI_ADAPTERS);
    if (adapters == null) {
      return new ServletAdapter[0];
    }
    return TypeCastUtility.castValue(adapters, ServletAdapter[].class);
  }

  protected void installResourceBundle() {
    String bundleName = getInitParameter("bundle-name");
    if (StringUtility.hasText(bundleName)) {
      bundleName = StringUtility.trim(bundleName);
    }
    else {
      bundleName = null;
    }
    String bundlePath = getInitParameter("bundle-path");
    if (StringUtility.hasText(bundlePath)) {
      bundlePath = StringUtility.trim(bundlePath);
    }
    else {
      bundlePath = null;
    }

    if (bundleName == null || bundlePath == null) {
      return;
    }
    m_resourceBundle = Platform.getBundle(bundleName);
    m_resourcePath = bundlePath;
  }

  protected void installServletAdapters() {
    ServletAdapter[] servletAdapters = getServletAdapters();
    for (ServletAdapter servletAdapter : servletAdapters) {
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
    m_publishStatusPage = BooleanUtility.nvl(TypeCastUtility.castValue(Activator.getDefault().getBundle().getBundleContext().getProperty(Activator.PROP_PUBLISH_STATUS_PAGE), Boolean.class), true);
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

  protected void writeStaticResource(HttpServletRequest request, HttpServletResponse response) throws IOException, ProcessingException {
    String pathInfo = request.getPathInfo();
    if (!StringUtility.hasText(pathInfo)) {
      // ensure proper resource loading if trailing slash is missing
      response.setStatus(HttpServletResponse.SC_MOVED_PERMANENTLY);
      response.setHeader("Location", new Path(JaxWsHelper.getBaseAddress(request, false)).append(request.getRequestURI()).addTrailingSeparator().toString());
      return;
    }

    if (pathInfo == null || pathInfo.endsWith("/") || pathInfo.equals("")) {
      pathInfo = "/jaxws-services.html";
    }

    URL url = null;
    if (m_resourceBundle != null) {
      url = m_resourceBundle.getResource(new Path(m_resourcePath).append(pathInfo).toPortableString());
    }
    if (url == null) {
      url = Activator.getDefault().getBundle().getResource(new Path("/resources/html/").append(pathInfo).toPortableString());
    }
    if (url == null) {
      response.sendError(HttpServletResponse.SC_NOT_FOUND);
      return;
    }

    // substitute content
    byte[] content;
    if (new Path(pathInfo).lastSegment().equals("jaxws-services.html")) {
      String html = IOUtility.getContent(new InputStreamReader(url.openStream()), true);
      if (html.contains(JAXWS_RI_SERVICES_PLACEHOLDER)) {
        html = substituteJaxWsServices(html, request.getContextPath());
      }
      content = html.getBytes("UTF-8");
    }
    else {
      content = IOUtility.getContent(url.openStream(), true);
    }

    response.getOutputStream().write(content);
    response.setStatus(HttpServletResponse.SC_OK);
    response.setContentLength(content.length);

    // determine MIME type
    String mimeType = getServletContext().getMimeType(pathInfo);
    if (mimeType == null) {
      String[] tokens = pathInfo.split("[.]");
      mimeType = FileUtility.getContentTypeForExtension(tokens[tokens.length - 1]);
    }
    if (mimeType == null) {
      mimeType = "application/unknown";
    }
    response.setContentType(mimeType);
  }

  private String substituteJaxWsServices(String html, String contextPath) throws IOException {
    StringBuilder builder = new StringBuilder();

    List<ServletAdapter> adapters = new ArrayList<ServletAdapter>(m_urlAdapterMap.values());
    Collections.sort(adapters, new Comparator<ServletAdapter>() {

      @Override
      public int compare(ServletAdapter adapter1, ServletAdapter adapter2) {
        return CompareUtility.compareTo(adapter1.getAlias(), adapter2.getAlias());
      }
    });

    for (ServletAdapter adapter : adapters) {
      String endpointAddress = adapter.getAddress(contextPath).toString();

      builder.append("<table class=\"service_box\" cellpadding=\"0\" cellspacing=\"0\">");

      builder.append("<tr><td colspan=\"2\" class=\"service_name\">" + StringUtility.nvl(adapter.getAlias(), "?") + "</td></tr>");
      builder.append("<tr><td class=\"left_content_box\">");

      builder.append("<table class=\"content_box\" cellpadding=\"0\" cellspacing=\"0\">");
      builder.append("<tr><td class=\"label\">Service Name:</td><td class=\"content\">" + adapter.getEndpoint().getServiceName() + "</td></tr>");
      builder.append("<tr><td class=\"label\">Port Name:</td><td class=\"content\">" + adapter.getEndpoint().getPortName() + "</td></tr>");
      builder.append("<tr><td class=\"label\">Authentication:</td><td class=\"content\">" + getAuthenticationMethod(adapter) + "</td></tr>");
      builder.append("</table>");

      builder.append("</td><td class=\"right_content_box\">");

      builder.append("<table class=\"content_box\" cellpadding=\"0\" cellspacing=\"0\">");
      builder.append("<tr><td class=\"label\">Address:</td><td class=\"content\">" + endpointAddress + "</td></tr>");
      builder.append("<tr><td class=\"label\">WSDL:</td><td class=\"content\"><a href=\"" + endpointAddress + "?wsdl\">" + endpointAddress + "?wsdl</a></td></tr>");
      builder.append("</table>");

      builder.append("</td></tr>");
      builder.append("</table>");
    }

    return html.replaceFirst(JAXWS_RI_SERVICES_PLACEHOLDER, builder.toString());
  }

  private String getAuthenticationMethod(ServletAdapter adapter) {
    List<Handler> handlers = adapter.getEndpoint().getBinding().getHandlerChain();
    for (Handler handler : handlers) {
      if (handler instanceof IScoutTransactionHandlerWrapper) {
        handler = ((IScoutTransactionHandlerWrapper) handler).getHandler();
      }
      if (handler instanceof IAuthenticationHandler) {
        return ((IAuthenticationHandler) handler).getName();
      }
    }

    return "None";
  }

  public Bundle getResourceBundle() {
    return m_resourceBundle;
  }

  public String getResourcePath() {
    return m_resourcePath;
  }
}
