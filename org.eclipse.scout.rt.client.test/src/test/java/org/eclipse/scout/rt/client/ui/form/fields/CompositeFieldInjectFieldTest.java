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

import org.eclipse.scout.extension.AbstractLocalExtensionTestCase;
import org.eclipse.scout.rt.client.testenvironment.TestEnvironmentClientSession;
import org.eclipse.scout.rt.client.ui.form.AbstractForm;
import org.eclipse.scout.rt.client.ui.form.fields.CompositeFieldInjectFieldTest.TestForm.MainBox.TabBox;
import org.eclipse.scout.rt.client.ui.form.fields.CompositeFieldInjectFieldTest.TestForm.PresentationBox;
import org.eclipse.scout.rt.client.ui.form.fields.CompositeFieldInjectFieldTest.TestForm.PresentationBox2;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.AbstractGroupBox;
import org.eclipse.scout.rt.client.ui.form.fields.stringfield.AbstractStringField;
import org.eclipse.scout.rt.client.ui.form.fields.stringfield.IStringField;
import org.eclipse.scout.rt.client.ui.form.fields.tabbox.AbstractTabBox;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.Replace;
import org.eclipse.scout.rt.platform.extension.InjectFieldTo;
import org.eclipse.scout.rt.platform.util.collection.OrderedCollection;
import org.eclipse.scout.rt.testing.client.runner.ClientTestRunner;
import org.eclipse.scout.rt.testing.client.runner.RunWithClientSession;
import org.eclipse.scout.rt.testing.platform.runner.RunWithSubject;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(ClientTestRunner.class)
@RunWithSubject("default")
@RunWithClientSession(TestEnvironmentClientSession.class)
public class CompositeFieldInjectFieldTest extends AbstractLocalExtensionTestCase {

  public static class TestForm extends AbstractForm {

    private final IFormField m_injected;

    public TestForm() {
      this(null);
    }

    public TestForm(IFormField injected) {
      super(false);
      m_injected = injected;
      callInitializer();
    }

    @Order(10)
    public class MainBox extends AbstractGroupBox {

      @Override
      protected void injectFieldsInternal(OrderedCollection<IFormField> fields) {
        if (m_injected != null) {
          fields.addLast(m_injected);
        }
        super.injectFieldsInternal(fields);
      }

      @Order(10)
      public class TabBox extends AbstractTabBox {
      }
    }

    @Replace
    public class TabBoxEx extends MainBox.TabBox {
      public TabBoxEx(MainBox container) {
        container.super();
      }
    }

    @Order(30)
    @InjectFieldTo(MainBox.TabBox.class)
    public class PresentationBox extends AbstractGroupBox {
      // box injected to replaced TabBox
    }

    @Order(30)
    @InjectFieldTo(TabBoxEx.class)
    public class PresentationBox2 extends AbstractGroupBox {
      // box injected to TabBoxEx replacing TabBox
    }
  }

  public static class StringFieldToInject extends AbstractStringField {
  }

  @Test
  public void test() {
    TestForm form = new TestForm();
    PresentationBox box1 = form.getFieldByClass(PresentationBox.class);
    PresentationBox2 box2 = form.getFieldByClass(PresentationBox2.class);

    assertNotNull(box1);
    assertNotNull(box2);
    assertEquals(2, form.getFieldByClass(TabBox.class).getFieldCount());
    assertSame(form, box1.getForm());
    assertSame(form, box2.getForm());
  }

  @Test
  public void testInjectWithExternalField() {
    IStringField toInject = new StringFieldToInject();
    TestForm form = new TestForm(toInject);

    IStringField field = form.getFieldByClass(StringFieldToInject.class);
    assertNotNull(field);
    assertSame(form, field.getForm());
  }
}
