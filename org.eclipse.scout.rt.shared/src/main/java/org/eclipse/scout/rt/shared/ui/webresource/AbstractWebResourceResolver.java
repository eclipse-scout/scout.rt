/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.shared.ui.webresource;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;
import static org.eclipse.scout.rt.platform.util.CollectionUtility.hasElements;

import java.net.URL;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import org.eclipse.scout.rt.platform.util.FileUtility;
import org.eclipse.scout.rt.platform.util.StringUtility;

public abstract class AbstractWebResourceResolver implements IWebResourceResolver {

  public static final String OUTPUT_FOLDER_NAME = "target/dist";
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
  public List<WebResourceDescriptor> resolveScriptResource(String path, boolean minified, boolean cacheEnabled, String theme) {
    if (path == null) {
      return emptyList();
    }
    String subFolder = getScriptResourceFolder(minified);
    String themePath = getThemePath(path, theme);
    List<WebResourceDescriptor> themeResource = lookupResource(path, themePath, subFolder, minified, cacheEnabled);
    if (hasElements(themeResource) || Objects.equals(path, themePath)) {
      return themeResource;
    }
    return lookupResource(path, path, subFolder, minified, cacheEnabled);
  }

  @Override
  public List<WebResourceDescriptor> resolveWebResource(String path, boolean minified, boolean cacheEnabled) {
    if (path == null) {
      return emptyList();
    }
    return lookupResource(path, path, WEB_RESOURCE_FOLDER_NAME, minified, cacheEnabled);
  }

  protected String getScriptResourceFolder(boolean minified) {
    return minified ? MIN_FOLDER_NAME : DEV_FOLDER_NAME;
  }

  protected List<WebResourceDescriptor> lookupResource(String requestedPath, String path, String subFolder, boolean minified, boolean cacheEnabled) {
    String[] lookupList = {null, null, path};
    if (minified) {
      String pathFromIndex = ScriptResourceIndexes.getMinifiedPath(path, cacheEnabled);
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
      Stream<URL> resourceStream = getResourceImpl(subFolder + '/' + stripLeadingSlash(lookupPath));
      if (resourceStream == null) {
        continue;
      }
      List<WebResourceDescriptor> resources = resourceStream
          .map(url -> new WebResourceDescriptor(url, requestedPath, lookupPath))
          .collect(toList());
      if (hasElements(resources)) {
        return resources;
      }
    }
    return emptyList();
  }

  protected String getMinifiedPath(String path) {
    String[] parts = FileUtility.getFilenameParts(path);
    if (parts == null || parts[1] == null) {
      return path;
    }
    return parts[0] + ".min." + parts[1];
  }

  public static String getThemePath(String path, String theme) {
    if (!StringUtility.hasText(theme)) {
      return path;
    }
    return ScriptRequest.tryParse(path)
        .filter(r -> "css".equalsIgnoreCase(r.fileExtension()))
        .flatMap(r -> ScriptRequest.tryParse(r.path(), r.baseName() + '-' + theme, r.fingerprint(), r.minimized(), r.fileExtension()))
        .map(r -> r.toString(false, false))
        .orElse(path);
  }

  /**
   * @return The {@link URL urls} for the resourcePath given or {@code null} or an empty {@link Stream} if the resource
   * could not be found.
   */
  protected abstract Stream<URL> getResourceImpl(String resourcePath);
}
