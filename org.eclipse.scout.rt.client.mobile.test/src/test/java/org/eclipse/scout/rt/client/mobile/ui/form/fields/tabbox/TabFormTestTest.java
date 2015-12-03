/*******************************************************************************
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client.mobile.ui.form.fields.tabbox;

import static org.junit.Assert.assertEquals;

import org.eclipse.scout.rt.client.mobile.ui.form.fields.tabbox.fixture.AbstractTemplate2GroupBox;
import org.eclipse.scout.rt.client.mobile.ui.form.fields.tabbox.fixture.AbstractTemplate3GroupBox;
import org.eclipse.scout.rt.client.mobile.ui.form.fields.tabbox.fixture.TestForm;
import org.eclipse.scout.rt.client.mobile.ui.form.fields.tabbox.fixture.TestForm.MainBox.TabBox.SimpleGroupBox;
import org.eclipse.scout.rt.client.mobile.ui.form.fields.tabbox.fixture.TestForm.MainBox.TabBox.TemplateExGroupBox;
import org.eclipse.scout.rt.client.mobile.ui.form.fields.tabbox.fixture.TestForm.MainBox.TabBox.TemplateGroupBox;
import org.eclipse.scout.rt.client.mobile.ui.form.fields.tabbox.fixture.TestFormData;
import org.eclipse.scout.rt.client.session.ClientSessionProvider;
import org.eclipse.scout.rt.client.testenvironment.TestEnvironmentClientSession;
import org.eclipse.scout.rt.testing.client.runner.ClientTestRunner;
import org.eclipse.scout.rt.testing.client.runner.RunWithClientSession;
import org.eclipse.scout.rt.testing.platform.runner.RunWithSubject;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(ClientTestRunner.class)
@RunWithSubject("default")
@RunWithClientSession(TestEnvironmentClientSession.class)
public class TabFormTestTest {

  @Test
  public void testSimpleExport() {
    TestForm f = new TestForm();
    TabForm tabForm = null;
    try {
      //Wrap tabbox
      TabBoxGroupBox mobileTabBox = new TabBoxGroupBox(f.getTabBox());
      mobileTabBox.initField();

      //Select first groupBox
      mobileTabBox.getTableField().getTable().selectFirstRow();
      tabForm = ClientSessionProvider.currentSession().getDesktop().findForm(TabForm.class);

      tabForm.getRootGroupBox().getFieldByClass(SimpleGroupBox.TextSimpleField.class).setValue("s1");

      //store some values
      TestFormData formData = new TestFormData();
      f.exportFormData(formData);

      assertEquals("s1", formData.getTextSimple().getValue());
    }
    finally {
      if (tabForm != null) {
        tabForm.doClose();
      }
      f.doClose();
    }
  }

  @Test
  public void testTemplateExport() {
    TestForm f = new TestForm();
    TabForm tabForm = null;
    try {
      //Wrap tabbox
      TabBoxGroupBox mobileTabBox = new TabBoxGroupBox(f.getTabBox());
      mobileTabBox.initField();

      //Select template groupBox
      mobileTabBox.getTableField().getTable().selectRow(1);

      tabForm = ClientSessionProvider.currentSession().getDesktop().findForm(TabForm.class);
      tabForm.getRootGroupBox().getFieldByClass(TemplateGroupBox.Text1Field.class).setValue("s1");
      tabForm.getRootGroupBox().getFieldByClass(TemplateGroupBox.Text2Field.class).setValue("s2");
      tabForm.getRootGroupBox().getFieldByClass(AbstractTemplate2GroupBox.T2Text1Field.class).setValue("t1");
      tabForm.getRootGroupBox().getFieldByClass(AbstractTemplate3GroupBox.T3Text1Field.class).setValue("t2");

      //store some values
      TestFormData formData = new TestFormData();
      f.exportFormData(formData);

      assertEquals("s1", formData.getTemplateGroupBox().getText1().getValue());
      assertEquals("s2", formData.getTemplateGroupBox().getText2().getValue());
      assertEquals("t2", formData.getTemplateGroupBox().getTabTemplate().getT3Text1().getValue());
    }
    finally {
      if (tabForm != null) {
        tabForm.doClose();
      }
      f.doClose();
    }
  }

  @Test
  public void testTemplateExExport() {
    TestForm f = new TestForm();
    TabForm tabForm = null;
    try {
      //Wrap tabbox
      TabBoxGroupBox mobileTabBox = new TabBoxGroupBox(f.getTabBox());
      mobileTabBox.initField();

      //Select templateEx groupBox
      mobileTabBox.getTableField().getTable().selectRow(2);

      tabForm = ClientSessionProvider.currentSession().getDesktop().findForm(TabForm.class);
      tabForm.getRootGroupBox().getFieldByClass(TemplateExGroupBox.Text1Field.class).setValue("s1");
      tabForm.getRootGroupBox().getFieldByClass(TemplateExGroupBox.Text2Field.class).setValue("s2");
      tabForm.getRootGroupBox().getFieldByClass(TemplateExGroupBox.Text3Field.class).setValue("s3");

      //store some values
      TestFormData formData = new TestFormData();
      f.exportFormData(formData);

      assertEquals("s1", formData.getTemplateExGroupBox().getText1().getValue());
      assertEquals("s2", formData.getTemplateExGroupBox().getText2().getValue());
      assertEquals("s3", formData.getTemplateExGroupBox().getText3().getValue());
    }
    finally {
      if (tabForm != null) {
        tabForm.doClose();
      }
      f.doClose();
    }
  }
}
