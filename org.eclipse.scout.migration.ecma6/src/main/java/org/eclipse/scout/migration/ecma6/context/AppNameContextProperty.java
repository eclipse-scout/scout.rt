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
package org.eclipse.scout.migration.ecma6.context;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.scout.migration.ecma6.configuration.Configuration;
import org.eclipse.scout.rt.platform.Order;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Order(100)
public class AppNameContextProperty implements IContextProperty<String> {

  private static final Logger LOG = LoggerFactory.getLogger(AppNameContextProperty.class);

  private static final Pattern APP_NAME_JS_REGEX = Pattern.compile("res/([^\\-]*)-all-macro\\.js");
  private static final Pattern APP_NAME_LESS_REGEX = Pattern.compile("res/([^\\-]*)-all-macro\\.less");

  // May be used as file name -> must not contain illegal characters
  private String m_value = "TODO MIG appname";

  @Override
  public void setup(Context context) {
    Path indexHtml = Configuration.get().getSourceModuleDirectory().resolve(Paths.get("src/main/resources/WebContent/index.html"));
    if (Files.exists(indexHtml)) {
      String appName = null;
      Set<String> potentialAppNames = new HashSet<>();
      Matcher matcherJs = APP_NAME_JS_REGEX.matcher(context.ensureWorkingCopy(indexHtml).getSource());
      while (matcherJs.find()) {
        potentialAppNames.add(matcherJs.group(1));
      }

      String potentialAppName = null;
      String jsFolderName = Configuration.get().getJsFolderName();
      String namespace = Configuration.get().getNamespace();
      if (jsFolderName != null && !"scout".equals(jsFolderName)) {
        potentialAppName = jsFolderName;
      }
      else if (namespace != null && !"scout".equals(namespace)) {
        potentialAppName = namespace;
      }
      // Search macro that contains the potentialAppName (typically macro, folder and namespace have the same name, but this can of course vary).
      Matcher matcherLess = APP_NAME_LESS_REGEX.matcher(context.ensureWorkingCopy(indexHtml).getSource());
      while (matcherLess.find()) {
        String macroName = matcherLess.group(1);
        if (potentialAppNames.contains(macroName)) {
          if (macroName.equals(potentialAppName)) {
            appName = macroName;
            break;
          }
          // Use the the name of the last macro found as fallback
          appName = macroName;
        }
      }
      if (appName != null) {
        LOG.debug("Found property " + getClass().getSimpleName() + " : " + appName);
        m_value = appName;
      }
      else {
        LOG.warn("Could not resolve appname in file'" + indexHtml + "'");
      }
    }
    else {
      LOG.debug("Could not resolve index.html file to extract appname '" + indexHtml + "'");
    }
  }

  @Override
  public String getValue() {
    return m_value;
  }
}
