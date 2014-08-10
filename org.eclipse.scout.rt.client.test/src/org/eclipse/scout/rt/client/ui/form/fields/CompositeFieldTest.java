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
import org.eclipse.scout.rt.client.ui.form.fields.stringfield.AbstractStringField;
import org.junit.Test;

/**
 * Tests for {@link AbstractCompositeField}
 * 
 * @since 4.0.1
 */
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

  @Order(10.0)
  public class TextField extends AbstractStringField {
  }
}
