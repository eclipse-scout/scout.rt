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
package org.eclipse.scout.rt.shared.extension.fixture;

import org.eclipse.scout.rt.client.ui.form.AbstractForm;
import org.eclipse.scout.rt.client.ui.form.fields.button.AbstractCloseButton;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.AbstractGroupBox;
import org.eclipse.scout.rt.client.ui.form.fields.stringfield.AbstractStringField;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.shared.extension.fixture.BasicForm.MainBox.BottomBox;
import org.eclipse.scout.rt.shared.extension.fixture.BasicForm.MainBox.BottomBox.FirstNameField;
import org.eclipse.scout.rt.shared.extension.fixture.BasicForm.MainBox.CloseButton;
import org.eclipse.scout.rt.shared.extension.fixture.BasicForm.MainBox.TopBox;
import org.eclipse.scout.rt.shared.extension.fixture.BasicForm.MainBox.TopBox.NameField;

public class BasicForm extends AbstractForm {

  public BasicForm() {
    this(true);
  }

  protected BasicForm(boolean callInitializer) {
    super(callInitializer);
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

  public BottomBox getBottomBox() {
    return getFieldByClass(BottomBox.class);
  }

  public FirstNameField getFirstNameField() {
    return getFieldByClass(FirstNameField.class);
  }

  public CloseButton getCloseButton() {
    return getFieldByClass(CloseButton.class);
  }

  public class MainBox extends AbstractGroupBox {

    @Order(10)
    public class TopBox extends AbstractGroupBox {

      @Order(10)
      public class NameField extends AbstractStringField {
      }
    }

    @Order(20)
    public class BottomBox extends AbstractGroupBox {

      @Order(10)
      public class FirstNameField extends AbstractStringField {
      }
    }

    @Order(1000)
    public class CloseButton extends AbstractCloseButton {
    }
  }
}
