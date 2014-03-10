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

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.eclipse.scout.commons.IOUtility;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.server.commons.servletfilter.HttpServletEx;
import org.eclipse.scout.rt.ui.json.IJsonSession;
import org.eclipse.scout.rt.ui.json.JsonRequest;
import org.eclipse.scout.rt.ui.json.JsonResponse;
import org.eclipse.scout.rt.ui.json.JsonUIException;
import org.eclipse.scout.ui.html.Activator;
import org.json.JSONException;
import org.json.JSONObject;

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

  private final ArrayList<ResourceHandler> m_resourceHandlers;
  private final JavascriptDebugResourceInterceptor m_jsInterceptor;

  protected AbstractJsonServlet() {
    m_resourceHandlers = new ArrayList<ResourceHandler>();
    m_jsInterceptor = new JavascriptDebugResourceInterceptor(m_resourceHandlers);
    getResourceHandlers().add(new ResourceHandler(Activator.getContext().getBundle(), "WebContent"));
  }

  /**
   * @return the modifiable list of resource handlers used to find web resources in bundle directories "WebContent"
   */
  public ArrayList<ResourceHandler> getResourceHandlers() {
    return m_resourceHandlers;
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    //serve only /json
    String pathInfo = req.getPathInfo();
    if (pathInfo == null || !pathInfo.equals("/json")) {
      resp.sendError(HttpServletResponse.SC_NOT_FOUND);
      return;
    }

    LOG.info("POST request started.");
    try {
      JsonRequest uiReq = new JsonRequest(toJSON(req));
      String sessionAttributeName = "JsonUi#" + uiReq.getSessionPartId();
      HttpSession httpSession = req.getSession();
      //FIXME reload must NOT create a new session, maybe we need to store sessionpartId in cookie or local http cache??
      IJsonSession jsonSession = (IJsonSession) httpSession.getAttribute(sessionAttributeName);
      if (jsonSession == null) {
        jsonSession = createJsonSession();
        jsonSession.init(req, uiReq.getSessionPartId());
        httpSession.setAttribute(sessionAttributeName, jsonSession);
      }
      JsonResponse uiRes = jsonSession.processRequest(req, uiReq);
      String jsonText = uiRes.toJson().toString();
      byte[] data = jsonText.getBytes("UTF-8");
      resp.setContentLength(data.length);
      resp.setContentType("application/json; charset=utf-8");
      resp.getOutputStream().write(data);

      LOG.debug("Returning: " + jsonText);
    }
    catch (Exception e) {
      LOG.error("Exception while processing post request", e);
      resp.getWriter().print("ERROR: " + e.getMessage());
    }
    finally {
      LOG.info("POST request finished.");
    }
  }

  protected JSONObject toJSON(HttpServletRequest req) throws JsonUIException {
    try {
      String jsonData = IOUtility.getContent(req.getReader());
      LOG.debug("Received: " + jsonData);

      if (StringUtility.isNullOrEmpty(jsonData)) {
        jsonData = "{}"; // FIXME
      }
      return new JSONObject(jsonData);
    }
    catch (ProcessingException | IOException | JSONException e) {
      throw new JsonUIException(e.getMessage(), e);
    }
  }

  protected abstract IJsonSession createJsonSession() throws JsonUIException;

  /**
   * default doGet returns resources based on the {@link ResourceHandler} list
   */
  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    String pathInfo = req.getPathInfo();
    if (m_jsInterceptor.handle(req, resp, pathInfo)) {
      return;
    }
    if (pathInfo == null || pathInfo.equals("")) {
      resp.sendRedirect(req.getRequestURI() + "/");
      return;
    }
    System.err.println("NOT_FOUND " + pathInfo);
    resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
    return;
  }

}
