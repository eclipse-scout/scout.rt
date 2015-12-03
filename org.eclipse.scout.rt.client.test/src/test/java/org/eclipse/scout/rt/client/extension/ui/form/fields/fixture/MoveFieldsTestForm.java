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
