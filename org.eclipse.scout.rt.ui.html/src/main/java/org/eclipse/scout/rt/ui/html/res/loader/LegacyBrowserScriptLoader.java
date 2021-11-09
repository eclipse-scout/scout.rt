/*
 * Copyright (c) 2010-2021 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.ui.html.res.loader;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;

import org.eclipse.scout.rt.platform.resource.BinaryResource;
import org.eclipse.scout.rt.platform.resource.BinaryResources;
import org.eclipse.scout.rt.platform.resource.MimeType;
import org.eclipse.scout.rt.platform.util.IOUtility;
import org.eclipse.scout.rt.server.commons.servlet.cache.HttpCacheControl;
import org.eclipse.scout.rt.ui.html.ResourceBase;

public class LegacyBrowserScriptLoader extends AbstractResourceLoader {

  public static final String LEGACY_BROWSERS_SCRIPT = "legacy-browsers.js";

  @Override
  public BinaryResource loadResource(String pathInfo) throws IOException {
    URL url = ResourceBase.class.getResource(LEGACY_BROWSERS_SCRIPT);
    URLConnection connection = url.openConnection();
    return BinaryResources.create()
        .withFilename(pathInfo)
        .withContent(IOUtility.readFromUrl(url))
        .withContentType(MimeType.JS.getType())
        .withCharset(StandardCharsets.UTF_8)
        .withLastModified(connection.getLastModified())
        .withCachingAllowed(true)
        .withCacheMaxAge(HttpCacheControl.MAX_AGE_4_HOURS)
        .build();
  }
}
