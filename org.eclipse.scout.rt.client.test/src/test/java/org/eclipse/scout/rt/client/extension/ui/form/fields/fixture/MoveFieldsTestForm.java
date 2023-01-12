/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.extension.ui.form.fields.fixture;

import org.eclipse.scout.rt.client.extension.ui.form.fields.fixture.MoveFieldsTestForm.MainBox.BottomBox;
import org.eclipse.scout.rt.client.extension.ui.form.fields.fixture.MoveFieldsTestForm.MainBox.TopBox;
import org.eclipse.scout.rt.client.extension.ui.form.fields.fixture.MoveFieldsTestForm.MainBox.TopBox.SubBox;
import org.eclipse.scout.rt.client.extension.ui.form.fields.fixture.MoveFieldsTestForm.MainBox.TopBox.SubBox.StringField;
import org.eclipse.scout.rt.client.ui.form.AbstractForm;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.AbstractGroupBox;
import org.eclipse.scout.rt.client.ui.form.fields.stringfield.AbstractStringField;
import org.eclipse.scout.rt.platform.Order;

public class MoveFieldsTestForm extends AbstractForm {

  public MoveFieldsTestForm() {
  }

  public MainBox getMainBox() {
    return (MainBox) getRootGroupBox();
  }

  public TopBox getTopBox() {
    return getFieldByClass(TopBox.class);
  }

  public BottomBox getBottomBox() {
    return getFieldByClass(BottomBox.class);
  }

  public SubBox getSubBox() {
    return getFieldByClass(SubBox.class);
  }

  public StringField getStringField() {
    return getFieldByClass(StringField.class);
  }

  public class MainBox extends AbstractGroupBox {

    @Order(10)
    public class TopBox extends AbstractGroupBox {

      @Order(10)
      public class SubBox extends AbstractGroupBox {

        @Order(10)
        public class StringField extends AbstractStringField {
        }
      }
    }

    @Order(20)
    public class BottomBox extends AbstractGroupBox {
    }
  }
}
