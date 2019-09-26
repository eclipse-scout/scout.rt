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
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.scout.migration.ecma6.Configuration;
import org.eclipse.scout.migration.ecma6.PathInfo;
import org.eclipse.scout.migration.ecma6.WorkingCopy;
import org.eclipse.scout.migration.ecma6.context.Context;
import org.eclipse.scout.rt.platform.Order;

@Order(40030)
public class T40030_IndexJs extends AbstractTask {

  private static final String JS_FILE_EXTENSION = ".js";
  public static final String OLD_FILE_SUFFIX = "-module.js";
  private static final Pattern INCLUDE_PAT = Pattern.compile("__include\\(\"([\\w.\\-/]+)\"\\);");

  @Override
  public boolean accept(PathInfo pathInfo, Context context) {
    String fileName = pathInfo.getPath().getFileName().toString();
    return fileName.endsWith(OLD_FILE_SUFFIX);
  }

  @Override
  public void process(PathInfo pathInfo, Context context) {
    Configuration config = Configuration.get();

    String baseName = pathInfo.getPath().getFileName().toString();
    baseName = baseName.substring(0, baseName.length() - OLD_FILE_SUFFIX.length());
    int firstDelim = baseName.indexOf('-');
    if (firstDelim > 0) {
      baseName = '-' + baseName.substring(firstDelim + 1);
    }
    else {
      baseName = "";
    }
    baseName = "index" + baseName + JS_FILE_EXTENSION;

    WorkingCopy workingCopy = context.getWorkingCopy(pathInfo.getPath());
    String nl = workingCopy.getLineDelimiter();
    Matcher matcher = INCLUDE_PAT.matcher(workingCopy.getSource());
    StringBuilder newSource = new StringBuilder();
    while (matcher.find()) {
      String oldPath = matcher.group(1);
      Path path = Paths.get(oldPath);
      if (config.getJsFolderName().equals(path.subpath(0, 1).toString())) {
        path = path.subpath(1, path.getNameCount());
      }
      String name = nameWithoutJsExtension(path);
      if ("main".equals(name) && "scout".equals(config.getNamespace())) {
        // 'main' export must be renamed to 'scout'
        name = "scout";
        path = Paths.get("scout");
      }
      String pathStr = nameWithoutJsExtension(path.toString().replace('\\', '/'));
      if ("objectFactories".equals(name)) {
        // object factories file does not export anything. therefore import using wildcard
        newSource.append("export * from './").append(pathStr).append("';").append(nl);
      }
      else if (name.indexOf('-') < 0) {
        newSource.append("export { default as ").append(name).append(" } from './").append(pathStr).append("';").append(nl);
      }
    }

    if ("@eclipse-scout/core".equals(config.getPersistLibraryName())) {
      newSource
          .append(nl)
          .append("export { default as JQueryUtils } from './jquery/jquery-scout';")
          .append(nl)
          .append("export { default as JQuerySelectors } from './jquery/jquery-scout-selectors';")
          .append(nl)
          .append(nl)
          .append("export { default as LoginApp } from './login/LoginApp';")
          .append(nl)
          .append("export { default as LoginBox } from './login/LoginBox';")
          .append(nl)
          .append("export { default as LogoutApp } from './login/LogoutApp';")
          .append(nl)
          .append("export { default as LogoutBox } from './login/LogoutBox';")
          .append(nl);
    }

    newSource.append(nl)
        .append("import * as self from './index.js';").append(nl)
        .append("export default self;").append(nl)
        .append("window.").append(config.getNamespace()).append(" = Object.assign(window.").append(config.getNamespace()).append(" || {}, self);").append(nl);
    workingCopy.setSource(newSource.toString());
    workingCopy.setRelativeTargetPath(Paths.get("src/main/js/" + baseName));
  }

  protected String nameWithoutJsExtension(Path p) {
    return nameWithoutJsExtension(p.getFileName().toString());
  }

  public static String nameWithoutJsExtension(String name) {
    if (name.endsWith(JS_FILE_EXTENSION)) {
      return name.substring(0, name.length() - JS_FILE_EXTENSION.length());
    }
    return name;
  }
}
