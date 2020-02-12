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

import org.eclipse.scout.migration.ecma6.MigrationUtility;
import org.eclipse.scout.migration.ecma6.PathInfo;
import org.eclipse.scout.migration.ecma6.WorkingCopy;
import org.eclipse.scout.migration.ecma6.configuration.Configuration;
import org.eclipse.scout.migration.ecma6.context.Context;
import org.eclipse.scout.migration.ecma6.model.old.JsFile;
import org.eclipse.scout.migration.ecma6.model.references.AliasedMember;
import org.eclipse.scout.migration.ecma6.model.references.JsImport;
import org.eclipse.scout.rt.platform.Order;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Order(40100)
public class T40100_JsApp extends AbstractTask {
  private static final Logger LOG = LoggerFactory.getLogger(T40100_JsApp.class);

  private static PathMatcher MACRO_MATCHER = FileSystems.getDefault().getPathMatcher("glob:src/main/resources/WebContent/res/index.js");

  @Override
  public boolean accept(PathInfo pathInfo, Context context) {
    return MACRO_MATCHER.matches(pathInfo.getModuleRelativePath());
  }

  @Override
  public void process(PathInfo pathInfo, Context context) {
    final WorkingCopy appJsWc = context.ensureWorkingCopy(Configuration.get().getTargetModuleDirectory().resolve(Paths.get("src/main/js", Configuration.get().getNamespace() + ".js")), true);
    final JsFile appJsFile = context.ensureJsFile(appJsWc, false);

    final WorkingCopy indexWc = context.ensureWorkingCopy(pathInfo.getPath());
    String indexSource = indexWc.getSource();
    // try to find document ready block
    indexSource = indexSource.replaceAll("\\A\\$\\(document\\)\\.ready\\(\\s*function\\(\\s*\\)\\s*\\{\\s*", "");
    indexSource = indexSource.replaceAll("\\}\\s*\\)\\;\\Z", "");
    // create import for app
    StringBuilder appSourceBuilder = new StringBuilder(appJsWc.getSource());
    appSourceBuilder.append(appJsWc.getLineDelimiter()).append(appJsWc.getLineDelimiter());
    if (indexSource.contains("scout.RemoteApp")) {
      indexSource = indexSource.replace("scout.RemoteApp", "RemoteApp");
      JsImport imp = appJsFile.getImport("@eclipse-scout/core");
      if (imp == null) {
        imp = new JsImport("@eclipse-scout/core");
        imp.addMember(new AliasedMember("RemoteApp"));
        appJsFile.addImport(imp);
      }
      else {
        appSourceBuilder = new StringBuilder(appSourceBuilder.toString().replace("ref1, ", ""));
        imp.setDefaultMember(null);
        imp.addMember(new AliasedMember("RemoteApp"));
      }
    }
    else {
      appSourceBuilder.append(MigrationUtility.todoText("Manual migration required.")).append(appJsWc.getLineDelimiter());
    }
    if (indexSource.contains("modelsUrl:")) {
      appSourceBuilder.append(MigrationUtility.todoText("Remove attribute modelsUrl manually (not needed anymore).")).append(appJsWc.getLineDelimiter());
    }
    appSourceBuilder.append(indexSource);
    appJsWc.setSource(appSourceBuilder.toString());

    // delete index.js
    try {
      Files.delete(pathInfo.getPath());
    }
    catch (IOException e) {
      LOG.warn("Could not delete macro '" + pathInfo.getPath() + "'");
    }
  }

}
