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

import static org.junit.Assert.assertEquals;

import org.eclipse.scout.rt.client.testenvironment.TestEnvironmentClientSession;
import org.eclipse.scout.rt.client.ui.form.fixture.TestForm;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.shared.data.form.fixture.TestFormData;
import org.eclipse.scout.rt.testing.client.runner.ClientTestRunner;
import org.eclipse.scout.rt.testing.client.runner.RunWithClientSession;
import org.eclipse.scout.rt.testing.platform.runner.RunWithSubject;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(ClientTestRunner.class)
@RunWithSubject("default")
@RunWithClientSession(TestEnvironmentClientSession.class)
public class FormDataImportWithTemplatesTest {

  @Test
  public void testExportImport() {
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
    f.getG1Box().getTestListBox().setValue(CollectionUtility.hashSet("g1L"));
    f.getG2Box().getTestListBox().setValue(CollectionUtility.hashSet("g2L"));

    TestFormData formData = new TestFormData();
    f.exportFormData(formData);

    f = new TestForm();
    f.importFormData(formData);

    //new form should contain the stored values
    assertEquals("t1", f.getText1Field().getValue());
    assertEquals("t3", f.getText3Field().getValue());
    assertEquals("t4", f.getText4Field().getValue());
    assertEquals("g1t1", f.getG1Box().getText1Field().getValue());
    assertEquals("g1t2", f.getG1Box().getText2Field().getValue());
    assertEquals("g2t1", f.getG2Box().getText1Field().getValue());
    assertEquals("g3g2", f.getG3G4Text2Field().getValue());
    assertEquals("g1L", CollectionUtility.firstElement(f.getG1Box().getTestListBox().getValue()));
    assertEquals("g2L", CollectionUtility.firstElement(f.getG2Box().getTestListBox().getValue()));
  }
}
