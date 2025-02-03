/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.action.menu.form.fields;

import static org.junit.Assert.*;

import java.math.BigDecimal;

import org.eclipse.scout.rt.client.testenvironment.TestEnvironmentClientSession;
import org.eclipse.scout.rt.client.ui.action.menu.form.fields.fixture.FormFieldMenuTestForm;
import org.eclipse.scout.rt.client.ui.action.menu.form.fields.fixture.FormFieldMenuTestForm.InnerForm.MainBox.TestStringField;
import org.eclipse.scout.rt.client.ui.action.menu.form.fields.fixture.FormFieldMenuTestForm.MainBox.FormFieldMenu.BigDecimalInMenuField;
import org.eclipse.scout.rt.client.ui.action.menu.form.fields.fixture.FormFieldMenuTestFormData;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.IGroupBox;
import org.eclipse.scout.rt.testing.client.runner.ClientTestRunner;
import org.eclipse.scout.rt.testing.client.runner.RunWithClientSession;
import org.eclipse.scout.rt.testing.platform.runner.RunWithSubject;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(ClientTestRunner.class)
@RunWithSubject("anna")
@RunWithClientSession(TestEnvironmentClientSession.class)
public class FormFieldMenuTest {
  @Test
  public void testGetForm() {
    FormFieldMenuTestForm form = new FormFieldMenuTestForm();
    form.setShowOnStart(false);
    form.start();

    BigDecimalInMenuField field = form.getBigDecimalInMenuField();
    assertTrue(field.isInitDone());
    assertSame(form, field.getForm());
    form.doClose();
    assertTrue(field.isDisposeDone());
    assertTrue(form.isDisposeDone());
  }

  @Test
  public void testSetFieldStyle() {
    FormFieldMenuTestForm form = new FormFieldMenuTestForm();
    form.setShowOnStart(false);
    form.start();

    BigDecimalInMenuField fieldInMenu = form.getBigDecimalInMenuField();
    IGroupBox mainBox = form.getRootGroupBox();
    TestStringField testStringField = form.getTestStringField();

    assertEquals(IFormField.FIELD_STYLE_ALTERNATIVE, fieldInMenu.getFieldStyle());
    assertEquals(IFormField.FIELD_STYLE_ALTERNATIVE, mainBox.getFieldStyle());
    assertEquals(IFormField.FIELD_STYLE_ALTERNATIVE, testStringField.getFieldStyle());

    mainBox.setFieldStyle(IFormField.FIELD_STYLE_CLASSIC, true);

    assertEquals(IFormField.FIELD_STYLE_ALTERNATIVE, fieldInMenu.getFieldStyle()); // change is not propagated into fields that are not part of the main field tree
    assertEquals(IFormField.FIELD_STYLE_CLASSIC, mainBox.getFieldStyle());
    assertEquals(IFormField.FIELD_STYLE_CLASSIC, testStringField.getFieldStyle());

    mainBox.setFieldStyle(IFormField.FIELD_STYLE_ALTERNATIVE, false);
    assertEquals(IFormField.FIELD_STYLE_ALTERNATIVE, fieldInMenu.getFieldStyle());
    assertEquals(IFormField.FIELD_STYLE_ALTERNATIVE, mainBox.getFieldStyle());
    assertEquals(IFormField.FIELD_STYLE_CLASSIC, testStringField.getFieldStyle()); // change is not propagated to children

    form.doClose();
  }

  @Test
  public void testSetDisabledStyle() {
    FormFieldMenuTestForm form = new FormFieldMenuTestForm();
    form.setShowOnStart(false);
    form.start();

    BigDecimalInMenuField field = form.getBigDecimalInMenuField();
    IGroupBox mainBox = form.getRootGroupBox();
    TestStringField testStringField = form.getTestStringField();

    assertEquals(IFormField.DISABLED_STYLE_DEFAULT, field.getDisabledStyle());
    assertEquals(IFormField.DISABLED_STYLE_DEFAULT, mainBox.getDisabledStyle());
    assertEquals(IFormField.DISABLED_STYLE_DEFAULT, testStringField.getDisabledStyle());

    mainBox.setDisabledStyle(IFormField.DISABLED_STYLE_READ_ONLY, true);

    assertEquals(IFormField.DISABLED_STYLE_DEFAULT, field.getDisabledStyle()); // change is not propagated into fields that are not part of the main field tree
    assertEquals(IFormField.DISABLED_STYLE_READ_ONLY, mainBox.getDisabledStyle());
    assertEquals(IFormField.DISABLED_STYLE_READ_ONLY, testStringField.getDisabledStyle());

    form.doClose();
  }

  @Test
  public void testSetStatusVisible() {
    FormFieldMenuTestForm form = new FormFieldMenuTestForm();
    form.setShowOnStart(false);
    form.start();

    BigDecimalInMenuField field = form.getBigDecimalInMenuField();
    IGroupBox mainBox = form.getRootGroupBox();
    TestStringField testStringField = form.getTestStringField();

    assertTrue(field.isStatusVisible());
    assertTrue(mainBox.isStatusVisible());
    assertTrue(testStringField.isStatusVisible());

    mainBox.setStatusVisible(false, true);
    assertTrue(field.isStatusVisible()); // status change is not propagated into fields that are not part of the main field tree
    assertFalse(mainBox.isStatusVisible());
    assertFalse(testStringField.isStatusVisible()); // nested inner forms are part of the main field tree

    form.doClose();
  }

  @Test
  public void testParentField() {
    FormFieldMenuTestForm form = new FormFieldMenuTestForm();
    form.setShowOnStart(false);
    form.start();

    BigDecimalInMenuField field = form.getBigDecimalInMenuField();
    assertNull(field.getParentField()); // the form field in the menu has no direct parent form field.

    form.doClose();
  }

  @Test
  public void testFormDataImport() {
    FormFieldMenuTestForm form = new FormFieldMenuTestForm();
    form.setShowOnStart(false);
    form.start();

    FormFieldMenuTestFormData data = new FormFieldMenuTestFormData();
    String value = "2.22";
    data.getBigDecimalInMenu().setValue(new BigDecimal(value));

    form.importFormData(data);
    assertEquals(new BigDecimal(value), form.getBigDecimalInMenuField().getValue());
    form.doClose();
  }
}
