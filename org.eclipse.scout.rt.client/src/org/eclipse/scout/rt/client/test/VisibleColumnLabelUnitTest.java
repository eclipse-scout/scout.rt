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
package org.eclipse.scout.rt.client.test;

import javax.swing.JLabel;

import org.eclipse.scout.commons.runtime.BundleBrowser;
import org.eclipse.scout.rt.client.services.common.test.AbstractClientTest;
import org.eclipse.scout.rt.client.services.common.test.ClientTestUtility;
import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.client.ui.basic.table.columns.IColumn;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.client.ui.form.fields.tablefield.ITableField;
import org.osgi.framework.Bundle;

public class VisibleColumnLabelUnitTest extends AbstractClientTest {

  @Override
  public void run() throws Exception {
    Bundle clientBundle = ClientTestUtility.getClientBundle();
    BundleBrowser b = new BundleBrowser(clientBundle.getSymbolicName(), ClientTestUtility.getFormsPackage());
    for (String className : b.getClasses(false, false)) {
      Class<?> c = clientBundle.loadClass(className);
      try {
        IForm form = (IForm) c.newInstance();
        for (IFormField childField : form.getAllFields()) {
          if (childField instanceof ITableField) {
            // / auch andere Tabellen durchsuchen?
            // / Pixel von AutoResize
            ITableField<?> tableField = (ITableField<?>) childField;
            ITable table = tableField.getTable();
            for (IColumn<?> column : table.getColumns()) {
              // test
              setSubTitle(form.getTitle() + " > " + childField.getLabel() + " [" + form.getClass().getSimpleName() + "." + childField.getClass().getSimpleName() + "]");
              JLabel l = new JLabel();
              int pix = l.getFontMetrics(l.getFont()).stringWidth(column.getHeaderCell().getText());
              int w = column.getWidth() - l.getInsets().left - l.getInsets().right;
              if (column.isVisible() && pix > w) {
                addWarningStatus("width (" + w + ") < label (" + pix + ")");
              }
              else {
                addOkStatus("width (" + w + ") > label (" + pix + ")");
              }
            }
          }
        }
      }
      catch (Exception e) {
      }
    }
  }

  @Override
  protected String getConfiguredTitle() {
    return "dialog tables: columns wide enough for label";
  }
}
