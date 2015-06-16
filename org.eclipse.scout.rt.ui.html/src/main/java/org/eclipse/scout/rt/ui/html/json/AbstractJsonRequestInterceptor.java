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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.scout.commons.IOUtility;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.ui.html.IServletRequestInterceptor;
import org.eclipse.scout.rt.ui.html.UiException;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Base class for request interceptors dealing with JSON messages.
 */
public abstract class AbstractJsonRequestInterceptor implements IServletRequestInterceptor {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(AbstractJsonRequestInterceptor.class);

  protected JSONObject createUnrecoverableFailureResponse() {
    JsonResponse response = new JsonResponse();
    response.markAsError(JsonResponse.ERR_UI_PROCESSING, "UI processing error");
    return response.toJson();
  }

  protected JSONObject createEmptyResponse() {
    return new JSONObject();
  }

  protected JSONObject createSessionTimeoutResponse() {
    JsonResponse response = new JsonResponse();
    response.markAsError(JsonResponse.ERR_SESSION_TIMEOUT, "The session has expired, please reload the page.");
    return response.toJson();
  }

  protected JSONObject createStartupFailedResponse() {
    JsonResponse response = new JsonResponse();
    response.markAsError(JsonResponse.ERR_STARTUP_FAILED, "Initialization failed");
    return response.toJson();
  }

  protected JSONObject createPingResponse() {
    JSONObject json = new JSONObject();
    json.put("pong", Boolean.TRUE);
    return json;
  }

  protected JSONObject createSessionTerminatedResponse() {
    JSONObject json = new JSONObject();
    json.put("sessionTerminated", Boolean.TRUE);
    return json;
  }

  protected void writeResponse(HttpServletResponse res, JsonResponse response) throws IOException {
    writeResponse(res, response.toJson());
  }

  protected void writeResponse(HttpServletResponse httpResp, JSONObject jsonResp) throws IOException {
    String jsonText = jsonResp.toString();
    byte[] data = jsonText.getBytes("UTF-8");
    httpResp.setContentLength(data.length);
    httpResp.setContentType("application/json");
    httpResp.setCharacterEncoding("UTF-8");
    httpResp.getOutputStream().write(data);
    if (LOG.isTraceEnabled()) {
      LOG.trace("Returned: " + jsonText);
    }
    else if (LOG.isDebugEnabled()) {
      // Truncate log output to not spam the log (and in case of eclipse to not make it freeze: https://bugs.eclipse.org/bugs/show_bug.cgi?id=175888)
      if (jsonText.length() > 10000) {
        jsonText = jsonText.substring(0, 10000) + "...";
      }
      LOG.debug("Returned: " + jsonText);
    }
  }

  protected JSONObject decodeJSONRequest(HttpServletRequest req) {
    try {
      String jsonData = IOUtility.getContent(req.getReader());
      if (LOG.isDebugEnabled()) {
        LOG.debug("Received: " + jsonData);
      }
      return (jsonData == null ? new JSONObject() : new JSONObject(jsonData));
    }
    catch (ProcessingException | IOException | JSONException e) {
      throw new UiException(e.getMessage(), e);
    }
  }
}
