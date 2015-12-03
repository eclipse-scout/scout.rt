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
package org.eclipse.scout.rt.shared.extension.dto.fixture;

import org.eclipse.scout.rt.client.dto.FormData;
import org.eclipse.scout.rt.client.dto.FormData.SdkCommand;
import org.eclipse.scout.rt.client.ui.form.AbstractForm;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.AbstractGroupBox;
import org.eclipse.scout.rt.client.ui.form.fields.stringfield.AbstractStringField;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.shared.extension.dto.fixture.OrigForm.MainBox.FirstStringField;
import org.eclipse.scout.rt.shared.extension.dto.fixture.OrigForm.MainBox.FirstUseOfTemplateBox;
import org.eclipse.scout.rt.shared.extension.dto.fixture.OrigForm.MainBox.SecondUseOfTemplateBox;

@FormData(value = OrigFormData.class, sdkCommand = SdkCommand.CREATE)
public class OrigForm extends AbstractForm {

  public static final String STRING_FIELD_ORIG_VALUE = "orig value";
  public static final String STRING_TEMPLATE_1_1 = "1.1";
  public static final String STRING_TEMPLATE_1_2 = "1.2";
  public static final String STRING_TEMPLATE_1_3 = "1.3";
  public static final String STRING_TEMPLATE_2_1 = "2.1";
  public static final String STRING_TEMPLATE_2_2 = "2.2";
  public static final String STRING_TEMPLATE_2_3 = "2.3";

  public OrigForm() {
    super();
  }

  public MainBox getMainBox() {
    return getFieldByClass(MainBox.class);
  }

  public FirstStringField getFirstStringField() {
    return getFieldByClass(FirstStringField.class);
  }

  public FirstUseOfTemplateBox getFirstUseOfTemplateBox() {
    return getFieldByClass(FirstUseOfTemplateBox.class);
  }

  public SecondUseOfTemplateBox getSecondUseOfTemplateBox() {
    return getFieldByClass(SecondUseOfTemplateBox.class);
  }

  @Order(1000)
  public class MainBox extends AbstractGroupBox {
    @Order(1000)
    public class FirstStringField extends AbstractStringField {
      @Override
      protected void execInitField() {
        super.execInitField();
        setValue(STRING_FIELD_ORIG_VALUE);
      }
    }

    @Order(2000)
    public class FirstUseOfTemplateBox extends AbstractTemplateBox {
      @Override
      protected void execInitField() {
        super.execInitField();
        getFirstStringInTemplateField().setValue(STRING_TEMPLATE_1_1);
        getSecondStringInTemplateField().setValue(STRING_TEMPLATE_1_2);
        getThirdStringInTemplateField().setValue(STRING_TEMPLATE_1_3);
      }
    }

    @Order(3000)
    public class SecondUseOfTemplateBox extends AbstractTemplateBox {
      @Override
      protected void execInitField() {
        super.execInitField();
        getFirstStringInTemplateField().setValue(STRING_TEMPLATE_2_1);
        getSecondStringInTemplateField().setValue(STRING_TEMPLATE_2_2);
        getThirdStringInTemplateField().setValue(STRING_TEMPLATE_2_3);
      }
    }
  }
}
