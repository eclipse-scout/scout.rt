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

import jakarta.servlet.http.HttpServletRequest;

import org.eclipse.scout.rt.platform.resource.BinaryResource;
import org.eclipse.scout.rt.server.commons.servlet.cache.HttpCacheKey;
import org.eclipse.scout.rt.server.commons.servlet.cache.HttpCacheObject;
import org.eclipse.scout.rt.server.commons.servlet.cache.IHttpResourceCache;
import org.eclipse.scout.rt.server.commons.servlet.cache.IHttpResponseInterceptor;
import org.eclipse.scout.rt.ui.html.IUiSession;
import org.eclipse.scout.rt.ui.html.json.IJsonAdapter;
import org.eclipse.scout.rt.ui.html.res.BinaryResourceHolder;
import org.eclipse.scout.rt.ui.html.res.IBinaryResourceProvider;

/**
 * This class loads resources that are temporary or dynamically registered on the {@link IUiSession}. This includes
 * adapter/form-fields such as the image field, WordAddIn docx documents, temporary and time-limited landing page files
 * etc.
 * <p>
 * The pathInfo is expected to have the following form: <code>/dynamic/[uiSessionId]/[adapterId]/[filename]</code>
 */
public class DynamicResourceLoader extends AbstractResourceLoader {

  private final HttpServletRequest m_req;

  public DynamicResourceLoader(HttpServletRequest req) {
    super(null /* no instance required as getCache() is overridden */);
    m_req = req;
  }

  @Override
  public IHttpResourceCache getCache(HttpCacheKey cacheKey) {
    DynamicResourceInfo info = createDynamicResourceInfo(cacheKey);
    if (info == null) {
      return null;
    }
    return info.getUiSession().getHttpResourceCache();
  }

  protected DynamicResourceInfo createDynamicResourceInfo(HttpCacheKey cacheKey) {
    return DynamicResourceInfo.fromPath(getRequest(), cacheKey.getResourcePath());
  }

  @Override
  public HttpCacheObject loadResource(HttpCacheKey cacheKey) {
    DynamicResourceInfo info = createDynamicResourceInfo(cacheKey);
    if (info == null) {
      return null;
    }

    IBinaryResourceProvider provider = getBinaryResourceProvider(info.getUiSession(), info.getJsonAdapterId());
    if (provider == null) {
      return null;
    }

    BinaryResourceHolder localResourceHolder = provider.provideBinaryResource(info.getFileName());
    if (localResourceHolder == null || localResourceHolder.get() == null) {
      return null;
    }
    BinaryResource localResource = localResourceHolder.get();
    BinaryResource httpResource = localResource.createAlias(cacheKey.getResourcePath());
    HttpCacheObject httpCacheObject = new HttpCacheObject(cacheKey, httpResource);
    for (IHttpResponseInterceptor interceptor : localResourceHolder.getHttpResponseInterceptors()) {
      httpCacheObject.addHttpResponseInterceptor(interceptor);
    }
    return httpCacheObject;
  }

  @Override
  public BinaryResource loadResource(String pathInfo) {
    throw new UnsupportedOperationException();
  }

  protected IBinaryResourceProvider getBinaryResourceProvider(IUiSession uiSession, String adapterId) {
    IJsonAdapter<?> jsonAdapter = uiSession.getJsonAdapter(adapterId);
    if (!(jsonAdapter instanceof IBinaryResourceProvider)) {
      return null;
    }
    return (IBinaryResourceProvider) jsonAdapter;
  }

  public HttpServletRequest getRequest() {
    return m_req;
  }
}
