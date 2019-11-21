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

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.eclipse.scout.rt.platform.exception.PlatformException;

public class FilesystemWebResourceResolver extends AbstractWebResourceResolver {

  private final Path m_root;

  protected FilesystemWebResourceResolver() {
    m_root = findModuleRoot().resolve(AbstractWebResourceResolver.OUTPUT_FOLDER_NAME);
  }

  @Override
  protected URL getResourceImpl(String resourcePath) {
    try {
      Path candidate = m_root.resolve(resourcePath);
      return toUrl(candidate);
    } catch (java.nio.file.InvalidPathException e){
      // filesystem implementation does not understand/allow this path
      return null;
    }
  }

  protected static URL toUrl(Path path) {
    if (Files.isReadable(path) && Files.isRegularFile(path)) {
      try {
        return path.toUri().toURL();
      }
      catch (MalformedURLException e) {
        throw new PlatformException("Invalid URL for path '{}'.", path, e);
      }
    }
    return null;
  }

  @SuppressWarnings("findbugs:NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE")
  protected static Path findModuleRoot() {
    Path workingDir = Paths.get("").toAbsolutePath();
    Path parentDir = workingDir.getParent();
    String folderName = workingDir.getFileName().toString();
    String appModuleName = folderName;
    if (folderName.endsWith(".dev") || folderName.endsWith("-dev")) {
      appModuleName = folderName.substring(0, folderName.length() - 4);
    }
    Path resourceRoot = parentDir.resolve(appModuleName);
    if (Files.isDirectory(resourceRoot) && Files.isReadable(resourceRoot)) {
      return resourceRoot;
    }

    if (appModuleName.endsWith(".app") || appModuleName.endsWith("-app")) {
      appModuleName = appModuleName.substring(0, appModuleName.length() - 4);
    }
    resourceRoot = parentDir.resolve(appModuleName);
    if (Files.isDirectory(resourceRoot) && Files.isReadable(resourceRoot)) {
      return resourceRoot;
    }

    return workingDir;
  }
}
