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

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextAttributeEvent;
import javax.servlet.ServletContextAttributeListener;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.eclipse.scout.commons.TypeCastUtility;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.jaxws216.internal.resources.BundleProxyClassLoader;
import org.eclipse.scout.jaxws216.internal.resources.BundleProxyResourceLoader;
import org.eclipse.scout.jaxws216.internal.resources.SunJaxWsXml;
import org.eclipse.scout.jaxws216.internal.resources.SunJaxWsXmlFinder;

import com.sun.xml.internal.ws.api.server.Container;
import com.sun.xml.internal.ws.resources.WsservletMessages;
import com.sun.xml.internal.ws.transport.http.DeploymentDescriptorParser;

public final class JaxWsServletContextListener implements ServletContextAttributeListener, ServletContextListener {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(JaxWsServletContextListener.class);

  @Override
  public void attributeAdded(ServletContextAttributeEvent servletcontextattributeevent) {
  }

  @Override
  public void attributeRemoved(ServletContextAttributeEvent servletcontextattributeevent) {
  }

  @Override
  public void attributeReplaced(ServletContextAttributeEvent servletcontextattributeevent) {
  }

  @Override
  @SuppressWarnings("unchecked")
  public void contextInitialized(ServletContextEvent event) {
    ServletContext context = event.getServletContext();

    List<ServletAdapter> servletAdapters = new ArrayList<ServletAdapter>();
    for (SunJaxWsXml cfg : new SunJaxWsXmlFinder().findAll()) {
      try {
        URL sunJaxWsXml = cfg.getResource();
        ClassLoader classLoader = new BundleProxyClassLoader(cfg.getBundle());
        ClassLoader oldContextCL = Thread.currentThread().getContextClassLoader();
        try {
          Thread.currentThread().setContextClassLoader(classLoader);

          DeploymentDescriptorParser<ServletAdapter> parser = new DeploymentDescriptorParser(classLoader, new BundleProxyResourceLoader(cfg.getBundle()), createContainer(context), new ServletAdapterFactory());
          List<ServletAdapter> adaptersInBundle = parser.parse(sunJaxWsXml.toExternalForm(), sunJaxWsXml.openStream());

          if (adaptersInBundle != null) {
            servletAdapters.addAll(adaptersInBundle);
          }
        }
        finally {
          Thread.currentThread().setContextClassLoader(oldContextCL);
        }
      }
      catch (Throwable t) {
        LOG.error(WsservletMessages.LISTENER_PARSING_FAILED(t) + " for bundle '" + cfg.getBundle().getSymbolicName() + "'", t);
      }
    }

    // install adapters
    context.setAttribute(EndpointServlet.JAXWS_RI_ADAPTERS, servletAdapters.toArray(new ServletAdapter[servletAdapters.size()])); // adapters are installed by {@link JaxWsHttpServlet}
  }

  @Override
  public void contextDestroyed(ServletContextEvent event) {
    Object adapters = event.getServletContext().getAttribute(EndpointServlet.JAXWS_RI_ADAPTERS);
    if (adapters == null) {
      return;
    }
    // dispose servlet adapters
    ServletAdapter[] servletAdapters = TypeCastUtility.castValue(adapters, ServletAdapter[].class);
    for (ServletAdapter servletAdapter : servletAdapters) {
      try {
        servletAdapter.getEndpoint().dispose();
      }
      catch (Throwable t) {
        LOG.error("failed to dispose webservice endpoint", t);
      }
    }
  }

  protected Container createContainer(ServletContext context) {
    return new ServletContainer(context);
  }
}
