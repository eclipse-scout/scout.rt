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
package org.eclipse.scout.migration.ecma6.task;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.eclipse.scout.migration.ecma6.Configuration;
import org.eclipse.scout.migration.ecma6.PathInfo;
import org.eclipse.scout.migration.ecma6.WorkingCopy;
import org.eclipse.scout.migration.ecma6.context.Context;
import org.eclipse.scout.rt.platform.Order;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Order(80000)
public class T80000_RemoveNamespaceFolder extends AbstractTask {
  private static final Logger LOG = LoggerFactory.getLogger(T80000_RemoveNamespaceFolder.class);

  private static final Path SRC_MAIN_JS = Paths.get("src", "main", "js");
  private Path m_relativeNamespaceDirectory;

  @Override
  public void setup(Context context) {
    m_relativeNamespaceDirectory = Paths.get("src", "main", "js", Configuration.get().getJsFolderName());
  }

  @Override
  public boolean accept(PathInfo pathInfo, Context context) {
    return Configuration.get().isRemoveJsFolder() && pathInfo.getModuleRelativePath().startsWith(m_relativeNamespaceDirectory);
  }

  @Override
  public void process(PathInfo pathInfo, Context context) {
    WorkingCopy wc = context.ensureWorkingCopy(pathInfo.getPath());
    if (wc.getRelativeTargetPath() != null) {
      if (wc.getRelativeTargetPath().startsWith(m_relativeNamespaceDirectory)) {
        Path newTargetPath = SRC_MAIN_JS.resolve(wc.getRelativeTargetPath().subpath(4, wc.getRelativeTargetPath().getNameCount()));
        LOG.debug("move file with already existing target path ['" + wc.getRelativeTargetPath() + "' -> '" + newTargetPath + "'] ");
        wc.setRelativeTargetPath(newTargetPath);
      }
    }
    else {
      wc.setRelativeTargetPath(SRC_MAIN_JS.resolve(pathInfo.getModuleRelativePath().subpath(4, pathInfo.getModuleRelativePath().getNameCount())));
    }
  }
}
