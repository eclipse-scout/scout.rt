/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.form.fields.stringfield;

import static org.junit.Assert.assertEquals;

import org.eclipse.scout.rt.client.testenvironment.TestEnvironmentClientSession;
import org.eclipse.scout.rt.client.ui.form.AbstractForm;
import org.eclipse.scout.rt.client.ui.form.fields.button.AbstractCloseButton;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.AbstractGroupBox;
import org.eclipse.scout.rt.client.ui.form.fields.stringfield.StringFieldSetMultilineTextTest.MyForm.MainBox.GroupBox.CloseButton;
import org.eclipse.scout.rt.client.ui.form.fields.stringfield.StringFieldSetMultilineTextTest.MyForm.MainBox.GroupBox.Text1Field;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.testing.client.runner.ClientTestRunner;
import org.eclipse.scout.rt.testing.client.runner.RunWithClientSession;
import org.eclipse.scout.rt.testing.platform.runner.RunWithSubject;
import org.eclipse.scout.testing.client.form.FormHandler;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Writing text with newlines into a single line text field must eliminate the newlines
 */
@RunWith(ClientTestRunner.class)
@RunWithSubject("default")
@RunWithClientSession(TestEnvironmentClientSession.class)
public class StringFieldSetMultilineTextTest {

  @Test
  public void test() {
    MyForm f = new MyForm();
    try {
      f.startForm();
      f.getText1Field().getUIFacade().parseAndSetValueFromUI("ABC\nDEF\nGHI");
      assertEquals(f.getText1Field().getValue(), "ABC DEF GHI");
    }
    finally {
      f.doClose();
    }
  }

  public static final class MyForm extends AbstractForm {

    private MyForm() {
      super();
    }

    @Override
    protected String getConfiguredTitle() {
      return "MyForm";
    }

    @Override
    protected int getConfiguredModalityHint() {
      return MODALITY_HINT_MODELESS;
    }

    public Text1Field getText1Field() {
      return getFieldByClass(Text1Field.class);
    }

    @Order(10)
    public class MainBox extends AbstractGroupBox {
      @Order(10)
      public class GroupBox extends AbstractGroupBox {
        @Order(10)
        public class Text1Field extends AbstractStringField {
          @Override
          protected String getConfiguredLabel() {
            return "Text 1";
          }
        }

        @Order(100)
        public class CloseButton extends AbstractCloseButton {

          @Override
          protected String getConfiguredLabel() {
            return "Close";
          }
        }
      }
    }

    public CloseButton getCloseButton() {
      return getFieldByClass(CloseButton.class);
    }

    public void startForm() {
      startInternal(new FormHandler());
    }
  }
}
