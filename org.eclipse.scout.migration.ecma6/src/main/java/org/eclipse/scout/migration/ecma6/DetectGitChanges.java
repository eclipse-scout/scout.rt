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
package org.eclipse.scout.migration.ecma6;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.scout.rt.platform.util.FileUtility;

/**
 * Erstellt ausgehend vom GIT Workspace in einem lokalen Ordner die Struktur mit den Source Files, wie in diesem Share
 * <p>
 * Mit einem 3-way-compare können Changes nachgezogen werden
 * </p>
 */
public class DetectGitChanges {
  /**
   * Workspace root
   */
  private static final String WORKSPACE = "C:/dev/workspaces/bsicrm-16.2-j";

  /**
   * Basisverzeichnis auf G
   */
  private static final String G_BASE = "G:\\Customers\\Produktentwicklung\\BSI CRM 16\\FSR2\\Umstellung IDE auf Intellij und Ecma-Script6\\";

  /**
   * /git-base enthält die Files welche als Basis dienten für die pre-migration
   */
  private static final String GIT_BASE = G_BASE + "\\git-base";

  /**
   * /git-latest ist dieselbe Struktur wie /git-base aber mit dem neusten pull vom git
   */
  private static final String GIT_LATEST = G_BASE + "\\git-latest";

  /**
   * migration_guides enthält die pre-migrierten files aus /git-base
   */
  private static final String MIG_GUIDE = G_BASE + "\\migration_guides";

  public static void main(String[] args) {
    prepare("org.eclipse.scout.rt/org.eclipse.scout.rt.ui.html/src/main/js", "01 - org.eclipse.scout.rt.ui.html/src");
    prepare("org.eclipse.scout.rt/org.eclipse.scout.rt.ui.html/src/main/js/scout", "01 - org.eclipse.scout.rt.ui.html/src");

    //prepare("org.eclipse.scout.rt/org.eclipse.scout.rt.svg.ui.html", "02 - org.eclipse.scout.rt.svg.ui.html");

    prepare("org.eclipse.scout.rt/org.eclipse.scout.rt.ui.html.test/src/test/js/scout", "03 - org.eclipse.scout.rt.ui.html.test/test");

    prepare("org.eclipse.scout.docs/code/widgets/org.eclipse.scout.jswidgets.ui.html.app/src", "10 - org.eclipse.scout.jswidgets.ui.html/src");

    prepare("org.eclipse.scout.docs/code/widgets/org.eclipse.scout.widgets.heatmap.ui.html/src", "40 - org.eclipse.scout.widgets.heatmap.ui.html/src");

    prepare("org.eclipse.scout.docs/code/widgets/org.eclipse.scout.widgets.ui.html/src", "41 - org.eclipse.scout.widgets.ui.html/src");
    prepare("org.eclipse.scout.docs/code/widgets/org.eclipse.scout.widgets.ui.html.app/src", "41 - org.eclipse.scout.widgets.ui.html/src");
    prepare("org.eclipse.scout.docs/code/widgets/org.eclipse.scout.widgets.ui.html.app/src/main/resources/WebContent/res", "41 - org.eclipse.scout.widgets.ui.html.app/src/main/resources/WebContent");

    prepare("org.eclipse.scout.docs/code/contacts/org.eclipse.scout.contacts.ui.html", "43 - org.eclipse.scout.contacts.ui.html/template");
    prepare("org.eclipse.scout.docs/code/contacts/org.eclipse.scout.contacts.ui.html/src/main/resources/WebContent/res", "43 - org.eclipse.scout.contacts.ui.html/template/src/main/resources/WebContent");

    prepare("bsi.scout.rt/com.bsiag.scout.rt.ui.html", "50 - com.bsiag.scout.rt.ui.html/template");
    prepare("bsi.scout.rt/com.bsiag.scout.rt.ui.html/src/main/js/bsiscout", "50 - com.bsiag.scout.rt.ui.html/template/src/main/js");
    prepare("bsi.scout.rt/com.bsiag.scout.rt.ui.html/src/main/js/scout", "50 - com.bsiag.scout.rt.ui.html/template/src/main/js");
    prepare("bsi.scout.rt/com.bsiag.scout.rt.ui.html/src/test/js/bsiscout", "50 - com.bsiag.scout.rt.ui.html/template/src/test/js");
    prepare("bsi.scout.rt/com.bsiag.scout.rt.ui.html/src/test/js/scout", "50 - com.bsiag.scout.rt.ui.html/template/src/test/js");

    prepare("bsi.scout.rt/com.bsiag.scout.rt.pdfviewer.ui.html", "52 - com.bsiag.scout.rt.pdfviewer.ui.html/template");
    prepare("bsi.scout.rt/com.bsiag.scout.rt.pdfviewer.ui.html/src/main/resources/WebContent/res/pdfjs-1.10.88", "52 - com.bsiag.scout.rt.pdfviewer.ui.html/template/src/main/resources/WebContent");
    prepare("bsi.scout.rt/com.bsiag.scout.rt.pdfviewer.ui.html/src/main/js/bsiscout", "52 - com.bsiag.scout.rt.pdfviewer.ui.html/template/src/main/js");

    prepare("bsi.scout.rt/com.bsiag.scout.rt.officeaddin.ui.html", "54 - com.bsiag.scout.rt.officeaddin.ui.html/template");
    prepare("bsi.scout.rt/com.bsiag.scout.rt.officeaddin.ui.html/src/main/js/scout/officeaddin", "54 - com.bsiag.scout.rt.officeaddin.ui.html/template/src/main/js/officeaddin");
    prepare("bsi.scout.rt/com.bsiag.scout.rt.officeaddin.ui.html/src/main/resources/WebContent", "54 - com.bsiag.scout.rt.officeaddin.ui.html/template/src/main/resources");
    prepare("bsi.scout.rt/com.bsiag.scout.rt.officeaddin.ui.html/src/main/resources/WebContent/res", "54 - com.bsiag.scout.rt.officeaddin.ui.html/template/src/main/resources");

    prepare("bsi.scout.rt/com.bsiag.scout.rt.htmleditor.ui.html", "56 - com.bsiag.scout.rt.htmleditor.ui.html/template");
    prepare("bsi.scout.rt/com.bsiag.scout.rt.htmleditor.ui.html/src/main/js/bsiscout", "56 - com.bsiag.scout.rt.htmleditor.ui.html/template/src/main/js");

    prepare("bsi.scout.rt/com.bsiag.widgets.ui.html/src", "90 - com.bsiag.widgets.ui.html/src");
    prepare("bsi.scout.rt/com.bsiag.widgets.ui.html/src/main/js/bsiwidgets", "90 - com.bsiag.widgets.ui.html/src/main/js");
    prepare("bsi.scout.rt/com.bsiag.widgets.ui.html/src/main/resources/WebContent/res", "90 - com.bsiag.widgets.ui.html/src/main/resources/WebContent");

    //prepare("com.bsiag.crm.ui.html.graph", "100 - com.bsiag.crm.ui.html.graph");

    prepare("com.bsiag.crm.ui.html.core", "110 - com.bsiag.crm.ui.html.core/template");
    prepare("com.bsiag.crm.ui.html.core/src/main/js/crm", "110 - com.bsiag.crm.ui.html.core/template/src/main/js");
    prepare("com.bsiag.crm.ui.html.core/src/main/resources/WebContent/res", "110 - com.bsiag.crm.ui.html.core/template/src/main/resources/WebContent");

    prepare("bsistudio/com.bsiag.studio.ui.html", "112 - com.bsiag.studio.ui.html/template");
    prepare("bsistudio/com.bsiag.studio.ui.html/src/main/js/studio", "112 - com.bsiag.studio.ui.html/template/src/test/js");
    prepare("bsistudio/com.bsiag.studio.ui.html", "112 - com.bsiag.studio.ui.html/template2");
    prepare("bsistudio/com.bsiag.studio.ui.html/src/main/js/studio", "112 - com.bsiag.studio.ui.html/template2/src/main/js");

    //prepare("bsistudio/com.bsiag.studio.ui.html.test", "113 - com.bsiag.studio.ui.html.test");

    //prepare("com.bsiag.crm.studio.ui.html", "120 - com.bsiag.crm.studio.ui.html");

    prepare("bsicrm/com.bsiag.crm.refmigrate.ui.html", "150 - com.bsiag.crm.refmigrate.ui.html/template");
    prepare("bsicrm/com.bsiag.crm.refmigrate.ui.html/src/main/resources/WebContent/res", "150 - com.bsiag.crm.refmigrate.ui.html/template/src/main/resources/WebContent");

    prepare("bsiagbsicrm/com.bsiag.bsicrm.ui.html", "200 - com.bsiag.bsicrm.ui.html/template");
    prepare("bsiagbsicrm/com.bsiag.bsicrm.ui.html/src/main/resources/WebContent/res", "200 - com.bsiag.bsicrm.ui.html/template/src/main/resources/WebContent");

    prepare("bsiagbsicrm/com.bsiag.bsicrm.migration.app.ui.html", "220 - com.bsiag.bsicrm.migration.app.ui.html/template");
    prepare("bsiagbsicrm/com.bsiag.bsicrm.migration.app.ui.html/src/main/resources/WebContent/res", "220 - com.bsiag.bsicrm.migration.app.ui.html/template/src/main/resources/WebContent");

    prepare("bsistudio/com.bsiag.studio.step.base", "500 - com.bsiag.studio.step.base/template");

    prepare("bsistudio/com.bsiag.studio.step.crm", "510 - com.bsiag.studio.step.crm/models");
    prepare("bsistudio/com.bsiag.studio.step.crm", "510 - com.bsiag.studio.step.crm/template");

    prepare("bsistudio/com.bsiag.studio.step.weather", "520 - com.bsiag.studio.step.weather/template");

    prepare("bsistudio/com.bsiag.studio.step.media", "530 - com.bsiag.studio.step.media/template");

    prepare("bsistudio/com.bsiag.studio.step.example", "540 - com.bsiag.studio.step.example/template");

    prepare("bsistudio/com.bsiag.studio.step.prototyping", "550 - com.bsiag.studio.step.prototyping/template");

    prepare("bsistudio/com.bsiag.studio.step.ml", "560 - com.bsiag.studio.step.ml/template");

    prepare("bsiml/com.bsiag.ml.cortex", "600 - com.bsiag.ml.cortex/template");

    prepare("bsiportal/com.bsiag.portal.ui", "700 - com.bsiag.portal.ui/template");

    prepare("bsistudio/com.bsiag.bsistudio.lab.ui.html", "800 - com.bsiag.bsistudio.lab.ui.html/template");
    prepare("bsistudio/com.bsiag.bsistudio.lab.ui.html/src/main/js/crm", "800 - com.bsiag.bsistudio.lab.ui.html/template/src/main/js");
    prepare("bsistudio/com.bsiag.bsistudio.lab.ui.html/src/main/resources/WebContent/res", "800 - com.bsiag.bsistudio.lab.ui.html/template/src/main/resources/WebContent");

    prepare("bsibriefcase/com.bsiag.briefcase.ui.html", "900 - com.bsiag.briefcase.ui.htmltemplate/template");
    prepare("bsibriefcase/com.bsiag.briefcase.ui.html/src/main/js/briefcase", "900 - com.bsiag.briefcase.ui.htmltemplate/template/src/main/js");
    prepare("bsibriefcase/com.bsiag.briefcase.ui.html/src/test/js/briefcase", "900 - com.bsiag.briefcase.ui.htmltemplate/template/src/test/js");

    //prepare("bsibriefcase/com.bsiag.bsibriefcase.ui.html", "920 - com.bsiag.bsibriefcase.ui.html");

    prepare("bsibriefcase/com.bsiag.bsibriefcase.app", "940 - com.bsiag.bsibriefcase.app/template");
    prepare("bsibriefcase/com.bsiag.bsibriefcase.app/src/main/js/bsibriefcase", "940 - com.bsiag.bsibriefcase.app/template/src/main/js");
    prepare("bsibriefcase/com.bsiag.bsibriefcase.app/src/main/resources/WebContent/res", "940 - com.bsiag.bsibriefcase.app/template/src/main/resources/WebContent");
  }

  /**
   * For all files in migGuidePath find corresponding relative path in workspace modulePath and copy it to
   * {@link #GIT_LATEST}
   */
  private static void prepare(String relativeModulePath, String relativeMigGuidePath) {
    AtomicInteger copyCount = new AtomicInteger();
    Path queryPath = Paths.get((MIG_GUIDE + "/" + relativeMigGuidePath).replace('\\', '/'));
    FileUtility.listTree(queryPath.toFile(), true, false)
        .forEach(f -> {
          Path rel = queryPath.relativize(f.toPath());
          if (rel.toString().isEmpty()) return;

          File src = Paths.get(WORKSPACE + "/" + relativeModulePath + "/" + (rel.toString())).toFile();
          File dst = Paths.get(GIT_LATEST + "/" + relativeModulePath + "/" + (rel.toString())).toFile();
          //System.out.println("" + relativeMigGuidePath + "\t" + rel + "\t" + src + "\t" + src.exists() + "\t" + dst + "\t" + dst.exists());
          if (src.exists()) {
            try {
              FileUtility.copyFile(src, dst);
              copyCount.incrementAndGet();
            }
            catch (IOException e) {
              System.out.println("FAILED copy " + src + " to " + dst);
              e.printStackTrace(System.out);
            }
          }
        });
    System.out.println(relativeMigGuidePath + ": Copied " + copyCount.get() + " files");
  }
}
