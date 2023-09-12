/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.platform.util.date;

import static org.junit.Assert.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;
import java.util.Locale;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.nls.NlsLocale;
import org.eclipse.scout.rt.platform.text.TEXTS;
import org.eclipse.scout.rt.platform.util.NumberFormatProvider;
import org.eclipse.scout.rt.platform.util.TypeCastUtility;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(PlatformTestRunner.class)
public class DateTimePeriodFormatterTest {

  protected DateTimePeriodFormatter m_formatter = BEANS.get(DateTimePeriodFormatter.class);

  /**
   * test cases for conversion of special dates
   */
  @Test
  public void testFormatTimePeriod1() {
    assertNull(m_formatter.formatTimePeriod(null));
    assertEquals("00:00:00", m_formatter.formatTimePeriod(BigDecimal.ZERO));
    assertEquals("1 " + TEXTS.get("Day") + " 00:00:00", m_formatter.formatTimePeriod(BigDecimal.ONE));
    assertEquals("00:00:00", m_formatter.formatTimePeriod(BigDecimal.valueOf(-3.654789D)));
    assertEquals("3 " + TEXTS.get("Days") + " 15:42:54", m_formatter.formatTimePeriod(BigDecimal.valueOf(3.65479D)));
    assertEquals("11 " + TEXTS.get("Days") + " 11:11:11", m_formatter.formatTimePeriod(BigDecimal.valueOf(11.4661D)));
  }

  @Test
  public void testFormatTimePeriod2() {
    BigDecimal value = BigDecimal.valueOf(11L).setScale(100, RoundingMode.HALF_UP);
    BigDecimal day = BigDecimal.valueOf(86400L).setScale(100, RoundingMode.HALF_UP);
    BigDecimal d = value.divide(day, RoundingMode.HALF_UP);
    String result = m_formatter.formatTimePeriod(d);
    assertEquals("Formatted Time", "00:00:11", result);
  }

  @Test
  public void testFormatTimePeriod3() {
    Object o = 5L;
    BigDecimal d = TypeCastUtility.castValue(o, BigDecimal.class);
    String result = m_formatter.formatTimePeriod(d);
    assertEquals("Formatted Time", "5 " + TEXTS.get("Days") + " 00:00:00", result);
  }

  @Test
  public void testFormatTimePeriodOfMs() {
    char decimalSeparator = BEANS.get(NumberFormatProvider.class).getNumberInstance(NlsLocale.get()).getDecimalFormatSymbols().getDecimalSeparator();

    assertNull(m_formatter.formatTimePeriodOfMs(null));
    assertEquals("00:00:00" + decimalSeparator + "000", m_formatter.formatTimePeriodOfMs(-1L));
    assertEquals("00:00:00" + decimalSeparator + "005", m_formatter.formatTimePeriodOfMs(5L));
    assertEquals("00:00:02" + decimalSeparator + "001", m_formatter.formatTimePeriodOfMs(2001L));
    assertEquals("00:09:20" + decimalSeparator + "000", m_formatter.formatTimePeriodOfMs(560000L));
    assertEquals("00:00:00" + decimalSeparator + "999", m_formatter.formatTimePeriodOfMs(999L)); // Test rounding.
  }

  @Test
  public void testFormatDateTimeWithSeconds() {
    assertNull(m_formatter.formatDateTimeWithSeconds(null));

    Locale backup = NlsLocale.get();
    try {
      NlsLocale.set(new Locale("de", "CH"));
      Date date = DateUtility.parse("20220928 123456.789", "yyyyMMdd HHmmss.SSS");
      assertEquals("28.09.22, 12:34:56", m_formatter.formatDateTimeWithSeconds(date));
    }
    finally {
      NlsLocale.set(backup);
    }
  }

  @Test
  public void testFormatDateTimeWithMilliSeconds() {
    assertNull(m_formatter.formatDateTimeWithMilliSeconds(null));

    Locale backup = NlsLocale.get();
    try {
      NlsLocale.set(new Locale("de", "CH"));
      Date date = DateUtility.parse("20220928 123456.789", "yyyyMMdd HHmmss.SSS");
      assertEquals("28.09.22, 12:34:56.789", m_formatter.formatDateTimeWithMilliSeconds(date));
    }
    finally {
      NlsLocale.set(backup);
    }
  }
}
