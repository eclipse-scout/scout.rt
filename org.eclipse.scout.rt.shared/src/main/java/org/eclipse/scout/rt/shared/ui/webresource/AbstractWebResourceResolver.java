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
    String subFolder = getScriptResourceFolder(minified);
    String themePath = getThemePath(path, theme);
    Optional<WebResourceDescriptor> themeResource = lookupResource(path, themePath, subFolder, minified);
    if (themeResource.isPresent() || Objects.equals(path, themePath)) {
      return themeResource;
    }
    return lookupResource(path, path, subFolder, minified);
  }

  @Override
  public Optional<WebResourceDescriptor> resolveIndexFile(String path) {
    if (path == null) {
      return Optional.empty();
    }
    String subFolder = getScriptResourceFolder(true);
    return lookupResource(path, path, subFolder, false);
  }

  @Override
  public Optional<WebResourceDescriptor> resolveWebResource(String path, boolean minified) {
    if (path == null) {
      return Optional.empty();
    }
    return lookupResource(path, path, WEB_RESOURCE_FOLDER_NAME, minified);
  }

  protected String getScriptResourceFolder(boolean minified) {
    return minified ? MIN_FOLDER_NAME : DEV_FOLDER_NAME;
  }

  protected Optional<WebResourceDescriptor> lookupResource(String requestedPath, String path, String subFolder, boolean minified) {
    String[] lookupList = {null, null, path};
    if (minified) {
      String pathFromIndex = ScriptResourceIndexes.getMinifiedPath(path);
      if (Objects.equals(path, pathFromIndex)) {
        // no mapping in the script resource index. try with simple minified extension
        String minifiedPath = getMinifiedPath(path);
        if (!Objects.equals(path, minifiedPath)) {
          lookupList[1] = minifiedPath;
        }
      }
      else {
        lookupList[0] = pathFromIndex;
      }
    }

    for (String lookupPath : lookupList) {
      if (lookupPath == null) {
        continue;
      }
      URL url = getResourceImpl(subFolder + '/' + stripLeadingSlash(lookupPath));
      if (url != null) {
        return Optional.of(new WebResourceDescriptor(url, requestedPath, lookupPath));
      }
    }
    return Optional.empty();
  }

  protected String getMinifiedPath(String path) {
    String[] parts = FileUtility.getFilenameParts(path);
    if (parts == null || parts[1] == null) {
      return path;
    }
    return parts[0] + ".min." + parts[1];
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
