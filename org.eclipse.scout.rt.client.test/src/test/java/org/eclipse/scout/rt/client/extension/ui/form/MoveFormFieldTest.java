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
package org.eclipse.scout.rt.client.extension.ui.form;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.eclipse.scout.extension.AbstractLocalExtensionTestCase;
import org.eclipse.scout.rt.client.extension.ui.form.fields.fixture.MoveFieldsTestForm;
import org.eclipse.scout.rt.client.extension.ui.form.fields.fixture.MoveFieldsTestForm.MainBox.TopBox.SubBox.StringField;
import org.eclipse.scout.rt.client.extension.ui.form.fixture.AbstractTemplateFieldsBox;
import org.eclipse.scout.rt.client.extension.ui.form.fixture.AbstractTemplateFieldsBox.BottomStringField;
import org.eclipse.scout.rt.client.extension.ui.form.fixture.AbstractTemplateFieldsBox.TopStringField;
import org.eclipse.scout.rt.client.extension.ui.form.fixture.AbstractTemplateGroupsBox;
import org.eclipse.scout.rt.client.extension.ui.form.fixture.AbstractTemplateGroupsBox.BottomFieldsBox;
import org.eclipse.scout.rt.client.extension.ui.form.fixture.AbstractTemplateGroupsBox.TopFieldsBox;
import org.eclipse.scout.rt.client.extension.ui.form.fixture.MoveFormFieldStackOverflowForm;
import org.eclipse.scout.rt.client.extension.ui.form.fixture.MultiTemplateUsageForm;
import org.eclipse.scout.rt.client.extension.ui.form.fixture.MultiTemplateUsageForm.MainBox.FirstTemplateBox;
import org.eclipse.scout.rt.client.extension.ui.form.fixture.MultiTemplateUsageForm.MainBox.FirstTemplateBox.MiddleStringField;
import org.eclipse.scout.rt.client.extension.ui.form.fixture.MultiTemplateUsageForm.MainBox.MainBoxStringField;
import org.eclipse.scout.rt.client.extension.ui.form.fixture.MultiTemplateUsageForm.MainBox.SecondTemplateBox;
import org.eclipse.scout.rt.client.extension.ui.form.fixture.OrigForm;
import org.eclipse.scout.rt.client.extension.ui.form.fixture.OrigForm.MainBox.BottomBox;
import org.eclipse.scout.rt.client.extension.ui.form.fixture.OrigForm.MainBox.TopBox;
import org.eclipse.scout.rt.client.extension.ui.form.fixture.OrigForm.MainBox.TopBox.NameField;
import org.eclipse.scout.rt.client.extension.ui.form.fixture.OrigFormEx;
import org.eclipse.scout.rt.client.extension.ui.form.fixture.OrigFormEx.BottomBoxEx;
import org.eclipse.scout.rt.client.extension.ui.form.fixture.OrigFormEx.BottomBoxEx.CityField;
import org.eclipse.scout.rt.client.extension.ui.form.fixture.SingleTemplateUsageForm;
import org.eclipse.scout.rt.client.testenvironment.TestEnvironmentClientSession;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.classid.ClassIdentifier;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.shared.extension.IExtensionRegistry;
import org.eclipse.scout.rt.testing.client.runner.ClientTestRunner;
import org.eclipse.scout.rt.testing.client.runner.RunWithClientSession;
import org.eclipse.scout.rt.testing.platform.runner.RunWithSubject;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(ClientTestRunner.class)
@RunWithSubject("default")
@RunWithClientSession(TestEnvironmentClientSession.class)
public class MoveFormFieldTest extends AbstractLocalExtensionTestCase {

  @Test
  public void testMoveField() throws Exception {
    BEANS.get(IExtensionRegistry.class).registerMove(NameField.class, 20d, BottomBox.class);
    OrigForm form = new OrigForm();
    assertOrigFormMovedFields(form);
    assertEquals(2, form.getBottomBox().getFieldCount());
  }

  @Test
  public void testMoveFieldToReplacedContainer() {
    BEANS.get(IExtensionRegistry.class).registerMove(NameField.class, 20d, BottomBox.class);
    OrigFormEx form = new OrigFormEx();
    assertOrigFormMovedFields(form);

    // additional assertions for OrigFormEx with replaced BottomBox
    assertEquals(3, form.getBottomBox().getFieldCount());
    assertTrue(form.getBottomBox() instanceof BottomBoxEx);
    assertSame(form.getNameField(), form.getFieldByClass(BottomBoxEx.class).getFieldByClass(NameField.class));
    assertSame(form.getFieldByClass(CityField.class), form.getBottomBox().getFieldByClass(CityField.class));
  }

  /**
   * Basic assertions for OrigForm and NameField moved to BottomBox.
   */
  private void assertOrigFormMovedFields(OrigForm form) {
    assertEquals(1, form.getTopBox().getFieldCount());
    assertSame(form.getSalutationField(), form.getTopBox().getFields().get(0));

    assertSame(form.getStreetField(), form.getBottomBox().getFields().get(0));
    assertSame(form.getNameField(), form.getBottomBox().getFields().get(1));
    assertEquals(20d, form.getNameField().getOrder(), 0);

    assertEquals(form.getTopBox().getFields(), form.getTopBox().getControlFields());
    assertTrue(form.getTopBox().getGroupBoxes().isEmpty());
    assertTrue(form.getTopBox().getCustomProcessButtons().isEmpty());
    assertTrue(form.getTopBox().getSystemProcessButtons().isEmpty());

    assertEquals(form.getBottomBox().getFields(), form.getBottomBox().getControlFields());
    assertTrue(form.getBottomBox().getGroupBoxes().isEmpty());
    assertTrue(form.getBottomBox().getCustomProcessButtons().isEmpty());
    assertTrue(form.getBottomBox().getSystemProcessButtons().isEmpty());

    assertSame(form.getNameField(), form.getTopBox().getFieldByClass(NameField.class));
    assertSame(form.getNameField(), form.getBottomBox().getFieldByClass(NameField.class));
  }

  @Test
  public void testDeregisterMove() throws Exception {
    BEANS.get(IExtensionRegistry.class).registerMove(NameField.class, 20d, BottomBox.class);
    boolean changed = BEANS.get(IExtensionRegistry.class).deregisterMove(NameField.class, 20d, BottomBox.class);
    assertTrue(changed);

    OrigForm form = new OrigForm();
    assertEquals(2, form.getTopBox().getFieldCount());
    assertEquals(5, form.getTopBox().getFieldByClass(NameField.class).getOrder(), 0.000001);
  }

  @Test
  public void testMoveFieldMultipleTimes() throws Exception {
    BEANS.get(IExtensionRegistry.class).registerMove(NameField.class, 20d, BottomBox.class);
    BEANS.get(IExtensionRegistry.class).registerMove(NameField.class, 10d, TopBox.class);
    OrigForm form = new OrigForm();

    assertEquals(2, form.getTopBox().getFieldCount());
    assertSame(form.getSalutationField(), form.getTopBox().getFields().get(0));
    assertSame(form.getNameField(), form.getTopBox().getFields().get(1));
    assertEquals(10d, form.getNameField().getOrder(), 0);

    assertEquals(1, form.getBottomBox().getFieldCount());
    assertSame(form.getStreetField(), form.getBottomBox().getFields().get(0));

    assertEquals(form.getTopBox().getFields(), form.getTopBox().getControlFields());
    assertTrue(form.getTopBox().getGroupBoxes().isEmpty());
    assertTrue(form.getTopBox().getCustomProcessButtons().isEmpty());
    assertTrue(form.getTopBox().getSystemProcessButtons().isEmpty());

    assertEquals(form.getBottomBox().getFields(), form.getBottomBox().getControlFields());
    assertTrue(form.getBottomBox().getGroupBoxes().isEmpty());
    assertTrue(form.getBottomBox().getCustomProcessButtons().isEmpty());
    assertTrue(form.getBottomBox().getSystemProcessButtons().isEmpty());

    assertSame(form.getNameField(), form.getTopBox().getFieldByClass(NameField.class));
    assertNull(form.getBottomBox().getFieldByClass(NameField.class));
  }

  @Test
  public void testMoveFieldToRoot() throws Exception {
    BEANS.get(IExtensionRegistry.class).registerMoveToRoot(NameField.class, 30d);
    OrigForm form = new OrigForm();

    assertEquals(1, form.getTopBox().getFieldCount());
    assertSame(form.getSalutationField(), form.getTopBox().getFields().get(0));

    assertEquals(3, form.getMainBox().getFieldCount());
    assertSame(form.getTopBox(), form.getMainBox().getFields().get(0));
    assertSame(form.getBottomBox(), form.getMainBox().getFields().get(1));
    assertSame(form.getNameField(), form.getMainBox().getFields().get(2));

    assertSame(form.getNameField(), form.getTopBox().getFieldByClass(NameField.class));
    assertSame(form.getNameField(), form.getMainBox().getFieldByClass(NameField.class));
  }

  @Test
  public void testMoveFieldOrderOnlyReparentMethod() throws Exception {
    BEANS.get(IExtensionRegistry.class).registerMove(NameField.class, -5d, null);
    doTestMoveFieldOrderOnly();
  }

  @Test
  public void testMoveFieldOrderOnlyMoveMethod() throws Exception {
    BEANS.get(IExtensionRegistry.class).registerMove(NameField.class, -5d);
    doTestMoveFieldOrderOnly();
  }

  private void doTestMoveFieldOrderOnly() {
    OrigForm form = new OrigForm();

    assertEquals(2, form.getTopBox().getFieldCount());
    assertSame(form.getNameField(), form.getTopBox().getFields().get(0));
    assertEquals(-5, form.getNameField().getOrder(), 0);
    assertSame(form.getSalutationField(), form.getTopBox().getFields().get(1));

    assertEquals(1, form.getBottomBox().getFieldCount());
    assertSame(form.getStreetField(), form.getBottomBox().getFields().get(0));

    assertEquals(form.getTopBox().getFields(), form.getTopBox().getControlFields());
    assertTrue(form.getTopBox().getGroupBoxes().isEmpty());
    assertTrue(form.getTopBox().getCustomProcessButtons().isEmpty());
    assertTrue(form.getTopBox().getSystemProcessButtons().isEmpty());

    assertEquals(form.getBottomBox().getFields(), form.getBottomBox().getControlFields());
    assertTrue(form.getBottomBox().getGroupBoxes().isEmpty());
    assertTrue(form.getBottomBox().getCustomProcessButtons().isEmpty());
    assertTrue(form.getBottomBox().getSystemProcessButtons().isEmpty());
  }

  @Test
  public void testMoveFieldContainerOnly() throws Exception {
    BEANS.get(IExtensionRegistry.class).registerMove(NameField.class, null, BottomBox.class);
    OrigForm form = new OrigForm();

    assertEquals(1, form.getTopBox().getFieldCount());
    assertSame(form.getSalutationField(), form.getTopBox().getFields().get(0));

    assertEquals(2, form.getBottomBox().getFieldCount());
    assertSame(form.getNameField(), form.getBottomBox().getFields().get(0));
    assertEquals(5, form.getNameField().getOrder(), 0);
    assertSame(form.getStreetField(), form.getBottomBox().getFields().get(1));

    assertEquals(form.getTopBox().getFields(), form.getTopBox().getControlFields());
    assertTrue(form.getTopBox().getGroupBoxes().isEmpty());
    assertTrue(form.getTopBox().getCustomProcessButtons().isEmpty());
    assertTrue(form.getTopBox().getSystemProcessButtons().isEmpty());

    assertEquals(form.getBottomBox().getFields(), form.getBottomBox().getControlFields());
    assertTrue(form.getBottomBox().getGroupBoxes().isEmpty());
    assertTrue(form.getBottomBox().getCustomProcessButtons().isEmpty());
    assertTrue(form.getBottomBox().getSystemProcessButtons().isEmpty());
  }

  @Test
  public void testSingleTemplateUsageFormSetup() throws Exception {
    SingleTemplateUsageForm form = new SingleTemplateUsageForm();

    assertEquals(1, form.getMainBox().getFieldCount());
    assertSame(form.getTemplateUsageBox(), form.getMainBox().getFields().get(0));
    assertAbstractTemplateFieldsBox(form.getTemplateUsageBox(), true);
  }

  @Test
  public void testSingleTemplateUsageFormMoveFieldInTemplate() throws Exception {
    BEANS.get(IExtensionRegistry.class).registerMove(AbstractTemplateFieldsBox.TopStringField.class, 30d, null);
    SingleTemplateUsageForm form = new SingleTemplateUsageForm();

    assertEquals(1, form.getMainBox().getFieldCount());
    assertSame(form.getTemplateUsageBox(), form.getMainBox().getFields().get(0));
    assertAbstractTemplateFieldsBox(form.getTemplateUsageBox(), false);
  }

  @Test
  public void testSingleTemplateUsageFormMoveFieldInTemplateExplicitCotnainer() throws Exception {
    BEANS.get(IExtensionRegistry.class).registerMove(AbstractTemplateFieldsBox.TopStringField.class, 30d, SingleTemplateUsageForm.MainBox.TemplateUsageBox.class);
    SingleTemplateUsageForm form = new SingleTemplateUsageForm();

    assertEquals(1, form.getMainBox().getFieldCount());
    assertSame(form.getTemplateUsageBox(), form.getMainBox().getFields().get(0));
    assertAbstractTemplateFieldsBox(form.getTemplateUsageBox(), false);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testSingleTemplateUsageFormMoveTemplateFieldOutOfTemplate() throws Exception {
    BEANS.get(IExtensionRegistry.class).registerMove(AbstractTemplateFieldsBox.TopStringField.class, 30d, SingleTemplateUsageForm.MainBox.class);
    new SingleTemplateUsageForm();
  }

  @Test
  public void testMultiTemplateUsageFormSetup() throws Exception {
    MultiTemplateUsageForm form = new MultiTemplateUsageForm();

    assertEquals(3, form.getMainBox().getFieldCount());
    assertSame(form.getFirstTemplateBox(), form.getMainBox().getFields().get(0));
    assertSame(form.getSecondTemplateBox(), form.getMainBox().getFields().get(1));
    assertSame(form.getMainBoxStringField(), form.getMainBox().getFields().get(2));

    // first template box
    FirstTemplateBox firstTemplateBox = form.getFirstTemplateBox();
    assertEquals(3, firstTemplateBox.getFieldCount());
    assertSame(firstTemplateBox.getTopFieldsBox(), firstTemplateBox.getFields().get(0));
    assertSame(form.getMiddleStringField(), firstTemplateBox.getFields().get(1));
    assertSame(firstTemplateBox.getBottomFieldsBox(), firstTemplateBox.getFields().get(2));

    // top fields box in first template box
    assertAbstractTemplateFieldsBox(firstTemplateBox.getTopFieldsBox(), true);
    assertAbstractTemplateFieldsBox(firstTemplateBox.getBottomFieldsBox(), true);

    // second template box
    SecondTemplateBox secondTemplateBox = form.getSecondTemplateBox();
    assertEquals(2, secondTemplateBox.getFieldCount());
    assertSame(secondTemplateBox.getTopFieldsBox(), secondTemplateBox.getFields().get(0));
    assertSame(secondTemplateBox.getBottomFieldsBox(), secondTemplateBox.getFields().get(1));

    // top fields box in first template box
    assertAbstractTemplateFieldsBox(secondTemplateBox.getTopFieldsBox(), true);
    assertAbstractTemplateFieldsBox(secondTemplateBox.getBottomFieldsBox(), true);
  }

  @Test
  public void testMultiTemplateUsageFormMoveTopStringField() throws Exception {
    BEANS.get(IExtensionRegistry.class).registerMove(AbstractTemplateFieldsBox.TopStringField.class, 30d, null);
    MultiTemplateUsageForm form = new MultiTemplateUsageForm();

    assertEquals(3, form.getMainBox().getFieldCount());
    assertSame(form.getFirstTemplateBox(), form.getMainBox().getFields().get(0));
    assertSame(form.getSecondTemplateBox(), form.getMainBox().getFields().get(1));
    assertSame(form.getMainBoxStringField(), form.getMainBox().getFields().get(2));

    // first template box
    FirstTemplateBox firstTemplateBox = form.getFirstTemplateBox();
    assertEquals(3, firstTemplateBox.getFieldCount());
    assertSame(firstTemplateBox.getTopFieldsBox(), firstTemplateBox.getFields().get(0));
    assertSame(form.getMiddleStringField(), firstTemplateBox.getFields().get(1));
    assertSame(firstTemplateBox.getBottomFieldsBox(), firstTemplateBox.getFields().get(2));

    // top fields box in first template box
    assertAbstractTemplateFieldsBox(firstTemplateBox.getTopFieldsBox(), false);
    assertAbstractTemplateFieldsBox(firstTemplateBox.getBottomFieldsBox(), false);

    // second template box
    SecondTemplateBox secondTemplateBox = form.getSecondTemplateBox();
    assertEquals(2, secondTemplateBox.getFieldCount());
    assertSame(secondTemplateBox.getTopFieldsBox(), secondTemplateBox.getFields().get(0));
    assertSame(secondTemplateBox.getBottomFieldsBox(), secondTemplateBox.getFields().get(1));

    // top fields box in first template box
    assertAbstractTemplateFieldsBox(secondTemplateBox.getTopFieldsBox(), false);
    assertAbstractTemplateFieldsBox(secondTemplateBox.getBottomFieldsBox(), false);
  }

  @Test
  public void testMultiTemplateUsageFormMoveFirstBoxAndTopStringField() throws Exception {
    BEANS.get(IExtensionRegistry.class).registerMove(AbstractTemplateFieldsBox.TopStringField.class, 30d, null);
    BEANS.get(IExtensionRegistry.class).registerMove(AbstractTemplateGroupsBox.TopFieldsBox.class, 30d, null);
    MultiTemplateUsageForm form = new MultiTemplateUsageForm();

    assertEquals(3, form.getMainBox().getFieldCount());
    assertSame(form.getFirstTemplateBox(), form.getMainBox().getFields().get(0));
    assertSame(form.getSecondTemplateBox(), form.getMainBox().getFields().get(1));
    assertSame(form.getMainBoxStringField(), form.getMainBox().getFields().get(2));

    // first template box
    FirstTemplateBox firstTemplateBox = form.getFirstTemplateBox();
    assertEquals(3, firstTemplateBox.getFieldCount());
    assertSame(form.getMiddleStringField(), firstTemplateBox.getFields().get(0));
    assertSame(firstTemplateBox.getBottomFieldsBox(), firstTemplateBox.getFields().get(1));
    assertSame(firstTemplateBox.getTopFieldsBox(), firstTemplateBox.getFields().get(2));

    // top fields box in first template box
    assertAbstractTemplateFieldsBox(firstTemplateBox.getTopFieldsBox(), false);
    assertAbstractTemplateFieldsBox(firstTemplateBox.getBottomFieldsBox(), false);

    // second template box
    SecondTemplateBox secondTemplateBox = form.getSecondTemplateBox();
    assertEquals(2, secondTemplateBox.getFieldCount());
    assertSame(secondTemplateBox.getBottomFieldsBox(), secondTemplateBox.getFields().get(0));
    assertSame(secondTemplateBox.getTopFieldsBox(), secondTemplateBox.getFields().get(1));

    // top fields box in first template box
    assertAbstractTemplateFieldsBox(secondTemplateBox.getTopFieldsBox(), false);
    assertAbstractTemplateFieldsBox(secondTemplateBox.getBottomFieldsBox(), false);
  }

  @Test
  public void testMultiTemplateUsageFormMoveTopStringFieldInFirstTemplateBoxUsingClassIdentifier() throws Exception {
    BEANS.get(IExtensionRegistry.class).registerMove(new ClassIdentifier(FirstTemplateBox.class, TopStringField.class), 30d);
    MultiTemplateUsageForm form = new MultiTemplateUsageForm();

    assertEquals(3, form.getMainBox().getFieldCount());
    assertSame(form.getFirstTemplateBox(), form.getMainBox().getFields().get(0));
    assertSame(form.getSecondTemplateBox(), form.getMainBox().getFields().get(1));
    assertSame(form.getMainBoxStringField(), form.getMainBox().getFields().get(2));

    // first template box
    FirstTemplateBox firstTemplateBox = form.getFirstTemplateBox();
    assertEquals(3, firstTemplateBox.getFieldCount());
    assertSame(firstTemplateBox.getTopFieldsBox(), firstTemplateBox.getFields().get(0));
    assertSame(form.getMiddleStringField(), firstTemplateBox.getFields().get(1));
    assertSame(firstTemplateBox.getBottomFieldsBox(), firstTemplateBox.getFields().get(2));

    // top fields box in first template box
    assertAbstractTemplateFieldsBox(firstTemplateBox.getTopFieldsBox(), false);
    assertAbstractTemplateFieldsBox(firstTemplateBox.getBottomFieldsBox(), false);

    // second template box
    SecondTemplateBox secondTemplateBox = form.getSecondTemplateBox();
    assertEquals(2, secondTemplateBox.getFieldCount());
    assertSame(secondTemplateBox.getTopFieldsBox(), secondTemplateBox.getFields().get(0));
    assertSame(secondTemplateBox.getBottomFieldsBox(), secondTemplateBox.getFields().get(1));

    // top fields box in first template box
    assertAbstractTemplateFieldsBox(secondTemplateBox.getTopFieldsBox(), true);
    assertAbstractTemplateFieldsBox(secondTemplateBox.getBottomFieldsBox(), true);
  }

  @Test
  public void testMultiTemplateUsageFormMoveTopStringFieldInFirstTemplateBoxUsingOverspecifiedClassIdentifier() throws Exception {
    BEANS.get(IExtensionRegistry.class).registerMove(new ClassIdentifier(MultiTemplateUsageForm.class, MultiTemplateUsageForm.MainBox.class, FirstTemplateBox.class, TopStringField.class), 30d);
    MultiTemplateUsageForm form = new MultiTemplateUsageForm();

    assertEquals(3, form.getMainBox().getFieldCount());
    assertSame(form.getFirstTemplateBox(), form.getMainBox().getFields().get(0));
    assertSame(form.getSecondTemplateBox(), form.getMainBox().getFields().get(1));
    assertSame(form.getMainBoxStringField(), form.getMainBox().getFields().get(2));

    // first template box
    FirstTemplateBox firstTemplateBox = form.getFirstTemplateBox();
    assertEquals(3, firstTemplateBox.getFieldCount());
    assertSame(firstTemplateBox.getTopFieldsBox(), firstTemplateBox.getFields().get(0));
    assertSame(form.getMiddleStringField(), firstTemplateBox.getFields().get(1));
    assertSame(firstTemplateBox.getBottomFieldsBox(), firstTemplateBox.getFields().get(2));

    // top fields box in first template box
    assertAbstractTemplateFieldsBox(firstTemplateBox.getTopFieldsBox(), false);
    assertAbstractTemplateFieldsBox(firstTemplateBox.getBottomFieldsBox(), false);

    // second template box
    SecondTemplateBox secondTemplateBox = form.getSecondTemplateBox();
    assertEquals(2, secondTemplateBox.getFieldCount());
    assertSame(secondTemplateBox.getTopFieldsBox(), secondTemplateBox.getFields().get(0));
    assertSame(secondTemplateBox.getBottomFieldsBox(), secondTemplateBox.getFields().get(1));

    // top fields box in first template box
    assertAbstractTemplateFieldsBox(secondTemplateBox.getTopFieldsBox(), true);
    assertAbstractTemplateFieldsBox(secondTemplateBox.getBottomFieldsBox(), true);
  }

  @Test
  public void testMultiTemplateUsageFormMoveWithDeepLinkedTarget() throws Exception {
    BEANS.get(IExtensionRegistry.class).registerMove(new ClassIdentifier(MainBoxStringField.class), 15d, new ClassIdentifier(SecondTemplateBox.class, TopFieldsBox.class));
    MultiTemplateUsageForm form = new MultiTemplateUsageForm();

    assertClasses(form.getMainBox().getFields(), FirstTemplateBox.class, SecondTemplateBox.class);

    // first template box
    FirstTemplateBox firstTemplateBox = form.getFirstTemplateBox();
    assertClasses(firstTemplateBox.getFields(), TopFieldsBox.class, MiddleStringField.class, BottomFieldsBox.class);
    assertClasses(firstTemplateBox.getTopFieldsBox().getFields(), TopStringField.class, BottomStringField.class);
    assertClasses(firstTemplateBox.getBottomFieldsBox().getFields(), TopStringField.class, BottomStringField.class);

    // second template box
    SecondTemplateBox secondTemplateBox = form.getSecondTemplateBox();
    assertClasses(secondTemplateBox.getFields(), TopFieldsBox.class, BottomFieldsBox.class);
    assertClasses(secondTemplateBox.getTopFieldsBox().getFields(), TopStringField.class, MainBoxStringField.class, BottomStringField.class);
    assertClasses(secondTemplateBox.getBottomFieldsBox().getFields(), TopStringField.class, BottomStringField.class);
  }

  @Test
  public void testMultiTemplateUsageFormMoveWithDeepLinkedTargetOverspecified() throws Exception {
    BEANS.get(IExtensionRegistry.class).registerMove(new ClassIdentifier(MainBoxStringField.class), 15d, new ClassIdentifier(MultiTemplateUsageForm.class, MultiTemplateUsageForm.MainBox.class, SecondTemplateBox.class, TopFieldsBox.class));
    MultiTemplateUsageForm form = new MultiTemplateUsageForm();

    assertClasses(form.getMainBox().getFields(), FirstTemplateBox.class, SecondTemplateBox.class);

    // first template box
    FirstTemplateBox firstTemplateBox = form.getFirstTemplateBox();
    assertClasses(firstTemplateBox.getFields(), TopFieldsBox.class, MiddleStringField.class, BottomFieldsBox.class);
    assertClasses(firstTemplateBox.getTopFieldsBox().getFields(), TopStringField.class, BottomStringField.class);
    assertClasses(firstTemplateBox.getBottomFieldsBox().getFields(), TopStringField.class, BottomStringField.class);

    // second template box
    SecondTemplateBox secondTemplateBox = form.getSecondTemplateBox();
    assertClasses(secondTemplateBox.getFields(), TopFieldsBox.class, BottomFieldsBox.class);
    assertClasses(secondTemplateBox.getTopFieldsBox().getFields(), TopStringField.class, MainBoxStringField.class, BottomStringField.class);
    assertClasses(secondTemplateBox.getBottomFieldsBox().getFields(), TopStringField.class, BottomStringField.class);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testMultiTemplateUsageFormMoveTopStringFieldOutOfContainer() throws Exception {
    BEANS.get(IExtensionRegistry.class).registerMove(AbstractTemplateFieldsBox.TopStringField.class, 30d, MultiTemplateUsageForm.MainBox.class);
    new MultiTemplateUsageForm();
  }

  @Test
  public void testMoveFormFieldStackOverflow() throws Exception {
    BEANS.get(IExtensionRegistry.class).registerMove(MoveFormFieldStackOverflowForm.MainBox.TopBox.NameField.class, 30d, MoveFormFieldStackOverflowForm.MainBox.BottomBox.class);
    new MoveFormFieldStackOverflowForm();
  }

  private void assertAbstractTemplateFieldsBox(AbstractTemplateFieldsBox templateFieldsBox, boolean expectedOrderTopFieldBottomField) {
    assertEquals(2, templateFieldsBox.getFieldCount());
    assertSame(templateFieldsBox.getTopStringField(), templateFieldsBox.getFields().get(expectedOrderTopFieldBottomField ? 0 : 1));
    assertSame(templateFieldsBox.getBottomStringField(), templateFieldsBox.getFields().get(expectedOrderTopFieldBottomField ? 1 : 0));

    assertEquals(templateFieldsBox.getFields(), templateFieldsBox.getControlFields());
    assertTrue(templateFieldsBox.getGroupBoxes().isEmpty());
    assertTrue(templateFieldsBox.getCustomProcessButtons().isEmpty());
    assertTrue(templateFieldsBox.getSystemProcessButtons().isEmpty());
  }

  @Test
  public void testMoveFormFieldGetByClassOnAnyLevel() throws Exception {
    MoveFieldsTestForm form = setupTestMoveFormFieldGetByOnAnyLevel();
    StringField stringField = form.getStringField();
    assertSame(stringField, form.getMainBox().getFieldByClass(StringField.class));
    assertSame(stringField, form.getTopBox().getFieldByClass(StringField.class));
    assertSame(stringField, form.getSubBox().getFieldByClass(StringField.class));
    assertSame(stringField, form.getBottomBox().getFieldByClass(StringField.class));
  }

  @Test
  public void testMoveFormFieldGetByIdStringOnAnyLevel() throws Exception {
    MoveFieldsTestForm form = setupTestMoveFormFieldGetByOnAnyLevel();
    StringField stringField = form.getStringField();
    assertSame(stringField, form.getMainBox().getFieldById(stringField.getFieldId()));
    assertSame(stringField, form.getTopBox().getFieldById(stringField.getFieldId()));
    assertSame(stringField, form.getSubBox().getFieldById(stringField.getFieldId()));
    assertSame(stringField, form.getBottomBox().getFieldById(stringField.getFieldId()));
  }

  @Test
  public void testMoveFormFieldGetByIdStringClassOnAnyLevel() throws Exception {
    MoveFieldsTestForm form = setupTestMoveFormFieldGetByOnAnyLevel();
    StringField stringField = form.getStringField();
    assertSame(stringField, form.getMainBox().getFieldById(stringField.getFieldId(), StringField.class));
    assertSame(stringField, form.getTopBox().getFieldById(stringField.getFieldId(), StringField.class));
    assertSame(stringField, form.getSubBox().getFieldById(stringField.getFieldId(), StringField.class));
    assertSame(stringField, form.getBottomBox().getFieldById(stringField.getFieldId(), StringField.class));
  }

  private MoveFieldsTestForm setupTestMoveFormFieldGetByOnAnyLevel() {
    BEANS.get(IExtensionRegistry.class).registerMove(MoveFieldsTestForm.MainBox.TopBox.SubBox.StringField.class, null, MoveFieldsTestForm.MainBox.BottomBox.class);

    MoveFieldsTestForm form = new MoveFieldsTestForm();
    // check setup
    assertEquals(0, form.getSubBox().getFieldCount());
    assertSame(form.getBottomBox(), form.getStringField().getParentField());
    return form;
  }

  protected static void assertClasses(List<?> objects, Class<?>... expectedClasses) {
    assertEquals(expectedClasses.length, CollectionUtility.size(objects));

    for (int i = 0; i < expectedClasses.length; i++) {
      assertSame(expectedClasses[i], objects.get(i).getClass());
    }
  }
}
