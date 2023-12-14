/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.ui.html.json;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;

import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.platform.util.ConnectionErrorDetector;
import org.eclipse.scout.rt.platform.util.IOUtility;
import org.eclipse.scout.rt.platform.util.StringUtility;
import org.eclipse.scout.rt.platform.util.concurrent.ThreadInterruption;
import org.eclipse.scout.rt.platform.util.concurrent.ThreadInterruption.IRestorer;
import org.eclipse.scout.rt.ui.html.UiException;
import org.json.JSONArray;
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
    return createUnrecoverableFailureResponse(null);
  }

  /**
   * Same as {@link #createUnrecoverableFailureResponse()} but assign the given sequenceNo to the response. This is
   * useful when an error happens during handling of a specific response. By passing the sequenceNo to the UI, the
   * response queue order can be kept consistent.
   */
  public JSONObject createUnrecoverableFailureResponse(Long sequenceNo) {
    final JsonResponse response = new JsonResponse(sequenceNo);
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
   * UI Text <code>ui.UnsafeUpload</code>
   *
   * @return {@link JSONObject} to indicate that the file upload is unsafe.
   */
  public JSONObject createUnsafeUploadResponse() {
    final JsonResponse response = new JsonResponse();
    response.markAsError(JsonResponse.ERR_UNSAFE_UPLOAD, "Unsafe file upload.");
    return response.toJson();
  }

  /**
   * UI Text <code>ui.RejectedUpload</code>
   *
   * @return {@link JSONObject} to indicate that the file upload was rejected.
   */
  public JSONObject createRejectedUploadResponse() {
    final JsonResponse response = new JsonResponse();
    response.markAsError(JsonResponse.ERR_REJECTED_UPLOAD, "Rejected file upload.");
    return response.toJson();
  }

  /**
   * @return {@link JSONObject} to indicate that the session expired.
   */
  public JSONObject createSessionTimeoutResponse() {
    final JsonResponse response = new JsonResponse();
    response.markAsError(JsonResponse.ERR_SESSION_TIMEOUT, "The session has expired, please reload the page.");
    // Implementation note - The same JSON message is also generated here:
    // org.eclipse.scout.rt.server.commons.authentication.ServletFilterHelper.sendJsonSessionTimeout(HttpServletResponse)
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
   * @return {@link JSONObject} to indicate that the UI is running with an older version of the code and the page has to
   *         be reloaded.
   */
  public JSONObject createVersionMismatchResponse() {
    final JsonResponse response = new JsonResponse();
    response.markAsError(JsonResponse.ERR_VERSION_MISMATCH, "Version mismatch");
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
    final byte[] data = jsonText.getBytes(StandardCharsets.UTF_8);
    servletResponse.setContentLength(data.length);
    if (servletResponse.getContentType() == null) {
      servletResponse.setContentType("application/json");
    }
    servletResponse.setCharacterEncoding(StandardCharsets.UTF_8.name());

    // Clear the current thread's interruption status before writing the response to the output stream.
    // Otherwise, the stream gets silently corrupted, which makes the client to loose the connection.
    IRestorer interruption = ThreadInterruption.clear();
    try {
      servletResponse.getOutputStream().write(data);
    }
    catch (final Exception e) {
      if (BEANS.get(ConnectionErrorDetector.class).isConnectionError(e)) {
        // Ignore disconnect errors: we do not want to throw an exception, if the client closed the connection.
        LOG.debug("Connection Error: ", e);
        return;
      }
      throw e;
    }
    finally {
      interruption.restore();
    }
    if (LOG.isTraceEnabled()) {
      LOG.trace("Returned: {}", formatJsonForLogging(jsonText));
    }
    else if (LOG.isDebugEnabled()) {
      LOG.debug("Returned: {}", formatJsonResponseForLogging(jsonResponse));
    }
  }

  /**
   * Reads the content of {@link ServletRequest} into a {@link JSONObject}.
   */
  public JSONObject readJsonRequest(final ServletRequest servletRequest) {
    try (Reader in = servletRequest.getReader()) {
      final String jsonData = IOUtility.readString(in);
      if (LOG.isTraceEnabled()) { // log before json parsing (in case parsing fails)
        LOG.trace("Received: {}", formatJsonForLogging(jsonData));
      }
      JSONObject jsonRequest = (jsonData == null ? new JSONObject() : new JSONObject(jsonData));
      if (LOG.isDebugEnabled() && !LOG.isTraceEnabled()) {
        LOG.debug("Received: {}", formatJsonRequestForLogging(jsonRequest));
      }
      return jsonRequest;
    }
    catch (RuntimeException | IOException e) {
      throw new UiException(e.getMessage(), e);
    }
  }

  protected String formatJsonForLogging(String jsonText) {
    if (LOG.isDebugEnabled() && !LOG.isTraceEnabled() && jsonText != null && jsonText.length() > 10000) {
      // Truncate log output to not spam the log (and in case of eclipse to not make it freeze: https://bugs.eclipse.org/bugs/show_bug.cgi?id=175888)
      return jsonText.substring(0, 10000) + "...";
    }
    return jsonText;
  }

  /**
   * @return the given json response formatted as string without user data (safe for logging)
   */
  protected String formatJsonResponseForLogging(JSONObject jsonResponse) {
    List<String> list = new ArrayList<>();
    if (jsonResponse.has(JsonResponse.PROP_SEQUENCE_NO)) {
      list.add("#" + jsonResponse.get(JsonResponse.PROP_SEQUENCE_NO));
    }
    if (jsonResponse.has(JsonResponse.PROP_COMBINED)) {
      list.add("combined: " + jsonResponse.get(JsonResponse.PROP_COMBINED));
    }
    if (jsonResponse.has(JsonResponse.PROP_ERROR)) {
      list.add("error: " + jsonResponse.get(JsonResponse.PROP_ERROR));
    }
    if (jsonResponse.has(JsonResponse.PROP_STARTUP_DATA)) {
      JSONObject o = jsonResponse.optJSONObject(JsonResponse.PROP_STARTUP_DATA);
      list.add("startupData: " + (o == null ? "(invalid)" : "{" + o.length() + "}"));
    }
    if (jsonResponse.has(JsonResponse.PROP_EVENTS)) {
      JSONArray a = jsonResponse.optJSONArray(JsonResponse.PROP_EVENTS);
      list.add("events: " + (a == null ? "(invalid)" : formatEventTypesForLogging(a)));
    }
    if (jsonResponse.has(JsonResponse.PROP_ADAPTER_DATA)) {
      JSONObject o = jsonResponse.optJSONObject(JsonResponse.PROP_ADAPTER_DATA);
      list.add("adapterData: " + (o == null ? "(invalid)" : "{" + o.length() + "}"));
    }
    return CollectionUtility.format(list);
  }

  /**
   * @return the given json request formatted as string without personal data or user input (safe for logging)
   */
  protected String formatJsonRequestForLogging(JSONObject jsonRequest) {
    List<String> list = new ArrayList<>();
    if (jsonRequest.has(JsonRequest.PROP_SEQUENCE_NO)) {
      list.add("#" + jsonRequest.get(JsonRequest.PROP_SEQUENCE_NO));
    }
    if (jsonRequest.has(JsonRequest.PROP_ACK_SEQUENCE_NO)) {
      list.add("#ACK: " + jsonRequest.get(JsonRequest.PROP_ACK_SEQUENCE_NO));
    }
    if (jsonRequest.has(JsonRequest.PROP_STARTUP)) {
      list.add(JsonRequest.PROP_STARTUP + ": " + jsonRequest.get(JsonRequest.PROP_STARTUP));
    }
    if (jsonRequest.has(JsonRequest.PROP_UNLOAD)) {
      list.add(JsonRequest.PROP_UNLOAD + ": " + jsonRequest.get(JsonRequest.PROP_UNLOAD));
    }
    if (jsonRequest.has(JsonRequest.PROP_LOG)) {
      list.add(JsonRequest.PROP_LOG + ": " + jsonRequest.get(JsonRequest.PROP_LOG));
    }
    if (jsonRequest.has(JsonRequest.PROP_POLL)) {
      list.add(JsonRequest.PROP_POLL + ": " + jsonRequest.get(JsonRequest.PROP_POLL));
    }
    if (jsonRequest.has(JsonRequest.PROP_CANCEL)) {
      list.add(JsonRequest.PROP_CANCEL + ": " + jsonRequest.get(JsonRequest.PROP_CANCEL));
    }
    if (jsonRequest.has(JsonRequest.PROP_PING)) {
      list.add(JsonRequest.PROP_PING + ": " + jsonRequest.get(JsonRequest.PROP_PING));
    }
    if (jsonRequest.has(JsonRequest.PROP_SYNC_RESPONSE_QUEUE)) {
      list.add(JsonRequest.PROP_SYNC_RESPONSE_QUEUE + ": " + jsonRequest.get(JsonRequest.PROP_SYNC_RESPONSE_QUEUE));
    }
    if (jsonRequest.has(JsonRequest.PROP_EVENTS)) {
      JSONArray a = jsonRequest.optJSONArray(JsonRequest.PROP_EVENTS);
      list.add("events: " + (a == null ? "(invalid)" : formatEventTypesForLogging(a)));
    }
    if (jsonRequest.has(JsonRequest.PROP_UI_SESSION_ID)) {
      list.add("uiSessionId: " + jsonRequest.get(JsonRequest.PROP_UI_SESSION_ID));
    }
    return CollectionUtility.format(list);
  }

  protected String formatEventTypesForLogging(JSONArray events) {
    List<String> types = new ArrayList<>();
    if (events != null) {
      for (int i = 0; i < events.length(); i++) {
        JSONObject event = events.optJSONObject(i);
        if (event != null && event.has(JsonEvent.TYPE)) {
          types.add(event.get(JsonEvent.TYPE) + "");
        }
        else {
          types.add("?");
        }
      }
    }
    return "[" + CollectionUtility.format(types) + "]";
  }
}
