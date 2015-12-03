/*******************************************************************************
 * Copyright (c) 2014-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.ui.html.json;

import java.io.EOFException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.util.IOUtility;
import org.eclipse.scout.rt.platform.util.StringUtility;
import org.eclipse.scout.rt.ui.html.UiException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Helper class to create JSON (JavaScript Object Notation) response objects and to read a JSON object from
 * {@link ServletRequest}, or to write a JSON object into {@link ServletResponse}.
 */
@ApplicationScoped
public class JsonRequestHelper {

  private static final Logger LOG = LoggerFactory.getLogger(JsonRequestHelper.class);

  /**
   * @return {@link JSONObject} to indicate that an unrecoverable failure occurred.
   */
  public JSONObject createUnrecoverableFailureResponse() {
    final JsonResponse response = new JsonResponse();
    response.markAsError(JsonResponse.ERR_UI_PROCESSING, "UI processing error");
    return response.toJson();
  }

  /**
   * @return {@link JSONObject} which represents an empty response.
   */
  public JSONObject createEmptyResponse() {
    return new JSONObject();
  }

  /**
   * @return {@link JSONObject} to indicate that the session expired.
   */
  public JSONObject createSessionTimeoutResponse() {
    final JsonResponse response = new JsonResponse();
    response.markAsError(JsonResponse.ERR_SESSION_TIMEOUT, "The session has expired, please reload the page.");
    return response.toJson();
  }

  /**
   * @return {@link JSONObject} to indicate that startup failed.
   */
  public JSONObject createStartupFailedResponse() {
    final JsonResponse response = new JsonResponse();
    response.markAsError(JsonResponse.ERR_STARTUP_FAILED, "Initialization failed");
    return response.toJson();
  }

  /**
   * @return {@link JSONObject} to respond to a ping request.
   */
  public JSONObject createPingResponse() {
    final JSONObject json = new JSONObject();
    json.put("pong", Boolean.TRUE);
    return json;
  }

  /**
   * @param redirectUrl
   *          optional, URL where to redirect the UI. If <code>null</code>, session is informed about session
   *          termination, but no redirection happens.
   * @return {@link JSONObject} to indicate that the session was terminated.
   */
  public JSONObject createSessionTerminatedResponse(final String redirectUrl) {
    final JSONObject json = new JSONObject();
    json.put("sessionTerminated", Boolean.TRUE);
    if (StringUtility.hasText(redirectUrl)) {
      json.put("redirectUrl", redirectUrl);
    }
    return json;
  }

  /**
   * Writes the given {@link JSONObject} into the given {@link ServletResponse}.
   */
  public void writeResponse(final ServletResponse servletResponse, final JSONObject jsonResponse) throws IOException {
    String jsonText = jsonResponse.toString();
    final byte[] data = jsonText.getBytes(StandardCharsets.UTF_8.name());
    if (LOG.isDebugEnabled() && !LOG.isTraceEnabled() && jsonText.length() > 10000) {
      // Truncate log output to not spam the log (and in case of eclipse to not make it freeze: https://bugs.eclipse.org/bugs/show_bug.cgi?id=175888)
      jsonText = jsonText.substring(0, 10000) + "...";
    }
    servletResponse.setContentLength(data.length);
    if (servletResponse.getContentType() == null) {
      servletResponse.setContentType("application/json");
    }
    servletResponse.setCharacterEncoding(StandardCharsets.UTF_8.name());
    try {
      servletResponse.getOutputStream().write(data);
    }
    catch (final EOFException e) {
      final StringBuilder sb = new StringBuilder("EOF - Client disconnected, cannot write response");
      if (LOG.isDebugEnabled()) {
        sb.append(": ").append(jsonText);
      }
      else {
        sb.append(" (").append(data.length).append(" bytes)");
      }
      LOG.warn(sb.toString());
      return;
    }
    if (LOG.isDebugEnabled()) {
      LOG.debug("Returned: " + jsonText);
    }
  }

  /**
   * Reads the content of {@link ServletRequest} into a {@link JSONObject}.
   */
  public JSONObject readJsonRequest(final ServletRequest servletRequest) {
    try {
      final String jsonData = IOUtility.getContent(servletRequest.getReader());
      if (LOG.isDebugEnabled()) {
        LOG.debug("Received: " + jsonData);
      }
      return (jsonData == null ? new JSONObject() : new JSONObject(jsonData));
    }
    catch (RuntimeException | IOException e) {
      throw new UiException(e.getMessage(), e);
    }
  }
}
