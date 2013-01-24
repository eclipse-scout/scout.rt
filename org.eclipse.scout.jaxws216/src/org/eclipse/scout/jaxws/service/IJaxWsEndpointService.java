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
package org.eclipse.scout.jaxws.service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.scout.jaxws.internal.servlet.ServletAdapter;
import org.eclipse.scout.service.IService;

/**
 * Service to access installed JAX-WS endpoints and intercept HTTP-GET request that are not target to a JAX-WS endpoint.
 */
public interface IJaxWsEndpointService extends IService {

  /**
   * To get a specific JAX-WS endpoint
   * 
   * @param alias
   *          the alias of the JAX-WS endpoint to be looked for
   * @return the {@link ServletAdapter} or null if no JAX-WS endpoint exists for the given alias
   */
  ServletAdapter getServletAdapter(String alias);

  /**
   * To get all installed JAX-WS endpoints
   * 
   * @return all installed JAX-WS endpoints
   */
  ServletAdapter[] getServletAdapters();

  /**
   * To get the authentication method of a given {@link ServletAdapter}
   * 
   * @param adapter
   *          {@link ServletAdapter}
   * @return the authentication method or "None" if not applicable
   */
  String getAuthenticationMethod(ServletAdapter adapter);

  /**
   * To intercept a HTTP-GET request that is not target to a JAX-WS endpoint.
   * 
   * @param request
   *          {@link HttpServletRequest}
   * @param response
   *          {@link HttpServletResponse}
   * @param servletAdapters
   *          the JAX-WS endpoints installed
   * @throws Exception
   */
  void onGetRequest(HttpServletRequest request, HttpServletResponse response, ServletAdapter[] servletAdapters) throws Exception;
}
