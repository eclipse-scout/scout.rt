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
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.scout.commons.CollectionUtility;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.client.ui.form.PrintDevice;
import org.eclipse.scout.rt.client.ui.form.fields.ICompositeField;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.IGroupBox;
import org.eclipse.scout.rt.client.ui.form.fields.tabbox.ITabBox;
import org.eclipse.scout.rt.spec.client.FormFieldUtility;

/**
 *
 */
public class FormPrinter {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(PrintFormListener.class);
  private final File m_destinationFolder;
  private final String m_contentType;

  public FormPrinter(File destinationFolder) {
    this(destinationFolder, "image/jpg");
  }

  /**
   *
   */
  public FormPrinter(File destinationFolder, String contentType) {
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
   * Returns all
   * Adds form and fields to print queues.
   * <p>
   * Collects hidden tab boxes (not selected) to be printed, because they do not appear on the print of the form, and
   * add them to the print queue.
   * </p>
   * 
   * @param form
   */
  public Map<File, Object> getPrintObjects(IForm form) {
    LinkedHashMap<File, Object> printMap = new LinkedHashMap<File, Object>();
    printMap.put(getPrintFile(form), form);
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
              printMap.put(getPrintFile(g), g);
            }
          }
        }
      }
    }
    return printMap;
  }

  public File[] getPrintFiles(IForm form) {
    Map<File, Object> printObjects = getPrintObjects(form);
    return CollectionUtility.toArray(printObjects.keySet(), File.class);
  }

  protected File getPrintFile(Object o) {
    String name = o.getClass().getName();
    return getPrintFile(o, name);
  }

  protected File getPrintFile(IFormField field) {
    String name = FormFieldUtility.getUniqueFieldId(field);
    return getPrintFile(field, name);
  }

  private File getPrintFile(Object o, String name) {
    String ext = getContentType().substring(getContentType().lastIndexOf("/") + 1);
    return new File(getDestinationFolder(), name + "." + ext);
  }

  protected void printGroupBox(IGroupBox g) {
    File out = getPrintFile(g);
    LOG.info("Printing: {}", out.getPath());
    ICompositeField parentTab = g.getParentField();
    if (!g.isVisible()) {
      g.setVisible(true);
    }
    if (parentTab instanceof ITabBox) {
      ((ITabBox) parentTab).setSelectedTab(g);
    }
    getDestinationFolder().mkdirs();
    HashMap<String, Object> parameters = createPrintParams(out);
    parentTab.printField(PrintDevice.File, parameters);
  }

  protected void printForm(IForm form) {
    File out = getPrintFile(form);
    LOG.info("Printing: {}", out.getPath());
    getDestinationFolder().mkdirs();
    HashMap<String, Object> parameters = createPrintParams(out);
    form.printForm(PrintDevice.File, parameters);
  }

  protected void print(Object o) {
    if (o instanceof IForm) {
      printForm((IForm) o);
    }
    else if (o instanceof IFormField) {
      printGroupBox((IGroupBox) o);
    }
    else {
      LOG.error("Could not print field {}", o);
    }
  }

  private HashMap<String, Object> createPrintParams(File out) {
    HashMap<String, Object> parameters = new HashMap<String, Object>();
    parameters.put("file", out);
    parameters.put("contentType", getContentType());
    return parameters;
  }

}
