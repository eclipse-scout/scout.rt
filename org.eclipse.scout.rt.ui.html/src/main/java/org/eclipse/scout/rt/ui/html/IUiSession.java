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
package org.eclipse.scout.rt.ui.html;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.eclipse.scout.rt.client.IClientSession;
import org.eclipse.scout.rt.platform.Bean;
import org.eclipse.scout.rt.platform.resource.BinaryResource;
import org.eclipse.scout.rt.ui.html.json.IJsonAdapter;
import org.eclipse.scout.rt.ui.html.json.JsonMessageRequestHandler;
import org.eclipse.scout.rt.ui.html.json.JsonRequest;
import org.eclipse.scout.rt.ui.html.json.JsonResponse;
import org.eclipse.scout.rt.ui.html.json.JsonStartupRequest;
import org.eclipse.scout.rt.ui.html.res.IBinaryResourceConsumer;
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

  void init(HttpServletRequest req, HttpServletResponse resp, JsonStartupRequest jsonStartupReq);

  /**
   * @return <code>true</code> if {@link #init(HttpServletRequest, JsonStartupRequest)} was been called. Note: This will
   *         also be <code>true</code> after the session has been disposed.
   */
  boolean isInitialized();

  /**
   * Returns a reentrant lock that can be used to synchronize on the {@link IUiSession}.
   */
  ReentrantLock uiSessionLock();

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
   * Called from the model after the client session has been stopped.
   * <p>
   * <b>Important:</b> Because this method internally calls {@link #dispose()}, it must only be called in a context that
   * holds the {@link #uiSessionLock()}! (Either the current thread or a caller that waits for the current thread.)
   */
  void logout();

  /**
   * @return the current UI response that is collecting changes for the next
   *         {@link #processJsonRequest(HttpServletRequest, JsonRequest)} cycle. This is never <code>null</code>,
   *         <b>except</b> when the UI session is disposed (see {@link #isDisposed()}).
   */
  JsonResponse currentJsonResponse();

  HttpServletRequest currentHttpRequest();

  HttpServletResponse currentHttpResponse();

  /**
   * @return a JSON object to send back to the client or <code>null</code> if an empty response shall be sent.
   */
  JSONObject processJsonRequest(HttpServletRequest req, HttpServletResponse resp, JsonRequest jsonReq);

  /**
   * @param httpRequest
   *          the HTTP request
   * @param resourceConsumer
   *          the target adapter that receives the uploaded files
   * @param uploadResources
   *          list of uploaded files
   * @param uploadProperties
   *          a map of all other submitted string properties (usually not needed)
   * @return a JSON object to send back to the client or <code>null</code> if an empty response shall be sent.
   */
  JSONObject processFileUpload(HttpServletRequest req, HttpServletResponse resp, IBinaryResourceConsumer resourceConsumer,
      List<BinaryResource> uploadResources, Map<String, String> uploadProperties);

  void processCancelRequest();

  String getUiSessionId();

  String getClientSessionId();

  IClientSession getClientSession();

  IJsonAdapter<?> getRootJsonAdapter();

  /**
   * Returns an existing IJsonAdapter instance for the given adapter ID.
   */
  IJsonAdapter<?> getJsonAdapter(String id);

  /**
   * Returns an existing IJsonAdapter instance for the given model object.
   */
  <M, A extends IJsonAdapter<? super M>> A getJsonAdapter(M model, IJsonAdapter<?> parent);

  <M, A extends IJsonAdapter<? super M>> A getJsonAdapter(M model, IJsonAdapter<?> parent, boolean checkRoot);

  List<IJsonAdapter<?>> getJsonChildAdapters(IJsonAdapter<?> parent);

  /**
   * Returns all JSON adapters which belong to the given model object.
   */
  <M> List<IJsonAdapter<M>> getJsonAdapters(M model);

  /**
   * Creates a new initialized IJsonAdapter instance for the given model or returns an existing instance. As a
   * side-effect a newly created adapter is added to the current JSON response.
   */
  <M, A extends IJsonAdapter<? super M>> A getOrCreateJsonAdapter(M model, IJsonAdapter<?> parent);

  <M, A extends IJsonAdapter<? super M>> A createJsonAdapter(M model, IJsonAdapter<?> parent);

  String createUniqueId();

  void registerJsonAdapter(IJsonAdapter<?> adapter);

  void unregisterJsonAdapter(String id);

  /**
   * @return the URL where to redirect the UI on logout
   */
  String getLogoutRedirectUrl();

  boolean isInspectorHint();

  /**
   * Blocks the current thread/request until a model job started by a background job has terminated.
   */
  void waitForBackgroundJobs(int pollWaitSeconds);

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
   * Sets the new theme (session & cookie) and triggers a reload of the current page in the browser.
   *
   * @param theme
   */
  void updateTheme(String theme);
}
