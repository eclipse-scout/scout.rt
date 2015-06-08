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
import java.util.concurrent.locks.ReentrantLock;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.eclipse.scout.commons.CompareUtility;
import org.eclipse.scout.commons.DateUtility;
import org.eclipse.scout.commons.IOUtility;
import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.job.ModelJobs;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.job.IJobManager;
import org.eclipse.scout.rt.platform.service.AbstractService;
import org.eclipse.scout.rt.ui.html.IServletRequestInterceptor;
import org.eclipse.scout.rt.ui.html.IUiSession;
import org.eclipse.scout.rt.ui.html.UiServlet;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.MDC;

/**
 * This interceptor contributes to the {@link UiServlet} as the default POST handler
 * <p>
 * Provides the {@link MDC#put(String, String)} properties {@value #SCOUT_SESSION_ID}
 */
@Order(10)
public class JsonMessageRequestInterceptor extends AbstractService implements IServletRequestInterceptor {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(JsonMessageRequestInterceptor.class);

  public static final String SCOUT_SESSION_ID = "scout.session.id";
  public static final String SCOUT_UI_SESSION_ID = "scout.ui.session.id";

  @Override
  public boolean interceptGet(UiServlet servlet, HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    return false;
  }

  @Override
  public boolean interceptPost(UiServlet servlet, HttpServletRequest httpReq, HttpServletResponse httpResp) throws ServletException, IOException {
    //serve only /json
    String pathInfo = httpReq.getPathInfo();
    if (CompareUtility.notEquals(pathInfo, "/json")) {
      LOG.info("404_NOT_FOUND_POST: " + pathInfo);
      httpResp.sendError(HttpServletResponse.SC_NOT_FOUND);
      return true;
    }
    IUiSession uiSession = null;
    JsonRequest jsonReq = null;
    try {
      //disable cache
      servlet.getHttpCacheControl().disableCacheHeaders(httpReq, httpResp);
      JSONObject jsonReqObj = decodeJSONRequest(httpReq);
      if (isPingRequest(jsonReqObj)) {
        writeResponse(httpResp, createPingResponse());
        return true;
      }

      jsonReq = new JsonRequest(jsonReqObj);
      uiSession = getOrCreateUiSession(servlet, httpReq, httpResp, jsonReq);
      if (uiSession == null) {
        return true;
      }

      long start = System.nanoTime();
      String oldScoutSessionId = MDC.get(SCOUT_SESSION_ID);
      String oldScoutUiSessionId = MDC.get(SCOUT_UI_SESSION_ID);
      try {
        MDC.put(SCOUT_SESSION_ID, uiSession.getClientSessionId());
        MDC.put(SCOUT_UI_SESSION_ID, uiSession.getUiSessionId());

        if (jsonReq.isPollForBackgroundJobsRequest()) {
          // Blocks the current thread until:
          // - a model job terminates
          // - the max. wait time has exceeded
          int curIdle = (int) ((System.currentTimeMillis() - uiSession.getLastAccessedTime()) / 1000L);
          int maxIdle = httpReq.getSession().getMaxInactiveInterval();
          int pollWait = Math.max(Math.min(maxIdle - curIdle, 60), 0);
          if (LOG.isDebugEnabled()) {
            LOG.debug("polling begin for " + pollWait + " seconds");
          }
          uiSession.waitForBackgroundJobs(pollWait);
          if (LOG.isDebugEnabled()) {
            LOG.debug("polling end after " + DateUtility.formatNanos(System.nanoTime() - start) + " ms");
          }
        }
        else if (jsonReq.isCancelRequest()) {
          // Cancel all running model jobs for the requested session (interrupt if necessary)
          BEANS.get(IJobManager.class).cancel(ModelJobs.newFutureFilter().andMatchSession(uiSession.getClientSession()).andAreNotBlocked(), true);
          writeResponse(httpResp, createEmptyResponse());
          return true;
        }

        // GUI requests for the same session must be processed consecutively
        if (LOG.isDebugEnabled()) {
          LOG.debug("started");
        }
        uiSession.uiSessionLock().lock();
        try {
          if (uiSession.currentJsonResponse() == null) {
            // Missing current JSON response, probably because the UiSession is disposed -> send empty answer
            writeResponse(httpResp, createSessionTerminatedResponse());
            return true;
          }
          JSONObject jsonResp = uiSession.processRequest(httpReq, jsonReq);
          if (jsonResp == null) {
            jsonResp = createEmptyResponse();
          }
          writeResponse(httpResp, jsonResp);
        }
        finally {
          uiSession.uiSessionLock().unlock();
        }
        if (LOG.isDebugEnabled()) {
          LOG.debug("completed in " + DateUtility.formatNanos(System.nanoTime() - start) + " ms");
        }
      }
      finally {
        if (oldScoutSessionId != null) {
          MDC.put(SCOUT_SESSION_ID, oldScoutSessionId);
        }
        else {
          MDC.remove(SCOUT_SESSION_ID);
        }
        if (oldScoutUiSessionId != null) {
          MDC.put(SCOUT_UI_SESSION_ID, oldScoutUiSessionId);
        }
        else {
          MDC.remove(SCOUT_UI_SESSION_ID);
        }
      }
    }
    catch (Exception e) {
      if (jsonReq == null || uiSession == null || jsonReq.isStartupRequest()) {
        // Send a special error code when an error happens during initialization, because
        // the UI has no translated texts to show in this case.
        LOG.error("Error while initializing UI session", e);
        writeResponse(httpResp, createStartupFailedResponse());
      }
      else {
        LOG.error("Unexpected error while processing JSON request", e);
        writeResponse(httpResp, createUnrecoverableFailureResponse());
      }
    }
    return true;
  }

  /**
   * Check if request is a simple ping. We don't use JsonRequest here because a ping request has no uiSessionId.
   */
  protected boolean isPingRequest(JSONObject json) {
    return json.has("ping");
  }

  protected IUiSession getOrCreateUiSession(UiServlet servlet, HttpServletRequest req, HttpServletResponse resp, JsonRequest jsonReq) throws ServletException, IOException {
    String uiSessionAttributeName = IUiSession.HTTP_SESSION_ATTRIBUTE_PREFIX + jsonReq.getUiSessionId();
    HttpSession httpSession = req.getSession();

    // Because the app-server might keep or request locks on the httpSession object, we don't synchronize directly
    // on httpSession, but use a dedicated session lock object instead.
    ReentrantLock httpSessionLock = httpSessionLock(httpSession);
    httpSessionLock.lock();
    try {
      IUiSession uiSession = (IUiSession) httpSession.getAttribute(uiSessionAttributeName);

      if (jsonReq.isUnloadRequest()) {
        LOG.info("Unloading UI session with ID " + jsonReq.getUiSessionId() + " (requested by UI)");
        if (uiSession != null) {
          // Unbinding the uiSession will cause it to be disposed automatically, see UiSession.valueUnbound()
          uiSession.uiSessionLock().lock();
          try {
            httpSession.removeAttribute(uiSessionAttributeName);
          }
          finally {
            uiSession.uiSessionLock().unlock();
          }
        }
        writeResponse(resp, createEmptyResponse()); // send empty response to satisfy clients expecting a valid response
        return null;
      }

      //check startup
      if (uiSession == null && !jsonReq.isStartupRequest()) {
        LOG.info("Request cannot be processed due to UI session timeout [id=" + jsonReq.getUiSessionId() + "]");
        writeResponse(resp, createSessionTimeoutResponse());
        return null;
      }
      else if (uiSession != null && jsonReq.isStartupRequest()) {
        throw new IllegalStateException("Startup requested for existing UI session with ID " + jsonReq.getUiSessionId());
      }

      if (uiSession == null) {
        LOG.info("Creating new UI session with ID " + jsonReq.getUiSessionId() + ", timeout after " + req.getSession().getMaxInactiveInterval() + " sec...");

        uiSession = BEANS.get(IUiSession.class);
        uiSession.uiSessionLock().lock();
        try {
          uiSession.init(req, new JsonStartupRequest(jsonReq));
          httpSession.setAttribute(uiSessionAttributeName, uiSession);
        }
        finally {
          uiSession.uiSessionLock().unlock();
        }
      }

      //check timeout
      int idleSeconds = (int) ((System.currentTimeMillis() - uiSession.getLastAccessedTime()) / 1000L);
      if (idleSeconds > httpSession.getMaxInactiveInterval()) {
        LOG.info("detected UI session timeout [id=" + jsonReq.getUiSessionId() + "] after idle of " + idleSeconds + " seconds (maxInactiveInterval=" + httpSession.getMaxInactiveInterval() + ")");
        httpSession.invalidate();
        writeResponse(resp, createSessionTimeoutResponse());
        return null;
      }

      //update timeout
      if (!jsonReq.isPollForBackgroundJobsRequest()) {
        uiSession.touch();
      }

      return uiSession;
    }
    finally {
      httpSessionLock.unlock();
    }
  }

  protected ReentrantLock httpSessionLock(HttpSession httpSession) {
    synchronized (httpSession) {
      String lockAttributeName = "scout.htmlui.httpsession.lock";
      ReentrantLock lock = (ReentrantLock) httpSession.getAttribute(lockAttributeName);
      if (lock == null) {
        lock = new ReentrantLock();
        httpSession.setAttribute(lockAttributeName, lock);
      }
      return lock;
    }
  }

  protected JSONObject createSessionTimeoutResponse() {
    JsonResponse response = new JsonResponse();
    response.markAsError(JsonResponse.ERR_SESSION_TIMEOUT, "The session has expired, please reload the page.");
    return response.toJson();
  }

  protected JSONObject createUnrecoverableFailureResponse() {
    JsonResponse response = new JsonResponse();
    response.markAsError(JsonResponse.ERR_UI_PROCESSING, "UI processing error");
    return response.toJson();
  }

  protected JSONObject createStartupFailedResponse() {
    JsonResponse response = new JsonResponse();
    response.markAsError(JsonResponse.ERR_STARTUP_FAILED, "Initialization failed");
    return response.toJson();
  }

  protected JSONObject createPingResponse() {
    JSONObject json = new JSONObject();
    JsonObjectUtility.putProperty(json, "pong", Boolean.TRUE);
    return json;
  }

  protected JSONObject createEmptyResponse() {
    return new JSONObject();
  }

  protected JSONObject createSessionTerminatedResponse() {
    JSONObject json = new JSONObject();
    JsonObjectUtility.putProperty(json, "sessionTerminated", Boolean.TRUE);
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
      throw new JsonException(e.getMessage(), e);
    }
  }
}
