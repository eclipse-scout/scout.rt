/*******************************************************************************
 * Copyright (c) 2014 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client.extension.ui.form.fields;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;

import org.eclipse.scout.extension.AbstractLocalExtensionTestCase;
import org.eclipse.scout.rt.client.extension.ui.form.fields.fixture.AddRemoveFieldsForm;
import org.eclipse.scout.rt.client.extension.ui.form.fields.fixture.AddRemoveFieldsForm.MainBox.BottomBox.BottomBoxField;
import org.eclipse.scout.rt.client.extension.ui.form.fields.fixture.AddRemoveFieldsForm.MainBox.TopBox.TopBoxField;
import org.eclipse.scout.rt.client.extension.ui.form.fields.fixture.AddRemoveStringField;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.client.ui.form.fields.ICompositeField;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.testing.client.runner.ScoutClientTestRunner;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(ScoutClientTestRunner.class)
public class CompositeFieldAddRemoveMoveFieldTest extends AbstractLocalExtensionTestCase {

  @Test(expected = IllegalArgumentException.class)
  public void testRemoveFieldNull() throws Exception {
    AddRemoveFieldsForm form = new AddRemoveFieldsForm();
    form.getTopBox().removeField(null);
  }

  @Test
  public void testRemoveFieldExisting() throws Exception {
    AddRemoveFieldsForm form = new AddRemoveFieldsForm();
    TopBoxField field = form.getTopBoxField();
    form.getTopBox().removeField(field);
    assertEquals(0, form.getTopBox().getFieldCount());
    assertNull(field.getParentField());
    assertSame(form, field.getForm());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testRemoveFieldForeignBox() throws Exception {
    AddRemoveFieldsForm form = new AddRemoveFieldsForm();
    form.getTopBox().removeField(form.getBottomBoxField());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testRemoveFieldForeignForm() throws Exception {
    AddRemoveFieldsForm form = new AddRemoveFieldsForm();
    AddRemoveFieldsForm otherForm = new AddRemoveFieldsForm();
    form.getTopBox().removeField(otherForm.getTopBoxField());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testRemoveFieldTwice() throws Exception {
    AddRemoveFieldsForm form = new AddRemoveFieldsForm();
    TopBoxField field = form.getTopBoxField();
    try {
      form.getTopBox().removeField(field);
    }
    catch (Exception e) {
      fail("exception must not occur here. " + e.getMessage());
    }
    form.getTopBox().removeField(field);
  }

  @Test(expected = IllegalStateException.class)
  public void testRemoveFieldAfterFormStarted() throws Exception {
    AddRemoveFieldsForm form = new AddRemoveFieldsForm();
    form.start();

    form.getTopBox().removeField(form.getTopBoxField());
  }

  @Test
  public void testRemoveFieldAfterFormStartedAndStopped() throws Exception {
    AddRemoveFieldsForm form = new AddRemoveFieldsForm();
    form.start();
    form.doClose();

    TopBoxField field = form.getTopBoxField();
    form.getTopBox().removeField(field);
    assertEquals(0, form.getTopBox().getFieldCount());
    assertNull(field.getParentField());
    assertSame(form, field.getForm());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testAddFieldNull() throws Exception {
    AddRemoveFieldsForm form = new AddRemoveFieldsForm();
    form.getTopBox().addField(null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testAddFieldExistingInBox() throws Exception {
    AddRemoveFieldsForm form = new AddRemoveFieldsForm();
    form.getTopBox().addField(form.getTopBoxField());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testAddFieldExistingInOtherBox() throws Exception {
    AddRemoveFieldsForm form = new AddRemoveFieldsForm();
    form.getTopBox().addField(form.getBottomBoxField());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testAddFieldExistingInOtherForm() throws Exception {
    AddRemoveFieldsForm form = new AddRemoveFieldsForm();
    AddRemoveFieldsForm otherForm = new AddRemoveFieldsForm();
    form.getTopBox().addField(otherForm.getTopBoxField());
  }

  @Test
  public void testAddFieldRemovedFromOtherBox() throws Exception {
    AddRemoveFieldsForm form = new AddRemoveFieldsForm();
    BottomBoxField field = form.getBottomBoxField();
    form.getBottomBox().removeField(field);

    form.getTopBox().addField(field);
    Assert.assertEquals(2, form.getTopBox().getFieldCount());
    Assert.assertEquals(0, form.getBottomBox().getFieldCount());
    assertSame(form, field.getForm());
    assertSame(form.getTopBox(), field.getParentField());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testAddFieldRemovedFromOtherForm() throws Exception {
    AddRemoveFieldsForm otherForm = new AddRemoveFieldsForm();
    TopBoxField field = otherForm.getTopBoxField();
    otherForm.getTopBox().removeField(field);

    AddRemoveFieldsForm form = new AddRemoveFieldsForm();
    form.getTopBox().addField(field);
  }

  @Test
  public void testAddFieldDynamic() throws Exception {
    AddRemoveFieldsForm form = new AddRemoveFieldsForm();
    AddRemoveStringField field = new AddRemoveStringField();
    form.getTopBox().addField(field);
    assertSame(form.getTopBox(), field.getParentField());
    assertSame(form, field.getForm());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testAddFieldDynamicTwice() throws Exception {
    AddRemoveFieldsForm form = new AddRemoveFieldsForm();
    AddRemoveStringField field = new AddRemoveStringField();
    form.getTopBox().addField(field);
    form.getTopBox().addField(field);
  }

  @Test
  public void testAddFieldDynamicTwiceNewInstances() throws Exception {
    AddRemoveFieldsForm form = new AddRemoveFieldsForm();
    AddRemoveStringField field = new AddRemoveStringField();
    form.getTopBox().addField(field);
    assertSame(form.getTopBox(), field.getParentField());
    assertSame(form, field.getForm());

    field = new AddRemoveStringField();
    form.getTopBox().addField(field);
    assertSame(form.getTopBox(), field.getParentField());
    assertSame(form, field.getForm());
  }

  @Test(expected = IllegalStateException.class)
  public void testAddFieldAfterFormStarted() throws Exception {
    AddRemoveFieldsForm form = new AddRemoveFieldsForm();
    form.start();

    AddRemoveStringField field = new AddRemoveStringField();
    form.getTopBox().addField(field);
  }

  @Test
  public void testAddFieldAfterFormStartedAndStopped() throws Exception {
    AddRemoveFieldsForm form = new AddRemoveFieldsForm();
    form.start();
    form.doClose();

    AddRemoveStringField field = new AddRemoveStringField();
    form.getTopBox().addField(field);
    assertEquals(2, form.getTopBox().getFieldCount());
    assertSame(form.getTopBox(), field.getParentField());
    assertSame(form, field.getForm());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testMoveFieldNullField() throws Exception {
    AddRemoveFieldsForm form = new AddRemoveFieldsForm();
    form.getTopBox().moveFieldTo(null, form.getBottomBox());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testMoveFieldNullNewContainer() throws Exception {
    AddRemoveFieldsForm form = new AddRemoveFieldsForm();
    form.getTopBox().moveFieldTo(form.getTopBoxField(), null);
  }

  @Test
  public void testMoveFieldExisting() throws Exception {
    AddRemoveFieldsForm form = new AddRemoveFieldsForm();
    TopBoxField field = form.getTopBoxField();
    form.getTopBox().moveFieldTo(field, form.getBottomBox());
    assertEquals(0, form.getTopBox().getFieldCount());
    assertSame(form.getBottomBox(), field.getParentField());
    assertSame(form, field.getForm());

    assertGetFieldMethodsOnContainer(form, form.getTopBox(), field, TopBoxField.class);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testMoveFieldForeignBox() throws Exception {
    AddRemoveFieldsForm form = new AddRemoveFieldsForm();
    form.getTopBox().moveFieldTo(form.getBottomBoxField(), form.getBottomBox());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testMoveFieldForeignForm() throws Exception {
    AddRemoveFieldsForm form = new AddRemoveFieldsForm();
    AddRemoveFieldsForm otherForm = new AddRemoveFieldsForm();
    form.getTopBox().moveFieldTo(otherForm.getTopBoxField(), form.getBottomBox());
  }

  @Test
  public void testMoveFieldTwice() throws Exception {
    AddRemoveFieldsForm form = new AddRemoveFieldsForm();
    TopBoxField field = form.getTopBoxField();
    form.getTopBox().moveFieldTo(field, form.getBottomBox());
    form.getBottomBox().moveFieldTo(field, form.getMainBox());

    assertSame(field, form.getTopBoxField());
    assertGetFieldMethodsOnContainer(form, form.getTopBox(), field, TopBoxField.class);
    assertGetFieldMethodsOnContainer(form, form.getBottomBox(), field, TopBoxField.class);
  }

  @Test(expected = IllegalStateException.class)
  public void testMoveFieldAfterFormStarted() throws Exception {
    AddRemoveFieldsForm form = new AddRemoveFieldsForm();
    form.start();

    form.getTopBox().moveFieldTo(form.getTopBoxField(), form.getBottomBox());
  }

  @Test
  public void testMoveFieldAfterFormStartedAndStopped() throws Exception {
    AddRemoveFieldsForm form = new AddRemoveFieldsForm();
    form.start();
    form.doClose();

    TopBoxField field = form.getTopBoxField();
    form.getTopBox().moveFieldTo(field, form.getBottomBox());
    assertEquals(0, form.getTopBox().getFieldCount());
    assertSame(form.getBottomBox(), field.getParentField());
    assertSame(form, field.getForm());
    assertGetFieldMethodsOnContainer(form, form.getTopBox(), field, TopBoxField.class);
  }

  protected static void assertGetFieldMethodsOnContainer(IForm form, ICompositeField container, IFormField field, Class<? extends IFormField> fieldType) {
    assertSame(field, container.getFieldByClass(fieldType));
    assertSame(field, container.getFieldById(field.getFieldId()));
    assertSame(field, container.getFieldById(field.getFieldId(), fieldType));
  }
}
