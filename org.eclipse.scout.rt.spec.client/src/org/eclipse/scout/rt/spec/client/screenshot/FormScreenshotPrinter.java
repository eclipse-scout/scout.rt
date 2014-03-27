/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.spec.client.screenshot;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.scout.commons.ITypeWithClassId;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.client.ui.form.PrintDevice;
import org.eclipse.scout.rt.client.ui.form.fields.ICompositeField;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.IGroupBox;
import org.eclipse.scout.rt.client.ui.form.fields.tabbox.ITabBox;

/**
 * Screenshot printer for forms which allows generation of screenshots of an entire forms as well as screenshots of
 * hidden tabboxes.
 */
public class FormScreenshotPrinter {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(PrintScreenshotsFormListener.class);
  private final File m_destinationFolder;
  private final String m_contentType;
  private IForm m_form;

  public FormScreenshotPrinter(File destinationFolder) {
    this(destinationFolder, "image/jpg");
  }

  public FormScreenshotPrinter(File destinationFolder, String contentType) {
    m_destinationFolder = destinationFolder;
    m_contentType = contentType;
  }

  public String getContentType() {
    return m_contentType;
  }

  public File getDestinationFolder() {
    return m_destinationFolder;
  }

  /**
   * Returns form and tab boxes for print queue.
   * <p>
   * Collects hidden tab boxes (not selected) to be printed, because they do not appear on the print of the form, and
   * add them to the print queue.
   * </p>
   * 
   * @param form
   */
  public List<ITypeWithClassId> getPrintObjects(IForm form) {
    ArrayList<ITypeWithClassId> printMap = new ArrayList<ITypeWithClassId>();
    printMap.add(form);
    for (IFormField field : form.getAllFields()) {
      if (field instanceof ITabBox && field.isVisible()) {
        final ITabBox tabBox = (ITabBox) field;

        IGroupBox selectedTab = null;
        if (tabBox.isVisible()) {
          selectedTab = tabBox.getSelectedTab();
        }
        if (tabBox.isVisible()) {
          for (final IGroupBox g : tabBox.getGroupBoxes()) {
            if (g != selectedTab) {
              printMap.add(g);
            }
          }
        }
      }
    }
    return printMap;
  }

  protected File getPrintFile(ITypeWithClassId o) {
    return getPrintFile(o.classId());
  }

  protected File getPrintFile(String baseName) {
    String ext = getContentType().substring(getContentType().lastIndexOf("/") + 1);
    return new File(getDestinationFolder(), baseName + "." + ext);
  }

  protected void printGroupBox(IGroupBox g, File file) {
    LOG.info("Printing: {}", file.getPath());
    ICompositeField parentTab = g.getParentField();
    if (!g.isVisible()) {
      g.setVisible(true);
    }
    if (parentTab instanceof ITabBox) {
      ((ITabBox) parentTab).setSelectedTab(g);
    }
    Map<String, Object> parameters = createPrintParams(file);
    parentTab.printField(PrintDevice.File, parameters);
  }

  protected void printForm(IForm form, File file) {
    LOG.info("Printing: {}", file.getPath());
    Map<String, Object> parameters = createPrintParams(file);
    form.printForm(PrintDevice.File, parameters);
  }

  protected void print(ITypeWithClassId o) {
    File file = getPrintFile(o);
    getDestinationFolder().mkdirs();
    if (o instanceof IForm) {
      printForm((IForm) o, file);
    }
    else if (o instanceof IGroupBox) {
      printGroupBox((IGroupBox) o, file);
    }
    else {
      LOG.error("Could not print object {}", o);
    }
  }

  private Map<String, Object> createPrintParams(File out) {
    HashMap<String, Object> parameters = new HashMap<String, Object>();
    parameters.put("file", out);
    parameters.put("contentType", getContentType());
    return parameters;
  }

  /**
   * @param form
   */
  public void setForm(IForm form) {
    m_form = form;
  }

}
