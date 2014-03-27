/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.ui.json.servlet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.server.commons.servletfilter.HttpServletEx;
import org.eclipse.scout.rt.ui.json.JsonRendererFactory;
import org.eclipse.scout.rt.ui.json.JsonUIException;
import org.eclipse.scout.ui.html.Activator;

/**
 * Instances of this class must be registered as servlet root path "/"
 * <p>
 * The index.html is served as "/" or "/index.html" using HTTP GET
 * <p>
 * Ajax requests are processed as "/json" using HTTP POST
 */
public abstract class AbstractJsonServlet extends HttpServletEx implements IJsonSessionProvider, IResourceProvider {
  private static final long serialVersionUID = 1L;
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(AbstractJsonServlet.class);

  private final PostRequestHandler m_postRequestHandler;
  private final List<IHttpRequestInterceptor> m_getRequestInterceptors;
  private final List<IHttpRequestInterceptor> m_postRequestInterceptors;
  private final List<ResourceHandler> m_resourceHandlers;

  protected AbstractJsonServlet() {
    m_resourceHandlers = new ArrayList<ResourceHandler>();
    m_getRequestInterceptors = new ArrayList<IHttpRequestInterceptor>();
    m_postRequestInterceptors = new ArrayList<IHttpRequestInterceptor>();

    m_postRequestHandler = createPostRequestHandler();
    initResourceHandlers();
    initRequestInterceptors();

    JsonRendererFactory.init(createJsonRendererFactory());
  }

  protected void initRequestInterceptors() {
    m_getRequestInterceptors.add(new JavascriptDebugRequestInterceptor(this));

    injectRequestInterceptors(m_postRequestInterceptors, m_getRequestInterceptors);
  }

  protected void initResourceHandlers() {
    //Add handler to find web resources in bundle directories "WebContent"
    m_resourceHandlers.add(new ResourceHandler(Activator.getContext().getBundle(), "WebContent"));

    injectResourceHandlers(m_resourceHandlers);
  }

  protected void injectResourceHandlers(List<ResourceHandler> handlers) {
  }

  protected void injectRequestInterceptors(List<IHttpRequestInterceptor> getInterceptors, List<IHttpRequestInterceptor> postInterceptors) {
  }

  protected PostRequestHandler createPostRequestHandler() {
    return new PostRequestHandler(this);
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    LOG.info("POST request started.");

    //serve only /json
    String pathInfo = req.getPathInfo();
    if (pathInfo == null || !pathInfo.equals("/json")) {
      resp.sendError(HttpServletResponse.SC_NOT_FOUND);
      return;
    }

    try {
      if (m_postRequestInterceptors != null) {
        for (IHttpRequestInterceptor handler : m_postRequestInterceptors) {
          if (handler.beforePost(req, resp)) {
            return;
          }
        }
      }

      m_postRequestHandler.handle(req, resp);
    }
    catch (Exception e) {
      LOG.error("Exception while processing post request", e);
    }
    finally {
      LOG.info("POST request finished.");
    }
  }

  public JsonRendererFactory createJsonRendererFactory() throws JsonUIException {
    return new JsonRendererFactory();
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    LOG.info("GET request started.");

    String pathInfo = req.getPathInfo();
    if (pathInfo == null || pathInfo.equals("")) {
      resp.sendRedirect(req.getRequestURI() + "/");
      return;
    }

    try {
      if (m_getRequestInterceptors != null) {
        for (IHttpRequestInterceptor handler : m_getRequestInterceptors) {
          if (handler.beforeGet(req, resp)) {
            return;
          }
        }
      }

      if (m_resourceHandlers != null) {
        for (ResourceHandler handler : m_resourceHandlers) {
          if (handler.handle(req, resp)) {
            return;
          }
        }
      }
      LOG.error("Requested file not found: " + pathInfo);
      resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
    }
    catch (Exception e) {
      LOG.error("Exception while processing get request", e);
    }
    finally {
      LOG.info("GET request finished.");
    }
  }

  @Override
  public List<ResourceHandler> getResourceHandlers() {
    return m_resourceHandlers;
  }

  public List<IHttpRequestInterceptor> getPostRequestInterceptors() {
    return m_postRequestInterceptors;
  }

  public List<IHttpRequestInterceptor> getGetRequestInterceptors() {
    return m_getRequestInterceptors;
  }

}
