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

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.config.CONFIG;
import org.eclipse.scout.rt.platform.exception.ExceptionTranslator;
import org.eclipse.scout.rt.platform.util.CompareUtility;
import org.eclipse.scout.rt.platform.util.StringUtility;
import org.eclipse.scout.rt.platform.util.concurrent.IRunnable;
import org.eclipse.scout.rt.ui.html.AbstractUiServletRequestHandler;
import org.eclipse.scout.rt.ui.html.IUiSession;
import org.eclipse.scout.rt.ui.html.MaxUserIdleTimeProperty;
import org.eclipse.scout.rt.ui.html.UiRunContexts;
import org.eclipse.scout.rt.ui.html.UiServlet;
import org.eclipse.scout.rt.ui.html.cache.IHttpCacheControl;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

/**
 * This handler contributes to the {@link UiServlet} as the POST handler for /json.
 * <p>
 * Provides the {@link MDC#put(String, String)} properties {@value #MDC_SCOUT_SESSION_ID}
 */
@Order(5010)
public class JsonMessageRequestHandler extends AbstractUiServletRequestHandler {
  private static final Logger LOG = LoggerFactory.getLogger(JsonMessageRequestHandler.class);

  private static final int BACKGROUND_POLLING_INTERVAL_SECONDS = 60;

  private final int m_maxUserIdleTime = CONFIG.getPropertyValue(MaxUserIdleTimeProperty.class).intValue();

  private final IHttpCacheControl m_httpCacheControl = BEANS.get(IHttpCacheControl.class);
  private final JsonRequestHelper m_jsonRequestHelper = BEANS.get(JsonRequestHelper.class);

  @Override
  public boolean handlePost(final UiServlet servlet, final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
    // serve only /json
    String pathInfo = req.getPathInfo();
    if (CompareUtility.notEquals(pathInfo, "/json")) {
      return false;
    }

    IUiSession uiSession = null;
    JsonRequest jsonReq = null;
    try {
      // never cache json requests
      m_httpCacheControl.disableCacheHeaders(req, resp);

      JSONObject jsonObject = m_jsonRequestHelper.readJsonRequest(req);
      if (isPingRequest(jsonObject)) {
        handlePing(resp);
        return true;
      }

      jsonReq = new JsonRequest(jsonObject);
      uiSession = getOrCreateUiSession(req, resp, jsonReq);
      if (uiSession == null) {
        return true;
      }

      // Associate subsequent processing with the uiSession and jsonRequest.
      UiRunContexts.copyCurrent()
          .withSession(uiSession)
          .withJsonRequest(jsonReq)
          .run(new IRunnable() {

            @Override
            public void run() throws Exception {
              handleJsonRequest(IUiSession.CURRENT.get(), JsonRequest.CURRENT.get(), req, resp);
            }
          }, BEANS.get(ExceptionTranslator.class));
    }
    catch (Exception e) {
      if (jsonReq == null || uiSession == null || jsonReq.isStartupRequest()) {
        // Send a special error code when an error happens during initialization, because
        // the UI has no translated texts to show in this case.
        LOG.error("Error while initializing UI session", e);
        writeJsonResponse(resp, m_jsonRequestHelper.createStartupFailedResponse());
      }
      else {
        LOG.error("Unexpected error while processing JSON request", e);
        writeJsonResponse(resp, m_jsonRequestHelper.createUnrecoverableFailureResponse());
      }
    }
    return true;
  }

  /**
   * Method invoked to handle a JSON request from UI.
   */
  protected void handleJsonRequest(IUiSession uiSession, JsonRequest jsonRequest, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws IOException {
    long start = System.nanoTime();
    if (LOG.isDebugEnabled()) {
      LOG.debug("JSON request started");
    }

    if (jsonRequest.isLogRequest()) {
      handleLog(httpServletResponse, jsonRequest.getRequestObject());
      return;
    }

    if (jsonRequest.isPollForBackgroundJobsRequest()) {
      handlePollForBackgroundJobs(uiSession, start);
    }
    else if (jsonRequest.isCancelRequest()) {
      handleCancel(httpServletResponse, uiSession);
      return;
    }

    // GUI requests for the same session must be processed consecutively
    uiSession.uiSessionLock().lock();
    try {
      if (uiSession.isDisposed() || uiSession.currentJsonResponse() == null) {
        handleUiSessionDisposed(httpServletResponse, uiSession, jsonRequest);
        return;
      }
      handleEvents(httpServletRequest, httpServletResponse, uiSession, jsonRequest);
    }
    finally {
      uiSession.uiSessionLock().unlock();
    }
    if (LOG.isDebugEnabled()) {
      LOG.debug("JSON request completed in " + StringUtility.formatNanos(System.nanoTime() - start) + " ms");
    }
  }

  protected void handleEvents(HttpServletRequest req, HttpServletResponse resp, IUiSession uiSession, JsonRequest jsonReq) throws IOException {
    JSONObject jsonResp = uiSession.processJsonRequest(req, resp, jsonReq);
    if (jsonResp == null) {
      jsonResp = m_jsonRequestHelper.createEmptyResponse();
    }
    writeJsonResponse(resp, jsonResp);
  }

  protected void handleUiSessionDisposed(HttpServletResponse resp, IUiSession uiSession, JsonRequest jsonReq) throws IOException {
    if (jsonReq.isPollForBackgroundJobsRequest()) { // TODO BSH isManualLogout?
      writeJsonResponse(resp, m_jsonRequestHelper.createSessionTerminatedResponse(uiSession.getLogoutRedirectUrl()));
    }
    else {
      writeJsonResponse(resp, m_jsonRequestHelper.createSessionTimeoutResponse());
    }
  }

  protected void handlePing(HttpServletResponse resp) throws IOException {
    writeJsonResponse(resp, m_jsonRequestHelper.createPingResponse());
  }

  protected void handleLog(HttpServletResponse resp, JSONObject jsonReqObj) throws IOException {
    String message = jsonReqObj.getString("message");

    if (message.length() > 10000) {
      // Truncate the message to prevent log inflation by malicious log requests
      message = message.substring(0, 10000) + "...";
    }
    LOG.error(message);
    writeJsonResponse(resp, m_jsonRequestHelper.createEmptyResponse());
  }

  protected void handleCancel(HttpServletResponse resp, IUiSession uiSession) throws IOException {
    uiSession.processCancelRequest();
    writeJsonResponse(resp, m_jsonRequestHelper.createEmptyResponse());
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
    writeJsonResponse(resp, m_jsonRequestHelper.createSessionTimeoutResponse());
  }

  protected void handleMaxIdeTimeout(HttpServletResponse resp, JsonRequest jsonReq, HttpSession httpSession, int idleSeconds, int maxIdleSeconds) throws IOException {
    LOG.info("Detected UI session timeout [id=" + jsonReq.getUiSessionId() + "] after idle of " + idleSeconds + " seconds (maxInactiveInterval=" + maxIdleSeconds + ")");
    httpSession.invalidate();
    writeJsonResponse(resp, m_jsonRequestHelper.createSessionTimeoutResponse());
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
    writeJsonResponse(resp, m_jsonRequestHelper.createEmptyResponse()); // send empty response to satisfy clients expecting a valid response
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
    m_jsonRequestHelper.writeResponse(servletResponse, jsonObject);
  }
}
