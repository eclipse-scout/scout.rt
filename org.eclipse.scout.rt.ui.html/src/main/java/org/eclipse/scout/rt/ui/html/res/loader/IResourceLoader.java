/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.ui.html.res.loader;

import java.io.IOException;

import org.eclipse.scout.rt.platform.resource.BinaryResource;
import org.eclipse.scout.rt.server.commons.servlet.cache.HttpCacheKey;
import org.eclipse.scout.rt.server.commons.servlet.cache.HttpCacheObject;
import org.eclipse.scout.rt.server.commons.servlet.cache.IHttpResourceCache;

/**
 * This is the interface for all individual resource loaders that are collected by
 * {@link ResourceLoaders#create(jakarta.servlet.http.HttpServletRequest, String)}
 */
public interface IResourceLoader {

  /**
   * @param resourcePath
   *          may be null
   * @return the {@link HttpCacheKey} for the resourcePath or null if the resourcePath is not handled
   */
  HttpCacheKey createCacheKey(String resourcePath);

  /**
   * @param cacheKey
   *          not null
   * @return the {@link HttpCacheObject} for the cacheKey or null if the cacheKey is not handled
   */
  HttpCacheObject loadResource(HttpCacheKey cacheKey) throws IOException;

  /**
   * Tries to find the {@link BinaryResource} of the requested path.
   *
   * @return the result if it could be found or {@code null} otherwise.
   */
  BinaryResource loadResource(String pathInfo) throws IOException;

  /**
   * Checks if the specified {@link HttpCacheObject} is a valid response for the requested resource path.
   *
   * @param requestedExternalPath
   *          The requested path.
   * @param cachedObject
   *          The response candidate to validate. May be {@code null}.
   * @return {@code true} if the {@link HttpCacheObject} is valid and can be processed further. {@code false} if the
   *         candidate is not valid (e.g. because it is {@code null} or contains not the expected content).
   */
  boolean validateResource(String requestedExternalPath, HttpCacheObject cachedObject);

  /**
   * Gets the {@link IHttpResourceCache} to be used for this loader.
   *
   * @param cacheKey
   * @return The {@link IHttpResourceCache} to store the {@link HttpCacheObject}s or <code>null</code> if no caching
   *         should be supported for this {@link IResourceLoader}.
   */
  IHttpResourceCache getCache(HttpCacheKey cacheKey);
}
