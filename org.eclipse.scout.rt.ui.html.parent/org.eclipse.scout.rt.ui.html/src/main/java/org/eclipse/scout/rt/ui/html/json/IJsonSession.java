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

import javax.servlet.http.HttpServletRequest;

import org.eclipse.scout.rt.client.IClientSession;
import org.json.JSONObject;

public interface IJsonSession {

  void init(HttpServletRequest request, JsonRequest jsonReq) throws JsonException;

  void dispose();

  String getJsonSessionId();

  IClientSession getClientSession();

  String createUniqueIdFor(IJsonAdapter<?> adapter);

  /**
   * Returns an existing IJsonAdapter instance for the given adapter ID.
   */
  IJsonAdapter<?> getJsonAdapter(String id);

  /**
   * Returns an existing IJsonAdapter instance for the given model object.
   */
  <M, A extends IJsonAdapter<? super M>> A getJsonAdapter(M model);

  /**
   * Creates a new IJsonAdapter instance for the given model or returns an existing instance.
   * As a side-effect a newly created adapter is added to the current JSON response.
   */
  <M, A extends IJsonAdapter<? super M>> A getOrCreateJsonAdapter(M model);

  void registerJsonAdapter(IJsonAdapter<?> adapter);

  void unregisterJsonAdapter(String id);

  /**
   * @return the current ui response that is collecting changes for the next
   *         {@link #processRequest(HttpServletRequest, JsonRequest)} cycle
   */
  JsonResponse currentJsonResponse();

  HttpServletRequest currentHttpRequest();

  JSONObject processRequest(HttpServletRequest httpReq, JsonRequest jsonReq) throws JsonException;

  /**
   * Performs clean-up operations on the current session, after a request has been processed.
   */
  void flush();

}
