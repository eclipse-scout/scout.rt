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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.scout.migration.ecma6.MigrationUtility;
import org.eclipse.scout.migration.ecma6.PathInfo;
import org.eclipse.scout.migration.ecma6.WorkingCopy;
import org.eclipse.scout.migration.ecma6.configuration.Configuration;
import org.eclipse.scout.migration.ecma6.context.Context;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.exception.ProcessingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Order(40010)
public class T40010_LessModule extends AbstractTask {
  public static final String OLD_FILE_SUFFIX = "-module" + Context.LESS_FILE_SUFFIX;
  private static final Logger LOG = LoggerFactory.getLogger(T40010_LessModule.class);
  private final Pattern IMPORT_PAT = Pattern.compile("@import\\s+\"([\\w/.]+)\";");
  private final List<String> m_importsInCurrentFile = new ArrayList<>();
  private final Set<String> m_allLessFilesInCurrentModule = new HashSet<>();
  private final Map<String /*themename*/, Path/*theme file*/> m_indexLessFiles = new HashMap<>();

  public static String removeLessFileExtension(String path) {
    if (path.endsWith(Context.LESS_FILE_SUFFIX)) {
      return path.substring(0, path.length() - Context.LESS_FILE_SUFFIX.length());
    }
    return path;
  }

  @Override
  public boolean accept(PathInfo pathInfo, Context context) {
    String fileName = pathInfo.getPath().getFileName().toString();
    return fileName.endsWith(OLD_FILE_SUFFIX);
  }

  @Override
  public void process(PathInfo pathInfo, Context context) {
    WorkingCopy workingCopy = context.ensureWorkingCopy(pathInfo.getPath());
    if (isLegacyModule(pathInfo.getPath())) {
      // deletion is handled in step 2
      return;
    }

    m_importsInCurrentFile.clear();
    m_allLessFilesInCurrentModule.clear();
    cache(context);

    String nl = workingCopy.getLineDelimiter();
    String newContent = T25000_ModelsGetModelToImport.replace(IMPORT_PAT, workingCopy.getSource(), this::removeFirstPathSegmentAndFileSuffix);
    if ("@eclipse-scout/core".equals(Configuration.get().getPersistLibraryName())) {
      newContent = newContent + nl + "@import \"login/LoginBox\";" + nl;
    }
    workingCopy.setSource(newContent);
    String fileName = pathInfo.getPath().getFileName().toString();
    String newDefaultThemeFileName = fileName.replace(OLD_FILE_SUFFIX, "-index.less");
    m_indexLessFiles.put(Context.THEME_DEFAULT, pathInfo.getPath().getParent().resolve(newDefaultThemeFileName));

    flushNonDefaultThemes(pathInfo, context, newDefaultThemeFileName, nl);
  }

  public Path getIndexLessFile(String theme) {
    return m_indexLessFiles.get(Optional.ofNullable(theme).orElse(Context.THEME_DEFAULT));
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
      if (relPath.endsWith("-macro.less")) {
        // skip macros
        continue;
      }
      m_allLessFilesInCurrentModule.add(relPath);
    }
  }

  protected void flushNonDefaultThemes(PathInfo pathInfo, Context context, String newDefaultThemeFileName, String nl) {
    for (String theme : context.getNonDefaultThemes()) {
      String src = buildNonDefaultThemeSource(context, newDefaultThemeFileName, nl, theme);
      String fileName = toThemeFileName(removeLessFileExtension(newDefaultThemeFileName), theme);
      Path targetFile = Configuration.get().getTargetModuleDirectory().resolve(pathInfo.getModuleRelativePath().getParent().resolve(fileName));
      LOG.info("Create index.less for theme '" + theme + "': [source:" + pathInfo + ", target:" + targetFile + "]");
      if (Files.exists(targetFile)) {
        throw new ProcessingException("File '" + targetFile + "' already exists.");
      }
      try {
        Files.createFile(targetFile);
        m_indexLessFiles.put(theme, targetFile);
      }
      catch (IOException e) {
        throw new ProcessingException("Could not create file '" + targetFile + "'.", e);
      }
      WorkingCopy workingCopy = context.ensureWorkingCopy(targetFile);
      // used to ensure initial source is read
      workingCopy.getSource();
      workingCopy.setSource(src);
    }
  }

  protected String toThemeFileName(String defaultThemeFileName, String theme) {
    return defaultThemeFileName + '-' + theme + Context.LESS_FILE_SUFFIX;
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
}
