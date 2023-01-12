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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.eclipse.scout.rt.client.dto.Data;
import org.eclipse.scout.rt.client.extension.ui.form.fields.groupbox.AbstractGroupBoxExtension;
import org.eclipse.scout.rt.client.ui.form.fields.bigdecimalfield.AbstractBigDecimalField;
import org.eclipse.scout.rt.client.ui.form.fields.datefield.AbstractDateField;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.extension.Extends;
import org.eclipse.scout.rt.shared.extension.dto.fixture.OrigForm.MainBox;

@Extends(MainBox.class)
@Data(MultipleExtGroupBoxExtensionData.class)
public class MultipleExtGroupBoxExtension extends AbstractGroupBoxExtension<MainBox> {

  public static final BigDecimal BIGDECIMAL_FIELD_ORIG_VAL = BigDecimal.valueOf(12.3);
  public static final Date DATE_FIELD_ORIG_VAL = getTestDate();

  public MultipleExtGroupBoxExtension(MainBox ownerBox) {
    super(ownerBox);
  }

  @Order(2000)
  public class SecondBigDecimalField extends AbstractBigDecimalField {
    @Override
    protected void execInitField() {
      super.execInitField();
      setValue(BIGDECIMAL_FIELD_ORIG_VAL);
    }
  }

  @Order(3000)
  public class ThirdDateField extends AbstractDateField {
    @Override
    protected void execInitField() {
      super.execInitField();
      setValue(DATE_FIELD_ORIG_VAL);
    }
  }

  private static Date getTestDate() {
    try {
      return new SimpleDateFormat("yyyyMMdd").parse("20141105");
    }
    catch (ParseException e) {
      throw new RuntimeException(e);
    }
  }
}
