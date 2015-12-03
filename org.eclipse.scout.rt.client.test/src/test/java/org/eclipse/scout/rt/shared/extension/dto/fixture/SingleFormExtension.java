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
