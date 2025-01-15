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

import java.util.regex.Matcher;

import org.eclipse.scout.rt.client.services.common.icon.IconLocator;
import org.eclipse.scout.rt.client.services.common.icon.IconSpec;
import org.eclipse.scout.rt.platform.resource.BinaryResource;
import org.eclipse.scout.rt.platform.resource.BinaryResources;
import org.eclipse.scout.rt.server.commons.servlet.cache.HttpCacheControl;

/**
 * This class loads static icon images from {@link IconLocator} (<code>/resource/icons</code> folders of all jars on the
 * classpath).
 */
public class IconLoader extends AbstractResourceLoader {

  @Override
  public BinaryResource loadResource(String pathInfo) {
    Matcher matcher = ResourceLoaders.ICON_PATTERN.matcher(pathInfo);
    if (!matcher.find()) {
      return null;
    }

    String imageId = matcher.group(1);
    IconSpec iconSpec = IconLocator.instance().getIconSpec(imageId);
    if (iconSpec == null) {
      return null;
    }

    // cache: use max-age caching for at most 4 hours
    return BinaryResources.create()
        .withFilename(iconSpec.getName())
        .withContent(iconSpec.getContent())
        .withLastModified(System.currentTimeMillis())
        .withCachingAllowed(true)
        .withCacheMaxAge(HttpCacheControl.MAX_AGE_4_HOURS)
        .build();
  }
}
