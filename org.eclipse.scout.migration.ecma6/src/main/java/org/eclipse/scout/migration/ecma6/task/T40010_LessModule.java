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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.scout.migration.ecma6.MigrationUtility;
import org.eclipse.scout.migration.ecma6.PathInfo;
import org.eclipse.scout.migration.ecma6.WorkingCopy;
import org.eclipse.scout.migration.ecma6.configuration.Configuration;
import org.eclipse.scout.migration.ecma6.context.Context;
import org.eclipse.scout.rt.platform.Order;

@Order(40010)
public class T40010_LessModule extends AbstractTask {

  public static final String LESS_FILE_SUFFIX = ".less";
  public static final String OLD_FILE_SUFFIX = "-module" + LESS_FILE_SUFFIX;

  private final Pattern IMPORT_PAT = Pattern.compile("@import\\s+\"([\\w/.]+)\";");
  private final List<String> m_importsInCurrentFile = new ArrayList<>();
  private final Set<String> m_allLessFilesInCurrentModule = new HashSet<>();
  private final Set<String> m_nonDefaultThemes = new HashSet<>();

  @Override
  public boolean accept(PathInfo pathInfo, Context context) {
    String fileName = pathInfo.getPath().getFileName().toString();
    return fileName.endsWith(OLD_FILE_SUFFIX);
  }

  @Override
  public void process(PathInfo pathInfo, Context context) {
    WorkingCopy workingCopy = context.ensureWorkingCopy(pathInfo.getPath());
    if (isLegacyModule(pathInfo.getPath())) {
      workingCopy.setDeleted(true);
      return;
    }

    m_importsInCurrentFile.clear();
    m_allLessFilesInCurrentModule.clear();
    m_nonDefaultThemes.clear();
    cache(context);

    String nl = workingCopy.getLineDelimiter();
    String newContent = T25000_ModelsGetModelToImport.replace(IMPORT_PAT, workingCopy.getSource(), this::removeFirstPathSegmentAndFileSuffix);
    if ("@eclipse-scout/core".equals(Configuration.get().getPersistLibraryName())) {
      newContent = newContent + nl + "@import \"login/LoginBox\";" + nl;
    }
    workingCopy.setSource(newContent);

    Path relPath = Configuration.get().getSourceModuleDirectory().relativize(pathInfo.getPath());
    String newDefaultThemeFileName = "index.less";
    workingCopy.setRelativeTargetPath(relPath.getParent().resolve(newDefaultThemeFileName));

    flushNonDefaultThemes(pathInfo, context, newDefaultThemeFileName, nl);
  }

  protected boolean isLegacyModule(Path p) {
    String fileName = p.getFileName().toString();
    return fileName.endsWith("login" + OLD_FILE_SUFFIX) || fileName.endsWith("logout" + OLD_FILE_SUFFIX);
  }

  protected void cache(Context context) {
    Path sourceDir = Configuration.get().getSourceModuleDirectory();
    for (Path p : context.getAllLessFiles()) {
      String relPath = MigrationUtility.removeFirstSegments(sourceDir.relativize(p), 4);
      if (relPath.endsWith(OLD_FILE_SUFFIX)) {
        // skip modules
        continue;
      }
      m_allLessFilesInCurrentModule.add(relPath);
      String theme = parseTheme(p);
      if (theme != null) {
        m_nonDefaultThemes.add(theme);
      }
    }
  }

  protected void flushNonDefaultThemes(PathInfo pathInfo, Context context, String newDefaultThemeFileName, String nl) {
    for (String theme : m_nonDefaultThemes) {
      String src = buildNonDefaultThemeSource(context, newDefaultThemeFileName, nl, theme);
      String fileName = toThemeFileName(removeLessFileExtension(newDefaultThemeFileName), theme);
      Path targetFile = Configuration.get().getTargetModuleDirectory().resolve(pathInfo.getModuleRelativePath().getParent().resolve(fileName));
      WorkingCopy workingCopy = context.newFile(targetFile);
      workingCopy.setSource(src);
    }
  }

  protected String toThemeFileName(String defaultThemeFileName, String theme) {
    return defaultThemeFileName + '-' + theme + LESS_FILE_SUFFIX;
  }

  protected String buildNonDefaultThemeSource(Context context, String newDefaultThemeFileName, String nl, String theme) {
    List<String> imports = new ArrayList<>();
    String mainImport = "@import \"" + removeLessFileExtension(newDefaultThemeFileName) + "\";" + nl + nl;
    for (String importInDefaultTheme : m_importsInCurrentFile) {
      String themeFileName = toThemeFileName(importInDefaultTheme, theme);
      if (existsFile(context, themeFileName)) {
        imports.add("@import \"" + removeLessFileExtension(themeFileName) + "\";");
      }
    }
    imports.sort(Collections.reverseOrder()); // short paths first
    return mainImport + String.join(nl, imports);
  }

  protected boolean existsFile(Context context, String path) {
    return m_allLessFilesInCurrentModule.contains(path);
  }

  protected void removeFirstPathSegmentAndFileSuffix(Matcher matcher, StringBuilder result) {
    String lessImportPath = matcher.group(1);
    int firstSlash = lessImportPath.indexOf('/');
    if (firstSlash > 0) {
      lessImportPath = lessImportPath.substring(firstSlash + 1);
    }
    lessImportPath = removeLessFileExtension(lessImportPath);
    m_importsInCurrentFile.add(lessImportPath);
    result.append("@import \"").append(lessImportPath).append("\";");
  }

  public static String parseTheme(Path lessFile) {
    String filenameWithExtension = lessFile.getFileName().toString();
    String fileName = filenameWithExtension.substring(0, filenameWithExtension.length() - LESS_FILE_SUFFIX.length());
    int firstDelimiterPos = fileName.indexOf('-');
    if (firstDelimiterPos < 0) {
      // no theme in file name
      return null;
    }
    return fileName.substring(firstDelimiterPos + 1);
  }

  public static String removeLessFileExtension(String path) {
    if (path.endsWith(LESS_FILE_SUFFIX)) {
      return path.substring(0, path.length() - LESS_FILE_SUFFIX.length());
    }
    return path;
  }
}
