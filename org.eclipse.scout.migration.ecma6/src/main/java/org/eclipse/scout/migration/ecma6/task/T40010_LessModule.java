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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.scout.migration.ecma6.PathInfo;
import org.eclipse.scout.migration.ecma6.WorkingCopy;
import org.eclipse.scout.migration.ecma6.context.Context;
import org.eclipse.scout.rt.platform.Order;

@Order(40010)
public class T40010_LessModule extends AbstractTask {

  private final Pattern IMPORT_PAT = Pattern.compile("@import\\s+\"([\\w/.]+)\";");
  private static final String FILE_SUFFIX = "-module.less";

  @Override
  public boolean accept(PathInfo pathInfo, Context context) {
    String fileName = pathInfo.getPath().getFileName().toString();
    return fileName.endsWith(FILE_SUFFIX);
  }

  @Override
  public void process(PathInfo pathInfo, Context context) {
    WorkingCopy workingCopy = context.ensureWorkingCopy(pathInfo.getPath());

    String newContent = T30010_ModelsGetModelToImport.replace(IMPORT_PAT, workingCopy.getSource(), this::removeFirstPathSegment);
    workingCopy.setSource(newContent);

    workingCopy.setRelativeTargetPath(getNewTargetPath(pathInfo.getPath(), context));
  }

  protected Path getNewTargetPath(Path origPath, Context context) {
    Path relPath = context.getSourceRootDirectory().relativize(origPath);
    String oldFileName = relPath.getFileName().toString();
    return relPath.getParent().resolve(toNewFileName(oldFileName));
  }

  protected static String toNewFileName(String oldFileName) {
    String nameWithNewSuffix = oldFileName.substring(0, oldFileName.length() - FILE_SUFFIX.length()) + "-theme.less";
    String[] elements = nameWithNewSuffix.split("-");
    String[] newElements = new String[elements.length - 1];
    System.arraycopy(elements, 1, newElements, 0, newElements.length);
    return String.join("-", newElements);
  }

  protected void removeFirstPathSegment(Matcher matcher, StringBuilder result) {
    String lessImportPath = matcher.group(1);
    int firstSlash = lessImportPath.indexOf('/');
    if (firstSlash > 0) {
      lessImportPath = lessImportPath.substring(firstSlash + 1);
    }

    result.append("@import \"").append(lessImportPath).append("\";");
  }
}
