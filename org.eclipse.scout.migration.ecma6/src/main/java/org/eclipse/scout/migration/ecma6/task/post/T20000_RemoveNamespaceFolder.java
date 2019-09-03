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
package org.eclipse.scout.migration.ecma6.task.post;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.eclipse.scout.migration.ecma6.Configuration;
import org.eclipse.scout.migration.ecma6.FileUtility;
import org.eclipse.scout.migration.ecma6.PathInfo;
import org.eclipse.scout.migration.ecma6.context.Context;
import org.eclipse.scout.migration.ecma6.pathfilter.IMigrationExcludePathFilter;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.exception.VetoException;

@Order(20000)
public class T20000_RemoveNamespaceFolder implements IPostMigrationTask {

  @Override
  public void execute(Context context) {
    try {
      moveSrcNamespaceDiretory(context);
    }
    catch (IOException e) {
      throw new VetoException("Could not move namespace folders 'src/main/js/[namespace] -> src/main/js'", e);
    }
  }

  protected void moveSrcNamespaceDiretory(Context context) throws IOException {
    Path srcNamespaceDirectory = Configuration.get().getTargetModuleDirectory().resolve(Paths.get("src", "main", "js", Configuration.get().getNamespace()));
    if (Files.isDirectory(srcNamespaceDirectory)) {
      FileUtility.moveDirectory(srcNamespaceDirectory, Configuration.get().getTargetModuleDirectory().resolve(Paths.get("src", "main", "js")),
          path -> BEANS.all(IMigrationExcludePathFilter.class).stream().noneMatch(filter -> filter.test(new PathInfo(path))));
    }
  }
}
