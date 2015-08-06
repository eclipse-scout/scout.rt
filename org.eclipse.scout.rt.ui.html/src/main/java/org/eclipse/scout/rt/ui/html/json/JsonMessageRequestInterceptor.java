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
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.ui.html.IServletRequestInterceptor;
import org.eclipse.scout.rt.ui.html.IUiSession;
import org.eclipse.scout.rt.ui.html.UiServlet;
import org.eclipse.scout.rt.ui.html.cache.IHttpCacheControl;
import org.json.JSONObject;
import org.slf4j.MDC;

/**
 * This interceptor contributes to the {@link UiServlet} as the POST handler for /json.
 * <p>
 * Provides the {@link MDC#put(String, String)} properties {@value #MDC_SCOUT_SESSION_ID}
 */
@Order(10)
public class JsonMessageRequestInterceptor extends AbstractJsonRequestInterceptor implements IServletRequestInterceptor {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(JsonMessageRequestInterceptor.class);

  private static final int BACKGROUND_POLLING_INTERVAL_SECONDS = 60;

  @Override
  public boolean interceptGet(UiServlet servlet, HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    return false;
  }

  @Override
  public boolean interceptPost(UiServlet servlet, HttpServletRequest httpReq, HttpServletResponse httpResp) throws ServletException, IOException {
    //serve only /json
    String pathInfo = httpReq.getPathInfo();
    if (CompareUtility.notEquals(pathInfo, "/json")) {
      return false;
    }

    IUiSession uiSession = null;
    JsonRequest jsonReq = null;
    try {
      //disable cache
      BEANS.get(IHttpCacheControl.class).disableCacheHeaders(httpReq, httpResp);
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
      String oldScoutSessionId = MDC.get(MDC_SCOUT_SESSION_ID);
      String oldScoutUiSessionId = MDC.get(MDC_SCOUT_UI_SESSION_ID);
      try {
        MDC.put(MDC_SCOUT_SESSION_ID, uiSession.getClientSessionId());
        MDC.put(MDC_SCOUT_UI_SESSION_ID, uiSession.getUiSessionId());

        if (jsonReq.isPollForBackgroundJobsRequest()) {
          int curIdle = (int) ((System.currentTimeMillis() - uiSession.getLastAccessedTime()) / 1000L);
          int maxIdle = httpReq.getSession().getMaxInactiveInterval();
          // Default don't wait longer than the container timeout for security reasons. However, the minimum is _not_ 0,
          // because that might trigger many very short polling calls until the ui session is really disposed.
          int pollWait = Math.max(Math.min(maxIdle - curIdle, BACKGROUND_POLLING_INTERVAL_SECONDS), 3);
          if (LOG.isDebugEnabled()) {
            LOG.debug("polling begin for " + pollWait + " seconds");
          }
          // Blocks the current thread until:
          // - a model job terminates
          // - the max. wait time has exceeded
          uiSession.waitForBackgroundJobs(pollWait);
          if (LOG.isDebugEnabled()) {
            LOG.debug("polling end after " + StringUtility.formatNanos(System.nanoTime() - start) + " ms");
          }
        }
        else if (jsonReq.isCancelRequest()) {
          uiSession.processCancelRequest();
          writeResponse(httpResp, createEmptyResponse());
          return true;
        }

        // GUI requests for the same session must be processed consecutively
        if (LOG.isDebugEnabled()) {
          LOG.debug("JSON request started");
        }
        uiSession.uiSessionLock().lock();
        try {
          if (uiSession.isDisposed() || uiSession.currentJsonResponse() == null) {
            if (jsonReq.isPollForBackgroundJobsRequest()) { // TODO BSH isManualLogout?
              writeResponse(httpResp, createSessionTerminatedResponse(uiSession.getLogoutRedirectUrl()));
            }
            else {
              writeResponse(httpResp, createSessionTimeoutResponse());
            }
            return true;
          }
          JSONObject jsonResp = uiSession.processJsonRequest(httpReq, jsonReq);
          if (jsonResp == null) {
            jsonResp = createEmptyResponse();
          }
          writeResponse(httpResp, jsonResp);
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
      int maxIdleSeconds = httpSession.getMaxInactiveInterval();
      if (idleSeconds > maxIdleSeconds) {
        LOG.info("detected UI session timeout [id=" + jsonReq.getUiSessionId() + "] after idle of " + idleSeconds + " seconds (maxInactiveInterval=" + maxIdleSeconds + ")");
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
}
