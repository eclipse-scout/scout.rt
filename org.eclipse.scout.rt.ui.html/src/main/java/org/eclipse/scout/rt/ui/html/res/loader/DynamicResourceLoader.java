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

import javax.servlet.http.HttpServletRequest;

import org.eclipse.scout.rt.platform.resource.BinaryResource;
import org.eclipse.scout.rt.server.commons.servlet.cache.HttpCacheKey;
import org.eclipse.scout.rt.server.commons.servlet.cache.HttpCacheObject;
import org.eclipse.scout.rt.server.commons.servlet.cache.IHttpResourceCache;
import org.eclipse.scout.rt.server.commons.servlet.cache.IHttpResponseInterceptor;
import org.eclipse.scout.rt.ui.html.IUiSession;
import org.eclipse.scout.rt.ui.html.json.IJsonAdapter;
import org.eclipse.scout.rt.ui.html.res.BinaryResourceHolder;
import org.eclipse.scout.rt.ui.html.res.IBinaryResourceProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class loads resources that are temporary or dynamically registered on the {@link IUiSession}. This includes
 * adapter/form-fields such as the image field, WordAddIn docx documents, temporary and time-limited landing page files
 * etc.
 * <p>
 * The pathInfo is expected to have the following form: <code>/dynamic/[uiSessionId]/[adapterId]/[filename]</code>
 */
public class DynamicResourceLoader extends AbstractResourceLoader {

  private static final Logger LOG = LoggerFactory.getLogger(DynamicResourceLoader.class);

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
      LOG.warn("invalid dynamic-resource request received.", new Exception("origin"));
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
  public BinaryResource loadResource(String pathInfo) throws IOException {
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
