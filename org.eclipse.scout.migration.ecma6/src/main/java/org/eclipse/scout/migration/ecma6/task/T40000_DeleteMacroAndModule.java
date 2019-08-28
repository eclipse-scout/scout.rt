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

import org.eclipse.scout.migration.ecma6.PathInfo;
import org.eclipse.scout.migration.ecma6.context.Context;
import org.eclipse.scout.rt.platform.Order;

@Order(40000)
public class T40000_DeleteMacroAndModule extends AbstractTask {

  private static final String[] TO_DELETE_FILE_SUFFIXES = {
      "-module.js",
      "-module.json", // module.less is renamed to theme.less and must therefore not be deleted
      "-macro.json",
      "-macro.less",
      "-macro.js"
  };

  @Override
  public boolean accept(PathInfo pathInfo, Context context) {
    String fileName = pathInfo.getPath().getFileName().toString();
    for (String suffix : TO_DELETE_FILE_SUFFIXES) {
      if (fileName.endsWith(suffix)) {
        return true;
      }
    }
    return false;
  }

  @Override
  public void process(PathInfo pathInfo, Context context) {
    context.ensureWorkingCopy(pathInfo.getPath()).setDeleted(true);
  }
}
