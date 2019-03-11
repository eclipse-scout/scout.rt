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

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;

import org.eclipse.scout.rt.rest.client.proxy.IRestClientExceptionTransformer;

/**
 * Interface to a generic REST client helper dealing with REST requests to an API server.
 * <p>
 * Implementers of this class are expected to provide support for cancellation and exception transformation.
 */
public interface IRestClientHelper {

  /**
   * Creates a new {@link WebTarget} that uses the default exception transformer defined by the implementing class.
   *
   * @param resourcePath
   *          Path to the resource, relative to the API root. This path must <i>not</i> contain template strings (they
   *          would be encoded).
   */
  WebTarget target(String resourcePath);

  /**
   * @param resourcePath
   *          Path to the resource, relative to the API root. This path must <i>not</i> contain template strings (they
   *          would be encoded).
   * @param exceptionTransformer
   *          optional {@link IRestClientExceptionTransformer} used by the returned {@link WebTarget} and any objects it
   *          creates to transform {@link WebApplicationException}s and {@link javax.ws.rs.ProcessingException}s. The
   *          {@code null}-transformer returns the passed exception unchanged.
   */
  WebTarget target(String resourcePath, IRestClientExceptionTransformer exceptionTransformer);

  /**
   * Applies all specified query parameters to the specified {@code target}
   */
  WebTarget applyQueryParams(WebTarget target, Map<String, Object> queryParams);

  /**
   * @return {@link Entity} containing an empty JSON string.
   * @deprecated Do not use this method, it will be removed in 9.0.x! Use <code>Entity.json("")</code> instead.
   */
  @Deprecated
  Entity<String> emptyJson();
}
