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
import org.eclipse.scout.rt.platform.IPlatform;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.Platform;
import org.eclipse.scout.rt.platform.config.CONFIG;
import org.eclipse.scout.rt.platform.exception.DefaultExceptionTranslator;
import org.eclipse.scout.rt.platform.util.CompareUtility;
import org.eclipse.scout.rt.platform.util.StringUtility;
import org.eclipse.scout.rt.platform.util.concurrent.IRunnable;
import org.eclipse.scout.rt.ui.html.AbstractUiServletRequestHandler;
import org.eclipse.scout.rt.ui.html.HttpSessionHelper;
import org.eclipse.scout.rt.ui.html.ISessionStore;
import org.eclipse.scout.rt.ui.html.IUiSession;
import org.eclipse.scout.rt.ui.html.MaxUserIdleTimeProperty;
import org.eclipse.scout.rt.ui.html.UiRunContexts;
import org.eclipse.scout.rt.ui.html.UiServlet;
import org.eclipse.scout.rt.ui.html.cache.IHttpCacheControl;
import org.eclipse.scout.rt.ui.html.json.JsonRequest.RequestType;
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

  private final HttpSessionHelper m_httpSessionHelper = BEANS.get(HttpSessionHelper.class);
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
    JsonRequest jsonRequest = null;
    try {
      // never cache json requests
      m_httpCacheControl.disableCacheHeaders(req, resp);

      JSONObject jsonObject = m_jsonRequestHelper.readJsonRequest(req);
      jsonRequest = new JsonRequest(jsonObject);
      if (jsonRequest.getRequestType() == RequestType.PING_REQUEST) {
        handlePingRequest(resp);
        return true;
      }

      uiSession = getOrCreateUiSession(req, resp, jsonRequest);
      if (uiSession == null) {
        return true;
      }

      // Associate subsequent processing with the uiSession and jsonRequest.
      UiRunContexts.copyCurrent()
          .withSession(uiSession)
          .withJsonRequest(jsonRequest)
          .run(new IRunnable() {

            @Override
            public void run() throws Exception {
              handleJsonRequest(IUiSession.CURRENT.get(), JsonRequest.CURRENT.get(), req, resp);
            }
          }, DefaultExceptionTranslator.class);
    }
    catch (Exception e) {
      if (jsonRequest == null || uiSession == null || jsonRequest.getRequestType() == RequestType.STARTUP_REQUEST) {
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
    final long startNanos = System.nanoTime();
    if (LOG.isDebugEnabled()) {
      LOG.debug("JSON request started");
    }

    switch (jsonRequest.getRequestType()) {
      case LOG_REQUEST:
        handleLogRequest(httpServletResponse, uiSession, jsonRequest.getRequestObject());
        return;
      case CANCEL_REQUEST:
        handleCancelRequest(httpServletResponse, uiSession);
        return;
      case POLL_REQUEST:
        handlePollRequest(uiSession, startNanos);
        break;
    }

    // GUI requests for the same session must be processed consecutively
    uiSession.uiSessionLock().lock();
    try {
      if (uiSession.isDisposed()) {
        handleUiSessionDisposed(httpServletResponse, uiSession, jsonRequest);
      }
      else {
        handleEvents(httpServletRequest, httpServletResponse, uiSession, jsonRequest);
      }
    }
    finally {
      uiSession.uiSessionLock().unlock();
    }

    if (LOG.isDebugEnabled()) {
      LOG.debug("JSON request completed in {}ms", StringUtility.formatNanos(System.nanoTime() - startNanos));
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
    // When the UI session becomes invalid, we usually want to show the "session timeout" error message.
    //
    // There is one exception: When the user clicked a "logout" action in the model, we want to redirect
    // the UI to the /logout URL. This is done in UiSession.logout(). For all other client sessions on that
    // HTTP session, the redirect should also be made, but via poller.
    //
    // And here is the exception to the exception: When the platform is no longer valid, it means that
    // we probably cannot show the /logout URL. To prevent nasty error messages from the app server, we
    // fall back to the "session timeout" error message.

    boolean platformValid = (Platform.get() != null && Platform.get().getState() == IPlatform.State.PlatformStarted);
    if (platformValid && jsonReq.getRequestType() == RequestType.POLL_REQUEST) {
      writeJsonResponse(resp, m_jsonRequestHelper.createSessionTerminatedResponse(uiSession.getLogoutRedirectUrl()));
    }
    else {
      writeJsonResponse(resp, m_jsonRequestHelper.createSessionTimeoutResponse());
    }
  }

  protected void handlePingRequest(HttpServletResponse resp) throws IOException {
    writeJsonResponse(resp, m_jsonRequestHelper.createPingResponse());
  }

  protected void handleLogRequest(HttpServletResponse resp, IUiSession uiSession, JSONObject jsonReqObj) throws IOException {
    String message = jsonReqObj.getString("message");
    JSONObject event = jsonReqObj.optJSONObject("event");

    String header = "JavaScript exception occured";
    if (event != null) {
      String target = event.getString("target");
      String type = event.getString("type");
      header += " while processing event " + type + " for adapter " + uiSession.getJsonAdapter(target);
    }
    message = header + "\n" + message;
    if (message.length() > 10000) {
      // Truncate the message to prevent log inflation by malicious log requests
      message = message.substring(0, 10000) + "...";
    }
    LOG.error(message);
    writeJsonResponse(resp, m_jsonRequestHelper.createEmptyResponse());
  }

  protected void handleCancelRequest(HttpServletResponse resp, IUiSession uiSession) throws IOException {
    uiSession.processCancelRequest();
    writeJsonResponse(resp, m_jsonRequestHelper.createEmptyResponse());
  }

  protected void handlePollRequest(IUiSession uiSession, long start) {
    int curIdle = (int) ((System.currentTimeMillis() - uiSession.getLastAccessedTime()) / 1000L);
    int maxIdle = m_maxUserIdleTime;
    // Default don't wait longer than the container timeout for security reasons. However, the minimum is _not_ 0,
    // because that might trigger many very short polling calls until the ui session is really disposed.
    int pollWait = Math.max(Math.min(maxIdle - curIdle, BACKGROUND_POLLING_INTERVAL_SECONDS), 3);
    LOG.debug("Polling begin for {} seconds", pollWait);
    // Blocks the current thread until:
    // - a model job terminates
    // - the max. wait time has exceeded
    uiSession.waitForBackgroundJobs(pollWait);
    if (LOG.isDebugEnabled()) {
      LOG.debug("Polling end after {} ms", StringUtility.formatNanos(System.nanoTime() - start));
    }
  }

  protected void handleSessionTimeout(HttpServletResponse resp, JsonRequest jsonReq) throws IOException {
    LOG.info("Request cannot be processed due to UI session timeout [id={}]", jsonReq.getUiSessionId());
    writeJsonResponse(resp, m_jsonRequestHelper.createSessionTimeoutResponse());
  }

  protected void handleMaxIdeTimeout(HttpServletResponse resp, JsonRequest jsonReq, IUiSession uiSession, int idleSeconds, int maxIdleSeconds) throws IOException {
    LOG.info("Detected UI session timeout [id={}] after idle of {} seconds (maxInactiveInterval={})", jsonReq.getUiSessionId(), idleSeconds, maxIdleSeconds);
    if (uiSession != null) {
      uiSession.uiSessionLock().lock();
      try {
        uiSession.dispose();
      }
      finally {
        uiSession.uiSessionLock().unlock();
      }
    }
    writeJsonResponse(resp, m_jsonRequestHelper.createSessionTimeoutResponse());
  }

  protected void handleUnloadRequest(HttpServletResponse resp, JsonRequest jsonReq, IUiSession uiSession) throws IOException {
    LOG.info("Unloading UI session with ID {} (requested by UI)", jsonReq.getUiSessionId());
    if (uiSession != null) {
      uiSession.uiSessionLock().lock();
      try {
        uiSession.dispose();
      }
      finally {
        uiSession.uiSessionLock().unlock();
      }
    }
    writeJsonResponse(resp, m_jsonRequestHelper.createEmptyResponse()); // send empty response to satisfy clients expecting a valid response
  }

  protected IUiSession getOrCreateUiSession(HttpServletRequest req, HttpServletResponse resp, JsonRequest jsonReq) throws ServletException, IOException {
    HttpSession httpSession = req.getSession();
    // Because the app-server might keep or request locks on the httpSession object, we don't synchronize directly
    // on httpSession, but use a dedicated session lock object instead.
    ReentrantLock httpSessionLock = m_httpSessionHelper.getHttpSessionLock(httpSession);
    httpSessionLock.lock();
    try {
      ISessionStore sessionStore = m_httpSessionHelper.getSessionStore(httpSession);

      IUiSession uiSession = sessionStore.getUiSession(jsonReq.getUiSessionId());

      if (jsonReq.getRequestType() == RequestType.UNLOAD_REQUEST) {
        handleUnloadRequest(resp, jsonReq, uiSession);
        return null;
      }

      // check startup
      if (uiSession == null && jsonReq.getRequestType() != RequestType.STARTUP_REQUEST) {
        handleSessionTimeout(resp, jsonReq);
        return null;
      }
      else if (uiSession != null && jsonReq.getRequestType() == RequestType.STARTUP_REQUEST) {
        throw new IllegalStateException("Startup requested for existing UI session with ID " + jsonReq.getUiSessionId());
      }

      if (uiSession == null) {
        LOG.info("Creating new UI session with ID {} [maxIdleTime={}s, httpSession.maxInactiveInterval={}s]", jsonReq.getUiSessionId(), m_maxUserIdleTime, req.getSession().getMaxInactiveInterval());

        uiSession = BEANS.get(IUiSession.class);
        uiSession.uiSessionLock().lock();
        try {
          uiSession.init(req, resp, new JsonStartupRequest(jsonReq));
          sessionStore.registerUiSession(uiSession);
        }
        finally {
          uiSession.uiSessionLock().unlock();
        }
      }

      // check timeout
      int idleSeconds = (int) ((System.currentTimeMillis() - uiSession.getLastAccessedTime()) / 1000L);
      int maxIdleSeconds = m_maxUserIdleTime;
      if (idleSeconds > maxIdleSeconds) {
        handleMaxIdeTimeout(resp, jsonReq, uiSession, idleSeconds, maxIdleSeconds);
        return null;
      }

      // update timeout
      if (jsonReq.getRequestType() != RequestType.POLL_REQUEST) {
        uiSession.touch();
      }

      return uiSession;
    }
    finally {
      httpSessionLock.unlock();
    }
  }

  /**
   * Writes the given {@link JSONObject} into the given {@link ServletResponse}.
   */
  protected void writeJsonResponse(ServletResponse servletResponse, JSONObject jsonObject) throws IOException {
    m_jsonRequestHelper.writeResponse(servletResponse, jsonObject);
  }
}
