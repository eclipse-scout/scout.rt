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

import javax.servlet.http.HttpServletRequest;

import org.eclipse.scout.rt.client.services.common.icon.IconLocator;
import org.eclipse.scout.rt.client.services.common.icon.IconSpec;
import org.eclipse.scout.rt.platform.resource.BinaryResource;
import org.eclipse.scout.rt.platform.resource.BinaryResources;
import org.eclipse.scout.rt.ui.html.cache.HttpCacheKey;
import org.eclipse.scout.rt.ui.html.cache.HttpCacheObject;
import org.eclipse.scout.rt.ui.html.cache.IHttpCacheControl;

/**
 * This class loads static icon images from {@link IconLocator} (<code>/resource/icons</code> folders of all jars on the
 * classpath).
 */
public class IconLoader extends AbstractResourceLoader {

  public IconLoader(HttpServletRequest req) {
    super(req);
  }

  @Override
  public HttpCacheObject loadResource(HttpCacheKey cacheKey) {
    String pathInfo = cacheKey.getResourcePath();
    final String imageId = pathInfo.substring(pathInfo.lastIndexOf('/') + 1);
    IconSpec iconSpec = IconLocator.instance().getIconSpec(imageId);
    if (iconSpec != null) {
      // cache: use max-age caching for at most 4 hours
      BinaryResource content = BinaryResources.create()
          .withFilename(iconSpec.getName())
          .withContentType(detectContentType(pathInfo))
          .withContent(iconSpec.getContent())
          .withLastModified(System.currentTimeMillis())
          .build();

      return new HttpCacheObject(cacheKey, true, IHttpCacheControl.MAX_AGE_4_HOURS, content);
    }
    return null;
  }

}
