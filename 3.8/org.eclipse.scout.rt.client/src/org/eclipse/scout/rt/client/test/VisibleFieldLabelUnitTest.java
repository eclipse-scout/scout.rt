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
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.client.ui.form.fields.button.AbstractButton;
import org.eclipse.scout.rt.client.ui.form.fields.datefield.AbstractDateField;
import org.eclipse.scout.rt.client.ui.form.fields.doublefield.AbstractDoubleField;
import org.eclipse.scout.rt.client.ui.form.fields.smartfield.AbstractSmartField;
import org.eclipse.scout.rt.client.ui.form.fields.stringfield.AbstractStringField;
import org.osgi.framework.Bundle;

public class VisibleFieldLabelUnitTest extends AbstractClientTest {

  @Override
  public void run() throws Exception {
    Bundle clientBundle = ClientTestUtility.getClientBundle();
    BundleBrowser b = new BundleBrowser(clientBundle.getSymbolicName(), ClientTestUtility.getFormsPackage());
    for (String className : b.getClasses(false, false)) {
      Class<?> c = clientBundle.loadClass(className);
      try {
        IForm form = (IForm) c.newInstance();
        for (IFormField childField : form.getAllFields()) {
          if (childField.getLabel() != null) {
            // test
            setSubTitle(form.getTitle() + " > " + childField.getLabel() + " [" + form.getClass().getSimpleName() + "." + childField.getClass().getSimpleName() + "]");
            JLabel l = new JLabel();
            int pix = l.getFontMetrics(l.getFont()).stringWidth(childField.getLabel());
            if (pix > 130 && (AbstractDateField.class.isAssignableFrom(childField.getClass()) || AbstractDoubleField.class.isAssignableFrom(childField.getClass()) || AbstractSmartField.class.isAssignableFrom(childField.getClass()) || AbstractStringField.class.isAssignableFrom(childField.getClass()))) {
              addWarningStatus(childField.getLabel() + " (" + pix + "px) > 130px");
            }
            else if (pix > (185 * childField.getGridData().w) && AbstractButton.class.isAssignableFrom(childField.getClass()) || AbstractButton.class.isAssignableFrom(childField.getClass())) {
              addWarningStatus(childField.getLabel() + " (" + pix + "px) > " + (185 * childField.getGridData().w) + "px");
            }
            else {
              addOkStatus();
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
    return "dialog fields: field wide enough for label";
  }
}
