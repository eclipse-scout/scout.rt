/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.extension.ui.form.fixture;

import org.eclipse.scout.rt.client.extension.ui.form.fixture.ExtendedForm.BottomDetailBox.BottomDetailBoxField;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.AbstractGroupBox;
import org.eclipse.scout.rt.client.ui.form.fields.stringfield.AbstractStringField;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.Replace;
import org.eclipse.scout.rt.platform.extension.InjectFieldTo;

public class ExtendedForm extends OrigForm {

  public ExtendedForm() {
  }

  public SalutationFieldEx getSalutationFieldEx() {
    return getFieldByClass(SalutationFieldEx.class);
  }

  public CountryField getCountryField() {
    return getFieldByClass(CountryField.class);
  }

  public BottomDetailBox getBottomDetailBox() {
    return getFieldByClass(BottomDetailBox.class);
  }

  public BottomDetailBoxField getBottomDetailBoxField() {
    return getFieldByClass(BottomDetailBoxField.class);
  }

  @Replace
  public class SalutationFieldEx extends MainBox.TopBox.SalutationField {

    public SalutationFieldEx(MainBox.TopBox container) {
      container.super();
    }

    @Override
    protected String execValidateValue(String rawValue) {
      String validatedValue = super.execValidateValue(rawValue);
      logOperation(SalutationFieldEx.class, EXEC_VALIDATE_VALUE_OPERATION_NAME);
      return validatedValue;
    }
  }

  @Order(20)
  @InjectFieldTo(MainBox.BottomBox.class)
  public class CountryField extends AbstractStringField {

    @Override
    protected String execValidateValue(String rawValue) {
      String validatedValue = super.execValidateValue(rawValue);
      logOperation(CountryField.class, EXEC_VALIDATE_VALUE_OPERATION_NAME);
      return validatedValue;
    }
  }

  @Order(30)
  @InjectFieldTo(MainBox.BottomBox.class)
  public class BottomDetailBox extends AbstractGroupBox {

    public class BottomDetailBoxField extends AbstractStringField {

      @Override
      protected String execValidateValue(String rawValue) {
        String validatedValue = super.execValidateValue(rawValue);
        logOperation(BottomDetailBoxField.class, EXEC_VALIDATE_VALUE_OPERATION_NAME);
        return validatedValue;
      }
    }
  }
}
