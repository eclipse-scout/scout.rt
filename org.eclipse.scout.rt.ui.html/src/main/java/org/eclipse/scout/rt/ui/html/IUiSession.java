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
package org.eclipse.scout.rt.ui.html;

import java.util.List;
import java.util.Locale;
import java.util.concurrent.locks.ReentrantLock;

import javax.servlet.http.HttpServletRequest;

import org.eclipse.scout.rt.client.IClientSession;
import org.eclipse.scout.rt.platform.Bean;
import org.eclipse.scout.rt.ui.html.json.IJsonAdapter;
import org.eclipse.scout.rt.ui.html.json.IJsonObjectFactory;
import org.eclipse.scout.rt.ui.html.json.JsonClientSession;
import org.eclipse.scout.rt.ui.html.json.JsonRequest;
import org.eclipse.scout.rt.ui.html.json.JsonResponse;
import org.eclipse.scout.rt.ui.html.json.JsonStartupRequest;
import org.json.JSONObject;

@Bean
public interface IUiSession {

  /**
   * Prefix for name of HTTP session attribute that is used to store the associated {@link IUiSession}s.
   * <p>
   * The full attribute name is: <b><code>{@link #HTTP_SESSION_ATTRIBUTE_PREFIX} + uiSessionId</code></b>
   */
  String HTTP_SESSION_ATTRIBUTE_PREFIX = "scout.htmlui.uisession."/*+JsonRequest.PROP_UI_SESSION_ID*/;

  void init(HttpServletRequest request, JsonStartupRequest jsonStartupRequest);

  /**
   * Returns a reentrant lock that can be used to synchronize on the {@link IUiSession}.
   */
  ReentrantLock uiSessionLock();

  void dispose();

  void logout();

  /**
   * @return the current ui response that is collecting changes for the next
   *         {@link #processRequest(HttpServletRequest, JsonRequest)} cycle
   */
  JsonResponse currentJsonResponse();

  HttpServletRequest currentHttpRequest();

  JSONObject processRequest(HttpServletRequest httpReq, JsonRequest jsonReq);

  String getUiSessionId();

  String getClientSessionId();

  IClientSession getClientSession();

  JsonClientSession<? extends IClientSession> getJsonClientSession();

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
   * Creates a new initialized IJsonAdapter instance for the given model or returns an existing instance.
   * As a side-effect a newly created adapter is added to the current JSON response.
   */
  <M, A extends IJsonAdapter<? super M>> A getOrCreateJsonAdapter(M model, IJsonAdapter<?> parent);

  <M, A extends IJsonAdapter<? super M>> A getOrCreateJsonAdapter(M model, IJsonAdapter<?> parent, IJsonObjectFactory objectFactory);

  <M, A extends IJsonAdapter<? super M>> A createJsonAdapter(M model, IJsonAdapter<?> parent, IJsonObjectFactory objectFactory);

  <M, A extends IJsonAdapter<? super M>> A createJsonAdapter(M model, IJsonAdapter<?> parent);

  String createUniqueIdFor(IJsonAdapter<?> adapter);

  void registerJsonAdapter(IJsonAdapter<?> adapter);

  void unregisterJsonAdapter(String id);

  boolean isInspectorHint();

  /**
   * Returns whether or not the client (browser) should poll for jobs running in the background.
   * When enabled, the feature is provided either by web-sockets or by long-polling. Web-sockets is
   * the preferred way, but its not supported by every web-container or browser yet. The default
   * implementation returns true.
   */
  boolean isBackgroundJobPollingEnabled();

  /**
   * Blocks the current thread/request until a model job started by a background job has terminated.
   */
  void waitForBackgroundJobs();

  /**
   * Sends a "localeChanged" event to the UI. All locale-relevant data (number formats, texts map etc.) is sent along.
   */
  void sendLocaleChangedEvent(Locale locale);
}
