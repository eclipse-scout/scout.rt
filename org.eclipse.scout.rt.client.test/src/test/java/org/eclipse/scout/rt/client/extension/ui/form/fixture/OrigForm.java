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
package org.eclipse.scout.rt.client.extension.ui.form.fixture;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.scout.rt.client.extension.ui.form.fixture.OrigForm.MainBox.BottomBox;
import org.eclipse.scout.rt.client.extension.ui.form.fixture.OrigForm.MainBox.BottomBox.StreetField;
import org.eclipse.scout.rt.client.extension.ui.form.fixture.OrigForm.MainBox.TopBox;
import org.eclipse.scout.rt.client.extension.ui.form.fixture.OrigForm.MainBox.TopBox.NameField;
import org.eclipse.scout.rt.client.extension.ui.form.fixture.OrigForm.MainBox.TopBox.SalutationField;
import org.eclipse.scout.rt.client.ui.form.AbstractForm;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.AbstractGroupBox;
import org.eclipse.scout.rt.client.ui.form.fields.stringfield.AbstractStringField;
import org.eclipse.scout.rt.platform.Order;

public class OrigForm extends AbstractForm {

  public static final String EXEC_VALIDATE_VALUE_OPERATION_NAME = "execValidateValue";
  private final List<String> m_operations;

  public OrigForm() {
    super(false);
    m_operations = new ArrayList<String>();
    callInitializer();
  }

  public void logOperation(Class<?> fieldClass, String operation) {
    m_operations.add(formatOperationLogEntry(fieldClass, operation));
  }

  public static String formatOperationLogEntry(Class<?> fieldClass, String operation) {
    return fieldClass.getSimpleName() + "." + operation;
  }

  public List<String> getOperations() {
    return m_operations;
  }

  public MainBox getMainBox() {
    return (MainBox) getRootGroupBox();
  }

  public TopBox getTopBox() {
    return getFieldByClass(TopBox.class);
  }

  public SalutationField getSalutationField() {
    return getFieldByClass(SalutationField.class);
  }

  public NameField getNameField() {
    return getFieldByClass(NameField.class);
  }

  public BottomBox getBottomBox() {
    return getFieldByClass(BottomBox.class);
  }

  public StreetField getStreetField() {
    return getFieldByClass(StreetField.class);
  }

  public class MainBox extends AbstractGroupBox {

    @Order(10)
    public class TopBox extends AbstractGroupBox {

      @Order(0)
      public class SalutationField extends AbstractStringField {

        @Override
        protected String execValidateValue(String rawValue) {
          logOperation(SalutationField.class, EXEC_VALIDATE_VALUE_OPERATION_NAME);
          return super.execValidateValue(rawValue);
        }
      }

      @Order(5)
      public class NameField extends AbstractStringField {
      }
    }

    @Order(20)
    public class BottomBox extends AbstractGroupBox {

      @Order(10)
      public class StreetField extends AbstractStringField {
      }
    }
  }
}
