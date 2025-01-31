/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.form.fields.wrappedform;

import static org.junit.Assert.*;

import org.eclipse.scout.rt.client.testenvironment.TestEnvironmentClientSession;
import org.eclipse.scout.rt.client.ui.form.AbstractForm;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.client.ui.form.fields.bigdecimalfield.AbstractBigDecimalField;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.AbstractGroupBox;
import org.eclipse.scout.rt.client.ui.form.fields.wrappedform.WrappedFormFieldTest.InnerForm.MainBox.Wrapped2FormField;
import org.eclipse.scout.rt.client.ui.form.fields.wrappedform.WrappedFormFieldTest.LastForm.MainBox.MyBigDecimalField;
import org.eclipse.scout.rt.client.ui.form.fields.wrappedform.WrappedFormFieldTest.OuterForm.MainBox.Wrapped1FormField;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.testing.client.runner.ClientTestRunner;
import org.eclipse.scout.rt.testing.client.runner.RunWithClientSession;
import org.eclipse.scout.rt.testing.platform.runner.RunWithSubject;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(ClientTestRunner.class)
@RunWithSubject("anna")
@RunWithClientSession(TestEnvironmentClientSession.class)
public class WrappedFormFieldTest {

  @Test
  public void testGetForm() {
    OuterForm outerForm = new OuterForm();
    outerForm.setShowOnStart(false);
    outerForm.start();

    Wrapped1FormField firstWrapped = outerForm.getFieldByClass(Wrapped1FormField.class);
    Wrapped2FormField secondWrapped = outerForm.getFieldByClass(Wrapped2FormField.class);
    MyBigDecimalField decimal = outerForm.getFieldByClass(MyBigDecimalField.class);
    InnerForm innerForm = firstWrapped.getInnerForm();
    LastForm lastForm = secondWrapped.getInnerForm();

    assertSame(outerForm, firstWrapped.getForm());
    assertSame(innerForm, secondWrapped.getForm());
    assertSame(lastForm, decimal.getForm());

    assertTrue(decimal.isInitConfigDone());
    assertTrue(decimal.isInitDone());
    assertFalse(decimal.isDisposeDone());

    outerForm.doClose();
    assertTrue(decimal.isInitConfigDone());
    assertFalse(decimal.isInitDone());
    assertTrue(decimal.isDisposeDone());
  }

  public static class OuterForm extends AbstractForm {
    public class MainBox extends AbstractGroupBox {
      public class Wrapped1FormField extends AbstractWrappedFormField<InnerForm> {
        @Override
        protected Class<? extends IForm> getConfiguredInnerForm() {
          return InnerForm.class;
        }
      }
    }
  }

  public static class InnerForm extends AbstractForm {
    public class MainBox extends AbstractGroupBox {
      public class Wrapped2FormField extends AbstractWrappedFormField<LastForm> {
        @Override
        protected Class<? extends IForm> getConfiguredInnerForm() {
          return LastForm.class;
        }
      }
    }
  }

  public static class LastForm extends AbstractForm {
    public class MainBox extends AbstractGroupBox {
      @Order(1000)
      public class MyBigDecimalField extends AbstractBigDecimalField {
      }
    }
  }
}
