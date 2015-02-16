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

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.eclipse.scout.rt.client.IClientSession;
import org.json.JSONObject;

public interface IJsonSession {

  String HTTP_SESSION_ATTRIBUTE_PREFIX = "scout.htmlui.session.json."/*+JsonRequest.PROP_JSON_SESSION_ID*/;

  void init(HttpServletRequest request, JsonStartupRequest jsonStartupRequest);

  void dispose();

  void logout();

  /**
   * @return the current ui response that is collecting changes for the next
   *         {@link #processRequest(HttpServletRequest, JsonRequest)} cycle
   */
  JsonResponse currentJsonResponse();

  HttpServletRequest currentHttpRequest();

  JSONObject processRequest(HttpServletRequest httpReq, JsonRequest jsonReq);

  ICustomHtmlRenderer getCustomHtmlRenderer();

  /**
   * Performs clean-up operations on the current session, after a request has been processed.
   */
  void flush();

  String getJsonSessionId();

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

  <M, A extends IJsonAdapter<? super M>> A getOrCreateJsonAdapter(M model, IJsonAdapter<?> parent, IJsonAdapterFactory adapterFactory);

  <M, A extends IJsonAdapter<? super M>> A createJsonAdapter(M model, IJsonAdapter<?> parent, IJsonAdapterFactory adapterFactory);

  <M, A extends IJsonAdapter<? super M>> A createJsonAdapter(M model, IJsonAdapter<?> parent);

  String createUniqueIdFor(IJsonAdapter<?> adapter);

  void registerJsonAdapter(IJsonAdapter<?> adapter);

  void unregisterJsonAdapter(String id);
}
