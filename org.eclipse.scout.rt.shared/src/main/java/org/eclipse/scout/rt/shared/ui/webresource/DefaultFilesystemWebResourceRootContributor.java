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

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;

public class DefaultFilesystemWebResourceRootContributor implements IFilesystemWebResourceRootContributor {
  @Override
  public List<Path> getRoots() {
    return Collections.singletonList(findModuleRoot().resolve(AbstractWebResourceResolver.OUTPUT_FOLDER_NAME));
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
