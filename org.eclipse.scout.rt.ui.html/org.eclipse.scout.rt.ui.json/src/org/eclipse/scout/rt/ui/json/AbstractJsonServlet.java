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
package org.eclipse.scout.rt.ui.json;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.eclipse.scout.commons.IOUtility;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.json.JSONException;
import org.json.JSONObject;

public abstract class AbstractJsonServlet extends HttpServlet {

  private static final long serialVersionUID = 1L;
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(AbstractJsonServlet.class);

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    LOG.info("POST request started.");
    try {
      JsonRequest uiReq = new JsonRequest(toJSON(req));
      String sessionAttributeName = "JsonUi#" + uiReq.getSessionPartId();
      HttpSession httpSession = req.getSession();
      IJsonSession jsonSession = (IJsonSession) httpSession.getAttribute(sessionAttributeName);
      if (jsonSession == null) {
        jsonSession = createJsonSession();
        jsonSession.init();
        httpSession.setAttribute(sessionAttributeName, jsonSession);
      }
      JsonResponse uiRes = jsonSession.processRequest(uiReq);
      String data = uiRes.toJson().toString();

      resp.setContentLength(data.length());
      resp.setContentType("application/json");
      resp.getOutputStream().print(data);

      LOG.debug("Returning: " + data);
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
        jsonData = "{}"; // TODO
      }
      return new JSONObject(jsonData);
    }
    catch (ProcessingException | IOException | JSONException e) {
      throw new JsonUIException(e.getMessage(), e);
    }
  }

  protected abstract IJsonSession createJsonSession() throws JsonUIException;

  /**
   * default doGet returns the index.html that is assumed to be besides the projects JsonServlet.java
   */
  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    URL url = getClass().getResource("index.html");
    LOG.info("GET request started. Returning " + url);
    try {
      InputStream is = url.openStream();
      String html = new String(IOUtility.getContent(is));
      resp.setContentType("text/html");
      resp.getOutputStream().print(html);
    }
    catch (Exception e) {
      LOG.error("Exception while processing post request", e);
      resp.getWriter().print("ERROR: " + e.getMessage());
    }
    finally {
      LOG.info("GET request finished.");
    }
  }

}
