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
import java.net.URL;
import java.net.URLConnection;

import javax.servlet.http.HttpServletRequest;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.resource.BinaryResource;
import org.eclipse.scout.rt.platform.util.IOUtility;
import org.eclipse.scout.rt.ui.html.cache.HttpCacheKey;
import org.eclipse.scout.rt.ui.html.cache.HttpCacheObject;
import org.eclipse.scout.rt.ui.html.cache.IHttpCacheControl;
import org.eclipse.scout.rt.ui.html.res.IWebContentService;

/**
 * This class loads binary files like png, jpg, woff, pdf, docx from WebContent/ folder.
 */
public class BinaryFileLoader extends AbstractResourceLoader {

  public BinaryFileLoader(HttpServletRequest req) {
    super(req);
  }

  @Override
  public HttpCacheObject loadResource(HttpCacheKey cacheKey) throws IOException {
    String pathInfo = cacheKey.getResourcePath();
    URL url = BEANS.get(IWebContentService.class).getWebContentResource(pathInfo);
    if (url == null) {
      // not handled here
      return null;
    }
    byte[] bytes = IOUtility.readFromUrl(url);
    URLConnection connection = url.openConnection();
    BinaryResource content = new BinaryResource(pathInfo, detectContentType(pathInfo), bytes, connection.getLastModified());
    return new HttpCacheObject(cacheKey, true, IHttpCacheControl.MAX_AGE_4_HOURS, content);
  }

}
