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

import org.eclipse.scout.rt.client.extension.ui.form.fixture.SingleTemplateUsageForm.MainBox.TemplateUsageBox;
import org.eclipse.scout.rt.client.ui.form.AbstractForm;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.AbstractGroupBox;
import org.eclipse.scout.rt.platform.Order;

public class SingleTemplateUsageForm extends AbstractForm {

  public SingleTemplateUsageForm() {
  }

  public MainBox getMainBox() {
    return (MainBox) getRootGroupBox();
  }

  public TemplateUsageBox getTemplateUsageBox() {
    return getFieldByClass(TemplateUsageBox.class);
  }

  public class MainBox extends AbstractGroupBox {

    @Order(10)
    public class TemplateUsageBox extends AbstractTemplateFieldsBox {
    }
  }
}
