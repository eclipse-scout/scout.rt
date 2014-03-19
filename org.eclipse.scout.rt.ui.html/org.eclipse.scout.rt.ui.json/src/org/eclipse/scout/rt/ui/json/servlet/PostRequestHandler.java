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
import javax.servlet.http.HttpSession;

import org.eclipse.scout.commons.IOUtility;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.ui.json.IJsonSession;
import org.eclipse.scout.rt.ui.json.JsonRequest;
import org.eclipse.scout.rt.ui.json.JsonResponse;
import org.eclipse.scout.rt.ui.json.JsonUIException;
import org.json.JSONException;
import org.json.JSONObject;

public class PostRequestHandler {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(PostRequestHandler.class);
  private IJsonSessionProvider m_jsonSessionProvider;

  public PostRequestHandler(IJsonSessionProvider jsonSessionProvider) {
    m_jsonSessionProvider = jsonSessionProvider;
  }

  public void handle(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    JsonRequest uiReq = new JsonRequest(toJSON(req));
    String sessionAttributeName = "JsonUi#" + uiReq.getSessionPartId();
    HttpSession httpSession = req.getSession();

    //FIXME really synchronize on this? blocks every call, maybe introduce a lock object saved on httpSession?
    IJsonSession jsonSession = null;
    synchronized (this) {
      jsonSession = (IJsonSession) httpSession.getAttribute(sessionAttributeName);
      if (jsonSession == null) {
        //FIXME reload must NOT create a new session, maybe we need to store sessionpartId in cookie or local http cache??
        jsonSession = m_jsonSessionProvider.createJsonSession();
        jsonSession.init(req, uiReq.getSessionPartId());
        httpSession.setAttribute(sessionAttributeName, jsonSession);
      }
    }

    //GUI requests for the same session must be processed consecutively
    synchronized (jsonSession) {
      JsonResponse uiRes = jsonSession.processRequest(req, uiReq);
      String jsonText = uiRes.toJson().toString();
      byte[] data = jsonText.getBytes("UTF-8");
      resp.setContentLength(data.length);
      resp.setContentType("application/json");
      resp.setCharacterEncoding("UTF-8");
      resp.getOutputStream().write(data);

      LOG.debug("Returned: " + jsonText);
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

}
