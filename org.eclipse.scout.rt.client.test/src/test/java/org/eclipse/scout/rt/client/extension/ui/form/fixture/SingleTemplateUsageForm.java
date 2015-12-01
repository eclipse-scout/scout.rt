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
