/*******************************************************************************
 * Copyright (c) 2014-2016 BSI Business Systems Integration AG.
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

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.resource.BinaryResource;
import org.eclipse.scout.rt.server.commons.servlet.cache.GlobalHttpResourceCache;
import org.eclipse.scout.rt.server.commons.servlet.cache.HttpCacheKey;
import org.eclipse.scout.rt.server.commons.servlet.cache.HttpCacheObject;
import org.eclipse.scout.rt.server.commons.servlet.cache.IHttpResourceCache;

/**
 * Abstract base class for resource loaders.
 */
public abstract class AbstractResourceLoader implements IResourceLoader {

  private final IHttpResourceCache m_cache;

  protected AbstractResourceLoader() {
    this(BEANS.get(GlobalHttpResourceCache.class));
  }

  /**
   * @param cache
   *          The {@link IHttpResourceCache} to use for this loader. May be <code>null</code> (which means no caching
   *          for this loader).
   */
  protected AbstractResourceLoader(IHttpResourceCache cache) {
    m_cache = cache; // may also be null
  }

  @Override
  public HttpCacheObject loadResource(HttpCacheKey cacheKey) throws IOException {
    String pathInfo = cacheKey.getResourcePath();
    BinaryResource content = loadResource(pathInfo);
    if (content == null) {
      return null;
    }
    return new HttpCacheObject(cacheKey, content);
  }

  /**
   * Override this method if your resource loader must create a special cache key which does not only contain the
   * resourcePath but also additional elements like locale, theme and so on.
   * <p>
   * The default impl. uses the resourcePath as cache key.
   */
  @Override
  public HttpCacheKey createCacheKey(String resourcePath) {
    return new HttpCacheKey(resourcePath);
  }

  @Override
  public IHttpResourceCache getCache(HttpCacheKey cacheKey) {
    return m_cache;
  }
}
