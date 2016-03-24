/*******************************************************************************
 * Copyright (c) 2014-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.ui.html.res.loader;

import java.io.IOException;

import org.eclipse.scout.rt.server.commons.servlet.cache.HttpCacheKey;
import org.eclipse.scout.rt.server.commons.servlet.cache.HttpCacheObject;

/**
 * This is the interface for all individual resource loaders that are collected by
 * {@link ResourceLoaders#create(javax.servlet.http.HttpServletRequest, String)}
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

}
