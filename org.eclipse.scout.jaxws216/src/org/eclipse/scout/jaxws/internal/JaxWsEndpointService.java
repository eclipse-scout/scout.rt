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
package org.eclipse.scout.jaxws.internal;

import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.ws.handler.Handler;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.scout.commons.CompareUtility;
import org.eclipse.scout.commons.FileUtility;
import org.eclipse.scout.commons.IOUtility;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.commons.UriBuilder;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.jaxws.Activator;
import org.eclipse.scout.jaxws.handler.internal.IScoutTransactionHandlerWrapper;
import org.eclipse.scout.jaxws.internal.resources.BundleProxyClassLoader;
import org.eclipse.scout.jaxws.internal.resources.BundleProxyResourceLoader;
import org.eclipse.scout.jaxws.internal.resources.SunJaxWsXml;
import org.eclipse.scout.jaxws.internal.resources.SunJaxWsXmlFinder;
import org.eclipse.scout.jaxws.internal.servlet.JaxWsHelper;
import org.eclipse.scout.jaxws.internal.servlet.ServletAdapter;
import org.eclipse.scout.jaxws.internal.servlet.ServletAdapterFactory;
import org.eclipse.scout.jaxws.internal.servlet.ServletContainer;
import org.eclipse.scout.jaxws.security.provider.IAuthenticationHandler;
import org.eclipse.scout.jaxws.service.IJaxWsEndpointService;
import org.eclipse.scout.service.AbstractService;
import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceRegistration;

import com.sun.xml.internal.ws.api.server.Container;
import com.sun.xml.internal.ws.resources.WsservletMessages;
import com.sun.xml.internal.ws.transport.http.DeploymentDescriptorParser;
import com.sun.xml.internal.ws.transport.http.DeploymentDescriptorParser.AdapterFactory;
import com.sun.xml.internal.ws.transport.http.ResourceLoader;

@SuppressWarnings("restriction")
public class JaxWsEndpointService extends AbstractService implements IJaxWsEndpointService {

  private static final IScoutLogger LOG = ScoutLogManager.getLogger(JaxWsEndpointService.class);

  public static final String HTML_STATUS_PAGE_TEMPLATE = "jaxws-services.html";
  public static final String HTML_STATUS_PAGE_ENDPOINT_PLACEHOLDER = "#jaxws-services#";

  private ServletAdapter[] m_servletAdapters;
  private String m_propResourceBundle;
  private String m_propResourcePath;

  @Override
  public final void initializeService(final ServiceRegistration registration) {
    m_servletAdapters = createEndpointAdapters();
    initResourceBundle();
  }

  @Override
  public ServletAdapter[] getServletAdapters() {
    return m_servletAdapters;
  }

  @Override
  public ServletAdapter getServletAdapter(final String alias) {
    if (alias == null) {
      return null;
    }
    for (ServletAdapter adapter : m_servletAdapters) {
      if (alias.equals(adapter.getAlias())) {
        return adapter;
      }
    }
    return null;
  }

  @Override
  public String getAuthenticationMethod(final ServletAdapter adapter) {
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

  @Override
  public final void onGetRequest(final HttpServletRequest request, final HttpServletResponse response, final ServletAdapter[] servletAdapters) throws Exception {
    String pathInfo = request.getPathInfo();
    if (!StringUtility.hasText(pathInfo)) {
      // ensure proper resource loading if trailing slash is missing
      response.setStatus(HttpServletResponse.SC_MOVED_PERMANENTLY);

      final String baseUrl = JaxWsHelper.getBaseAddress(request, false);
      IPath contextPath = new Path(request.getRequestURI()).addTrailingSeparator().makeAbsolute();
      if (contextPath.isUNC()) {
        contextPath = contextPath.makeUNC(false);
      }
      final URI redirectUri = new UriBuilder(baseUrl).path(contextPath.toString()).createURI();
      response.setHeader("Location", redirectUri.toString());
      return;
    }

    if (pathInfo == null || pathInfo.endsWith("/") || pathInfo.equals("")) {
      pathInfo = "/" + HTML_STATUS_PAGE_TEMPLATE; // status page
    }

    byte[] content = new byte[0];
    if (new Path(pathInfo).lastSegment().equals(HTML_STATUS_PAGE_TEMPLATE)) {
      // status page
      String html = createHtmlStatusPage(request.getContextPath(), servletAdapters);
      if (html != null) {
        content = html.getBytes("UTF-8");
      }
    }
    else {
      // other resource (e.g. image file)
      URL url = resolveResourceURL(pathInfo);
      if (url != null) {
        content = IOUtility.getContent(url.openStream(), true);
      }
      else {
        response.sendError(HttpServletResponse.SC_NOT_FOUND);
        return;
      }
    }

    String contentType = FileUtility.getContentTypeForExtension(new Path(pathInfo).getFileExtension());
    if (contentType == null) {
      contentType = "application/unknown";
    }
    response.setContentType(contentType);
    response.setContentLength(content.length);

    response.setStatus(HttpServletResponse.SC_OK);
    response.getOutputStream().write(content);
  }

  protected String createHtmlStatusPage(final String contextPath, final ServletAdapter[] servletAdapters) throws Exception {
    URL url = resolveResourceURL(HTML_STATUS_PAGE_TEMPLATE);
    if (url == null) {
      return null;
    }

    // substitute HTML with table of webservices
    String html = IOUtility.getContent(new InputStreamReader(url.openStream()), true);
    if (!html.contains(HTML_STATUS_PAGE_ENDPOINT_PLACEHOLDER)) {
      return html;
    }

    List<ServletAdapter> adaptersOrdered = new ArrayList<ServletAdapter>(Arrays.asList(servletAdapters));
    Collections.sort(adaptersOrdered, new Comparator<ServletAdapter>() {

      @Override
      public int compare(ServletAdapter adapter1, ServletAdapter adapter2) {
        return CompareUtility.compareTo(adapter1.getAlias(), adapter2.getAlias());
      }
    });

    StringBuilder builder = new StringBuilder();
    for (ServletAdapter adapter : adaptersOrdered) {
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
    return html.replaceFirst(HTML_STATUS_PAGE_ENDPOINT_PLACEHOLDER, builder.toString());
  }

  @Override
  public void disposeServices() {
    for (ServletAdapter servletAdapter : m_servletAdapters) {
      try {
        servletAdapter.getEndpoint().dispose();
      }
      catch (Throwable t) {
        LOG.error("failed to dispose webservice endpoint", t);
      }
    }
  }

  protected ServletAdapter[] createEndpointAdapters() {
    final List<ServletAdapter> servletAdapters = new ArrayList<ServletAdapter>();
    for (SunJaxWsXml cfg : new SunJaxWsXmlFinder().findAll()) {
      try {
        final URL sunJaxWsXml = cfg.getResource();
        final ClassLoader classLoader = new BundleProxyClassLoader(cfg.getBundle());
        final ClassLoader oldContextCL = Thread.currentThread().getContextClassLoader();
        try {
          Thread.currentThread().setContextClassLoader(classLoader);

          final ResourceLoader resourceLoader = new BundleProxyResourceLoader(cfg.getBundle());
          final Container container = createContainer(resourceLoader);
          final AdapterFactory adapterFactory = createServletAdapterFactory();
          final DeploymentDescriptorParser<ServletAdapter> parser = createDeploymentDescriptorParser(classLoader, resourceLoader, container, adapterFactory);
          final List<ServletAdapter> adaptersInBundle = parser.parse(sunJaxWsXml.toExternalForm(), sunJaxWsXml.openStream());
          if (adaptersInBundle != null) {
            servletAdapters.addAll(adaptersInBundle);
          }
        }
        finally {
          Thread.currentThread().setContextClassLoader(oldContextCL);
        }
      }
      catch (Exception e) {
        LOG.error(WsservletMessages.LISTENER_PARSING_FAILED(e) + " for bundle '" + cfg.getBundle().getSymbolicName() + "'", e);
      }
    }
    return servletAdapters.toArray(new ServletAdapter[servletAdapters.size()]);
  }

  @SuppressWarnings("unchecked")
  protected DeploymentDescriptorParser<ServletAdapter> createDeploymentDescriptorParser(final ClassLoader classLoader, final ResourceLoader resourceLoader, final Container container, final AdapterFactory adapterFactory) throws MalformedURLException {
    return new DeploymentDescriptorParser(classLoader, resourceLoader, container, adapterFactory);
  }

  protected Container createContainer(final ResourceLoader resourceLoader) {
    return new ServletContainer((com.sun.xml.internal.ws.api.ResourceLoader) Platform.getAdapterManager().getAdapter(resourceLoader, com.sun.xml.internal.ws.api.ResourceLoader.class));
  }

  protected AdapterFactory<ServletAdapter> createServletAdapterFactory() {
    return new ServletAdapterFactory();
  }

  protected URL resolveResourceURL(String pathInfo) {
    if (m_propResourceBundle == null || m_propResourcePath == null) {
      return resolveDefaultResourceURL(pathInfo);
    }
    Bundle resourceBundle = Platform.getBundle(m_propResourceBundle);
    if (resourceBundle == null) {
      LOG.warn("The resource bundle configured in config-ini could not be found [property='" + Activator.PROP_RESOURCE_BUNDLE + "']");
      return resolveDefaultResourceURL(pathInfo);
    }
    URL url = resourceBundle.getResource(new Path(m_propResourcePath).append(pathInfo).toPortableString());
    if (url == null) {
      // fallback
      return resolveDefaultResourceURL(pathInfo);
    }
    return url;
  }

  private void initResourceBundle() {
    // resource bundle
    m_propResourceBundle = Activator.getDefault().getBundle().getBundleContext().getProperty(Activator.PROP_RESOURCE_BUNDLE);
    if (!StringUtility.hasText(m_propResourceBundle)) {
      m_propResourceBundle = null;
    }

    // resource path
    m_propResourcePath = Activator.getDefault().getBundle().getBundleContext().getProperty(Activator.PROP_RESOURCE_PATH);
    if (!StringUtility.hasText(m_propResourcePath)) {
      m_propResourcePath = null;
    }
  }

  private URL resolveDefaultResourceURL(String pathInfo) {
    return Activator.getDefault().getBundle().getResource(new Path("/resources/html/").append(pathInfo).toPortableString());
  }
}
