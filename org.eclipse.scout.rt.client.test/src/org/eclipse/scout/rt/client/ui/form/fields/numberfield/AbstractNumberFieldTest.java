/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client.ui.form.fields.numberfield;

import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.junit.Assert;
import org.junit.Test;

public class AbstractNumberFieldTest extends AbstractNumberField<BigDecimal> {

  @Override
  protected BigDecimal getConfiguredMinValue() {
    return null;
  }

  @Override
  protected BigDecimal getConfiguredMaxValue() {
    return null;
  }

  public static void assertParseToBigDecimalInternalThrowsProcessingException(String msg, AbstractNumberField<?> field, String textValue) {
    boolean exceptionOccured = false;
    try {
      field.parseToBigDecimalInternal(textValue);
    }
    catch (ProcessingException e) {
      exceptionOccured = true;
    }
    assertTrue(msg, exceptionOccured);
  }

  @Test
  public void testFormatValueInternal() {
    setRoundingMode(RoundingMode.HALF_EVEN);
    Assert.assertEquals("12", formatValueInternal(BigDecimal.valueOf(12.5)));
    setRoundingMode(RoundingMode.HALF_UP);
    Assert.assertEquals("13", formatValueInternal(BigDecimal.valueOf(12.5)));
  }

}
