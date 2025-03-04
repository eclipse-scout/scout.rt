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
import java.time.Duration;
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

  @Test
  public void testFormatDuration() {
    // null and zero
    assertNull(m_formatter.formatDuration(null));
    assertEquals("0s", m_formatter.formatDuration(Duration.ZERO));

    // negative value
    assertEquals("0s", m_formatter.formatDuration(Duration.ofDays(-1L)));

    // simple values
    assertEquals("1 " + TEXTS.get("Day") + " 0h 00m 00s", m_formatter.formatDuration(Duration.ofDays(1L)));
    assertEquals("3 " + TEXTS.get("Days") + " 15h 42m 54s", m_formatter.formatDuration(Duration.ofDays(3L)
        .plusHours(15L).plusMinutes(42L).plusSeconds(54L)));
    assertEquals("11 " + TEXTS.get("Days") + " 11h 11m 11s", m_formatter.formatDuration(Duration.ofDays(11L)
        .plusHours(11L).plusMinutes(11L).plusSeconds(11L)));

    // ignore millis part
    assertEquals("0s", m_formatter.formatDuration(Duration.ofMillis(123)));
    assertEquals("0s", m_formatter.formatDuration(Duration.ofMillis(999)));
  }

  @Test
  public void testFormatDurationPadding() {
    assertEquals("1s", m_formatter.formatDuration(Duration.ofSeconds(1)));
    assertEquals("1m 00s", m_formatter.formatDuration(Duration.ofMinutes(1)));
    assertEquals("1h 00m 00s", m_formatter.formatDuration(Duration.ofHours(1)));
    assertEquals("1 " + TEXTS.get("Day") + " 0h 00m 00s", m_formatter.formatDuration(Duration.ofDays(1L)));

    assertEquals("1m 01s", m_formatter.formatDuration(Duration.ofMinutes(1).plusSeconds(1)));

    assertEquals("1h 00m 01s", m_formatter.formatDuration(Duration.ofHours(1).plusSeconds(1)));
    assertEquals("1h 01m 01s", m_formatter.formatDuration(Duration.ofHours(1).plusMinutes(1).plusSeconds(1)));
    assertEquals("1h 01m 00s", m_formatter.formatDuration(Duration.ofHours(1).plusMinutes(1)));

    assertEquals("1 " + TEXTS.get("Day") + " 0h 00m 00s", m_formatter.formatDuration(Duration.ofDays(1L).plusHours(0).plusMinutes(0).plusSeconds(0)));
    assertEquals("1 " + TEXTS.get("Day") + " 0h 00m 01s", m_formatter.formatDuration(Duration.ofDays(1L).plusHours(0).plusMinutes(0).plusSeconds(1)));
    assertEquals("1 " + TEXTS.get("Day") + " 0h 01m 00s", m_formatter.formatDuration(Duration.ofDays(1L).plusHours(0).plusMinutes(1).plusSeconds(0)));
    assertEquals("1 " + TEXTS.get("Day") + " 0h 01m 01s", m_formatter.formatDuration(Duration.ofDays(1L).plusHours(0).plusMinutes(1).plusSeconds(1)));
    assertEquals("1 " + TEXTS.get("Day") + " 1h 00m 00s", m_formatter.formatDuration(Duration.ofDays(1L).plusHours(1).plusMinutes(0).plusSeconds(0)));
    assertEquals("1 " + TEXTS.get("Day") + " 1h 00m 01s", m_formatter.formatDuration(Duration.ofDays(1L).plusHours(1).plusMinutes(0).plusSeconds(1)));
    assertEquals("1 " + TEXTS.get("Day") + " 1h 01m 00s", m_formatter.formatDuration(Duration.ofDays(1L).plusHours(1).plusMinutes(1).plusSeconds(0)));
    assertEquals("1 " + TEXTS.get("Day") + " 1h 01m 01s", m_formatter.formatDuration(Duration.ofDays(1L).plusHours(1).plusMinutes(1).plusSeconds(1)));
  }

  /**
   * test cases for conversion of special dates
   */
  @Test
  public void testFormatTimePeriod1() {
    assertNull(m_formatter.formatTimePeriod(null));
    assertEquals("0s", m_formatter.formatTimePeriod(BigDecimal.ZERO));
    assertEquals("1 " + TEXTS.get("Day") + " 0h 00m 00s", m_formatter.formatTimePeriod(BigDecimal.ONE));
    assertEquals("0s", m_formatter.formatTimePeriod(BigDecimal.valueOf(-3.654789D)));
    assertEquals("3 " + TEXTS.get("Days") + " 15h 42m 54s", m_formatter.formatTimePeriod(BigDecimal.valueOf(3.65479D)));
    assertEquals("11 " + TEXTS.get("Days") + " 11h 11m 11s", m_formatter.formatTimePeriod(BigDecimal.valueOf(11.4661D)));
  }

  @Test
  public void testFormatTimePeriod2() {
    BigDecimal value = BigDecimal.valueOf(11L).setScale(100, RoundingMode.HALF_UP);
    BigDecimal day = BigDecimal.valueOf(86400L).setScale(100, RoundingMode.HALF_UP);
    BigDecimal d = value.divide(day, RoundingMode.HALF_UP);
    String result = m_formatter.formatTimePeriod(d);
    assertEquals("Formatted Time", "11s", result);
  }

  @Test
  public void testFormatTimePeriod3() {
    Object o = 5L;
    BigDecimal d = TypeCastUtility.castValue(o, BigDecimal.class);
    String result = m_formatter.formatTimePeriod(d);
    assertEquals("Formatted Time", "5 " + TEXTS.get("Days") + " 0h 00m 00s", result);
  }

  @Test
  public void testFormatTimePeriodOfMs() {
    char decimalSeparator = BEANS.get(NumberFormatProvider.class).getNumberInstance(NlsLocale.get()).getDecimalFormatSymbols().getDecimalSeparator();

    assertNull(m_formatter.formatTimePeriodOfMs(null));
    assertEquals("0" + decimalSeparator + "000s", m_formatter.formatTimePeriodOfMs(-1L));
    assertEquals("0" + decimalSeparator + "050s", m_formatter.formatTimePeriodOfMs(50L));
    assertEquals("2" + decimalSeparator + "001s", m_formatter.formatTimePeriodOfMs(2_001L));
    assertEquals("9m 20" + decimalSeparator + "000s", m_formatter.formatTimePeriodOfMs(560_000L));
    assertEquals("1h 09m 20" + decimalSeparator + "000s", m_formatter.formatTimePeriodOfMs(4_160_000L));
    assertEquals("2 " + TEXTS.get("Days") + " 1h 09m 20" + decimalSeparator + "000s", m_formatter.formatTimePeriodOfMs(176_960_000L));
    assertEquals("0" + decimalSeparator + "999s", m_formatter.formatTimePeriodOfMs(999L)); // Test rounding.
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
