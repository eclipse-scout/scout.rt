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
package org.eclipse.scout.rt.client.ui.form.fields.groupbox;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.client.ui.form.fields.button.AbstractButton;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.P_TestGroupBox.InnerGroupBox.InnerInnerGroupBox;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.P_TestGroupBox.InnerGroupBox.InnerInnerGroupBox.TestIntegerField;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.P_TestGroupBox.InnerGroupBox.TestStringField;
import org.eclipse.scout.rt.client.ui.form.fields.integerfield.AbstractIntegerField;
import org.eclipse.scout.rt.client.ui.form.fields.stringfield.AbstractStringField;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Tests for {@link AbstractGroupBox}
 *
 * @since 4.1.0
 */
@RunWith(PlatformTestRunner.class)
public class AbstractGroupBoxTest {

  @Test
  public void testGetForm() {
    IForm formMock = mock(IForm.class);
    P_TestGroupBox groupBox = new P_TestGroupBox();
    groupBox.setFormInternal(formMock);
    assertEquals(formMock, groupBox.getForm());
    assertEquals(formMock, groupBox.getTestButton().getForm());
    assertEquals(formMock, groupBox.getInnerGroupBox().getForm());
    assertEquals(formMock, groupBox.getTestStringField().getForm());
    assertEquals(formMock, groupBox.getInnerInnerGroupBox().getForm());
    assertEquals(formMock, groupBox.getTestIntegerField().getForm());
  }

}

class P_TestGroupBox extends AbstractGroupBox {

  public InnerGroupBox getInnerGroupBox() {
    return getFieldByClass(InnerGroupBox.class);
  }

  public InnerInnerGroupBox getInnerInnerGroupBox() {
    return getFieldByClass(InnerInnerGroupBox.class);
  }

  public TestButton getTestButton() {
    return getFieldByClass(TestButton.class);
  }

  public TestIntegerField getTestIntegerField() {
    return getFieldByClass(TestIntegerField.class);
  }

  public TestStringField getTestStringField() {
    return getFieldByClass(TestStringField.class);
  }

  @Order(10)
  public class TestButton extends AbstractButton {
  }

  @Order(20)
  public class InnerGroupBox extends AbstractGroupBox {

    @Order(10)
    public class TestStringField extends AbstractStringField {
    }

    @Order(20)
    public class InnerInnerGroupBox extends AbstractGroupBox {

      @Order(10)
      public class TestIntegerField extends AbstractIntegerField {
      }
    }
  }
}
