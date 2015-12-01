/*******************************************************************************
 * Copyright (c) 2013 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client.mobile.ui.form.fields.tabbox.fixture;

import org.eclipse.scout.rt.client.dto.FormData;
import org.eclipse.scout.rt.client.dto.FormData.SdkCommand;
import org.eclipse.scout.rt.client.mobile.ui.form.fields.tabbox.fixture.TestForm.MainBox.CancelButton;
import org.eclipse.scout.rt.client.mobile.ui.form.fields.tabbox.fixture.TestForm.MainBox.OkButton;
import org.eclipse.scout.rt.client.mobile.ui.form.fields.tabbox.fixture.TestForm.MainBox.TabBox;
import org.eclipse.scout.rt.client.mobile.ui.form.fields.tabbox.fixture.TestForm.MainBox.TabBox.SimpleGroupBox;
import org.eclipse.scout.rt.client.mobile.ui.form.fields.tabbox.fixture.TestForm.MainBox.TabBox.SimpleGroupBox.TextSimpleField;
import org.eclipse.scout.rt.client.mobile.ui.form.fields.tabbox.fixture.TestForm.MainBox.TabBox.TemplateExGroupBox;
import org.eclipse.scout.rt.client.mobile.ui.form.fields.tabbox.fixture.TestForm.MainBox.TabBox.TemplateExGroupBox.Text3Field;
import org.eclipse.scout.rt.client.mobile.ui.form.fields.tabbox.fixture.TestForm.MainBox.TabBox.TemplateGroupBox;
import org.eclipse.scout.rt.client.ui.form.AbstractForm;
import org.eclipse.scout.rt.client.ui.form.AbstractFormHandler;
import org.eclipse.scout.rt.client.ui.form.fields.button.AbstractCancelButton;
import org.eclipse.scout.rt.client.ui.form.fields.button.AbstractOkButton;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.AbstractGroupBox;
import org.eclipse.scout.rt.client.ui.form.fields.stringfield.AbstractStringField;
import org.eclipse.scout.rt.client.ui.form.fields.tabbox.AbstractTabBox;
import org.eclipse.scout.rt.platform.Order;

@FormData(value = TestFormData.class, sdkCommand = SdkCommand.CREATE)
public class TestForm extends AbstractForm {

  public TestForm() {
    super();
  }

  public CancelButton getCancelButton() {
    return getFieldByClass(CancelButton.class);
  }

  public void startNew() {
    startInternal(new NewHandler());
  }

  public SimpleGroupBox getSimpleGroupBox() {
    return getFieldByClass(SimpleGroupBox.class);
  }

  public TemplateGroupBox getTemplateBox() {
    return getFieldByClass(TemplateGroupBox.class);
  }

  public TabBox getTabBox() {
    return getFieldByClass(TabBox.class);
  }

  public TemplateExGroupBox getTemplateExGroupBox() {
    return getFieldByClass(TemplateExGroupBox.class);
  }

  public MainBox getMainBox() {
    return getFieldByClass(MainBox.class);
  }

  public OkButton getOkButton() {
    return getFieldByClass(OkButton.class);
  }

  public TextSimpleField getTextSimpleField() {
    return getFieldByClass(TextSimpleField.class);
  }

  public Text3Field getText3Field() {
    return getFieldByClass(Text3Field.class);
  }

  @Order(10)
  public class MainBox extends AbstractGroupBox {

    @Order(10)
    public class TabBox extends AbstractTabBox {

      @Order(10)
      public class SimpleGroupBox extends AbstractGroupBox {

        @Order(10)
        public class TextSimpleField extends AbstractStringField {
        }

      }

      @Order(20)
      public class TemplateGroupBox extends AbstractTemplateGroupBox {
      }

      @Order(30)
      public class TemplateExGroupBox extends AbstractTemplateGroupBox {

        @Order(10)
        public class Text3Field extends AbstractStringField {
        }
      }

    }

    @Order(30)
    public class OkButton extends AbstractOkButton {
    }

    @Order(40)
    public class CancelButton extends AbstractCancelButton {
    }
  }

  public class NewHandler extends AbstractFormHandler {
  }
}
