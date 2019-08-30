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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.eclipse.scout.migration.ecma6.Configuration;
import org.eclipse.scout.migration.ecma6.PathInfo;
import org.eclipse.scout.migration.ecma6.WorkingCopy;
import org.eclipse.scout.migration.ecma6.context.Context;
import org.eclipse.scout.migration.ecma6.model.api.less.LessApiParser;
import org.eclipse.scout.rt.platform.Order;

@Order(40010)
public class T40010_LessModule extends AbstractTask {

  private final Pattern IMPORT_PAT = Pattern.compile("@import\\s+\"([\\w/.]+)\";");
  private final List<String> m_importsInCurrentFile = new ArrayList<>();
  public static final String OLD_FILE_SUFFIX = "-module.less";
  public static final String NEW_FILE_SUFFIX = "-theme.less";

  @Override
  public boolean accept(PathInfo pathInfo, Context context) {
    String fileName = pathInfo.getPath().getFileName().toString();
    return fileName.endsWith(OLD_FILE_SUFFIX);
  }

  @Override
  public void process(PathInfo pathInfo, Context context) {
    WorkingCopy workingCopy = context.ensureWorkingCopy(pathInfo.getPath());

    m_importsInCurrentFile.clear();
    String newContent = T25000_ModelsGetModelToImport.replace(IMPORT_PAT, workingCopy.getSource(), this::removeFirstPathSegmentAndFileSuffix);
    workingCopy.setSource(newContent);

    Path relPath = Configuration.get().getSourceModuleDirectory().relativize(pathInfo.getPath());
    String oldFileName = relPath.getFileName().toString();
    String newDefaultThemeFileName = toNewFileName(oldFileName);
    workingCopy.setRelativeTargetPath(relPath.getParent().resolve(newDefaultThemeFileName));

    flushNonDefaultThemes(pathInfo, context, newDefaultThemeFileName, workingCopy.getLineSeparator());
  }

  protected void flushNonDefaultThemes(PathInfo pathInfo, Context context, String newDefaultThemeFileName, String nl) {
    Set<String> nonDefaultThemes = context.getLessApi()
        .getGlobalVariables()
        .values().stream()
        .flatMap(var -> var.keySet().stream())
        .filter(theme -> !LessApiParser.DEFAULT_THEME_NAME.equals(theme))
        .collect(Collectors.toSet());

    for (String theme : nonDefaultThemes) {
      String src = buildNonDefaultThemeSource(context, newDefaultThemeFileName, nl, theme);
      String fileName = toThemeFileName(LessApiParser.removeLessFileExtension(newDefaultThemeFileName), theme);
      Path targetFile = Configuration.get().getTargetModuleDirectory().resolve(pathInfo.getModuleRelativePath().getParent().resolve(fileName));
      WorkingCopy workingCopy = context.newFile(targetFile);
      workingCopy.setSource(src);
    }
  }

  protected String toThemeFileName(String defaultThemeFileName, String theme) {
    return defaultThemeFileName + '-' + theme + LessApiParser.LESS_FILE_SUFFIX;
  }

  protected String buildNonDefaultThemeSource(Context context, String newDefaultThemeFileName, String nl, String theme) {
    List<String> imports = new ArrayList<>();
    String mainImport = "@import \"" + LessApiParser.removeLessFileExtension(newDefaultThemeFileName) + "\";" + nl + nl;
    for (String importInDefaultTheme : m_importsInCurrentFile) {
      String themeFileName = toThemeFileName(importInDefaultTheme, theme);
      if (existsFile(context, themeFileName)) {
        imports.add("@import \"" + LessApiParser.removeLessFileExtension(themeFileName) + "\";");
      }
    }
    imports.sort(Collections.reverseOrder()); // short paths first
    return mainImport + String.join(nl, imports);
  }

  protected boolean existsFile(Context context, String path) {
    return context.getLessApi().getLessFilesOfCurrentModule().contains(path);
  }

  protected static String toNewFileName(String oldFileName) {
    String nameWithNewSuffix = oldFileName.substring(0, oldFileName.length() - OLD_FILE_SUFFIX.length()) + NEW_FILE_SUFFIX;
    String[] elements = nameWithNewSuffix.split("-");
    String[] newElements = new String[elements.length - 1];
    System.arraycopy(elements, 1, newElements, 0, newElements.length);
    return String.join("-", newElements);
  }

  protected void removeFirstPathSegmentAndFileSuffix(Matcher matcher, StringBuilder result) {
    String lessImportPath = matcher.group(1);
    int firstSlash = lessImportPath.indexOf('/');
    if (firstSlash > 0) {
      lessImportPath = lessImportPath.substring(firstSlash + 1);
    }
    lessImportPath = LessApiParser.removeLessFileExtension(lessImportPath);
    m_importsInCurrentFile.add(lessImportPath);
    result.append("@import \"").append(lessImportPath).append("\";");
  }
}
