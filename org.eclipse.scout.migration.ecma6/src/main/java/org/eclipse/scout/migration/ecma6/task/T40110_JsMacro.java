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
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.eclipse.scout.migration.ecma6.MigrationUtility;
import org.eclipse.scout.migration.ecma6.PathInfo;
import org.eclipse.scout.migration.ecma6.WorkingCopy;
import org.eclipse.scout.migration.ecma6.configuration.Configuration;
import org.eclipse.scout.migration.ecma6.context.AppNameContextProperty;
import org.eclipse.scout.migration.ecma6.context.Context;
import org.eclipse.scout.migration.ecma6.model.old.JsFile;
import org.eclipse.scout.migration.ecma6.model.references.AliasedMember;
import org.eclipse.scout.migration.ecma6.model.references.JsImport;
import org.eclipse.scout.rt.platform.Order;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Order(40110)
public class T40110_JsMacro extends AbstractTask {
  private static final Logger LOG = LoggerFactory.getLogger(T40110_JsMacro.class);

  private static final PathMatcher MACRO_MATCHER = FileSystems.getDefault().getPathMatcher("glob:src/main/resources/WebContent/res/*-macro.js");
  private static final Map<String, String> OLD_TO_NEW_IMPORTS = new HashMap<>();
  private static final Set<String> IGNORED_OLD_IMPORTS = new HashSet<>();
  private static final Pattern OLD_IMPORT_PATTERN = Pattern.compile(Pattern.quote("__include(\"") + "([^\"]+)" + Pattern.quote("\");"));

  static {
    OLD_TO_NEW_IMPORTS.put("scout-module.js", "@eclipse-scout/core");
    OLD_TO_NEW_IMPORTS.put("bsiscout-module.js", "@bsi-scout/core");
    OLD_TO_NEW_IMPORTS.put("svg-module.js", "@eclipse-scout/svg");
    OLD_TO_NEW_IMPORTS.put("pdfviewer-module.js", "@bsi-scout/pdfviewer");
    OLD_TO_NEW_IMPORTS.put("graph-module.js", "@bsi-crm/graph");
    OLD_TO_NEW_IMPORTS.put("crm-module.js", "@bsi-crm/core");
    OLD_TO_NEW_IMPORTS.put("studio-module.js", "@bsi-studio/core");
    OLD_TO_NEW_IMPORTS.put("studio-crm-module.js", "@bsi-crm/studio");

    IGNORED_OLD_IMPORTS.add("jquery-all-macro.js");
  }

  @Override
  public boolean accept(PathInfo pathInfo, Context context) {
    return MACRO_MATCHER.matches(pathInfo.getModuleRelativePath());
  }

  @Override
  public void process(PathInfo pathInfo, Context context) {
    final WorkingCopy appJsWc = context.ensureWorkingCopy(Configuration.get().getTargetModuleDirectory().resolve(Paths.get("src/main/js", context.getProperty(AppNameContextProperty.class) + ".js")), true);
    final JsFile appJsFile = context.ensureJsFile(appJsWc, false);

    WorkingCopy macroWc = context.getWorkingCopy(pathInfo.getPath());
    String macroSource = macroWc.getSource();
    Matcher matcher = OLD_IMPORT_PATTERN.matcher(macroSource);
    int count = 1;
    List<JsImport> imports = new ArrayList<>();
    while (matcher.find()) {
      final JsImport imp = ensureImport(appJsWc, appJsFile, matcher.group(1), matcher.group(), count);
      if (imp != null) {
        imports.add(imp);
        count++;
      }
    }

    // Object.assign({}, ref1, ref2, ref3, ref4, ref5, ref6, ref7, ref8, ref9); // workaround so that the imports are not unused
    if (imports.size() > 0) {
      String sourceBuilder = appJsWc.getSource() +
          appJsWc.getLineDelimiter() +
          "Object.assign({}, " +
          imports.stream()
              .map(imp -> imp.getDefaultMember().getAlias())
              .collect(Collectors.joining(", "))
          +
          "); // workaround so that the imports are not unused";
      appJsWc.setSource(sourceBuilder);
    }

    // delete macro file
    try {
      Files.delete(pathInfo.getPath());
    }
    catch (IOException e) {
      LOG.warn("Could not delete macro '" + pathInfo.getPath() + "'");
    }
  }

  private JsImport ensureImport(WorkingCopy appJsWc, JsFile appJsFile, String oldImport, String oldIncludeLine, int refNr) {
    String newImp = OLD_TO_NEW_IMPORTS.get(oldImport);
    if (newImp == null) {
      if (!IGNORED_OLD_IMPORTS.contains(oldImport)) {
        MigrationUtility.prependTodo(appJsWc, "Manually create import for <" + oldIncludeLine + ">");
      }
      return null;
    }
    JsImport imp = appJsFile.getImport(newImp);
    if (imp == null) {
      imp = new JsImport(newImp);
      imp.setDefaultMember(new AliasedMember("*", "ref" + refNr));
      appJsFile.addImport(imp);
      return imp;
    }
    return null;

  }

}
