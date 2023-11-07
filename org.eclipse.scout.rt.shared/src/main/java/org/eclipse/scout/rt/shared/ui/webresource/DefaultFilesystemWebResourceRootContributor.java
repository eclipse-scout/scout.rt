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

  public static final String WEBPACK_CONFIG_JS = "webpack.config.js";

  @Override
  public List<Path> getRoots() {
    Path moduleRoot = findModuleRoot();
    if (moduleRoot == null) {
      return Collections.emptyList();
    }
    return Collections.singletonList(moduleRoot.resolve(AbstractWebResourceResolver.OUTPUT_FOLDER_NAME));
  }

  protected static Path findModuleRoot() {
    Path workingDir = Paths.get("").toAbsolutePath();
    Path parentDir = workingDir.getParent();
    if (parentDir == null) {
      return null;
    }

    // try module without .dev/-dev suffix
    String folderName = workingDir.getFileName().toString();
    String appModuleName = folderName;
    if (folderName.endsWith(".dev") || folderName.endsWith("-dev")) {
      appModuleName = folderName.substring(0, folderName.length() - 4);
    }
    Path resourceRoot = parentDir.resolve(appModuleName);
    if (isValidRootModule(resourceRoot)) {
      return resourceRoot;
    }

    // try module without .app/-app suffix
    if (appModuleName.endsWith(".app") || appModuleName.endsWith("-app")) {
      appModuleName = appModuleName.substring(0, appModuleName.length() - 4);
    }
    resourceRoot = parentDir.resolve(appModuleName);
    if (isValidRootModule(resourceRoot)) {
      return resourceRoot;
    }

    return workingDir;
  }

  /**
   * A module is considered valid if a file {@link #WEBPACK_CONFIG_JS} exists.
   */
  protected static boolean isValidRootModule(Path module) {
    return Files.isDirectory(module)
        && Files.isReadable(module)
        && Files.exists(module.resolve(WEBPACK_CONFIG_JS));
  }
}
