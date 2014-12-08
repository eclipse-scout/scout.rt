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
package org.eclipse.scout.rt.ui.html.json;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.eclipse.scout.commons.CompareUtility;
import org.eclipse.scout.commons.IOUtility;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.ui.html.AbstractRequestHandler;
import org.eclipse.scout.rt.ui.html.AbstractScoutAppServlet;
import org.eclipse.scout.rt.ui.html.cache.HttpCacheInfo;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Handle HTTP POST requests containing a JSON message
 */
public class JsonMessageHandler extends AbstractRequestHandler {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(JsonMessageHandler.class);

  public JsonMessageHandler(AbstractScoutAppServlet servlet, HttpServletRequest req, HttpServletResponse resp, String pathInfo) {
    super(servlet, req, resp, pathInfo);
  }

  @Override
  public boolean handle() throws ServletException, IOException {
    //serve only /json
    String pathInfo = getPathInfo();
    if (CompareUtility.notEquals(pathInfo, "/json")) {
      getHttpServletResponse().sendError(HttpServletResponse.SC_NOT_FOUND);
      return true;
    }

    //disable cache
    HttpCacheInfo info = new HttpCacheInfo(-1, -1, -1);
    getServlet().getHttpCacheControl().disableCacheHeaders(getHttpServletRequest(), getHttpServletResponse(), info);

    JsonRequest jsonReq = new JsonRequest(decodeJSONRequest());
    if (jsonReq.isPingRequest()) {
      writeResponse(createPingJsonResponse().toJson());
      return true;
    }

    IJsonSession jsonSession = getOrCreateJsonSession(jsonReq);
    if (jsonSession == null) {
      return true;
    }

    // GUI requests for the same session must be processed consecutively
    synchronized (jsonSession) {
      JSONObject json = jsonSession.processRequest(getHttpServletRequest(), jsonReq);
      writeResponse(json);
    }
    return true;
  }

  protected IJsonSession getOrCreateJsonSession(JsonRequest jsonReq) throws ServletException, IOException {
    String jsonSessionAttributeName = "scout.htmlui.session.json." + jsonReq.getJsonSessionId();
    HttpSession httpSession = getHttpServletRequest().getSession();

    //FIXME cgu really synchronize on this? blocks every call, maybe introduce a lock object saved on httpSession or even better use java.util.concurrent.locks.ReadWriteLock
    synchronized (httpSession) {
      IJsonSession jsonSession = (IJsonSession) httpSession.getAttribute(jsonSessionAttributeName);

      if (jsonReq.isUnloadRequest()) {
        LOG.info("Unloading JSON session with ID " + jsonReq.getJsonSessionId() + " (requested by UI)");
        if (jsonSession != null) {
          jsonSession.dispose();
          httpSession.removeAttribute(jsonSessionAttributeName);
        }
        return null;
      }

      if (jsonSession == null) {
        if (!jsonReq.isStartupRequest()) {
          LOG.info("Request cannot be processed due to JSON session timeout [id=" + jsonReq.getJsonSessionId() + "]");
          writeError(createSessionTimeoutJsonResponse());
          return null;
        }
        LOG.info("Creating new JSON session with ID " + jsonReq.getJsonSessionId() + "...");
        jsonSession = getServlet().createJsonSession();
        jsonSession.init(getHttpServletRequest(), new JsonStartupRequest(jsonReq));
        httpSession.setAttribute(jsonSessionAttributeName, jsonSession);
      }
      else if (jsonReq.isStartupRequest()) {
        throw new IllegalStateException("Startup requested for existing JSON session with ID " + jsonReq.getJsonSessionId());
      }
      return jsonSession;
    }
  }

  protected JsonResponse createSessionTimeoutJsonResponse() {
    JsonResponse jsonResp = new JsonResponse();
    jsonResp.setErrorCode(JsonResponse.ERR_SESSION_TIMEOUT);
    jsonResp.setErrorMessage("The session has expired, please reload the page."); // will be translated in client, see Session.js
    return jsonResp;
  }

  protected JsonResponse createPingJsonResponse() {
    return new JsonResponse();
  }

  protected void writeResponse(JSONObject json) throws IOException {
    HttpServletResponse resp = getHttpServletResponse();
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

  protected void writeError(JsonResponse jsonResp) throws IOException {
    getHttpServletResponse().setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    writeResponse(jsonResp.toJson());
  }

  protected JSONObject decodeJSONRequest() {
    try {
      String jsonData = IOUtility.getContent(getHttpServletRequest().getReader());
      if (LOG.isDebugEnabled()) {
        LOG.debug("Received: " + jsonData);
      }
      return (jsonData == null ? new JSONObject() : new JSONObject(jsonData));
    }
    catch (ProcessingException | IOException | JSONException e) {
      throw new JsonException(e.getMessage(), e);
    }
  }

}
