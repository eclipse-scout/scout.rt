/*******************************************************************************
 * Copyright (c) 2019 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.migration.ecma6;

import java.nio.file.Path;

import org.eclipse.scout.migration.ecma6.configuration.Configuration;

public class ManualFixes {
  public void apply(WorkingCopy wc) {
    String namespace = Configuration.get().getNamespace();
    Path pathInfo = wc.getPath();

    if ("scout".equals(namespace)) {
      if (pathEndsWith(pathInfo, "/popup/Popup.js")) {
        String source = wc.getSource();
        String ln = wc.getLineDelimiter();
        int a = source.indexOf("// TODO MIG:  Looks like a dynamic jsEnum. Must be migrated by hand or added to T70010_ManualFixes.");
        int b = source.indexOf("(function() {", a + 1);
        int c = source.indexOf("}());", b + 1);
        if (a >= 0 && b >= 0 && c >= 0) {
          c += 5;
          String iife = source.substring(b, c);
          source = source.replace(source.substring(a, c), "static SwitchRule = {};");
          source += ln + iife + ln;
          wc.setSource(source);
        }
      }

      if (pathEndsWith(pathInfo, "/util/styles.js")) {
        String source = wc.getSource();
        String ln = wc.getLineDelimiter();
        String aText = "let styleMap = {};";
        int a = source.indexOf(aText);
        if (a >= 0) {
          source = source.replace(aText, aText + ln + ln + "let element = null;");
          wc.setSource(source);
        }
      }

      if (pathEndsWith(pathInfo, "/util/Device.js")) {
        // there is no longer a "res" folder for resources. therefore the fastclick lib can be found on top level now.
        wc.setSource(wc.getSource().replace("'res/fastclick-1.0.6.js'", "'fastclick-1.0.6.js'"));
      }

      if (pathEndsWith(pathInfo, "/logging/logging.js")) {
        // there is no longer a "res" folder for resources. therefore the log4javascript lib folder can be found on top level now.
        wc.setSource(wc.getSource().replace("(options.resourceUrl, 'res/');", "(options.resourceUrl, '');"));
      }

      if (pathEndsWith(pathInfo, "/LoginBox.js")
          || pathEndsWith(pathInfo, "/LogoutBox.js")
          || pathEndsWith(pathInfo, "/LoginApp.js")
          || pathEndsWith(pathInfo, "/LogoutApp.js")) {
        // there is no longer a "res" folder for resources. therefore the log4javascript lib folder can be found on top level now.
        wc.setSource(wc.getSource().replace("'res/logo.png'", "'logo.png'"));
      }
    }

    if ("jswidgets".equals(namespace)) {
      if (pathEndsWith(pathInfo, "/App.js")) {
        String source = wc.getSource();
        source = source.replace("import DesktopModel from './DesktopModel';", "import DesktopModel from './desktop/DesktopModel';");
        wc.setSource(source);
      }

      if (pathEndsWith(pathInfo, "/desktop/Desktop.json")) {
        // the scout logo is moved into the img sub folder
        wc.setSource(wc.getSource().replace("logoUrl: 'scout-logo.png'", "logoUrl: 'img/scout-logo.png'"));
      }
      if (pathEndsWith(pathInfo, "/custom/chart/Chart.js")) {
        // Import the chart.js module and use it in the instance creation
        String importName = "ChartJs";
        String newSource = wc.getSource().replace("this.chart = new Chart(this.$container[0]", "this.chart = new " + importName + "(this.$container[0]");
        int importInsertPos = newSource.indexOf("import * as $ from 'jquery';");
        newSource = newSource.substring(0, importInsertPos) + "import {Chart as " + importName + "} from 'chart.js';" + wc.getLineDelimiter() + newSource.substring(importInsertPos);
        wc.setSource(newSource);
      }
    }

    //heatmap
    if ("scout".equals(namespace)) {
      if (pathEndsWith(pathInfo, "/HeatmapField.js")) {
        String ln = wc.getLineDelimiter();
        String source = wc.getSource();
        String marker = "export default class HeatmapField";
        source = source.replace(marker, "import * as L from 'leaflet';" + ln + ln + marker);
        wc.setSource(source);
      }

      if (pathEndsWith(pathInfo, "/heatmap-module.js")) {//will be saved as index.js
        String ln = wc.getLineDelimiter();
        String source = wc.getSource();
        String marker = "export { default as HeatmapFieldLayout } from './heatmap/HeatmapFieldLayout';";
        source = source.replace(marker, marker + ln + "export { default as simpleheat } from './heatmap/leaflet-heat';");
        wc.setSource(source);
      }
    }

    if ("widgets".equals(namespace)) {
      if (pathEndsWith(pathInfo, "/ChartField.js")) {
        String ln = wc.getLineDelimiter();
        String source = wc.getSource();
        String marker = "export default class ChartField";
        source = source.replace(marker, "import {Chart} from 'chart.js';" + ln + ln + marker);
        wc.setSource(source);
      }
    }

    // map control
    if ("bsiscout".equals(namespace)) {
      if (pathEndsWith(pathInfo, "/MapTableControl.js")) {
        String source = wc.getSource();
        String marker = "url: 'res/maps/' + mapId + '.json'";
        source = source.replace(marker, "url: 'maps/' + mapId + '.json'");
        wc.setSource(source);
      }
    }

    if ("studio".equals(namespace)) {
      if (pathEndsWith(pathInfo, "/ContentEditor.js")) {
        wc.setSource(wc.getSource().replace("'res/contenteditor.css'", "'contenteditor.css'"));
      }

      if (pathEndsWith(pathInfo, "/StorySummaryTile.js")) {
        wc.setSource(wc.getSource().replace("styles.element", "styles._getElement()"));
      }
    }
  }

  private static boolean pathEndsWith(Path path, String suffix) {
    return path.toString().replace('\\', '/').endsWith(suffix.replace('\\', '/'));
  }
}
