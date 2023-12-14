/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.rest.client;

import java.util.Map;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.client.WebTarget;

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
   *          creates to transform {@link WebApplicationException}s and {@link jakarta.ws.rs.ProcessingException}s. The
   *          {@code null}-transformer returns the passed exception unchanged.
   */
  WebTarget target(String resourcePath, IRestClientExceptionTransformer exceptionTransformer);

  /**
   * Applies all specified query parameters to the specified {@code target}
   */
  WebTarget applyQueryParams(WebTarget target, Map<String, Object> queryParams);
}
