/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.testing.platform.util;

import java.nio.file.Path;
import java.util.List;

public interface ITestSupportIsExcluded {

  Path getRoot();

  List<Path> getPathExclusions();

  default boolean isExcluded(Path path) {
    if (getRoot().getNameCount() >= path.getNameCount()) {
      return false;
    }
    Path subpath = path.subpath(getRoot().getNameCount(), path.getNameCount());
    for (Path pathExclusion : getPathExclusions()) {
      if (subpath.endsWith(pathExclusion)) {
        return true;
      }
    }
    return false;
  }
}
