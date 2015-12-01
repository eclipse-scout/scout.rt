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
package org.eclipse.scout.rt.client.extension.ui.form.fields.fixture;

import org.eclipse.scout.rt.client.extension.ui.form.fields.fixture.AddRemoveFieldsForm.MainBox.BottomBox;
import org.eclipse.scout.rt.client.extension.ui.form.fields.fixture.AddRemoveFieldsForm.MainBox.BottomBox.BottomBoxField;
import org.eclipse.scout.rt.client.extension.ui.form.fields.fixture.AddRemoveFieldsForm.MainBox.BottomBox.ChildBox;
import org.eclipse.scout.rt.client.extension.ui.form.fields.fixture.AddRemoveFieldsForm.MainBox.BottomBox.ChildBox.ChildBoxField;
import org.eclipse.scout.rt.client.extension.ui.form.fields.fixture.AddRemoveFieldsForm.MainBox.TopBox;
import org.eclipse.scout.rt.client.extension.ui.form.fields.fixture.AddRemoveFieldsForm.MainBox.TopBox.TopBoxField;
import org.eclipse.scout.rt.client.ui.form.AbstractForm;
import org.eclipse.scout.rt.client.ui.form.AbstractFormHandler;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.AbstractGroupBox;
import org.eclipse.scout.rt.client.ui.form.fields.stringfield.AbstractStringField;
import org.eclipse.scout.rt.platform.Order;

public class AddRemoveFieldsForm extends AbstractForm {

  public AddRemoveFieldsForm() {
  }

  public TopBoxField getTopBoxField() {
    return getFieldByClass(TopBoxField.class);
  }

  public BottomBox getBottomBox() {
    return getFieldByClass(BottomBox.class);
  }

  public BottomBoxField getBottomBoxField() {
    return getFieldByClass(BottomBoxField.class);
  }

  public TopBox getTopBox() {
    return getFieldByClass(TopBox.class);
  }

  public ChildBox getChildBox() {
    return getFieldByClass(ChildBox.class);
  }

  public ChildBoxField getChildBoxField() {
    return getFieldByClass(ChildBoxField.class);
  }

  public MainBox getMainBox() {
    return (MainBox) getRootGroupBox();
  }

  @Override
  public void start() {
    startInternal(new AddRemoveFormHandler());
  }

  public class MainBox extends AbstractGroupBox {

    @Order(10)
    public class TopBox extends AbstractGroupBox {

      @Order(10)
      public class TopBoxField extends AbstractStringField {
      }
    }

    @Order(20)
    public class BottomBox extends AbstractGroupBox {

      @Order(10)
      public class BottomBoxField extends AbstractStringField {
      }

      @Order(20)
      public class ChildBox extends AbstractGroupBox {

        @Order(10)
        public class ChildBoxField extends AbstractStringField {
        }
      }
    }
  }

  public class AddRemoveFormHandler extends AbstractFormHandler {
  }
}
