/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.form.fields;

import static org.junit.Assert.*;

import java.math.BigDecimal;

import org.eclipse.scout.rt.client.testenvironment.TestEnvironmentClientSession;
import org.eclipse.scout.rt.client.ui.action.menu.form.fields.fixture.FormFieldMenuTestForm;
import org.eclipse.scout.rt.client.ui.action.menu.form.fields.fixture.FormFieldMenuTestForm.InnerForm.MainBox.TestStringField;
import org.eclipse.scout.rt.client.ui.action.menu.form.fields.fixture.FormFieldMenuTestForm.MainBox.FormFieldMenu.BigDecimalInMenuField;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.IGroupBox;
import org.eclipse.scout.rt.testing.client.runner.ClientTestRunner;
import org.eclipse.scout.rt.testing.client.runner.RunWithClientSession;
import org.eclipse.scout.rt.testing.platform.runner.RunWithSubject;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(ClientTestRunner.class)
@RunWithSubject("anna")
@RunWithClientSession(TestEnvironmentClientSession.class)
public class FormFieldPropagationTest {
  @Test
  public void testMandatory() {
    FormFieldMenuTestForm form = new FormFieldMenuTestForm();
    form.setShowOnStart(false);
    form.start();

    BigDecimalInMenuField fieldInMenu = form.getBigDecimalInMenuField();
    IGroupBox mainBox = form.getRootGroupBox();
    TestStringField testStringField = form.getTestStringField();

    mainBox.setMandatory(true, true);
    assertTrue(fieldInMenu.isMandatory());
    assertTrue(mainBox.isMandatory());
    assertTrue(testStringField.isMandatory());

    mainBox.setMandatory(false, false);
    assertTrue(fieldInMenu.isMandatory());
    assertFalse(mainBox.isMandatory());
    assertTrue(testStringField.isMandatory());
  }

  @Test
  public void testIsEmpty() {
    FormFieldMenuTestForm form = new FormFieldMenuTestForm();
    form.setShowOnStart(false);
    form.start();

    BigDecimalInMenuField fieldInMenu = form.getBigDecimalInMenuField();
    IGroupBox mainBox = form.getRootGroupBox();
    TestStringField testStringField = form.getTestStringField();

    assertTrue(fieldInMenu.isEmpty());
    assertTrue(mainBox.isEmpty());
    assertTrue(testStringField.isEmpty());

    fieldInMenu.setValue(new BigDecimal("2.0"));

    assertFalse(fieldInMenu.isEmpty());
    assertFalse(mainBox.isEmpty());
    assertTrue(testStringField.isEmpty());
  }

  @Test
  public void testMarkSaved() {
    FormFieldMenuTestForm form = new FormFieldMenuTestForm();
    form.setShowOnStart(false);
    form.start();

    BigDecimalInMenuField fieldInMenu = form.getBigDecimalInMenuField();
    IGroupBox mainBox = form.getRootGroupBox();
    TestStringField testStringField = form.getTestStringField();
    fieldInMenu.setValue(new BigDecimal("2.0"));
    form.checkSaveNeeded();
    assertTrue(fieldInMenu.isSaveNeeded());
    assertTrue(mainBox.isSaveNeeded());
    assertFalse(testStringField.isSaveNeeded());

    form.markSaved();
    assertFalse(fieldInMenu.isSaveNeeded());
    assertFalse(mainBox.isSaveNeeded());
    assertFalse(testStringField.isSaveNeeded());
  }

  @Test
  public void testIsSaveNeeded() {
    FormFieldMenuTestForm form = new FormFieldMenuTestForm();
    form.setShowOnStart(false);
    form.start();

    BigDecimalInMenuField fieldInMenu = form.getBigDecimalInMenuField();
    IGroupBox mainBox = form.getRootGroupBox();
    TestStringField testStringField = form.getTestStringField();

    fieldInMenu.setValue(new BigDecimal("2.0"));

    assertTrue(fieldInMenu.isSaveNeeded());
    assertTrue(mainBox.isSaveNeeded());
    assertFalse(testStringField.isSaveNeeded());

    fieldInMenu.setValue(null);
    assertFalse(fieldInMenu.isSaveNeeded());
    assertFalse(mainBox.isSaveNeeded());
    assertFalse(testStringField.isSaveNeeded());

    testStringField.setValue("val");
    assertFalse(fieldInMenu.isSaveNeeded());
    assertTrue(mainBox.isSaveNeeded());
    assertTrue(testStringField.isSaveNeeded());
  }
}
