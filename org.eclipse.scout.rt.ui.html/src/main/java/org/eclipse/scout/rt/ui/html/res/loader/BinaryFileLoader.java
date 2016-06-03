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

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.resource.BinaryResource;
import org.eclipse.scout.rt.platform.resource.BinaryResources;
import org.eclipse.scout.rt.platform.util.IOUtility;
import org.eclipse.scout.rt.server.commons.servlet.cache.HttpCacheControl;
import org.eclipse.scout.rt.server.commons.servlet.cache.HttpCacheKey;
import org.eclipse.scout.rt.server.commons.servlet.cache.HttpCacheObject;
import org.eclipse.scout.rt.ui.html.res.IWebContentService;

/**
 * This class loads binary files like png, jpg, woff, pdf, docx from WebContent/ folder.
 */
public class BinaryFileLoader extends AbstractResourceLoader {

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
    BinaryResource content = BinaryResources.create()
        .withFilename(pathInfo)
        .withContent(bytes)
        .withLastModified(connection.getLastModified())
        .withCachingAllowed(true)
        .withCacheMaxAge(HttpCacheControl.MAX_AGE_4_HOURS)
        .build();

    return new HttpCacheObject(cacheKey, content);
  }

}
