/************************0******************************************************
 * Copyright (c) 2010 BSI B0usiness Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client.ui.basic.table.columns;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;

import org.easymock.EasyMock;
import org.eclipse.scout.commons.LocaleThreadLocal;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.ui.basic.table.ITableRow;
import org.eclipse.scout.rt.client.ui.form.fields.bigdecimalfield.AbstractBigDecimalField;
import org.eclipse.scout.rt.client.ui.form.fields.decimalfield.AbstractDecimalField;
import org.junit.Test;

public class AbstractDecimalColumnTest extends AbstractDecimalColumn<BigDecimal> {

  @Override
  protected AbstractDecimalField<BigDecimal> getEditorField() {
    return new AbstractBigDecimalField() {
    };
  }

  @Override
  protected BigDecimal getConfiguredMinValue() {
    return null;
  }

  @Override
  protected BigDecimal getConfiguredMaxValue() {
    return null;
  }

  @Test
  public void testDecimalFormatHandling() {
    DecimalFormat format = getFormat();
    String percentSuffix = ((DecimalFormat) NumberFormat.getPercentInstance(LocaleThreadLocal.get())).getPositiveSuffix();
    assertTrue("expected groupingUsed-property set to true as default", format.isGroupingUsed());
    assertTrue("expected groupingUsed-property set to true as default", isGroupingUsed());
    assertEquals("expected minFractionDigits-property set to 2 as default", 2, getMinFractionDigits());
    assertEquals("expected minFractionDigits-property set to 2 as default", 2, format.getMinimumFractionDigits());
    assertEquals("expected maxFractionDigits-property set to 2 as default", 2, getMaxFractionDigits());
    assertEquals("expected maxFractionDigits-property set to 2 as default", 2, format.getMaximumFractionDigits());
    assertFalse("expected percent-property set to false as default", isPercent());
    assertFalse("expected percent-property set to false as default", format.getPositiveSuffix().endsWith(percentSuffix) && format.getPositiveSuffix().equals(format.getNegativeSuffix()));
    assertEquals("expected multiplier-property set to 1 as default", 1, getMultiplier());
    assertEquals("expected multiplier-property set to 1 as default", 1, format.getMultiplier());
    assertEquals("expected fractionDigits-property set to 2 as default", 2, getFractionDigits());

    format.setGroupingUsed(false);
    format.setMinimumFractionDigits(4);
    format.setMaximumFractionDigits(7);
    format.setPositiveSuffix(percentSuffix);
    format.setNegativeSuffix(percentSuffix);
    format.setMultiplier(100);
    setFormat(format);
    format = getFormat();
    assertFalse("expected groupingUsed-property set to false after setting format", format.isGroupingUsed());
    assertFalse("expected groupingUsed-property set to false after setting format", isGroupingUsed());
    assertEquals("expected minFractionDigits-property set to 4 after setting format", 4, getMinFractionDigits());
    assertEquals("expected minFractionDigits-property set to 4 after setting format", 4, format.getMinimumFractionDigits());
    assertEquals("expected maxFractionDigits-property set to 7 after setting format", 7, getMaxFractionDigits());
    assertEquals("expected maxFractionDigits-property set to 7 after setting format", 7, format.getMaximumFractionDigits());
    assertTrue("expected percent-property set to true after setting format", isPercent());
    assertTrue("expected percent-property set to true after setting format", format.getPositiveSuffix().endsWith(percentSuffix) && format.getPositiveSuffix().equals(format.getNegativeSuffix()));
    assertEquals("expected multiplier-property set to 100 after setting format", 100, getMultiplier());
    assertEquals("expected multiplier-property set to 100 after setting format", 100, format.getMultiplier());

    setGroupingUsed(true);
    setMinFractionDigits(5);
    setMaxFractionDigits(6);
    setPercent(false);
    setMultiplier(1000);
    setFractionDigits(3);
    format = getFormat();
    assertTrue("expected groupingUsed-property set to true after using convenience setter", format.isGroupingUsed());
    assertTrue("expected groupingUsed-property set to true after using convenience setter", isGroupingUsed());
    assertEquals("expected minFractionDigits-property set to 5 after using convenience setter", 5, getMinFractionDigits());
    assertEquals("expected minFractionDigits-property set to 5 after using convenience setter", 5, format.getMinimumFractionDigits());
    assertEquals("expected maxFractionDigits-property set to 6 after using convenience setter", 6, getMaxFractionDigits());
    assertEquals("expected maxFractionDigits-property set to 6 after using convenience setter", 6, format.getMaximumFractionDigits());
    assertFalse("expected percent-property set to false after using convenience setter", format.getPositiveSuffix().endsWith(percentSuffix) && format.getPositiveSuffix().equals(format.getNegativeSuffix()));
    assertFalse("expected percent-property set to false after using convenience setter", isPercent());
    assertEquals("expected multiplier-property set to 1000 after using convenience setter", 1000, getMultiplier());
    assertEquals("expected multiplier-property set to 1000 after using convenience setter", 1000, format.getMultiplier());
    assertEquals("expected fractionDigits-property set to 3 after using setter", 3, getFractionDigits());

  }

  @Test
  public void testPrepareEditInternal() throws ProcessingException {
    setGroupingUsed(false);
    setMinFractionDigits(5);
    setMaxFractionDigits(6);
    setPercent(true);
    setMultiplier(100);
    setFractionDigits(3);

    ITableRow row = EasyMock.createMock(ITableRow.class);
    AbstractBigDecimalField field = (AbstractBigDecimalField) prepareEditInternal(row);
    assertFalse("expected groupingUsed-property to be propagated to field", field.isGroupingUsed());
    assertEquals("expected minFractionDigits-property to be propagated to field", 5, field.getMinFractionDigits());
    assertEquals("expected maxFractionDigits-property to be propagated to field", 6, field.getMaxFractionDigits());
    assertTrue("expected percent-property to be propagated to field", field.isPercent());
    assertEquals("expected multiplier-property to be propagated to field", 100, field.getMultiplier());
    assertEquals("expected fractionDigits-property to be propagated to field", 3, field.getFractionDigits());

  }

}
