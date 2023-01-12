/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.shared.extension.dto.fixture;

import java.math.BigDecimal;

import org.eclipse.scout.rt.client.dto.Data;
import org.eclipse.scout.rt.client.extension.ui.form.AbstractFormExtension;
import org.eclipse.scout.rt.client.ui.form.fields.bigdecimalfield.AbstractBigDecimalField;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.extension.Extends;
import org.eclipse.scout.rt.shared.extension.dto.fixture.OrigForm.MainBox;

@Data(SingleFormExtensionData.class)
public class SingleFormExtension extends AbstractFormExtension<OrigForm> {

  public static final BigDecimal BIG_DECIMAL_FIELD_ORIG_VALUE = new BigDecimal("22");

  public SingleFormExtension(OrigForm ownerForm) {
    super(ownerForm);
  }

  @Order(2000)
  @Extends(MainBox.class)
  public class SecondBigDecimalField extends AbstractBigDecimalField {
    @Override
    protected void execInitField() {
      super.execInitField();
      setValue(BIG_DECIMAL_FIELD_ORIG_VALUE);
    }
  }
}
