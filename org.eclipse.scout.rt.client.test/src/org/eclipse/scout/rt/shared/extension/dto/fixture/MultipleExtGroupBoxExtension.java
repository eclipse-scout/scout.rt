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
package org.eclipse.scout.rt.shared.extension.dto.fixture;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.eclipse.scout.commons.annotations.Data;
import org.eclipse.scout.commons.annotations.Extends;
import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.extension.ui.form.fields.groupbox.AbstractGroupBoxExtension;
import org.eclipse.scout.rt.client.ui.form.fields.datefield.AbstractDateField;
import org.eclipse.scout.rt.client.ui.form.fields.doublefield.AbstractDoubleField;
import org.eclipse.scout.rt.shared.extension.dto.fixture.OrigForm.MainBox;

/**
 *
 */
@SuppressWarnings("deprecation")
@Extends(MainBox.class)
@Data(MultipleExtGroupBoxExtensionData.class)
public class MultipleExtGroupBoxExtension extends AbstractGroupBoxExtension<MainBox> {

  public static final Double DOUBLE_FIELD_ORIG_VAL = Double.valueOf(12.3);
  public static final Date DATE_FIELD_ORIG_VAL = getTestDate();

  public MultipleExtGroupBoxExtension(MainBox ownerBox) {
    super(ownerBox);
  }

  @Order(2000)
  public class SecondDoubleField extends AbstractDoubleField {
    @Override
    protected void execInitField() throws ProcessingException {
      super.execInitField();
      setValue(DOUBLE_FIELD_ORIG_VAL);
    }
  }

  @Order(3000)
  public class ThirdDateField extends AbstractDateField {
    @Override
    protected void execInitField() throws ProcessingException {
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
