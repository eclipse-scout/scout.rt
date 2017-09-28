/*******************************************************************************
 * Copyright (c) 2014-2017 BSI Business Systems Integration AG.
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

import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.IPlatform.State;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.Platform;
import org.eclipse.scout.rt.platform.config.CONFIG;
import org.eclipse.scout.rt.platform.config.PlatformConfigProperties.ApplicationVersionProperty;
import org.eclipse.scout.rt.platform.context.RunContexts;
import org.eclipse.scout.rt.platform.exception.DefaultExceptionTranslator;
import org.eclipse.scout.rt.platform.exception.PlatformError;
import org.eclipse.scout.rt.platform.resource.MimeType;
import org.eclipse.scout.rt.platform.util.ObjectUtility;
import org.eclipse.scout.rt.platform.util.StringUtility;
import org.eclipse.scout.rt.server.commons.servlet.cache.HttpCacheControl;
import org.eclipse.scout.rt.ui.html.AbstractUiServletRequestHandler;
import org.eclipse.scout.rt.ui.html.HttpSessionHelper;
import org.eclipse.scout.rt.ui.html.ISessionStore;
import org.eclipse.scout.rt.ui.html.IUiSession;
import org.eclipse.scout.rt.ui.html.UiHtmlConfigProperties.BackgroundPollingIntervalProperty;
import org.eclipse.scout.rt.ui.html.UiHtmlConfigProperties.MaxUserIdleTimeProperty;
import org.eclipse.scout.rt.ui.html.UiServlet;
import org.eclipse.scout.rt.ui.html.UiSession;
import org.eclipse.scout.rt.ui.html.json.JsonRequest.RequestType;
import org.eclipse.scout.rt.ui.html.logging.IUiRunContextDiagnostics;
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

  private final int m_pollingInterval = CONFIG.getPropertyValue(BackgroundPollingIntervalProperty.class).intValue();
  private final int m_maxUserIdleTime = CONFIG.getPropertyValue(MaxUserIdleTimeProperty.class).intValue();

  private final HttpSessionHelper m_httpSessionHelper = BEANS.get(HttpSessionHelper.class);
  private final HttpCacheControl m_httpCacheControl = BEANS.get(HttpCacheControl.class);
  private final JsonRequestHelper m_jsonRequestHelper = BEANS.get(JsonRequestHelper.class);

  @Override
  public boolean handlePost(final HttpServletRequest req, final HttpServletResponse resp) throws IOException {
    // serve only /json
    String pathInfo = req.getPathInfo();
    if (ObjectUtility.notEquals(pathInfo, "/json")) {
      return false;
    }

    // only accept requests with mime type = application/json
    String contentType = req.getContentType();
    if (contentType == null || !contentType.contains(MimeType.JSON.getType())) {
      LOG.info("Request with wrong content type received, ignoring. ContentType: {}, IP:{}", contentType, req.getRemoteAddr());
      resp.sendError(HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE);
      return true;
    }

    final long startNanos = System.nanoTime();
    if (LOG.isDebugEnabled()) {
      LOG.debug("JSON request started");
    }

    IUiSession uiSession = null;
    JsonRequest jsonRequest = null;
    try {
      // disable caching
      m_httpCacheControl.checkAndSetCacheHeaders(req, resp, null, null);

      JSONObject jsonObject = m_jsonRequestHelper.readJsonRequest(req);
      jsonRequest = new JsonRequest(jsonObject);

      if (jsonRequest.getRequestType() == RequestType.PING_REQUEST) {
        // No UI session required for ping
        handlePingRequest(resp);
        return true;
      }

      // Resolve UI session
      if (jsonRequest.getRequestType() == RequestType.STARTUP_REQUEST) {
        JsonStartupRequest jsonStartupRequest = new JsonStartupRequest(jsonRequest);
        if (!validateVersion(jsonStartupRequest, resp)) {
          return true;
        }
        // Always create a new UI Session on startup
        uiSession = createUiSession(req, resp, jsonStartupRequest);
      }
      else {
        // Get and validate existing UI session
        uiSession = UiSession.get(req, jsonRequest);
        if (!validateUiSession(uiSession, resp, jsonRequest)) {
          return true;
        }

        // Touch the session (except for poll requests --> max idle timeout)
        if (jsonRequest.getRequestType() != RequestType.POLL_REQUEST) {
          uiSession.touch();
        }
      }

      // Associate subsequent processing with the uiSession and jsonRequest.
      RunContexts.copyCurrent()
          .withThreadLocal(IUiSession.CURRENT, uiSession)
          .withThreadLocal(JsonRequest.CURRENT, jsonRequest)
          .withDiagnostics(BEANS.all(IUiRunContextDiagnostics.class))
          .run(() -> handleJsonRequest(IUiSession.CURRENT.get(), JsonRequest.CURRENT.get(), req, resp), DefaultExceptionTranslator.class);
    }
    catch (Exception | PlatformError e) {
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
    finally {
      if (LOG.isDebugEnabled()) {
        LOG.debug("JSON request completed in {} ms", StringUtility.formatNanos(System.nanoTime() - startNanos));
      }
    }
    return true;
  }

  protected void handleJsonRequest(IUiSession uiSession, JsonRequest jsonRequest, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws IOException {
    // If client sent ACK#, cleanup response history accordingly
    uiSession.confirmResponseProcessed(jsonRequest.getAckSequenceNo());

    switch (jsonRequest.getRequestType()) {
      case LOG_REQUEST:
        handleLogRequest(httpServletResponse, uiSession, jsonRequest);
        return;
      case CANCEL_REQUEST:
        handleCancelRequest(httpServletResponse, uiSession);
        return;
      case UNLOAD_REQUEST:
        handleUnloadRequest(httpServletResponse, uiSession, jsonRequest);
        return;
      case SYNC_RESPONSE_QUEUE:
        handleSyncResponseQueueRequest(httpServletResponse, uiSession, jsonRequest);
        return;
      case REQUEST:
      case STARTUP_REQUEST:
      case POLL_REQUEST:
        break; // <-- !
      default:
        throw new IllegalStateException("Unexpected request type: " + jsonRequest.getRequestType());
    }

    // GUI requests for the same session must be processed consecutively, therefore acquire "UI session lock"
    if (jsonRequest.getRequestType() == RequestType.POLL_REQUEST) {
      // Block for a certain time
      boolean success = handlePollRequest(uiSession, jsonRequest);
      if (!success) {
        return; // Interrupted while waiting -> return immediately without sending a response
      }
      // Special case: Poll requests should only *try* to acquire the lock. If the lock is currently acquired
      // by some other thread, there is no reason to wait for it, because the other thread will already send
      // the entire JSON response to the UI. Waiting for too long here could cause the UI session to time out,
      // because the poller-induced "heart beat" mechanism would stop. Therefore, if the lock cannot be acquired,
      // an empty response is sent back to the UI.
      if (!uiSession.uiSessionLock().tryLock()) {
        writeJsonResponse(httpServletResponse, m_jsonRequestHelper.createEmptyResponse());
        return;
      }
    }
    else {
      uiSession.uiSessionLock().lock(); // NOSONAR
    }
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

    boolean platformValid = (Platform.get() != null && Platform.get().getState() == State.PlatformStarted);
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

  protected void handleLogRequest(HttpServletResponse resp, IUiSession uiSession, JsonRequest jsonRequest) throws IOException {
    String message = jsonRequest.getMessage();
    JSONObject event = jsonRequest.getEvent();

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

  /**
   * @return <code>true</code> if the request is still valid after polling and response should be sent back to the UI.
   *         <code>false</code> when the polling was interrupted and the processing should be stopped immediately.
   */
  protected boolean handlePollRequest(IUiSession uiSession, JsonRequest jsonRequest) {
    int curIdle = (int) ((System.currentTimeMillis() - uiSession.getLastAccessedTime()) / 1000L);
    int maxIdle = m_maxUserIdleTime;
    // Default don't wait longer than the container timeout for security reasons. However, the minimum is _not_ 0,
    // because that might trigger many very short polling calls until the ui session is really disposed.
    int pollWait = Math.max(Math.min(maxIdle - curIdle, m_pollingInterval), 3);
    LOG.debug("Polling begin for {} seconds", pollWait);
    // Blocks the current thread until:
    // - a model job terminates
    // - the max. wait time has exceeded
    final long startNanos = System.nanoTime();
    try {
      uiSession.waitForBackgroundJobs(jsonRequest, pollWait);
    }
    catch (InterruptedException e) {
      if (LOG.isDebugEnabled()) {
        LOG.debug("Polling INTERRUPTED after {} ms (ignoring response)", StringUtility.formatNanos(System.nanoTime() - startNanos));
      }
      return false;
    }
    if (LOG.isDebugEnabled()) {
      LOG.debug("Polling end after {} ms", StringUtility.formatNanos(System.nanoTime() - startNanos));
    }
    return true;
  }

  protected void handleSessionTimeout(HttpServletResponse resp, JsonRequest jsonReq) throws IOException {
    LOG.info("Request cannot be processed due to UI session timeout [id={}]", jsonReq.getUiSessionId());
    writeJsonResponse(resp, m_jsonRequestHelper.createSessionTimeoutResponse());
  }

  protected void handleMaxIdeTimeout(HttpServletResponse resp, IUiSession uiSession, int idleSeconds, int maxIdleSeconds) throws IOException {
    LOG.info("Detected idle timeout for UI session {} after {} seconds (maxIdleSeconds={})", uiSession.getUiSessionId(), idleSeconds, maxIdleSeconds);
    uiSession.uiSessionLock().lock();
    try {
      uiSession.dispose();
    }
    finally {
      uiSession.uiSessionLock().unlock();
    }
    writeJsonResponse(resp, m_jsonRequestHelper.createSessionTimeoutResponse());
  }

  /**
   * Handle "?unload" JSON requests from browsers that don't support the Beacon API. @see {@link UnloadRequestHandler}
   */
  protected void handleUnloadRequest(HttpServletResponse resp, IUiSession uiSession, JsonRequest jsonReq) throws IOException {
    LOG.info("Unloading UI session with ID {} (requested by UI)", jsonReq.getUiSessionId());
    if (uiSession != null) {
      final ReentrantLock uiSessionLock = uiSession.uiSessionLock();
      uiSessionLock.lock();
      try {
        uiSession.dispose();
      }
      finally {
        uiSessionLock.unlock();
      }
    }
    writeJsonResponse(resp, m_jsonRequestHelper.createEmptyResponse()); // send empty response to satisfy clients expecting a valid response
  }

  protected void handleSyncResponseQueueRequest(HttpServletResponse resp, IUiSession uiSession, JsonRequest jsonReq) throws IOException {
    LOG.info("Sync response queue for UI session {}", uiSession.getUiSessionId());
    // The "sync" request needs to acquire the UI session lock. If a request is still executing,
    // this will cause the "sync" request to block. It is important to wait for the other request
    // to finish, because we don't know if that request is still "connected" to a response channel.
    // If it is not (because the connection was lost in the mean time), we would miss it's response.
    final ReentrantLock uiSessionLock = uiSession.uiSessionLock();
    uiSessionLock.lock();
    try {
      JSONObject response = uiSession.processSyncResponseQueueRequest(jsonReq);
      if (response == null) {
        response = m_jsonRequestHelper.createEmptyResponse();
      }
      writeJsonResponse(resp, response);
    }
    finally {
      uiSessionLock.unlock();
    }
  }

  protected IUiSession createUiSession(HttpServletRequest req, HttpServletResponse resp, JsonStartupRequest jsonStartupReq) {
    HttpSession httpSession = req.getSession();
    ISessionStore sessionStore = m_httpSessionHelper.getSessionStore(httpSession);

    final long startNanos = System.nanoTime();
    if (LOG.isDebugEnabled()) {
      LOG.debug("JSON request started");
    }
    LOG.debug("Creating new UI session....");
    IUiSession uiSession = BEANS.get(IUiSession.class);
    uiSession.init(req, resp, jsonStartupReq);
    sessionStore.registerUiSession(uiSession);

    LOG.info("Created new UI session with ID {} in {} ms [maxIdleTime={}s, httpSession.maxInactiveInterval={}s]",
        uiSession.getUiSessionId(), StringUtility.formatNanos(System.nanoTime() - startNanos), m_maxUserIdleTime, req.getSession().getMaxInactiveInterval());
    return uiSession;
  }

  /**
   * @return <code>true</code> if session is valid, <code>false</code> otherwise
   */
  protected boolean validateUiSession(final IUiSession uiSession, final HttpServletResponse resp, final JsonRequest jsonRequest) throws IOException {
    // check if session was already disposed
    if (uiSession == null) {
      handleSessionTimeout(resp, jsonRequest);
      return false;
    }

    // check if max idle timeout has been reached for this session
    int idleSeconds = (int) ((System.currentTimeMillis() - uiSession.getLastAccessedTime()) / 1000L);
    int maxIdleSeconds = m_maxUserIdleTime;
    if (idleSeconds > maxIdleSeconds) {
      handleMaxIdeTimeout(resp, uiSession, idleSeconds, maxIdleSeconds);
      return false;
    }

    // valid
    return true;
  }

  protected boolean validateVersion(JsonStartupRequest jsonStartupRequest, HttpServletResponse resp) throws IOException {
    String requestVersion = jsonStartupRequest.getVersion();
    String applicationVersion = CONFIG.getPropertyValue(ApplicationVersionProperty.class);
    if (requestVersion != null && !requestVersion.equals(applicationVersion)) {
      LOG.info("Requested UI version '{}' does not match the actual version '{}'", requestVersion, applicationVersion);
      writeJsonResponse(resp, m_jsonRequestHelper.createVersionMismatchResponse());
      return false;
    }
    return true; // valid
  }

  /**
   * Writes the given {@link JSONObject} into the given {@link ServletResponse}.
   */
  protected void writeJsonResponse(ServletResponse servletResponse, JSONObject jsonObject) throws IOException {
    m_jsonRequestHelper.writeResponse(servletResponse, jsonObject);
  }
}
