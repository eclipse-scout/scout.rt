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
package org.eclipse.scout.rt.ui.html.json.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.eclipse.scout.commons.IOUtility;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.commons.annotations.Priority;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.ui.html.json.IJsonSession;
import org.eclipse.scout.rt.ui.html.json.JsonException;
import org.eclipse.scout.rt.ui.html.json.JsonRequest;
import org.eclipse.scout.rt.ui.html.json.JsonResponse;
import org.eclipse.scout.service.AbstractService;
import org.eclipse.scout.service.SERVICES;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * This interceptor contributes to the {@link AbstractJsonServlet} as the default interceptor
 */
@Priority(-100)
public class MainRequestInterceptor extends AbstractService implements IServletRequestInterceptor {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(MainRequestInterceptor.class);

  @Override
  public boolean interceptGet(AbstractJsonServlet servlet, HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    //delegate to static resources
    for (IServletResourceProvider provider : SERVICES.getServices(IServletResourceProvider.class)) {
      if (provider.handle(servlet, req, resp)) {
        return true;
      }
    }
    LOG.error("Requested file not found: " + req.getPathInfo());
    resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
    return true;
  }

  @Override
  public boolean interceptPost(AbstractJsonServlet servlet, HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    //serve only /json
    String pathInfo = req.getPathInfo();
    if (pathInfo == null || !pathInfo.equals("/json")) {
      resp.sendError(HttpServletResponse.SC_NOT_FOUND);
      return true;
    }

    JsonRequest uiReq = new JsonRequest(toJSON(req));
    String sessionAttributeName = "JsonUi#" + uiReq.getSessionPartId();
    HttpSession httpSession = req.getSession();

    //FIXME really synchronize on this? blocks every call, maybe introduce a lock object saved on httpSession?
    IJsonSession jsonSession = null;
    synchronized (this) {
      jsonSession = (IJsonSession) httpSession.getAttribute(sessionAttributeName);
      if (jsonSession == null) {
        if (!uiReq.isStartupRequest()) {
          LOG.info("Request cannot be processed due to session timeout.");
          writeError(resp, createSessionTimeoutJsonResponse());
          return true;
        }

        LOG.info("Creating new json session " + sessionAttributeName + "...");

        //FIXME reload must NOT create a new session, maybe we need to store sessionpartId in cookie or local http cache??
        jsonSession = servlet.createJsonSession();
        jsonSession.init(req, uiReq);
        httpSession.setAttribute(sessionAttributeName, jsonSession);
      }
    }

    //GUI requests for the same session must be processed consecutively
    synchronized (jsonSession) {
      JsonResponse uiRes = jsonSession.processRequest(req, uiReq);
      writeResponse(resp, uiRes);
    }
    return true;
  }

  protected JsonResponse createSessionTimeoutJsonResponse() {
    JsonResponse uiResp = new JsonResponse();
    uiResp.setErrorCode(JsonResponse.ERR_SESSION_TIMEOUT);
    uiResp.setErrorMessage("The session has expired, please reload the page."); //FIXME use TEXTS
    return uiResp;
  }

  protected void writeResponse(HttpServletResponse resp, JsonResponse uiRes) throws IOException {
    String jsonText = uiRes.toJson().toString();
    byte[] data = jsonText.getBytes("UTF-8");
    resp.setContentLength(data.length);
    resp.setContentType("application/json");
    resp.setCharacterEncoding("UTF-8");
    resp.getOutputStream().write(data);

    LOG.debug("Returned: " + jsonText);
  }

  protected void writeError(HttpServletResponse resp, JsonResponse uiRes) throws IOException {
    resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    writeResponse(resp, uiRes);
  }

  protected JSONObject toJSON(HttpServletRequest req) {
    try {
      String jsonData = IOUtility.getContent(req.getReader());
      LOG.debug("Received: " + jsonData);

      if (StringUtility.isNullOrEmpty(jsonData)) {
        jsonData = "{}"; // FIXME
      }
      return new JSONObject(jsonData);
    }
    catch (ProcessingException | IOException | JSONException e) {
      throw new JsonException(e.getMessage(), e);
    }
  }

}
