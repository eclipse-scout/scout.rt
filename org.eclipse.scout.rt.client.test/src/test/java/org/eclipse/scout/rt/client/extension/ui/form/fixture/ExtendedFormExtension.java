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

import org.eclipse.scout.rt.client.extension.ui.form.AbstractFormExtension;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.AbstractGroupBox;
import org.eclipse.scout.rt.client.ui.form.fields.stringfield.AbstractStringField;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.extension.Extends;

public class ExtendedFormExtension extends AbstractFormExtension<ExtendedForm> {

  public ExtendedFormExtension(ExtendedForm ownerForm) {
    super(ownerForm);
  }

  @Order(-10)
  @Extends(value = ExtendedForm.MainBox.class, pathToContainer = ExtendedForm.class)
  public class DetailBox extends AbstractGroupBox {

    @Order(10)
    public class StringField extends AbstractStringField {
    }
  }
}
