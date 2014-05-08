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

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.server.commons.servletfilter.HttpServletEx;
import org.eclipse.scout.rt.ui.json.IJsonSession;
import org.eclipse.scout.rt.ui.json.JsonRendererFactory;
import org.eclipse.scout.rt.ui.json.JsonException;
import org.eclipse.scout.service.SERVICES;

/**
 * Instances of this class must be registered as servlet root path "/"
 * <p>
 * The index.html is served as "/" or "/index.html" using HTTP GET
 * <p>
 * Ajax requests are processed as "/json" using HTTP POST
 */
public abstract class AbstractJsonServlet extends HttpServletEx {
  private static final long serialVersionUID = 1L;
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(AbstractJsonServlet.class);

  protected AbstractJsonServlet() {
    JsonRendererFactory.init(createJsonRendererFactory());
  }

  public JsonRendererFactory createJsonRendererFactory() {
    return new JsonRendererFactory();
  }

  public abstract IJsonSession createJsonSession();

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    LOG.info("GET request started.");

    //The servlet is registered at '/'. To make relative urls work we need to make sure the request url has a trailing '/'.
    //It is not possible to just check for an empty pathInfo because the container returns "/" even if the user has not entered a '/' at the end.
    String contextPath = getServletContext().getContextPath();
    if (StringUtility.hasText(contextPath) && req.getRequestURI().endsWith(contextPath)) {
      resp.sendRedirect(req.getRequestURI() + "/");
      return;
    }

    try {
      for (IServletRequestInterceptor service : SERVICES.getServices(IServletRequestInterceptor.class)) {
        if (service.interceptGet(this, req, resp)) {
          return;
        }
      }

    }
    catch (Exception e) {
      LOG.error("Exception while processing get request", e);
    }
    finally {
      LOG.info("GET request finished.");
    }
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    LOG.info("POST request started.");

    try {
      for (IServletRequestInterceptor service : SERVICES.getServices(IServletRequestInterceptor.class)) {
        if (service.interceptPost(this, req, resp)) {
          return;
        }
      }
    }
    catch (Exception e) {
      LOG.error("Exception while processing post request", e);
    }
    finally {
      LOG.info("POST request finished.");
    }
  }

}
