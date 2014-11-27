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
import org.eclipse.scout.commons.annotations.Priority;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.ui.html.json.IJsonSession;
import org.eclipse.scout.rt.ui.html.json.JsonException;
import org.eclipse.scout.rt.ui.html.json.JsonRequest;
import org.eclipse.scout.rt.ui.html.json.JsonResponse;
import org.eclipse.scout.rt.ui.html.json.JsonStartupRequest;
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
    IServletResourceProvider[] providers = SERVICES.getServices(IServletResourceProvider.class);
    for (IServletResourceProvider provider : providers) {
      if (provider.handle(servlet, req, resp)) {
        return true;
      }
    }
    LOG.error("Requested file not found: " + req.getPathInfo());
    resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
    resp.getOutputStream().print("Not Found: " + req.getPathInfo());
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

    JsonRequest jsonReq = new JsonRequest(toJSON(req));
    if (jsonReq.isPingRequest()) {
      writeResponse(resp, createPingJsonResponse().toJson());
      return true;
    }

    String jsonSessionAttributeName = "scout.htmlui.session.json." + jsonReq.getJsonSessionId();
    HttpSession httpSession = req.getSession();

    //FIXME cgu really synchronize on this? blocks every call, maybe introduce a lock object saved on httpSession or even better use java.util.concurrent.locks.ReadWriteLock
    IJsonSession jsonSession = null;
    synchronized (httpSession) {
      jsonSession = (IJsonSession) httpSession.getAttribute(jsonSessionAttributeName);

      if (jsonReq.isUnloadRequest()) {
        LOG.info("Unloading JSON session with ID " + jsonReq.getJsonSessionId() + " (requested by UI)");
        if (jsonSession != null) {
          jsonSession.dispose();
          httpSession.removeAttribute(jsonSessionAttributeName);
        }
        return true;
      }

      if (jsonSession == null) {
        if (!jsonReq.isStartupRequest()) {
          LOG.info("Request cannot be processed due to JSON session timeout [id=" + jsonReq.getJsonSessionId() + "]");
          writeError(resp, createSessionTimeoutJsonResponse());
          return true;
        }
        LOG.info("Creating new JSON session with ID " + jsonReq.getJsonSessionId() + "...");
        jsonSession = servlet.createUninitializedJsonSession();
        jsonSession.init(req, new JsonStartupRequest(jsonReq));
        httpSession.setAttribute(jsonSessionAttributeName, jsonSession);
      }
      else if (jsonReq.isStartupRequest()) {
        throw new IllegalStateException("Startup requested for existing JSON session with ID " + jsonReq.getJsonSessionId());
      }
    }

    // GUI requests for the same session must be processed consecutively
    synchronized (jsonSession) {
      JSONObject json = jsonSession.processRequest(req, jsonReq);
      writeResponse(resp, json);
    }
    return true;
  }

  protected JsonResponse createSessionTimeoutJsonResponse() {
    JsonResponse jsonResp = new JsonResponse();
    jsonResp.setErrorCode(JsonResponse.ERR_SESSION_TIMEOUT);
    jsonResp.setErrorMessage("The session has expired, please reload the page."); // FIXME use TEXTS
    return jsonResp;
  }

  protected JsonResponse createPingJsonResponse() {
    return new JsonResponse();
  }

  protected void writeResponse(HttpServletResponse resp, JSONObject json) throws IOException {
    String jsonText = json.toString();
    byte[] data = jsonText.getBytes("UTF-8");
    resp.setContentLength(data.length);
    resp.setContentType("application/json");
    resp.setCharacterEncoding("UTF-8");
    resp.getOutputStream().write(data);
    if (LOG.isDebugEnabled()) {
      LOG.debug("Returned: " + jsonText);
    }
  }

  protected void writeError(HttpServletResponse resp, JsonResponse jsonResp) throws IOException {
    resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    writeResponse(resp, jsonResp.toJson());
  }

  protected JSONObject toJSON(HttpServletRequest req) {
    try {
      String jsonData = IOUtility.getContent(req.getReader());
      if (LOG.isDebugEnabled()) {
        LOG.debug("Received: " + jsonData);
      }
      return (jsonData == null ? new JSONObject() : new JSONObject(jsonData));
//      if (StringUtility.isNullOrEmpty(jsonData)) {
//        jsonData = "{}"; // FIXME
//      }
//      return new JSONObject(jsonData);
    }
    catch (ProcessingException | IOException | JSONException e) {
      throw new JsonException(e.getMessage(), e);
    }
  }
}
