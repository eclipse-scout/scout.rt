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

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import org.eclipse.scout.rt.platform.Platform;
import org.eclipse.scout.rt.platform.exception.PlatformException;
import org.eclipse.scout.rt.platform.resource.BinaryResource;
import org.eclipse.scout.rt.platform.resource.BinaryResources;
import org.eclipse.scout.rt.platform.util.IOUtility;
import org.eclipse.scout.rt.platform.util.ImmutablePair;
import org.eclipse.scout.rt.server.commons.servlet.cache.HttpCacheControl;
import org.eclipse.scout.rt.shared.ui.webresource.WebResourceDescriptor;
import org.eclipse.scout.rt.shared.ui.webresource.WebResources;

public class WebResourceLoader extends AbstractResourceLoader {

  private final boolean m_minify;
  private final boolean m_cacheEnabled;
  private final String m_theme;

  public WebResourceLoader(boolean minify, boolean cacheEnabled, String theme) {
    m_minify = minify;
    m_cacheEnabled = cacheEnabled;
    m_theme = theme;
  }

  @Override
  public BinaryResource loadResource(String pathInfo) {
    return lookupResource(pathInfo)
        .map(pair -> toBinaryResource(pair.getLeft(), pathInfo, pair.getRight()))
        .orElse(null);
  }

  public boolean acceptFile(String file) {
    return lookupResource(file).isPresent();
  }

  public Optional<WebResourceDescriptor> resolveResource(String pathInfo) {
    return lookupResource(pathInfo).map(ImmutablePair::getLeft);
  }

  protected Optional<ImmutablePair<WebResourceDescriptor, Integer>> lookupResource(String file) {
    return WebResources.resolveScriptResource(file, m_minify, m_cacheEnabled, m_theme)
        .map(descriptor -> new ImmutablePair<>(descriptor, HttpCacheControl.MAX_AGE_ONE_YEAR))
        .or(() -> WebResources.resolveWebResource(file, m_minify, m_cacheEnabled)
            .map(descriptor -> new ImmutablePair<>(descriptor, HttpCacheControl.MAX_AGE_4_HOURS)));
  }

  protected BinaryResource toBinaryResource(WebResourceDescriptor descriptor, String pathInfo, int maxAge) {
    URL url = descriptor.getUrl();
    try {
      byte[] bytes = getContent(descriptor);

      // mime-type is computed based on pathInfo
      BinaryResources resources = BinaryResources.create()
          .withContent(bytes)
          .withCharset(StandardCharsets.UTF_8)
          .withCachingAllowed(m_cacheEnabled)
          .withFilename(pathInfo)
          .withCacheMaxAge(maxAge);
      if (!Platform.get().inDevelopmentMode()) {
        // don't apply lastModified in dev mode because this leaks a file handle which may block the webpack watcher from updating files (resource busy).
        resources.withLastModified(url.openConnection().getLastModified());
      }
      return resources.build();
    }
    catch (IOException e) {
      throw new PlatformException("Unable to read from url '{}'.", url, e);
    }
  }

  protected byte[] getContent(WebResourceDescriptor descriptor) throws IOException {
    // do not use IOUtility.readFromUrl because it temporarily leaks a file handle when calling getContentLength
    try (BufferedInputStream in = new BufferedInputStream(descriptor.getUrl().openConnection().getInputStream())) {
      return IOUtility.readBytes(in, -1);
    }
  }
}
