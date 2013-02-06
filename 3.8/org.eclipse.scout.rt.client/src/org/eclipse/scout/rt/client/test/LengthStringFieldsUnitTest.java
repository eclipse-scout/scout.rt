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
import org.eclipse.scout.rt.client.ui.form.fields.stringfield.IStringField;
import org.osgi.framework.Bundle;

public class LengthStringFieldsUnitTest extends AbstractClientTest {

  @Override
  public void run() throws Exception {
    Bundle clientBundle = ClientTestUtility.getClientBundle();
    BundleBrowser b = new BundleBrowser(clientBundle.getSymbolicName(), ClientTestUtility.getFormsPackage());
    for (String className : b.getClasses(false, false)) {
      Class<?> c = clientBundle.loadClass(className);
      try {
        IForm form = (IForm) c.newInstance();
        for (IFormField field : form.getAllFields()) {
          if (field instanceof IStringField) {
            // test
            setSubTitle(form.getTitle() + " > " + field.getLabel() + " [" + form.getClass().getSimpleName() + "." + field.getClass().getSimpleName() + "]");
            int length = ((IStringField) field).getMaxLength();
            int area = field.getGridData().h * field.getGridData().w;
            if (length < area * 30) {
              addErrorStatus("length<area*30");
            }
            else if (length > area * 300) {
              addErrorStatus("length>area*300");
            }
            else if (length < 10 && area > 1) {
              addWarningStatus("length<10 but area>1");
            }
            else if (length > 1000000) {
              addWarningStatus("length>1'000'000");
            }
            else {
              addOkStatus("length=" + length + ", area=" + area);
            }
          }
        }
      }
      catch (Exception e) {
        addErrorStatus("analyzing form " + c, e);
      }
    }
  }

  @Override
  protected String getConfiguredTitle() {
    return "string fields: size to length comparison";
  }

}
