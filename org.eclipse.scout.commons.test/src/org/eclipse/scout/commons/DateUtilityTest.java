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

import java.util.Calendar;
import java.util.Date;

import junit.framework.Assert;

import org.junit.Test;

public class DateUtilityTest {

  @Test
  public void testIsInDateRange() {
    Date from = DateUtility.parse("01-02-2011 00:00", "dd-MM-yyyy HH:mm");
    Date d = DateUtility.parse("09-02-2011 00:00", "dd-MM-yyyy HH:mm");
    Assert.assertFalse(DateUtility.isInDateRange(from, d, DateUtility.parse("08-02-2011 14:52", "dd-MM-yyyy HH:mm")));
    Assert.assertFalse(DateUtility.isInDateRange(from, d, DateUtility.parse("08-02-2011 23:59", "dd-MM-yyyy HH:mm")));
    Assert.assertTrue(DateUtility.isInDateRange(from, d, DateUtility.parse("09-02-2011 00:00", "dd-MM-yyyy HH:mm")));
    Assert.assertTrue(DateUtility.isInDateRange(from, d, DateUtility.parse("09-02-2011 00:01", "dd-MM-yyyy HH:mm")));
  }

  @Test
  public void testConvertDoubleTimeToDate() {
    //TimeField selector values:
    testConvertDoubleTimeToDate(0.0, "1970-01-01_00:00:00.000");
    testConvertDoubleTimeToDate(0.020833333, "1970-01-01_00:30:00.000");
    testConvertDoubleTimeToDate(0.041666667, "1970-01-01_01:00:00.000");
    testConvertDoubleTimeToDate(0.0625, "1970-01-01_01:30:00.000");
    testConvertDoubleTimeToDate(0.083333333, "1970-01-01_02:00:00.000");
    testConvertDoubleTimeToDate(0.104166667, "1970-01-01_02:30:00.000");
    testConvertDoubleTimeToDate(0.125, "1970-01-01_03:00:00.000");
    testConvertDoubleTimeToDate(0.145833333, "1970-01-01_03:30:00.000");
    testConvertDoubleTimeToDate(0.166666667, "1970-01-01_04:00:00.000");
    testConvertDoubleTimeToDate(0.1875, "1970-01-01_04:30:00.000");
    testConvertDoubleTimeToDate(0.208333333, "1970-01-01_05:00:00.000");
    testConvertDoubleTimeToDate(0.229166667, "1970-01-01_05:30:00.000");
    testConvertDoubleTimeToDate(0.25, "1970-01-01_06:00:00.000");
    testConvertDoubleTimeToDate(0.270833333, "1970-01-01_06:30:00.000");
    testConvertDoubleTimeToDate(0.291666667, "1970-01-01_07:00:00.000");
    testConvertDoubleTimeToDate(0.3125, "1970-01-01_07:30:00.000");
    testConvertDoubleTimeToDate(0.333333333, "1970-01-01_08:00:00.000");
    testConvertDoubleTimeToDate(0.354166667, "1970-01-01_08:30:00.000");
    testConvertDoubleTimeToDate(0.375, "1970-01-01_09:00:00.000");
    testConvertDoubleTimeToDate(0.395833333, "1970-01-01_09:30:00.000");
    testConvertDoubleTimeToDate(0.416666667, "1970-01-01_10:00:00.000");
    testConvertDoubleTimeToDate(0.4375, "1970-01-01_10:30:00.000");
    testConvertDoubleTimeToDate(0.458333333, "1970-01-01_11:00:00.000");
    testConvertDoubleTimeToDate(0.479166667, "1970-01-01_11:30:00.000");
    testConvertDoubleTimeToDate(0.5, "1970-01-01_12:00:00.000");
    testConvertDoubleTimeToDate(0.520833333, "1970-01-01_12:30:00.000");
    testConvertDoubleTimeToDate(0.541666667, "1970-01-01_13:00:00.000");
    testConvertDoubleTimeToDate(0.5625, "1970-01-01_13:30:00.000");
    testConvertDoubleTimeToDate(0.583333333, "1970-01-01_14:00:00.000");
    testConvertDoubleTimeToDate(0.604166667, "1970-01-01_14:30:00.000");
    testConvertDoubleTimeToDate(0.625, "1970-01-01_15:00:00.000");
    testConvertDoubleTimeToDate(0.645833333, "1970-01-01_15:30:00.000");
    testConvertDoubleTimeToDate(0.666666667, "1970-01-01_16:00:00.000");
    testConvertDoubleTimeToDate(0.6875, "1970-01-01_16:30:00.000");
    testConvertDoubleTimeToDate(0.708333333, "1970-01-01_17:00:00.000");
    testConvertDoubleTimeToDate(0.729166667, "1970-01-01_17:30:00.000");
    testConvertDoubleTimeToDate(0.75, "1970-01-01_18:00:00.000");
    testConvertDoubleTimeToDate(0.770833333, "1970-01-01_18:30:00.000");
    testConvertDoubleTimeToDate(0.791666667, "1970-01-01_19:00:00.000");
    testConvertDoubleTimeToDate(0.8125, "1970-01-01_19:30:00.000");
    testConvertDoubleTimeToDate(0.833333333, "1970-01-01_20:00:00.000");
    testConvertDoubleTimeToDate(0.854166667, "1970-01-01_20:30:00.000");
    testConvertDoubleTimeToDate(0.875, "1970-01-01_21:00:00.000");
    testConvertDoubleTimeToDate(0.895833333, "1970-01-01_21:30:00.000");
    testConvertDoubleTimeToDate(0.916666667, "1970-01-01_22:00:00.000");
    testConvertDoubleTimeToDate(0.9375, "1970-01-01_22:30:00.000");
    testConvertDoubleTimeToDate(0.958333333, "1970-01-01_23:00:00.000");
    testConvertDoubleTimeToDate(0.979166667, "1970-01-01_23:30:00.000");

    //Limit condition:
    testConvertDoubleTimeToDate(0.999988425925926, "1970-01-01_23:59:59.000");

    //Out of definition Range:
    testConvertDoubleTimeToDate(1.0, "1970-01-01_00:00:00.000");
    testConvertDoubleTimeToDate(1.25, "1970-01-01_06:00:00.000");
    testConvertDoubleTimeToDate(1.875, "1970-01-01_21:00:00.000");
    testConvertDoubleTimeToDate(9.0, "1970-01-01_00:00:00.000");
    testConvertDoubleTimeToDate(9.25, "1970-01-01_06:00:00.000");
    testConvertDoubleTimeToDate(9.875, "1970-01-01_21:00:00.000");

    testConvertDoubleTimeToDate(-0.125, "1970-01-01_21:00:00.000");
    testConvertDoubleTimeToDate(-0.25, "1970-01-01_18:00:00.000");
    testConvertDoubleTimeToDate(-0.5, "1970-01-01_12:00:00.000");
    testConvertDoubleTimeToDate(-0.75, "1970-01-01_06:00:00.000");
    testConvertDoubleTimeToDate(-1.0, "1970-01-01_00:00:00.000");
    testConvertDoubleTimeToDate(-7.125, "1970-01-01_21:00:00.000");
    testConvertDoubleTimeToDate(-7.25, "1970-01-01_18:00:00.000");
    testConvertDoubleTimeToDate(-7.75, "1970-01-01_06:00:00.000");
  }

  /**
   * @param input
   *          Double to be converted
   * @param expectedDate
   *          with the Pattern 'yyyy-MM-dd_HH:mm:ss.SSS'
   */
  private void testConvertDoubleTimeToDate(Double input, String expectedDate) {
    Date expected = DateUtility.parse(expectedDate, "yyyy-MM-dd_HH:mm:ss.SSS");
    Date value = DateUtility.convertDoubleTimeToDate(input);

    String message = "Conversion DoubleTime <" + input + "> to Date";
    assertDateEquals(message, expected, value);
  }

  @Test
  public void testConvertDateToDoubleTime() {
    //TimeField selector values:
    testConvertDateToDoubleTime("1970-01-01_00:00:00.000", 0.0);
    testConvertDateToDoubleTime("1970-01-01_00:30:00.000", 0.020833333333333332);
    testConvertDateToDoubleTime("1970-01-01_01:00:00.000", 0.041666666666666664);
    testConvertDateToDoubleTime("1970-01-01_01:30:00.000", 0.0625);
    testConvertDateToDoubleTime("1970-01-01_02:00:00.000", 0.083333333333333332);
    testConvertDateToDoubleTime("1970-01-01_02:30:00.000", 0.104166666666666667);
    testConvertDateToDoubleTime("1970-01-01_03:00:00.000", 0.125);
    testConvertDateToDoubleTime("1970-01-01_03:30:00.000", 0.145833333333333332);
    testConvertDateToDoubleTime("1970-01-01_04:00:00.000", 0.166666666666666664);
    testConvertDateToDoubleTime("1970-01-01_04:30:00.000", 0.1875);
    testConvertDateToDoubleTime("1970-01-01_05:00:00.000", 0.208333333333333332);
    testConvertDateToDoubleTime("1970-01-01_05:30:00.000", 0.229166666666666664);
    testConvertDateToDoubleTime("1970-01-01_06:00:00.000", 0.25);
    testConvertDateToDoubleTime("1970-01-01_06:30:00.000", 0.270833333333333332);
    testConvertDateToDoubleTime("1970-01-01_07:00:00.000", 0.291666666666666664);
    testConvertDateToDoubleTime("1970-01-01_07:30:00.000", 0.3125);
    testConvertDateToDoubleTime("1970-01-01_08:00:00.000", 0.333333333333333332);
    testConvertDateToDoubleTime("1970-01-01_08:30:00.000", 0.354166666666666664);
    testConvertDateToDoubleTime("1970-01-01_09:00:00.000", 0.375);
    testConvertDateToDoubleTime("1970-01-01_09:30:00.000", 0.395833333333333332);
    testConvertDateToDoubleTime("1970-01-01_10:00:00.000", 0.416666666666666664);
    testConvertDateToDoubleTime("1970-01-01_10:30:00.000", 0.4375);
    testConvertDateToDoubleTime("1970-01-01_11:00:00.000", 0.458333333333333332);
    testConvertDateToDoubleTime("1970-01-01_11:30:00.000", 0.479166666666666664);
    testConvertDateToDoubleTime("1970-01-01_12:00:00.000", 0.5);
    testConvertDateToDoubleTime("1970-01-01_12:30:00.000", 0.520833333333333332);
    testConvertDateToDoubleTime("1970-01-01_13:00:00.000", 0.541666666666666664);
    testConvertDateToDoubleTime("1970-01-01_13:30:00.000", 0.5625);
    testConvertDateToDoubleTime("1970-01-01_14:00:00.000", 0.583333333333333332);
    testConvertDateToDoubleTime("1970-01-01_14:30:00.000", 0.604166666666666664);
    testConvertDateToDoubleTime("1970-01-01_15:00:00.000", 0.625);
    testConvertDateToDoubleTime("1970-01-01_15:30:00.000", 0.645833333333333332);
    testConvertDateToDoubleTime("1970-01-01_16:00:00.000", 0.666666666666666664);
    testConvertDateToDoubleTime("1970-01-01_16:30:00.000", 0.6875);
    testConvertDateToDoubleTime("1970-01-01_17:00:00.000", 0.708333333333333332);
    testConvertDateToDoubleTime("1970-01-01_17:30:00.000", 0.729166666666666664);
    testConvertDateToDoubleTime("1970-01-01_18:00:00.000", 0.75);
    testConvertDateToDoubleTime("1970-01-01_18:30:00.000", 0.770833333333333332);
    testConvertDateToDoubleTime("1970-01-01_19:00:00.000", 0.791666666666666664);
    testConvertDateToDoubleTime("1970-01-01_19:30:00.000", 0.8125);
    testConvertDateToDoubleTime("1970-01-01_20:00:00.000", 0.833333333333333332);
    testConvertDateToDoubleTime("1970-01-01_20:30:00.000", 0.854166666666666664);
    testConvertDateToDoubleTime("1970-01-01_21:00:00.000", 0.875);
    testConvertDateToDoubleTime("1970-01-01_21:30:00.000", 0.895833333333333332);
    testConvertDateToDoubleTime("1970-01-01_22:00:00.000", 0.916666666666666664);
    testConvertDateToDoubleTime("1970-01-01_22:30:00.000", 0.9375);
    testConvertDateToDoubleTime("1970-01-01_23:00:00.000", 0.958333333333333332);
    testConvertDateToDoubleTime("1970-01-01_23:30:00.000", 0.979166666666666664);

    //Limit condition:
    testConvertDateToDoubleTime("1970-01-01_23:59:59.000", 0.999988425925926);

    //Out of definition Range:
    testConvertDateToDoubleTime("1970-01-02_00:00:00.000", 0.0);
    testConvertDateToDoubleTime("1970-01-02_06:00:00.000", 0.25);

    testConvertDateToDoubleTime("1970-01-15_00:00:00.000", 0.0);
    testConvertDateToDoubleTime("1970-01-15_06:00:00.000", 0.25);

    testConvertDateToDoubleTime("1969-12-31_00:00:00.000", 0.0);
    testConvertDateToDoubleTime("1969-12-31_18:00:00.000", 0.75);

    testConvertDateToDoubleTime("1969-12-05_00:00:00.000", 0.0);
    testConvertDateToDoubleTime("1969-12-05_18:00:00.000", 0.75);

  }

  /**
   * @param input
   *          Date defined with the Pattern 'yyyy-MM-dd_HH:mm:ss.SSS'
   * @param expected
   *          Double DoubleTime as defined in {@link DateUtility#convertDateToDoubleTime(java.util.Date)}
   */
  private void testConvertDateToDoubleTime(String input, Double expected) {
    Assert.assertEquals("Conversion Date <" + input + "> to DoubleTime", expected, DateUtility.convertDateToDoubleTime(DateUtility.parse(input, "yyyy-MM-dd_HH:mm:ss.SSS")));
  }

  @Test
  public void testConvertDateToDoubleTimeToDate() {
    //TimeField selector values:
    testConvertDateToDoubleTimeToDate("1970-01-01_00:00:00.000");
    testConvertDateToDoubleTimeToDate("1970-01-01_00:30:00.000");
    testConvertDateToDoubleTimeToDate("1970-01-01_01:00:00.000");
    testConvertDateToDoubleTimeToDate("1970-01-01_01:30:00.000");
    testConvertDateToDoubleTimeToDate("1970-01-01_02:00:00.000");
    testConvertDateToDoubleTimeToDate("1970-01-01_02:30:00.000");
    testConvertDateToDoubleTimeToDate("1970-01-01_03:00:00.000");
    testConvertDateToDoubleTimeToDate("1970-01-01_03:30:00.000");
    testConvertDateToDoubleTimeToDate("1970-01-01_04:00:00.000");
    testConvertDateToDoubleTimeToDate("1970-01-01_04:30:00.000");
    testConvertDateToDoubleTimeToDate("1970-01-01_05:00:00.000");
    testConvertDateToDoubleTimeToDate("1970-01-01_05:30:00.000");
    testConvertDateToDoubleTimeToDate("1970-01-01_06:00:00.000");
    testConvertDateToDoubleTimeToDate("1970-01-01_06:30:00.000");
    testConvertDateToDoubleTimeToDate("1970-01-01_07:00:00.000");
    testConvertDateToDoubleTimeToDate("1970-01-01_07:30:00.000");
    testConvertDateToDoubleTimeToDate("1970-01-01_08:00:00.000");
    testConvertDateToDoubleTimeToDate("1970-01-01_08:30:00.000");
    testConvertDateToDoubleTimeToDate("1970-01-01_09:00:00.000");
    testConvertDateToDoubleTimeToDate("1970-01-01_09:30:00.000");
    testConvertDateToDoubleTimeToDate("1970-01-01_10:00:00.000");
    testConvertDateToDoubleTimeToDate("1970-01-01_10:30:00.000");
    testConvertDateToDoubleTimeToDate("1970-01-01_11:00:00.000");
    testConvertDateToDoubleTimeToDate("1970-01-01_11:30:00.000");
    testConvertDateToDoubleTimeToDate("1970-01-01_12:00:00.000");
    testConvertDateToDoubleTimeToDate("1970-01-01_12:30:00.000");
    testConvertDateToDoubleTimeToDate("1970-01-01_13:00:00.000");
    testConvertDateToDoubleTimeToDate("1970-01-01_13:30:00.000");
    testConvertDateToDoubleTimeToDate("1970-01-01_14:00:00.000");
    testConvertDateToDoubleTimeToDate("1970-01-01_14:30:00.000");
    testConvertDateToDoubleTimeToDate("1970-01-01_15:00:00.000");
    testConvertDateToDoubleTimeToDate("1970-01-01_15:30:00.000");
    testConvertDateToDoubleTimeToDate("1970-01-01_16:00:00.000");
    testConvertDateToDoubleTimeToDate("1970-01-01_16:30:00.000");
    testConvertDateToDoubleTimeToDate("1970-01-01_17:00:00.000");
    testConvertDateToDoubleTimeToDate("1970-01-01_17:30:00.000");
    testConvertDateToDoubleTimeToDate("1970-01-01_18:00:00.000");
    testConvertDateToDoubleTimeToDate("1970-01-01_18:30:00.000");
    testConvertDateToDoubleTimeToDate("1970-01-01_19:00:00.000");
    testConvertDateToDoubleTimeToDate("1970-01-01_19:30:00.000");
    testConvertDateToDoubleTimeToDate("1970-01-01_20:00:00.000");
    testConvertDateToDoubleTimeToDate("1970-01-01_20:30:00.000");
    testConvertDateToDoubleTimeToDate("1970-01-01_21:00:00.000");
    testConvertDateToDoubleTimeToDate("1970-01-01_21:30:00.000");
    testConvertDateToDoubleTimeToDate("1970-01-01_22:00:00.000");
    testConvertDateToDoubleTimeToDate("1970-01-01_22:30:00.000");
    testConvertDateToDoubleTimeToDate("1970-01-01_23:00:00.000");
    testConvertDateToDoubleTimeToDate("1970-01-01_23:30:00.000");

    //Limit condition:
    testConvertDateToDoubleTimeToDate("1970-01-01_23:59:59.000");
  }

  /**
   * @param input
   *          Date defined with the Pattern 'yyyy-MM-dd_HH:mm:ss.SSS'
   */
  private void testConvertDateToDoubleTimeToDate(String input) {
    Assert.assertEquals("Conversion Date <" + input + "> to DoubleTime and to Date", DateUtility.parse(input, "yyyy-MM-dd_HH:mm:ss.SSS"), DateUtility.convertDoubleTimeToDate(DateUtility.convertDateToDoubleTime(DateUtility.parse(input, "yyyy-MM-dd_HH:mm:ss.SSS"))));
  }

  @Test
  public void testConvertDoubleTimeToDateToDoubleTime() {
    //TimeField selector values:
    testConvertDoubleTimeToDateToDoubleTime(0.0);
    testConvertDoubleTimeToDateToDoubleTime(0.020833333333333332);
    testConvertDoubleTimeToDateToDoubleTime(0.041666666666666664);
    testConvertDoubleTimeToDateToDoubleTime(0.0625);
    testConvertDoubleTimeToDateToDoubleTime(0.083333333333333332);
    testConvertDoubleTimeToDateToDoubleTime(0.104166666666666667);
    testConvertDoubleTimeToDateToDoubleTime(0.125);
    testConvertDoubleTimeToDateToDoubleTime(0.145833333333333332);
    testConvertDoubleTimeToDateToDoubleTime(0.166666666666666664);
    testConvertDoubleTimeToDateToDoubleTime(0.1875);
    testConvertDoubleTimeToDateToDoubleTime(0.208333333333333332);
    testConvertDoubleTimeToDateToDoubleTime(0.229166666666666664);
    testConvertDoubleTimeToDateToDoubleTime(0.25);
    testConvertDoubleTimeToDateToDoubleTime(0.270833333333333332);
    testConvertDoubleTimeToDateToDoubleTime(0.291666666666666664);
    testConvertDoubleTimeToDateToDoubleTime(0.3125);
    testConvertDoubleTimeToDateToDoubleTime(0.333333333333333332);
    testConvertDoubleTimeToDateToDoubleTime(0.354166666666666664);
    testConvertDoubleTimeToDateToDoubleTime(0.375);
    testConvertDoubleTimeToDateToDoubleTime(0.395833333333333332);
    testConvertDoubleTimeToDateToDoubleTime(0.416666666666666664);
    testConvertDoubleTimeToDateToDoubleTime(0.4375);
    testConvertDoubleTimeToDateToDoubleTime(0.458333333333333332);
    testConvertDoubleTimeToDateToDoubleTime(0.479166666666666664);
    testConvertDoubleTimeToDateToDoubleTime(0.5);
    testConvertDoubleTimeToDateToDoubleTime(0.520833333333333332);
    testConvertDoubleTimeToDateToDoubleTime(0.541666666666666664);
    testConvertDoubleTimeToDateToDoubleTime(0.5625);
    testConvertDoubleTimeToDateToDoubleTime(0.583333333333333332);
    testConvertDoubleTimeToDateToDoubleTime(0.604166666666666664);
    testConvertDoubleTimeToDateToDoubleTime(0.625);
    testConvertDoubleTimeToDateToDoubleTime(0.645833333333333332);
    testConvertDoubleTimeToDateToDoubleTime(0.666666666666666664);
    testConvertDoubleTimeToDateToDoubleTime(0.6875);
    testConvertDoubleTimeToDateToDoubleTime(0.708333333333333332);
    testConvertDoubleTimeToDateToDoubleTime(0.729166666666666664);
    testConvertDoubleTimeToDateToDoubleTime(0.75);
    testConvertDoubleTimeToDateToDoubleTime(0.770833333333333332);
    testConvertDoubleTimeToDateToDoubleTime(0.791666666666666664);
    testConvertDoubleTimeToDateToDoubleTime(0.8125);
    testConvertDoubleTimeToDateToDoubleTime(0.833333333333333332);
    testConvertDoubleTimeToDateToDoubleTime(0.854166666666666664);
    testConvertDoubleTimeToDateToDoubleTime(0.875);
    testConvertDoubleTimeToDateToDoubleTime(0.895833333333333332);
    testConvertDoubleTimeToDateToDoubleTime(0.916666666666666664);
    testConvertDoubleTimeToDateToDoubleTime(0.9375);
    testConvertDoubleTimeToDateToDoubleTime(0.958333333333333332);
    testConvertDoubleTimeToDateToDoubleTime(0.979166666666666664);

    //Limit condition:
    testConvertDoubleTimeToDateToDoubleTime(0.999988425925926);
  }

  /**
   * @param input
   *          Double DoubleTime as defined in {@link DateUtility#convertDateToDoubleTime(java.util.Date)}
   */
  private void testConvertDoubleTimeToDateToDoubleTime(Double input) {
    Assert.assertEquals("Conversion Date <" + input + "> to DoubleTime and to Date", input, DateUtility.convertDateToDoubleTime(DateUtility.convertDoubleTimeToDate(input)));
  }

  /**
   * @param message
   * @param expected
   * @param value
   */
  public static void assertDateEquals(String message, Date expected, Date value) {
    Calendar v = Calendar.getInstance();
    v.clear();
    v.setTime(value);

    Calendar e = Calendar.getInstance();
    e.clear();
    e.setTime(expected);

    Assert.assertEquals(message + "[MILLIS]", e.get(Calendar.MILLISECOND), v.get(Calendar.MILLISECOND));
    Assert.assertEquals(message + "[SEC]", e.get(Calendar.SECOND), v.get(Calendar.SECOND));
    Assert.assertEquals(message + "[MIN]", e.get(Calendar.MINUTE), v.get(Calendar.MINUTE));
    Assert.assertEquals(message + "[HOURS]", e.get(Calendar.HOUR_OF_DAY), v.get(Calendar.HOUR_OF_DAY));

    Assert.assertEquals(message + "[DAY]", e.get(Calendar.DAY_OF_MONTH), v.get(Calendar.DAY_OF_MONTH));
    Assert.assertEquals(message + "[MONTH]", e.get(Calendar.MONTH), v.get(Calendar.MONTH));
    Assert.assertEquals(message + "[YEAR]", e.get(Calendar.YEAR), v.get(Calendar.YEAR));

    Assert.assertEquals(message + "(DATE)", expected, value);
  }
}
