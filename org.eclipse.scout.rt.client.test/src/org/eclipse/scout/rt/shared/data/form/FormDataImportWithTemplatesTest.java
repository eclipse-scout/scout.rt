/*******************************************************************************
 * Copyright (c) 2013 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.shared.data.form;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.ui.form.fixture.TestForm;
import org.eclipse.scout.rt.shared.data.form.fixture.TestFormData;
import org.eclipse.scout.testing.client.runner.ScoutClientTestRunner;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(ScoutClientTestRunner.class)
public class FormDataImportWithTemplatesTest {

  @Test
  public void testExportImport() throws ProcessingException {
    //store some values
    TestForm f = new TestForm();
    f.getText1Field().setValue("t1");
    f.getText3Field().setValue("t3");
    f.getText4Field().setValue("t4");
    f.getG1Box().getText1Field().setValue("g1t1");
    f.getG1Box().getText2Field().setValue("g1t2");
    f.getG2Box().getText1Field().setValue("g2t1");
    f.getG2Box().getText2Field().setValue("g2t2");
    f.getG3G4Text2Field().setValue("g3g2");
    f.getG1Box().getTestListBox().setValue(new String[]{"g1L"});
    f.getG2Box().getTestListBox().setValue(new String[]{"g2L"});

    TestFormData formData = new TestFormData();
    f.exportFormData(formData);

    f = new TestForm();
    f.importFormData(formData);

    //new form should contain the stored values
    Assert.assertEquals("t1", f.getText1Field().getValue());
    Assert.assertEquals("t3", f.getText3Field().getValue());
    Assert.assertEquals("t4", f.getText4Field().getValue());
    Assert.assertEquals("g1t1", f.getG1Box().getText1Field().getValue());
    Assert.assertEquals("g1t2", f.getG1Box().getText2Field().getValue());
    Assert.assertEquals("g2t1", f.getG2Box().getText1Field().getValue());
    Assert.assertEquals("g3g2", f.getG3G4Text2Field().getValue());
    Assert.assertEquals("g1L", f.getG1Box().getTestListBox().getValue()[0]);
    Assert.assertEquals("g2L", f.getG2Box().getTestListBox().getValue()[0]);
  }
}
