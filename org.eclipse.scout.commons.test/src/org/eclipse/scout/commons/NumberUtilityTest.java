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
package org.eclipse.scout.commons;

import java.math.BigDecimal;
import java.math.BigInteger;

import org.eclipse.scout.rt.testing.commons.ScoutAssert;
import org.junit.Assert;
import org.junit.Test;

public class NumberUtilityTest {

  /**
   * Test method for {@link org.eclipse.scout.commons.NumberUtility#numberToBigDecimal(java.lang.Number)}.
   */
  @Test
  public void testToBigDecimalNumber() {
    ScoutAssert.assertComparableEquals(BigDecimal.valueOf(25d), NumberUtility.numberToBigDecimal(Integer.valueOf(25)));
    ScoutAssert.assertComparableEquals(BigDecimal.valueOf(25d), NumberUtility.numberToBigDecimal(Long.valueOf(25)));
    ScoutAssert.assertComparableEquals(BigDecimal.valueOf(25.987d), NumberUtility.numberToBigDecimal(Float.valueOf(25.987f)));
    ScoutAssert.assertComparableEquals(BigDecimal.valueOf(25.987d), NumberUtility.numberToBigDecimal(Double.valueOf(25.987d)));
    ScoutAssert.assertComparableEquals(BigDecimal.valueOf(-25d), NumberUtility.numberToBigDecimal(Double.valueOf(-25d)));
    ScoutAssert.assertComparableEquals(BigDecimal.valueOf(-25d), NumberUtility.numberToBigDecimal(BigDecimal.valueOf(-25d)));
    ScoutAssert.assertComparableEquals(BigDecimal.valueOf(-25d), NumberUtility.numberToBigDecimal(BigInteger.valueOf(-25)));

    Assert.assertNull(NumberUtility.numberToBigDecimal(Double.valueOf(Double.NEGATIVE_INFINITY)));
    Assert.assertNull(NumberUtility.numberToBigDecimal(Double.valueOf(Double.POSITIVE_INFINITY)));
    Assert.assertNull(NumberUtility.numberToBigDecimal(Double.valueOf(Double.NaN)));
    Assert.assertNull(NumberUtility.numberToBigDecimal(Float.valueOf(Float.NEGATIVE_INFINITY)));
    Assert.assertNull(NumberUtility.numberToBigDecimal(Float.valueOf(Float.POSITIVE_INFINITY)));
    Assert.assertNull(NumberUtility.numberToBigDecimal(Float.valueOf(Float.NaN)));
  }

}
