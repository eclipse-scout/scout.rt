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
package org.eclipse.scout.rt.client.ui.form.fields;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.mock;

import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.client.ui.form.IFormFieldVisitor;
import org.eclipse.scout.rt.client.ui.form.fields.P_BadVisitorCompositeField.FirstGroupBox.FirstField;
import org.eclipse.scout.rt.client.ui.form.fields.P_BadVisitorCompositeField.SecondGroupBox.SecondField;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.AbstractGroupBox;
import org.eclipse.scout.rt.client.ui.form.fields.stringfield.AbstractStringField;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Tests for {@link AbstractCompositeField}
 *
 * @since 4.0.1
 */
@RunWith(PlatformTestRunner.class)
public class CompositeFieldTest {

  @Test
  public void testSetAndGetForm() {
    IForm formMock = mock(IForm.class);
    P_TestCompositeField compositeField = new P_TestCompositeField();
    compositeField.setFormInternal(formMock);
    assertSame(formMock, compositeField.getForm());
    assertSame(formMock, compositeField.getTextField().getForm());
  }

  @Test
  public void testSetAndGetFormNull() {
    P_TestCompositeField compositeField = new P_TestCompositeField();
    compositeField.setFormInternal(null);
    assertNull(compositeField.getForm());
    assertNull(compositeField.getTextField().getForm());
  }

  /**
   * Tests that a badly implemented visitor on an {@link ICompositeField} still finds the right field using
   * {@link ICompositeField#getFieldByClass(Class)}, {@link ICompositeField#getFieldById(String)} or
   * {@link ICompositeField#getFieldById(String, Class)}. An {@link ICompositeField#visitFields(IFormFieldVisitor, int)}
   * is considered bad, if it does not immediately return if {@link IFormFieldVisitor#visitField(IFormField, int, int)}
   * returns <code>false</code>.
   */
  @Test
  public void testBadVisitor() {
    P_BadVisitorCompositeField composite = new P_BadVisitorCompositeField();
    IFormField field = ((ICompositeField) composite.getFields().get(0)).getFields().get(0);
    assertSame(field, composite.getFieldByClass(FirstField.class));
    assertSame(field, composite.getFieldById(field.getFieldId()));
    assertSame(field, composite.getFieldById(field.getFieldId(), FirstField.class));
  }
}

class P_TestCompositeField extends AbstractCompositeField {

  @Override
  public int getGridColumnCount() {
    return 1;
  }

  @Override
  public int getGridRowCount() {
    return 1;
  }

  public TextField getTextField() {
    return getFieldByClass(TextField.class);
  }

  @Order(10)
  public class TextField extends AbstractStringField {
  }
}

class P_BadVisitorCompositeField extends AbstractCompositeField {

  @Override
  public int getGridColumnCount() {
    return 1;
  }

  @Override
  public int getGridRowCount() {
    return 1;
  }

  public FirstField getFirstField() {
    return getFieldByClass(FirstField.class);
  }

  public SecondField getSecondField() {
    return getFieldByClass(SecondField.class);
  }

  @Override
  public boolean visitFields(IFormFieldVisitor visitor, int startLevel) {
    visitor.visitField(this, startLevel, 0);
    ((ICompositeField) getFields().get(0)).visitFields(visitor, startLevel + 1);
    ((ICompositeField) getFields().get(1)).visitFields(visitor, startLevel + 1);
    return true;
  }

  @Order(10)
  public class FirstGroupBox extends AbstractGroupBox {
    @Order(10)
    public class FirstField extends AbstractStringField {
    }
  }

  @Order(20)
  public class SecondGroupBox extends AbstractGroupBox {
    @Order(10)
    public class SecondField extends AbstractStringField {
    }
  }
}
