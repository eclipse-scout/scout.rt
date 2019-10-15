/*
 * Copyright (c) 2010-2019 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.shared.ui.webresource;

import java.net.URL;
import java.util.Objects;
import java.util.Optional;

import org.eclipse.scout.rt.platform.util.FileUtility;
import org.eclipse.scout.rt.platform.util.StringUtility;

public abstract class AbstractWebResourceResolver implements IWebResourceResolver {

  public static final String OUTPUT_FOLDER_NAME = "dist";
  public static final String DEV_FOLDER_NAME = "dev";
  public static final String MIN_FOLDER_NAME = "prod";
  public static final String WEB_RESOURCE_FOLDER_NAME = "res";

  public static String stripLeadingSlash(String path) {
    if (path == null) {
      return null;
    }
    if (path.startsWith("/")) {
      return path.substring(1);
    }
    return path;
  }

  @Override
  public Optional<WebResourceDescriptor> resolveScriptResource(String path, boolean minified, String theme) {
    if (path == null) {
      return Optional.empty();
    }
    String themePath = getThemePath(path, theme);
    Optional<WebResourceDescriptor> themeResource = findScriptResource(path, themePath, minified);
    if (themeResource.isPresent() || Objects.equals(path, themePath)) {
      return themeResource;
    }
    return findScriptResource(path, path, minified);
  }

  @Override
  public Optional<WebResourceDescriptor> resolveWebResource(String path) {
    return Optional.ofNullable(path)
        .map(p -> getResourceImpl(WEB_RESOURCE_FOLDER_NAME + '/' + stripLeadingSlash(p)))
        .map(url -> new WebResourceDescriptor(url, path, path));
  }

  protected String getWebResourceFolder(boolean minified) {
    return minified ? MIN_FOLDER_NAME : DEV_FOLDER_NAME;
  }

  protected Optional<WebResourceDescriptor> findScriptResource(String path, String resolvedPath, boolean minified) {
    if (minified) {
      resolvedPath = ScriptResourceIndexes.getMinifiedPath(resolvedPath);
    }
    final String lookupPath = resolvedPath;
    return Optional.ofNullable(getResourceImpl(getWebResourceFolder(minified) + '/' + stripLeadingSlash(lookupPath)))
        .map(u -> new WebResourceDescriptor(u, path, lookupPath));
  }

  protected String getThemePath(String path, String theme) {
    if (!StringUtility.hasText(theme)) {
      return path;
    }
    String[] parts = FileUtility.getFilenameParts(path);
    if (parts == null || !"css".equals(parts[1])) {
      return path;
    }
    return parts[0] + '-' + theme + ".css";
  }

  /**
   * @return The {@link URL} or {@code null}.
   */
  protected abstract URL getResourceImpl(String resourcePath);

}
