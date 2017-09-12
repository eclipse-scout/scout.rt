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
package org.eclipse.scout.rt.testing.platform.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.math.BigDecimal;

import org.junit.Test;

/**
 * Tests for {@link ScoutAssert}.
 * 
 * @since 3.10.0-M3
 */
public class ScoutAssertTest {

  /**
   * Test for {@link ScoutAssert#assertComparableEquals(Comparable, Comparable)} and
   * {@link ScoutAssert#assertComparableEquals(String, Comparable, Comparable)}.
   * 
   * @See Bug 420183
   */
  @Test
  public void testAssertComparableEquals() {
    BigDecimal b1 = BigDecimal.ONE;
    BigDecimal b2 = new BigDecimal("1.0000");
    BigDecimal b3 = BigDecimal.ONE;
    BigDecimal b4 = BigDecimal.ZERO;

    //Compare b1 and b2
    assertEquals(0, b1.compareTo(b2));
    assertNotEquals(b1, b2);
    ScoutAssert.assertComparableEquals(b1, b2);
    ScoutAssert.assertComparableEquals(b2, b1);
    ScoutAssert.assertComparableEquals("b1 <--ComparableEquals--> b2", b1, b2);
    ScoutAssert.assertComparableEquals("b2 <--ComparableEquals--> b1", b2, b1);

    //Compare same instance (b1 and b3):
    assertSame(b1, b3);
    ScoutAssert.assertComparableEquals(b1, b3);
    ScoutAssert.assertComparableEquals(b3, b1);
    ScoutAssert.assertComparableEquals("b1 <--ComparableEquals--> b3", b1, b3);
    ScoutAssert.assertComparableEquals("b3 <--ComparableEquals--> b1", b3, b1);

    //Compare null:
    ScoutAssert.assertComparableEquals(null, null);

    //Compare not_null and null => Expect AssertionError
    try {
      ScoutAssert.assertComparableEquals(b1, null);
      fail("Was expecteting an AssertionError");
    }
    catch (AssertionError e) {
      assertTrue(e.getMessage().contains("expected:<1> but was:<null>"));
    }

    //Compare null and not_null => Expect AssertionError
    try {
      ScoutAssert.assertComparableEquals(null, b1);
      fail("Was expecteting an AssertionError");
    }
    catch (AssertionError e) {
      assertTrue(e.getMessage().contains("expected:<null> but was:<1>"));
    }

    //Compare 0 and 1 => Expect AssertionError
    try {
      ScoutAssert.assertComparableEquals(b1, b4);
      fail("Was expecteting an AssertionError");
    }
    catch (AssertionError e) {
      assertTrue(e.getMessage().startsWith("expected"));
    }

    //Compare 1 and 0 => Expect AssertionError
    try {
      ScoutAssert.assertComparableEquals("MY_TEST", b4, b1);
      fail("Was expecteting an AssertionError");
    }
    catch (AssertionError e) {
      assertTrue(e.getMessage().startsWith("MY_TEST "));
      assertTrue(e.getMessage().contains("expected:<0> but was:<1>"));
    }
  }

}
