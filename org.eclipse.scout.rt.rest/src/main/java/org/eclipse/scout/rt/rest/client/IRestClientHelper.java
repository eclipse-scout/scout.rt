/*******************************************************************************
 * Copyright (c) 2010-2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.rest.client;

import java.util.Map;

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

/**
 * Interface to a generic REST client helper dealing with REST requests to a API server.
 */
public interface IRestClientHelper {

  /**
   * @param resourcePath
   *          Path to the resource, relative to the Studio API root. This path must <i>not</i> contain template strings
   *          (they would be encoded).
   */
  WebTarget target(String resourcePath);

  /**
   * Applies all specified query parameters to the specified {@code target}
   */
  WebTarget applyQueryParams(WebTarget target, Map<String, Object> queryParams);

  /**
   * Throws exception if response contains an error.
   */
  void throwOnResponseError(WebTarget target, Response response);

  /**
   * @return {@link Entity} containing an empty JSON string.
   */
  Entity<String> emptyJson();
}
