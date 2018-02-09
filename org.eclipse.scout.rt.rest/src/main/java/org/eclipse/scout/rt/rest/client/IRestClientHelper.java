/*
 * Copyright (c) BSI Business Systems Integration AG. All rights reserved.
 * http://www.bsiag.com/
 */
package org.eclipse.scout.rt.rest.client;

import java.util.Map;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

/**
 * Interface to a generic REST client helper dealing with REST requests to a API server.
 */
public interface IRestClientHelper {

  /**
   * @return new {@link Client} used for REST requests.
   */
  Client client();

  /**
   * @param resourcePath
   *          Path to the resource, relative to the Studio API root. This path must <i>not</i> contain template strings
   *          (they would be encoded).
   */
  WebTarget target(String resourcePath);

  /**
   * Same as {@link #target(String)}, but formats the resource path using {@link String#format(String, Object...)}. This
   * allows the caller to use template strings.
   * <p>
   * For example:<blockquote><code>.target("books/%s%pages/%s", "harry-potter", "246")</code></blockquote> is equivalent
   * to <blockquote><code>.target("books/harry-potter/pages/246")</code></blockquote>
   */
  WebTarget target(String formatString, String... args);

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
