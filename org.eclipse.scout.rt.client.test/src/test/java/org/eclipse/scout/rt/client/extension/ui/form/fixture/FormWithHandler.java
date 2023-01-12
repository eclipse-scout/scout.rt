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

import org.eclipse.scout.rt.client.extension.ui.form.fixture.FormWithHandler.MainBox.CloseButton;
import org.eclipse.scout.rt.client.extension.ui.form.fixture.FormWithHandler.MainBox.TextField;
import org.eclipse.scout.rt.client.ui.form.AbstractForm;
import org.eclipse.scout.rt.client.ui.form.AbstractFormHandler;
import org.eclipse.scout.rt.client.ui.form.fields.button.AbstractCloseButton;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.AbstractGroupBox;
import org.eclipse.scout.rt.client.ui.form.fields.stringfield.AbstractStringField;
import org.eclipse.scout.rt.platform.Order;

/**
 * @since 6.0
 */
public class FormWithHandler extends AbstractForm {

  public MainBox getMainBox() {
    return (MainBox) getRootGroupBox();
  }

  public CloseButton getCloseButton() {
    return getFieldByClass(CloseButton.class);
  }

  public TextField getTextField() {
    return getFieldByClass(TextField.class);
  }

  public class MainBox extends AbstractGroupBox {

    @Order(1000)
    public class TextField extends AbstractStringField {
    }

    @Order(2000)
    public class CloseButton extends AbstractCloseButton {
    }
  }

  public class ModifyHandler extends AbstractFormHandler {
  }
}
