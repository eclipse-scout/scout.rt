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
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.eclipse.scout.migration.ecma6.Configuration;
import org.eclipse.scout.migration.ecma6.MigrationUtility;
import org.eclipse.scout.migration.ecma6.WorkingCopy;
import org.eclipse.scout.migration.ecma6.context.Context;
import org.eclipse.scout.rt.platform.exception.ProcessingException;

public class T50000_IndexJs implements IPostMigrationTask {

  private static final String JS_FILE_EXTENSION = ".js";

  @Override
  public void execute(Context context) {
    String src = buildIndexSource(context);

    Path targetFile = Configuration.get().getTargetModuleDirectory().resolve("src/main/js/index.js");
    WorkingCopy workingCopy = context.newFile(targetFile);
    workingCopy.setSource(src);
    try {
      workingCopy.persist(targetFile); // explicitly persist because this is a post-mig-task
    }
    catch (IOException e) {
      throw new ProcessingException("Cannot write index.js", e);
    }
  }

  protected String buildIndexSource(Context context) {
    List<WorkingCopy> classes = new ArrayList<>();
    List<WorkingCopy> utilities = new ArrayList<>();
    Configuration config = Configuration.get();
    StringBuilder src = new StringBuilder();
    context.getWorkingCopies().stream()
        .filter(wc -> wc.getPath().getFileName().toString().endsWith(JS_FILE_EXTENSION))
        .filter(wc -> !wc.isDeleted())
        .forEach(js -> {
          if (isUtilityClass(js)) {
            utilities.add(js);
          }
          else {
            classes.add(js);
          }
        });

    WorkingCopy root = findRootWorkingCopy(utilities);
    utilities.remove(root);

    utilities.sort(Comparator.comparing(WorkingCopy::getPath));
    classes.sort(Comparator.comparing(WorkingCopy::getPath));

    if (root != null) {
      src.append("import * as ").append(config.getNamespace()).append(" from './").append(getSourceFolderRelPath(config, root)).append("';\n");
    }
    for (WorkingCopy util : utilities) {
      String rel = getSourceFolderRelPath(config, util);
      String name = nameWithoutJsExtension(util);
      src.append("import * as ").append(name).append(" from './").append(rel).append("';").append("\n");
    }
    src.append("\n");
    src.append("export default ").append(config.getNamespace()).append(";\n");
    for (WorkingCopy util : utilities) {
      String name = nameWithoutJsExtension(util);
      src.append("export { ").append(name).append(" };\n");
    }
    src.append("\n");

    for (WorkingCopy clazz : classes) {
      String rel = getSourceFolderRelPath(config, clazz);
      String name = nameWithoutJsExtension(clazz);
      src.append("export { default as ").append(name).append(" } from './").append(rel).append("';\n");
    }

    return src.toString();
  }

  protected String nameWithoutJsExtension(WorkingCopy wc) {
    return nameWithoutJsExtension(wc.getPath().getFileName().toString());
  }

  protected String nameWithoutJsExtension(String name) {
    if (name.endsWith(JS_FILE_EXTENSION)) {
      return name.substring(0, name.length() - JS_FILE_EXTENSION.length());
    }
    return name;
  }

  protected String getSourceFolderRelPath(Configuration config, WorkingCopy wc) {
    return nameWithoutJsExtension(MigrationUtility.removeFirstSegments(config.getSourceModuleDirectory().relativize(wc.getPath()), 4));
  }

  protected WorkingCopy findRootWorkingCopy(List<WorkingCopy> candidates) {
    WorkingCopy index = findWithName(candidates, "index.js");
    if (index != null) {
      return index;
    }
    return findWithName(candidates, "main.js");
  }

  protected WorkingCopy findWithName(List<WorkingCopy> candidates, String fileName) {
    return candidates.stream()
        .filter(wc -> wc.getPath().getFileName().toString().equals(fileName))
        .findAny()
        .orElse(null);
  }

  protected boolean isUtilityClass(WorkingCopy wc) {
    return Character.isLowerCase(wc.getPath().getFileName().toString().charAt(0));
  }
}
