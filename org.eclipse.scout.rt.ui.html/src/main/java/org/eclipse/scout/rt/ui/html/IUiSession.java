/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.ui.html;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

import javax.security.auth.Subject;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import org.eclipse.scout.rt.client.IClientSession;
import org.eclipse.scout.rt.platform.Bean;
import org.eclipse.scout.rt.platform.resource.BinaryResource;
import org.eclipse.scout.rt.server.commons.servlet.cache.IHttpResourceCache;
import org.eclipse.scout.rt.ui.html.json.IJsonAdapter;
import org.eclipse.scout.rt.ui.html.json.JsonMessageRequestHandler;
import org.eclipse.scout.rt.ui.html.json.JsonRequest;
import org.eclipse.scout.rt.ui.html.json.JsonResponse;
import org.eclipse.scout.rt.ui.html.json.JsonStartupRequest;
import org.eclipse.scout.rt.ui.html.res.IUploadable;
import org.json.JSONObject;

@Bean
public interface IUiSession {

  /**
   * The {@link IUiSession} which is currently associated with the current thread.
   */
  ThreadLocal<IUiSession> CURRENT = new ThreadLocal<>();

  /**
   * Cookie name used to store the preferred language of a user (even after user has logged out).
   */
  String PREFERRED_LOCALE_COOKIE_NAME = "scout.preferredLocale";

  /**
   * Returns a reentrant lock that can be used to synchronize on the {@link IUiSession}.
   */
  ReentrantLock uiSessionLock();

  void init(HttpServletRequest req, HttpServletResponse resp, JsonStartupRequest jsonStartupReq);

  /**
   * @return <code>true</code> if {@link #init(HttpServletRequest, HttpServletResponse, JsonStartupRequest)} was been
   *         called. Note: This will also be <code>true</code> after the session has been disposed.
   */
  boolean isInitialized();

  /**
   * Used to mark the session as persistent which means a persistent session cookie is used and the client session will
   * be restored after a browser restart.
   * <p>
   * This was mainly introduced for the iOS home screen mode. If the app is running in that mode the HTTP session will
   * be lost whenever the user leaves the app (e.g. switches to another app or just downloads a file). If persistent is
   * set to true the client session id will be put in the local storage instead of the session storage so that it can be
   * reused the next time the app is activated.
   */
  boolean isPersistent();

  String getHttpSessionId();

  String getUiSessionId();

  String getClientSessionId();

  IClientSession getClientSession();

  /**
   * All requests except the polling requests are calling this method from the {@link JsonMessageRequestHandler}
   * <p>
   * Note that {@link HttpSession#getLastAccessedTime()} is also updated on polling requests
   */
  void touch();

  /**
   * @return the last access time in millis since 01.01.1970 of a request, except polling requests
   *         <p>
   *         see {@link #touch()}
   */
  long getLastAccessedTime();

  /**
   * Marks the UI session is disposed (irreversible) and destroys the internal data structures and references (i.e. it
   * disposes the JSON adapter registry).
   * <p>
   * <b>Important:</b> This method must only be called in a context that holds the {@link #uiSessionLock()}! (Either the
   * current thread or a caller that waits for the current thread.)
   */
  void dispose();

  /**
   * @return <code>true</code> if {@link #dispose()} has been called.
   */
  boolean isDisposed();

  /**
   * @return the current UI response that is collecting changes for the next
   *         {@link #processJsonRequest(HttpServletRequest, HttpServletResponse, JsonRequest)} cycle. This is never
   *         <code>null</code>, <b>except</b> when the UI session is disposed (see {@link #isDisposed()}).
   */
  JsonResponse currentJsonResponse();

  HttpServletRequest currentHttpRequest();

  HttpServletResponse currentHttpResponse();

  /**
   * Used to confirm that the UI has successfully processed the given response sequence number. All responses that have
   * a sequence number <code>&lt;= sequenceNo</code> are removed from the response history.
   */
  void confirmResponseProcessed(Long sequenceNo);

  /**
   * Used to verify if the {@link Subject} we're running in should be replaced on the {@link IClientSession}.
   *
   * @param request
   *          the request that is checked if the update of the Subject has been requested
   */
  void verifySubject(HttpServletRequest request);

  /**
   * @return a JSON object to send back to the client or <code>null</code> if an empty response shall be sent.
   */
  JSONObject processJsonRequest(HttpServletRequest req, HttpServletResponse resp, JsonRequest jsonReq);

  /**
   * @param req
   *          the HTTP request
   * @param uploadable
   *          the target adapter that receives the uploaded files
   * @param uploadResources
   *          list of uploaded files
   * @param uploadProperties
   *          a map of all other submitted string properties (usually not needed)
   * @return a JSON object to send back to the client or <code>null</code> if an empty response shall be sent.
   */
  JSONObject processFileUpload(HttpServletRequest req, HttpServletResponse resp, IUploadable uploadable,
      List<BinaryResource> uploadResources, Map<String, String> uploadProperties);

  void processCancelRequest();

  JSONObject processSyncResponseQueueRequest(JsonRequest jsonRequest);

  /**
   * Called from the model after the client session has been stopped.
   * <p>
   * <b>Important:</b> Because this method internally calls {@link #dispose()}, it must only be called in a context that
   * holds the {@link #uiSessionLock()}! (Either the current thread or a caller that waits for the current thread.)
   */
  void logout();

  /**
   * @return the URL where to redirect the UI on logout
   */
  String getLogoutRedirectUrl();

  IJsonAdapter<?> getRootJsonAdapter();

  String createUniqueId();

  /**
   * Returns an existing IJsonAdapter instance for the given adapter ID.
   */
  IJsonAdapter<?> getJsonAdapter(String id);

  /**
   * Returns all JSON adapters which belong to the given model object.
   */
  <M> List<IJsonAdapter<M>> getJsonAdapters(M model);

  List<IJsonAdapter<?>> getJsonChildAdapters(IJsonAdapter<?> parent);

  /**
   * Returns an existing IJsonAdapter instance for the given model object.
   */
  <M, A extends IJsonAdapter<M>> A getJsonAdapter(M model, IJsonAdapter<?> parent);

  <M, A extends IJsonAdapter<M>> A getJsonAdapter(M model, IJsonAdapter<?> parent, boolean checkRoot);

  /**
   * Creates a new initialized IJsonAdapter instance for the given model or returns an existing instance. As a
   * side-effect a newly created adapter is added to the current JSON response.
   */
  <M, A extends IJsonAdapter<M>> A getOrCreateJsonAdapter(M model, IJsonAdapter<?> parent);

  <M, A extends IJsonAdapter<M>> A createJsonAdapter(M model, IJsonAdapter<?> parent);

  void registerJsonAdapter(IJsonAdapter<?> adapter);

  void unregisterJsonAdapter(IJsonAdapter<?> adapter);

  /**
   * Blocks the current thread/request until a model job started by a background job has terminated.
   */
  void waitForBackgroundJobs(JsonRequest jsonRequest, int pollWaitSeconds) throws InterruptedException;

  /**
   * Sends a "localeChanged" event to the UI. All locale-relevant data (number formats, texts map etc.) is sent along.
   */
  void sendLocaleChangedEvent(Locale locale);

  /**
   * Sends an explicit "disposeAdapter" event to the UI. Usually, JSON adapters are disposed automatically when their
   * parent adapter is disposed. However, for some adapters this has to be done manually, because their parent is not
   * disposed (e.g. forms).
   */
  void sendDisposeAdapterEvent(IJsonAdapter<?> adapter);

  /**
   * Sends a reload page event to trigger a page reload.
   */
  void sendReloadPageEvent();

  /**
   * Sets the new theme (session & cookie) and triggers a reload of the current page in the browser.
   */
  void updateTheme(String theme);

  /**
   * @return The {@link IHttpResourceCache} to use to cache resources for this {@link IUiSession}.
   */
  IHttpResourceCache getHttpResourceCache();

  UiSessionListeners listeners();

  default void addListener(UiSessionListener listener, Integer... eventTypes) {
    listeners().add(listener, false, eventTypes);
  }

  default void removeListener(UiSessionListener listener, Integer... eventTypes) {
    listeners().remove(listener, eventTypes);
  }
}
