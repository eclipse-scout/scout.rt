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

import java.nio.charset.CharsetEncoder;
import java.nio.charset.StandardCharsets;

import javax.servlet.http.HttpServletRequest;

import org.eclipse.scout.rt.platform.resource.BinaryResource;
import org.eclipse.scout.rt.platform.util.IOUtility;
import org.eclipse.scout.rt.platform.util.StringUtility;
import org.eclipse.scout.rt.server.commons.servlet.cache.HttpCacheKey;
import org.eclipse.scout.rt.server.commons.servlet.cache.HttpCacheObject;
import org.eclipse.scout.rt.server.commons.servlet.cache.HttpResponseHeaderContributor;
import org.eclipse.scout.rt.server.commons.servlet.cache.IHttpResourceCache;
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
  private static final String DEFAULT_FILENAME = "Download";

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
    if (localResourceHolder.isDownload()) {
      addResponseHeaderForDownload(httpCacheObject, localResource.getFilename());
    }
    return httpCacheObject;
  }

  protected IBinaryResourceProvider getBinaryResourceProvider(IUiSession uiSession, String adapterId) {
    IJsonAdapter<?> jsonAdapter = uiSession.getJsonAdapter(adapterId);
    if (!(jsonAdapter instanceof IBinaryResourceProvider)) {
      return null;
    }
    return (IBinaryResourceProvider) jsonAdapter;
  }

  /**
   * Sets the <code>Content-Disposition</code> HTTP header for downloads (with value <code>attachment</code>).
   * Additionally, a hint for the filename is added according to RFC 5987, both in UTF-8 and ISO-8859-1 encoding.
   * <p>
   * See:<i><br>
   * http://stackoverflow.com/questions/93551/how-to-encode-the-filename-parameter-of-content-disposition-header-in-http
   * <br/>
   * http://tools.ietf.org/html/rfc6266#section-5</i>
   */
  protected void addResponseHeaderForDownload(HttpCacheObject httpCacheObject, String originalFilename) {
    String isoFilename = getIsoFilename(originalFilename);
    if (StringUtility.isNullOrEmpty(originalFilename)) {
      originalFilename = DEFAULT_FILENAME;
    }
    if (StringUtility.isNullOrEmpty(isoFilename)) { // in case no valid character remaines
      isoFilename = DEFAULT_FILENAME;
    }

    // Set hint for browser to show the "save as" dialog (no in-line display, not even for known types, e.g. XML)
    httpCacheObject.addHttpResponseInterceptor(new HttpResponseHeaderContributor(
        "Content-Disposition",
        "attachment; filename=\"" + isoFilename + "\"; filename*=utf-8''" + IOUtility.urlEncode(originalFilename)));
  }

  /**
   * Returns the given filename in ISO-8859-1. All characters that are not part of this charset are stripped.
   */
  protected String getIsoFilename(String originalFilename) {
    String isoFilename = originalFilename;
    CharsetEncoder iso8859Encoder = StandardCharsets.ISO_8859_1.newEncoder();
    if (iso8859Encoder.canEncode(originalFilename)) {
      return isoFilename;
    }

    StringBuilder sb = new StringBuilder(originalFilename.length() - 1);
    for (char c : originalFilename.toCharArray()) {
      if (c != '"' && iso8859Encoder.canEncode(c)) {
        sb.append(c);
      }
    }
    return sb.toString();
  }

  public HttpServletRequest getRequest() {
    return m_req;
  }
}
