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
package org.eclipse.scout.rt.client.ui.form.fields;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.beans.PropertyChangeListener;
import java.util.ArrayList;

import org.eclipse.scout.extension.AbstractLocalExtensionTestCase;
import org.eclipse.scout.rt.client.extension.ui.form.fields.fixture.AddRemoveFieldsForm;
import org.eclipse.scout.rt.client.extension.ui.form.fields.fixture.AddRemoveFieldsForm.MainBox.BottomBox.BottomBoxField;
import org.eclipse.scout.rt.client.extension.ui.form.fields.fixture.AddRemoveFieldsForm.MainBox.BottomBox.ChildBox;
import org.eclipse.scout.rt.client.extension.ui.form.fields.fixture.AddRemoveFieldsForm.MainBox.TopBox.TopBoxField;
import org.eclipse.scout.rt.client.extension.ui.form.fields.fixture.AddRemoveStringField;
import org.eclipse.scout.rt.client.testenvironment.TestEnvironmentClientSession;
import org.eclipse.scout.rt.testing.client.runner.ClientTestRunner;
import org.eclipse.scout.rt.testing.client.runner.RunWithClientSession;
import org.eclipse.scout.rt.testing.platform.runner.RunWithSubject;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(ClientTestRunner.class)
@RunWithSubject("default")
@RunWithClientSession(TestEnvironmentClientSession.class)
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
    assertNoFieldPropertyChangeListener(field);
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
    assertNoFieldPropertyChangeListener(field);
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
    Assert.assertEquals(1, form.getBottomBox().getFieldCount());
    assertSame(form, field.getForm());
    assertSame(form.getTopBox(), field.getParentField());
    assertFieldPropertyChangeListener(field);
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
    assertFieldPropertyChangeListener(field);
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
    assertFieldPropertyChangeListener(field);

    field = new AddRemoveStringField();
    form.getTopBox().addField(field);
    assertSame(form.getTopBox(), field.getParentField());
    assertSame(form, field.getForm());
    assertFieldPropertyChangeListener(field);
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
    assertFieldPropertyChangeListener(field);

    form.getTopBox().moveFieldTo(field, form.getBottomBox());
    assertEquals(0, form.getTopBox().getFieldCount());
    assertSame(form.getBottomBox(), field.getParentField());
    assertSame(form, field.getForm());
    assertGetFieldMethodsOnContainer(form.getTopBox(), field, TopBoxField.class);
    assertFieldPropertyChangeListener(field);
  }

  @Test
  public void testMoveFieldToChildComposite() throws Exception {
    AddRemoveFieldsForm form = new AddRemoveFieldsForm();
    BottomBoxField field = form.getBottomBoxField();
    assertFieldPropertyChangeListener(field);
    ChildBox childBox = form.getChildBox();
    assertSame(childBox.getParentField(), field.getParentField());

    form.getBottomBox().moveFieldTo(field, childBox);
    assertEquals(1, form.getBottomBox().getFieldCount());
    assertSame(childBox, field.getParentField());
    assertSame(form, field.getForm());
    assertGetFieldMethodsOnContainer(form.getBottomBox(), field, BottomBoxField.class);
    assertFieldPropertyChangeListener(field);
  }

  @Test
  public void testMoveFieldToSameComposite() throws Exception {
    AddRemoveFieldsForm form = new AddRemoveFieldsForm();
    TopBoxField field = form.getTopBoxField();
    assertFieldPropertyChangeListener(field);

    form.getTopBox().moveFieldTo(field, form.getTopBox());
    assertEquals(1, form.getTopBox().getFieldCount());
    assertSame(form.getTopBox(), field.getParentField());
    assertSame(form, field.getForm());
    assertGetFieldMethodsOnContainer(form.getTopBox(), field, TopBoxField.class);
    assertFieldPropertyChangeListener(field);
  }

  /**
   * Assert that field is in a composite and there is exactly one
   * {@link AbstractCompositeField.P_FieldPropertyChangeListener} registered
   */
  protected void assertFieldPropertyChangeListener(AbstractFormField field) {
    ArrayList<PropertyChangeListener> propertyChangeListeners = field.getPropertyChangeListeners();
    int count = 0;
    for (PropertyChangeListener listener : propertyChangeListeners) {
      if (listener instanceof AbstractCompositeField.P_FieldPropertyChangeListener) {
        ++count;
        assertTrue(((AbstractCompositeField.P_FieldPropertyChangeListener) listener).isDirectChildOfComposite(field));
      }
    }
    assertEquals(1, count);
  }

  /**
   * Assert that there is no {@link AbstractCompositeField.P_FieldPropertyChangeListener} registered
   */
  protected void assertNoFieldPropertyChangeListener(AbstractFormField field) {
    int count = 0;
    for (PropertyChangeListener listener : field.getPropertyChangeListeners()) {
      if (listener instanceof AbstractCompositeField.P_FieldPropertyChangeListener) {
        ++count;
      }
    }
    assertEquals(0, count);
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
    assertSame(form.getMainBox(), field.getParentField());
    assertGetFieldMethodsOnContainer(form.getTopBox(), field, TopBoxField.class);
    assertGetFieldMethodsOnContainer(form.getBottomBox(), field, TopBoxField.class);
    assertFieldPropertyChangeListener(field);
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
    assertGetFieldMethodsOnContainer(form.getTopBox(), field, TopBoxField.class);
  }

  protected static void assertGetFieldMethodsOnContainer(ICompositeField container, IFormField field, Class<? extends IFormField> fieldType) {
    assertSame(field, container.getFieldByClass(fieldType));
    assertSame(field, container.getFieldById(field.getFieldId()));
    assertSame(field, container.getFieldById(field.getFieldId(), fieldType));
  }
}
