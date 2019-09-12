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
import java.util.function.Predicate;

import org.eclipse.scout.migration.ecma6.Configuration;
import org.eclipse.scout.migration.ecma6.PathFilters;
import org.eclipse.scout.migration.ecma6.PathInfo;
import org.eclipse.scout.migration.ecma6.WorkingCopy;
import org.eclipse.scout.migration.ecma6.context.Context;
import org.eclipse.scout.rt.platform.Order;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Order(70010)
public class T70010_ManualFixes extends AbstractTask {
  private static final Logger LOG = LoggerFactory.getLogger(T70010_ManualFixes.class);

  private static final Path SRC_MAIN_JS = Paths.get("src", "main", "js");

  private Predicate<PathInfo> m_filter = PathFilters.and(PathFilters.inSrcMainJs(), PathFilters.withExtension("js"));
  private Path m_relativeNamespaceDirectory;

  @Override
  public void setup(Context context) {
    m_relativeNamespaceDirectory = Paths.get("src", "main", "js", Configuration.get().getNamespace());
  }

  @Override
  public boolean accept(PathInfo pathInfo, Context context) {
    return pathInfo.getModuleRelativePath().startsWith(m_relativeNamespaceDirectory);
  }

  @Override
  public void process(PathInfo pathInfo, Context context) {
    if (pathInfo.getPath().endsWith("org.eclipse.scout.jswidgets.ui.html/src/main/js/App.js")) {
      WorkingCopy wc = context.ensureWorkingCopy(pathInfo.getPath());
      String source = wc.getSource();
      source = source.replace("import DesktopModel from './DesktopModel';", "import DesktopModel from './desktop/DesktopModel';");
      wc.setSource(source);
    }

    if (pathInfo.getPath().endsWith("org.eclipse.scout.rt.ui.html/src/main/js/popup/Popup.js")) {
      WorkingCopy wc = context.ensureWorkingCopy(pathInfo.getPath());
      String source = wc.getSource();
      String ln = wc.getLineDelimiter();
      int a = source.indexOf("// TODO MIG:  Looks like a dynamic jsEnum. Must be migrated by hand.");
      int b = source.indexOf("(function() {", a + 1);
      int c = source.indexOf("}());", b + 1);
      if (a >= 0 && b >= 0 && c >= 0) {
        c += 5;
        source = source.replace(source.substring(a, c), "static SwitchRule = {};");
        source += ln + source.substring(b, c) + ln;
        wc.setSource(source);
      }
    }

    if (pathInfo.getPath().endsWith("org.eclipse.scout.rt.ui.html/src/main/js/util/styles.js")) {
      WorkingCopy wc = context.ensureWorkingCopy(pathInfo.getPath());
      String source = wc.getSource();
      String ln = wc.getLineDelimiter();
      String aText = "let styleMap = {};";
      int a = source.indexOf(aText);
      if (a >= 0) {
        source = source.replace(aText, aText + ln + ln + "let element = null;");
        wc.setSource(source);
      }
    }

    if (pathInfo.getPath().endsWith("org.eclipse.scout.rt.ui.html/src/main/js/table/Table.js")) {
      WorkingCopy wc = context.ensureWorkingCopy(pathInfo.getPath());
      String source = wc.getSource();
      source = source.replace("_filterMenus(menus, destination, onlyVisible, enableDisableKeyStroke, notAllowedTypes) {", "_filterMenus(menuItems, destination, onlyVisible, enableDisableKeyStroke, notAllowedTypes) {");
      wc.setSource(source);
    }

    /*
    _filterMenus(menus, destination, onlyVisible, enableDisableKeyStroke, notAllowedTypes) {
    return menus.filterAccordingToSelection('Table', this.selectedRows.length, menus, destination, onlyVisible, enableDisableKeyStroke, notAllowedTypes);
    }
     */

  }
}
