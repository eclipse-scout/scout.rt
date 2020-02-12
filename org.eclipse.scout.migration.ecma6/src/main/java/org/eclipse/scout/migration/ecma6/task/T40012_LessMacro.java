/*
 * Copyright (c) 2010-2020 BSI Business Systems Integration AG.
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
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.scout.migration.ecma6.MigrationUtility;
import org.eclipse.scout.migration.ecma6.PathInfo;
import org.eclipse.scout.migration.ecma6.WorkingCopy;
import org.eclipse.scout.migration.ecma6.configuration.Configuration;
import org.eclipse.scout.migration.ecma6.context.Context;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.exception.ProcessingException;
import org.eclipse.scout.rt.platform.util.StringUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Order(40012)
public class T40012_LessMacro extends AbstractTask {
  public static final String OLD_FILE_SUFFIX = "-module" + Context.LESS_FILE_SUFFIX;
  private static final Logger LOG = LoggerFactory.getLogger(T40012_LessMacro.class);
  private static final Pattern ALL_MACRO_PATTERN = Pattern.compile(Pattern.quote("src/main/resources/WebContent/res/") + "(.*)" + Pattern.quote("-all-macro.less"));
  private static final String THEME_BSI_DARK = "bsi-dark";
  private static final String THEME_BSI_HC = "bsi-hc";
  private static final String THEME_DARK = "dark";
  private static final String THEME_BSI = "bsi";

  private static final Pattern IMPORT_PATTERN = Pattern.compile("\\@import\\s*\"([\\w\\.\\-\\_]+)");
  private static final Map<String, Map<String, String>> LIBS = new HashMap<>();
  static {
    // scout-module.less
    Map<String, String> themes = new HashMap<>();
    themes.put(Context.THEME_DEFAULT, "~@eclipse-scout/core/src/index");
    themes.put(THEME_DARK, "~@eclipse-scout/core/src/index-dark");
    LIBS.put("scout-module.less", themes);

    // svg-module.less
    themes = new HashMap<>();
    themes.put(Context.THEME_DEFAULT, "~@eclipse-scout/svg/src/main/js/index");
    LIBS.put("svg-module.less", themes);

    // bsiscout-module.less
    themes = new HashMap<>();
    themes.put(Context.THEME_DEFAULT, "~@bsi-scout/core/src/main/js/index");
    themes.put(THEME_BSI, "~@bsi-scout/core/src/main/js/index-bsi");
    themes.put(THEME_BSI_DARK, "~@bsi-scout/core/src/main/js/index-bsi-dark");
    themes.put(THEME_BSI_HC, "~@bsi-scout/core/src/main/js/index-bsi-hc");
    themes.put(THEME_DARK, "~@bsi-scout/core/src/main/js/index-dark");
    LIBS.put("bsiscout-module.less", themes);

    // pdfviewer-module.less
    themes = new HashMap<>();
    themes.put(Context.THEME_DEFAULT, "~@bsi-scout/pdfviewer/src/main/js/index");
    themes.put(THEME_DARK, "~@bsi-scout/pdfviewer/src/main/js/index-dark");
    themes.put(THEME_BSI, "~@bsi-scout/pdfviewer/src/main/js/index-bsi");
    LIBS.put("pdfviewer-module.less", themes);

    // crm-module.less
    themes = new HashMap<>();
    themes.put(Context.THEME_DEFAULT, "~@bsi-crm/core/src/main/js/index");
    themes.put(THEME_BSI_DARK, "~@bsi-crm/core/src/main/js/index-bsi-dark");
    themes.put(THEME_BSI_HC, "~@bsi-crm/core/src/main/js/index-bsi-hc");
    LIBS.put("crm-module.less", themes);

    // studio-module.less
    themes = new HashMap<>();
    themes.put(Context.THEME_DEFAULT, "~@bsi-crm/studio/src/main/js/index");
    themes.put(THEME_BSI_DARK, "~@bsi-crm/studio/src/main/js/index-bsi-dark");
    themes.put(THEME_BSI_HC, "~@bsi-crm/studio/src/main/js/index-bsi-hc");
    LIBS.put("studio-module.less", themes);

    // studio-crm-module.less
    themes = new HashMap<>();
    themes.put(Context.THEME_DEFAULT, "~@bsi-crm/studio/src/main/js/index");
    themes.put(THEME_BSI_DARK, "~@bsi-crm/studio/src/main/js/index-bsi-dark");
    LIBS.put("studio-crm-module.less", themes);

    // htmleditor-module.less
    themes = new HashMap<>();
    themes.put(Context.THEME_DEFAULT, "~@bsi-scout/htmleditor/src/main/js/index");
    themes.put(THEME_BSI_DARK, "~@bsi-scout/htmleditor/src/main/js/index-bsi-dark");
    LIBS.put("htmleditor-module.less", themes);

  }

  @Override
  public boolean accept(PathInfo pathInfo, Context context) {
    return ALL_MACRO_PATTERN.matcher(MigrationUtility.pathToString(pathInfo.getModuleRelativePath(), '/')).matches();
  }

  @Override
  public void process(PathInfo pathInfo, Context context) {
    final Matcher matcher = ALL_MACRO_PATTERN.matcher(MigrationUtility.pathToString(pathInfo.getModuleRelativePath(), '/'));
    if (!matcher.find()) {
      LOG.warn("Could not process '" + pathInfo + "', due to no themeprefix found.");
      return;
    }
    WorkingCopy macroWorkingCopy = context.ensureWorkingCopy(pathInfo.getPath());
    Path destFolder = Configuration.get().getTargetModuleDirectory().resolve(Paths.get("src/main/js"));
    String themeFilePrefix = matcher.group(1);
    // default theme
    createThemeFile(context, macroWorkingCopy, destFolder, themeFilePrefix, null);
    // all other themes
    for (String theme : context.getNonDefaultThemes()) {
      createThemeFile(context, macroWorkingCopy, destFolder, themeFilePrefix, theme);
    }
  }

  protected void createThemeFile(Context context, WorkingCopy macroWorkingCopy, Path destFolder, String filePrefix, String theme) {
    String filename = filePrefix + "-theme";
    if (StringUtility.hasText(theme)) {
      filename = filename + "-" + theme;
    }
    filename = filename + Context.LESS_FILE_SUFFIX;
    Path themeFile = destFolder.resolve(filename);
    if (Files.exists(themeFile)) {
      throw new ProcessingException("Theme file '" + themeFile + "' already exists.");
    }
    try {
      Files.createDirectories(themeFile.getParent());
      Files.createFile(themeFile);
    }
    catch (IOException e) {
      throw new ProcessingException("Could not create theme file '" + themeFile + "'.", e);
    }
    WorkingCopy workingCopy = context.ensureWorkingCopy(themeFile);
    // used to ensure initial source is read
    workingCopy.getSource();
    fillThemeContent(context, workingCopy, macroWorkingCopy, theme);
  }

  protected void fillThemeContent(Context context, WorkingCopy workingCopy, WorkingCopy macroWorkingCopy, String theme) {
    final String macroSource = macroWorkingCopy.getSource();
    final Matcher m = IMPORT_PATTERN.matcher(macroSource);
    StringBuilder source = new StringBuilder();
    while (m.find()) {
      addImport(source, workingCopy.getLineDelimiter(), m.group(1), Optional.ofNullable(theme).orElse(Context.THEME_DEFAULT));
    }

    final Path indexLessFile = BEANS.get(T40010_LessModule.class).getIndexLessFile(theme);
    if (indexLessFile != null) {
      source.append(workingCopy.getLineDelimiter()).append("// local index").append(workingCopy.getLineDelimiter());
      source.append("@import \"").append(context.relativeToModule(indexLessFile, Paths.get("src/main/js"))).append("\";");
    }

    workingCopy.setSource(source.toString());
  }

  protected void addImport(StringBuilder builder, String NL, String oldImport, String theme) {
    final Map<String, String> themes = LIBS.get(oldImport);
    if (themes == null) {
      builder.append(MigrationUtility.todoText("Check what imports are needed here (reference not migrated '" + oldImport + "').")).append(NL);
      return;
    }
    final String imp = themes.get(theme);
    if (StringUtility.hasText(imp)) {
      builder.append("@import \"").append(imp).append("\";").append(NL);
    }
  }
}
