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
import static org.mockito.Mockito.mock;

import org.eclipse.scout.rt.client.testenvironment.TestEnvironmentClientSession;
import org.eclipse.scout.rt.client.ui.action.menu.AbstractMenu;
import org.eclipse.scout.rt.client.ui.form.AbstractForm;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.AbstractGroupBox;
import org.eclipse.scout.rt.client.ui.form.fields.stringfield.AbstractStringField;
import org.eclipse.scout.rt.client.ui.form.fields.stringfield.IStringField;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.util.collection.OrderedCollection;
import org.eclipse.scout.rt.testing.client.runner.ClientTestRunner;
import org.eclipse.scout.rt.testing.client.runner.RunWithClientSession;
import org.eclipse.scout.rt.testing.platform.runner.RunWithSubject;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Tests for {@link AbstractCompositeField}
 *
 * @since 4.0.1
 */
@RunWith(ClientTestRunner.class)
@RunWithSubject("default")
@RunWithClientSession(TestEnvironmentClientSession.class)
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
  public void testGetFormConsistency() {
    IForm formMock = mock(IForm.class);
    IFormField dynamicField = new AbstractStringField() {
    };
    dynamicField.init();
    // Form may be set during a load handler because the form thread local is read while initConfig runs.
    // It could also be set explicitly as done here
    // In both cases the form needs to be set to the form of the parent field when connected
    dynamicField.setFormInternal(formMock);

    P_TestForm form = new P_TestForm(dynamicField);
    assertSame(form, form.getRootGroupBox().getForm());
    assertSame(form, form.getDynamicField().getForm());
    assertSame(form.getRootGroupBox(), form.getDynamicField().getParentField());
  }

  @Test
  public void testSetAndGetFormNull() {
    P_TestCompositeField compositeField = new P_TestCompositeField();
    compositeField.setFormInternal(null);
    assertNull(compositeField.getForm());
    assertNull(compositeField.getTextField().getForm());
  }

  @Test
  public void testAddField() {
    P_TestCompositeField compositeField = new P_TestCompositeField();
    compositeField.setEnabled(false, false, false);
    IStringField add = new AbstractStringField() {
    };
    AbstractMenu menu = new AbstractMenu() {
    };
    add.getContextMenu().addChildAction(menu);
    compositeField.addField(add);

    Assert.assertFalse(add.isEnabledIncludingParents());
    Assert.assertTrue(add.isEnabled());
    Assert.assertTrue(menu.isEnabled());
    Assert.assertFalse(menu.isEnabledIncludingParents());
  }
}

class P_TestCompositeField extends AbstractCompositeField {

  @Override
  public ICompositeFieldGrid<? extends ICompositeField> getFieldGrid() {
    return null;
  }

  public TextField getTextField() {
    return getFieldByClass(TextField.class);
  }

  @Order(10)
  public class TextField extends AbstractStringField {
  }
}

class P_TestForm extends AbstractForm {
  private IFormField m_dynamicField;

  public P_TestForm() {
  }

  public P_TestForm(IFormField dynamicField) {
    super(false);
    m_dynamicField = dynamicField;
    callInitializer();
  }

  public IFormField getDynamicField() {
    return m_dynamicField;
  }

  @Order(10)
  public class MainBox extends AbstractGroupBox {
    @Override
    protected void injectFieldsInternal(OrderedCollection<IFormField> fields) {
      super.injectFieldsInternal(fields);
      fields.addLast(m_dynamicField);
    }
  }
}
