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

import java.io.IOException;
import java.util.concurrent.locks.ReentrantLock;

import javax.servlet.ServletException;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.eclipse.scout.commons.CompareUtility;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.config.CONFIG;
import org.eclipse.scout.rt.ui.html.AbstractUiServletRequestHandler;
import org.eclipse.scout.rt.ui.html.IUiSession;
import org.eclipse.scout.rt.ui.html.MaxUserIdleTimeProperty;
import org.eclipse.scout.rt.ui.html.UiServlet;
import org.eclipse.scout.rt.ui.html.cache.IHttpCacheControl;
import org.json.JSONObject;
import org.slf4j.MDC;

/**
 * This handler contributes to the {@link UiServlet} as the POST handler for /json.
 * <p>
 * Provides the {@link MDC#put(String, String)} properties {@value #MDC_SCOUT_SESSION_ID}
 */
@Order(10)
public class JsonMessageRequestHandler extends AbstractUiServletRequestHandler {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(JsonMessageRequestHandler.class);

  private static final int BACKGROUND_POLLING_INTERVAL_SECONDS = 60;

  private final int m_maxUserIdleTime = CONFIG.getPropertyValue(MaxUserIdleTimeProperty.class).intValue();

  @Override
  public boolean handlePost(UiServlet servlet, HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    // serve only /json
    String pathInfo = req.getPathInfo();
    if (CompareUtility.notEquals(pathInfo, "/json")) {
      return false;
    }

    IUiSession uiSession = null;
    JsonRequest jsonReq = null;
    try {
      // never cache json requests
      BEANS.get(IHttpCacheControl.class).disableCacheHeaders(req, resp);

      JSONObject jsonObject = BEANS.get(JsonProtocolHelper.class).readJsonRequest(req);
      if (isPingRequest(jsonObject)) {
        handlePing(resp);
        return true;
      }

      jsonReq = new JsonRequest(jsonObject);
      uiSession = getOrCreateUiSession(req, resp, jsonReq);
      if (uiSession == null) {
        return true;
      }

      long start = System.nanoTime();
      String oldScoutSessionId = MDC.get(MDC_SCOUT_SESSION_ID);
      String oldScoutUiSessionId = MDC.get(MDC_SCOUT_UI_SESSION_ID);
      try {
        MDC.put(MDC_SCOUT_SESSION_ID, uiSession.getClientSessionId());
        MDC.put(MDC_SCOUT_UI_SESSION_ID, uiSession.getUiSessionId());
        if (LOG.isDebugEnabled()) {
          LOG.debug("JSON request started");
        }

        if (jsonReq.isLogRequest()) {
          handleLog(resp, jsonObject);
          return true;
        }

        if (jsonReq.isPollForBackgroundJobsRequest()) {
          handlePollForBackgroundJobs(uiSession, start);
        }
        else if (jsonReq.isCancelRequest()) {
          handleCancel(resp, uiSession);
          return true;
        }

        // GUI requests for the same session must be processed consecutively
        uiSession.uiSessionLock().lock();
        try {
          if (uiSession.isDisposed() || uiSession.currentJsonResponse() == null) {
            handleUiSessionDisposed(resp, uiSession, jsonReq);
            return true;
          }
          handleEvents(req, resp, uiSession, jsonReq);
        }
        finally {
          uiSession.uiSessionLock().unlock();
        }
        if (LOG.isDebugEnabled()) {
          LOG.debug("JSON request completed in " + StringUtility.formatNanos(System.nanoTime() - start) + " ms");
        }
      }
      finally {
        if (oldScoutSessionId != null) {
          MDC.put(MDC_SCOUT_SESSION_ID, oldScoutSessionId);
        }
        else {
          MDC.remove(MDC_SCOUT_SESSION_ID);
        }
        if (oldScoutUiSessionId != null) {
          MDC.put(MDC_SCOUT_UI_SESSION_ID, oldScoutUiSessionId);
        }
        else {
          MDC.remove(MDC_SCOUT_UI_SESSION_ID);
        }
      }
    }
    catch (Exception e) {
      if (jsonReq == null || uiSession == null || jsonReq.isStartupRequest()) {
        // Send a special error code when an error happens during initialization, because
        // the UI has no translated texts to show in this case.
        LOG.error("Error while initializing UI session", e);
        writeJsonResponse(resp, BEANS.get(JsonProtocolHelper.class).createStartupFailedResponse());
      }
      else {
        LOG.error("Unexpected error while processing JSON request", e);
        writeJsonResponse(resp, BEANS.get(JsonProtocolHelper.class).createUnrecoverableFailureResponse());
      }
    }
    return true;
  }

  protected void handleEvents(HttpServletRequest req, HttpServletResponse resp, IUiSession uiSession, JsonRequest jsonReq) throws IOException {
    JSONObject jsonResp = uiSession.processJsonRequest(req, resp, jsonReq);
    if (jsonResp == null) {
      jsonResp = BEANS.get(JsonProtocolHelper.class).createEmptyResponse();
    }
    writeJsonResponse(resp, jsonResp);
  }

  protected void handleUiSessionDisposed(HttpServletResponse resp, IUiSession uiSession, JsonRequest jsonReq) throws IOException {
    if (jsonReq.isPollForBackgroundJobsRequest()) { // TODO BSH isManualLogout?
      writeJsonResponse(resp, BEANS.get(JsonProtocolHelper.class).createSessionTerminatedResponse(uiSession.getLogoutRedirectUrl()));
    }
    else {
      writeJsonResponse(resp, BEANS.get(JsonProtocolHelper.class).createSessionTimeoutResponse());
    }
  }

  protected void handlePing(HttpServletResponse resp) throws IOException {
    writeJsonResponse(resp, BEANS.get(JsonProtocolHelper.class).createPingResponse());
  }

  protected void handleLog(HttpServletResponse resp, JSONObject jsonReqObj) throws IOException {
    String message = jsonReqObj.getString("message");

    if (message.length() > 10000) {
      // Truncate the message to prevent log inflation by malicious log requests
      message = message.substring(0, 10000) + "...";
    }
    LOG.error(message);
    writeJsonResponse(resp, BEANS.get(JsonProtocolHelper.class).createEmptyResponse());
  }

  protected void handleCancel(HttpServletResponse resp, IUiSession uiSession) throws IOException {
    uiSession.processCancelRequest();
    writeJsonResponse(resp, BEANS.get(JsonProtocolHelper.class).createEmptyResponse());
  }

  protected void handlePollForBackgroundJobs(IUiSession uiSession, long start) {
    int curIdle = (int) ((System.currentTimeMillis() - uiSession.getLastAccessedTime()) / 1000L);
    int maxIdle = m_maxUserIdleTime;
    // Default don't wait longer than the container timeout for security reasons. However, the minimum is _not_ 0,
    // because that might trigger many very short polling calls until the ui session is really disposed.
    int pollWait = Math.max(Math.min(maxIdle - curIdle, BACKGROUND_POLLING_INTERVAL_SECONDS), 3);
    if (LOG.isDebugEnabled()) {
      LOG.debug("Polling begin for " + pollWait + " seconds");
    }
    // Blocks the current thread until:
    // - a model job terminates
    // - the max. wait time has exceeded
    uiSession.waitForBackgroundJobs(pollWait);
    if (LOG.isDebugEnabled()) {
      LOG.debug("Polling end after " + StringUtility.formatNanos(System.nanoTime() - start) + " ms");
    }
  }

  protected void handleSessionTimeout(HttpServletResponse resp, JsonRequest jsonReq) throws IOException {
    LOG.info("Request cannot be processed due to UI session timeout [id=" + jsonReq.getUiSessionId() + "]");
    writeJsonResponse(resp, BEANS.get(JsonProtocolHelper.class).createSessionTimeoutResponse());
  }

  protected void handleMaxIdeTimeout(HttpServletResponse resp, JsonRequest jsonReq, HttpSession httpSession, int idleSeconds, int maxIdleSeconds) throws IOException {
    LOG.info("Detected UI session timeout [id=" + jsonReq.getUiSessionId() + "] after idle of " + idleSeconds + " seconds (maxInactiveInterval=" + maxIdleSeconds + ")");
    httpSession.invalidate();
    writeJsonResponse(resp, BEANS.get(JsonProtocolHelper.class).createSessionTimeoutResponse());
  }

  protected void handleUnload(HttpServletResponse resp, JsonRequest jsonReq, String uiSessionAttributeName, HttpSession httpSession, IUiSession uiSession) throws IOException {
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
    writeJsonResponse(resp, BEANS.get(JsonProtocolHelper.class).createEmptyResponse()); // send empty response to satisfy clients expecting a valid response
  }

  /**
   * Check if request is a simple ping. We don't use JsonRequest here because a ping request has no uiSessionId.
   */
  protected boolean isPingRequest(JSONObject json) {
    return json.has("ping");
  }

  protected IUiSession getOrCreateUiSession(HttpServletRequest req, HttpServletResponse resp, JsonRequest jsonReq) throws ServletException, IOException {
    String uiSessionAttributeName = IUiSession.HTTP_SESSION_ATTRIBUTE_PREFIX + jsonReq.getUiSessionId();
    HttpSession httpSession = req.getSession();

    // Because the app-server might keep or request locks on the httpSession object, we don't synchronize directly
    // on httpSession, but use a dedicated session lock object instead.
    ReentrantLock httpSessionLock = httpSessionLock(httpSession);
    httpSessionLock.lock();
    try {
      IUiSession uiSession = (IUiSession) httpSession.getAttribute(uiSessionAttributeName);

      if (jsonReq.isUnloadRequest()) {
        handleUnload(resp, jsonReq, uiSessionAttributeName, httpSession, uiSession);
        return null;
      }

      // check startup
      if (uiSession == null && !jsonReq.isStartupRequest()) {
        handleSessionTimeout(resp, jsonReq);
        return null;
      }
      else if (uiSession != null && jsonReq.isStartupRequest()) {
        throw new IllegalStateException("Startup requested for existing UI session with ID " + jsonReq.getUiSessionId());
      }

      if (uiSession == null) {
        LOG.info("Creating new UI session with ID " + jsonReq.getUiSessionId() + ". MaxIdleTime: " + m_maxUserIdleTime + "sec, httpSession.MaxInactiveInterval: " + req.getSession().getMaxInactiveInterval() + "sec");

        uiSession = BEANS.get(IUiSession.class);
        uiSession.uiSessionLock().lock();
        try {
          uiSession.init(req, resp, new JsonStartupRequest(jsonReq));
          httpSession.setAttribute(uiSessionAttributeName, uiSession);
        }
        finally {
          uiSession.uiSessionLock().unlock();
        }
      }

      // check timeout
      int idleSeconds = (int) ((System.currentTimeMillis() - uiSession.getLastAccessedTime()) / 1000L);
      int maxIdleSeconds = m_maxUserIdleTime;
      if (idleSeconds > maxIdleSeconds) {
        handleMaxIdeTimeout(resp, jsonReq, httpSession, idleSeconds, maxIdleSeconds);
        return null;
      }

      // update timeout
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

  /**
   * Writes the given {@link JSONObject} into the given {@link ServletResponse}.
   */
  protected void writeJsonResponse(ServletResponse servletResponse, JSONObject jsonObject) throws IOException {
    BEANS.get(JsonProtocolHelper.class).writeResponse(servletResponse, jsonObject);
  }
}
