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

import org.eclipse.scout.rt.client.extension.ui.form.fixture.MoveFormFieldStackOverflowForm.MainBox.BottomBox;
import org.eclipse.scout.rt.client.extension.ui.form.fixture.MoveFormFieldStackOverflowForm.MainBox.TopBox;
import org.eclipse.scout.rt.client.extension.ui.form.fixture.MoveFormFieldStackOverflowForm.MainBox.TopBox.ListBox;
import org.eclipse.scout.rt.client.extension.ui.form.fixture.MoveFormFieldStackOverflowForm.MainBox.TopBox.NameField;
import org.eclipse.scout.rt.client.ui.form.AbstractForm;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.AbstractGroupBox;
import org.eclipse.scout.rt.client.ui.form.fields.listbox.AbstractListBox;
import org.eclipse.scout.rt.client.ui.form.fields.stringfield.AbstractStringField;
import org.eclipse.scout.rt.platform.Order;

public class MoveFormFieldStackOverflowForm extends AbstractForm {

  public MoveFormFieldStackOverflowForm() {
  }

  public MainBox getMainBox() {
    return (MainBox) getRootGroupBox();
  }

  public TopBox getTopBox() {
    return getFieldByClass(TopBox.class);
  }

  public NameField getNameField() {
    return getFieldByClass(NameField.class);
  }

  public ListBox getListBox() {
    return getFieldByClass(ListBox.class);
  }

  public BottomBox getBottomBox() {
    return getFieldByClass(BottomBox.class);
  }

  public class MainBox extends AbstractGroupBox {

    @Order(10)
    public class TopBox extends AbstractGroupBox {

      @Order(10)
      public class NameField extends AbstractStringField {
      }

      @Order(20)
      public class ListBox extends AbstractListBox<String> {
      }
    }

    @Order(20)
    public class BottomBox extends AbstractGroupBox {
    }
  }
}
