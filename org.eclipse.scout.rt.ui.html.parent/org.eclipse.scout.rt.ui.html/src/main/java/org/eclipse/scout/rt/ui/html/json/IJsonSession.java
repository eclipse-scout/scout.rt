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

public interface IJsonSession {

  void init(HttpServletRequest request, JsonRequest jsonReq) throws JsonException;

  IClientSession getClientSession();

  String createUniqueIdFor(IJsonAdapter<?> adapter);

  /**
   * Returns an IJsonAdapter instance for the given adapter ID.
   */
  IJsonAdapter<?> getJsonAdapter(String id);

  /**
   * Returns an IJsonAdapter instance for the given model object.
   */
  IJsonAdapter<?> getJsonAdapter(Object modelObject);

  /**
   * Creates a new IJsonAdapter instance for the given modelObject, or returns an existing instance.
   */
  IJsonAdapter<?> getOrCreateJsonAdapter(Object modelObject); // TODO CGU: rename to 'model'

  /**
   * Creates a new IJsonAdapter instance for the given modelObject.
   */
  IJsonAdapter<?> createJsonAdapter(Object modelObject);

  void registerJsonAdapter(IJsonAdapter<?> adapter);

  void unregisterJsonAdapter(String id);

  /**
   * @return the current ui response that is collecting changes for the next
   *         {@link #processRequest(HttpServletRequest, JsonRequest)} cycle
   */
  JsonResponse currentJsonResponse();

  HttpServletRequest currentHttpRequest();

  JsonResponse processRequest(HttpServletRequest httpReq, JsonRequest jsonReq) throws JsonException;

}
