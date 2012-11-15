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

import org.eclipse.scout.commons.runtime.BundleBrowser;
import org.eclipse.scout.rt.client.services.common.test.AbstractClientTest;
import org.eclipse.scout.rt.client.services.common.test.ClientTestUtility;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.client.ui.form.fields.IValueField;
import org.osgi.framework.Bundle;

public class ExistsLabelFieldsUnitTest extends AbstractClientTest {

  @Override
  public void run() throws Exception {
    Bundle clientBundle = ClientTestUtility.getClientBundle();
    BundleBrowser b = new BundleBrowser(clientBundle.getSymbolicName(), ClientTestUtility.getFormsPackage());
    for (String className : b.getClasses(false, false)) {
      Class<?> c = clientBundle.loadClass(className);
      try {
        IForm form = (IForm) c.newInstance();
        for (IFormField childField : form.getAllFields()) {
          if (childField instanceof IValueField) {
            // test
            setSubTitle(form.getTitle() + " > " + childField.getLabel() + form.getClass().getSimpleName() + "." + childField.getClass().getSimpleName());
            if (childField.getLabel() == null) {
              addErrorStatus("no label");
            }
            else if (childField.getLabel() != null && childField.getLabel().contains("{undefined")) {
              addWarningStatus("no label translation");
            }
            else {
              addOkStatus(childField.getLabel());
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
    return "value fields without a label";
  }

}
