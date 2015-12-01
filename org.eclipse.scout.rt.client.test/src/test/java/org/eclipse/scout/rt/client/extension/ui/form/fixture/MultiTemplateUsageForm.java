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

import org.eclipse.scout.rt.client.extension.ui.form.fixture.MultiTemplateUsageForm.MainBox.FirstTemplateBox;
import org.eclipse.scout.rt.client.extension.ui.form.fixture.MultiTemplateUsageForm.MainBox.FirstTemplateBox.MiddleStringField;
import org.eclipse.scout.rt.client.extension.ui.form.fixture.MultiTemplateUsageForm.MainBox.MainBoxStringField;
import org.eclipse.scout.rt.client.extension.ui.form.fixture.MultiTemplateUsageForm.MainBox.SecondTemplateBox;
import org.eclipse.scout.rt.client.ui.form.AbstractForm;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.AbstractGroupBox;
import org.eclipse.scout.rt.client.ui.form.fields.stringfield.AbstractStringField;
import org.eclipse.scout.rt.platform.Order;

public class MultiTemplateUsageForm extends AbstractForm {

  public MultiTemplateUsageForm() {
    super();
  }

  public MainBox getMainBox() {
    return getFieldByClass(MainBox.class);
  }

  public FirstTemplateBox getFirstTemplateBox() {
    return getFieldByClass(FirstTemplateBox.class);
  }

  public MiddleStringField getMiddleStringField() {
    return getFieldByClass(MiddleStringField.class);
  }

  public SecondTemplateBox getSecondTemplateBox() {
    return getFieldByClass(SecondTemplateBox.class);
  }

  public MainBoxStringField getMainBoxStringField() {
    return getFieldByClass(MainBoxStringField.class);
  }

  public class MainBox extends AbstractGroupBox {

    @Order(10)
    public class FirstTemplateBox extends AbstractTemplateGroupsBox {

      @Order(15)
      public class MiddleStringField extends AbstractStringField {
      }
    }

    @Order(20)
    public class SecondTemplateBox extends AbstractTemplateGroupsBox {
    }

    @Order(30)
    public class MainBoxStringField extends AbstractStringField {
    }
  }
}
