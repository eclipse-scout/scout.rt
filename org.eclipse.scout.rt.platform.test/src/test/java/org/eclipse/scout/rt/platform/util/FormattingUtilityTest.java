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
package org.eclipse.scout.rt.platform.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;
import java.util.Locale;

import org.eclipse.scout.rt.platform.nls.NlsLocale;
import org.eclipse.scout.rt.platform.util.date.DateUtility;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Tests for the {@link FormattingUtility}.
 */
public class FormattingUtilityTest {

  private static Locale deChLocale;
  private static Locale deDeLocale;
  private static Locale enUsLocale;

  @BeforeClass
  public static void beforeClass() {
    deChLocale = new Locale("de", "CH");
    deDeLocale = new Locale("de", "DE");
    enUsLocale = new Locale("en", "US");
  }

  @Test
  public void testFormatNull() {
    assertFormat(deChLocale, "", null);
  }

  @Test
  public void testFormatBoolean() {
    assertFormat(deChLocale, "X", Boolean.TRUE);
    assertFormat(deChLocale, "", Boolean.FALSE);
  }

  @Test
  public void testFormatDate() {
    Date date = DateUtility.parse("20.03.2012", "dd.MM.yyyy");
    assertFormat(deChLocale, "20.03.2012", date);
    assertFormat(enUsLocale, "Mar 20, 2012", date);
  }

  @Test
  public void testFormatDateTime() {
    Date dateTime = DateUtility.parse("20.03.2012 14:06:56", "dd.MM.yyyy hh:mm:ss");
    assertFormat(deChLocale, "20.03.12 14:06", dateTime);
    assertFormat(enUsLocale, "3/20/12 2:06 PM", dateTime);
  }

  @Test
  public void testFormatTime() {
    Date time = DateUtility.parse("14:06:56", "hh:mm:ss");
    assertFormat(deChLocale, "01.01.70 14:06", time);
    assertFormat(enUsLocale, "1/1/70 2:06 PM", time);
  }

  @Test
  public void testFormatInteger() {
    assertFormat(deChLocale, "100", Integer.valueOf(100));
    assertFormat(deChLocale, "1'000", Integer.valueOf(1000));
    assertFormat(deChLocale, "1'000'000", Integer.valueOf(1000000));
    assertFormat(deChLocale, "-1'000", Integer.valueOf(-1000));

    assertFormat(deDeLocale, "100", Integer.valueOf(100));
    assertFormat(deDeLocale, "1.000", Integer.valueOf(1000));
    assertFormat(deDeLocale, "1.000.000", Integer.valueOf(1000000));
    assertFormat(deDeLocale, "-1.000", Integer.valueOf(-1000));

    assertFormat(deChLocale, "100", Integer.valueOf(100));
    assertFormat(deChLocale, "1'000", Integer.valueOf(1000));
    assertFormat(deChLocale, "1'000'000", Integer.valueOf(1000000));
    assertFormat(deChLocale, "-1'000", Integer.valueOf(-1000));
  }

  @Test
  public void testFormatLong() {
    assertFormat(deChLocale, "100", Long.valueOf(100));
    assertFormat(deChLocale, "1'000", Long.valueOf(1000));
    assertFormat(deChLocale, "1'000'000", Long.valueOf(1000000));
    assertFormat(deChLocale, "-1'000", Long.valueOf(-1000));

    assertFormat(deDeLocale, "100", Long.valueOf(100));
    assertFormat(deDeLocale, "1.000", Long.valueOf(1000));
    assertFormat(deDeLocale, "1.000.000", Long.valueOf(1000000));
    assertFormat(deDeLocale, "-1.000", Long.valueOf(-1000));

    assertFormat(enUsLocale, "100", Long.valueOf(100));
    assertFormat(enUsLocale, "1,000", Long.valueOf(1000));
    assertFormat(enUsLocale, "1,000,000", Long.valueOf(1000000));
    assertFormat(enUsLocale, "-1,000", Long.valueOf(-1000));
  }

  @Test
  public void testFormatBigInteger() {
    assertFormat(deChLocale, "100", BigInteger.valueOf(100));
    assertFormat(deChLocale, "1'000", BigInteger.valueOf(1000));
    assertFormat(deChLocale, "1'000'000", BigInteger.valueOf(1000000));
    assertFormat(deChLocale, "-1'000", BigInteger.valueOf(-1000));

    assertFormat(deDeLocale, "100", BigInteger.valueOf(100));
    assertFormat(deDeLocale, "1.000", BigInteger.valueOf(1000));
    assertFormat(deDeLocale, "1.000.000", BigInteger.valueOf(1000000));
    assertFormat(deDeLocale, "-1.000", BigInteger.valueOf(-1000));

    assertFormat(enUsLocale, "100", BigInteger.valueOf(100));
    assertFormat(enUsLocale, "1,000", BigInteger.valueOf(1000));
    assertFormat(enUsLocale, "1,000,000", BigInteger.valueOf(1000000));
    assertFormat(enUsLocale, "-1,000", BigInteger.valueOf(-1000));
  }

  @Test
  public void testFormatFloat() {
    assertFormat(deChLocale, "0.00", Float.valueOf(0f));
    assertFormat(deChLocale, "0.25", Float.valueOf(0.25f));
    assertFormat(deChLocale, "0.25", Float.valueOf(0.24879f));
    assertFormat(deChLocale, "1'000.00", Float.valueOf(1000f));
    assertFormat(deChLocale, "-1'000.46", Float.valueOf(-1000.456789f));

    assertFormat(deDeLocale, "0,00", Float.valueOf(0f));
    assertFormat(deDeLocale, "0,25", Float.valueOf(0.25f));
    assertFormat(deDeLocale, "0,25", Float.valueOf(0.24879f));
    assertFormat(deDeLocale, "1.000,00", Float.valueOf(1000f));
    assertFormat(deDeLocale, "-1.000,46", Float.valueOf(-1000.456789f));

    assertFormat(enUsLocale, "0.00", Float.valueOf(0f));
    assertFormat(enUsLocale, "0.25", Float.valueOf(0.25f));
    assertFormat(enUsLocale, "0.25", Float.valueOf(0.24879f));
    assertFormat(enUsLocale, "1,000.00", Float.valueOf(1000f));
    assertFormat(enUsLocale, "-1,000.46", Float.valueOf(-1000.456789f));
  }

  @Test
  public void testFormatDouble() {
    assertFormat(deChLocale, "0.00", Double.valueOf(0));
    assertFormat(deChLocale, "0.25", Double.valueOf(0.25));
    assertFormat(deChLocale, "0.25", Double.valueOf(0.24879));
    assertFormat(deChLocale, "1'000.00", Double.valueOf(1000));
    assertFormat(deChLocale, "-1'000.46", Double.valueOf(-1000.456789));

    assertFormat(deDeLocale, "0,00", Double.valueOf(0));
    assertFormat(deDeLocale, "0,25", Double.valueOf(0.25));
    assertFormat(deDeLocale, "0,25", Double.valueOf(0.24879));
    assertFormat(deDeLocale, "1.000,00", Double.valueOf(1000));
    assertFormat(deDeLocale, "-1.000,46", Double.valueOf(-1000.456789));

    assertFormat(enUsLocale, "0.00", Double.valueOf(0));
    assertFormat(enUsLocale, "0.25", Double.valueOf(0.25));
    assertFormat(enUsLocale, "0.25", Double.valueOf(0.24879));
    assertFormat(enUsLocale, "1,000.00", Double.valueOf(1000));
    assertFormat(enUsLocale, "-1,000.46", Double.valueOf(-1000.456789));
  }

  @Test
  public void testFormatBigDecimal() {
    assertFormat(deChLocale, "0.00", BigDecimal.valueOf(0));
    assertFormat(deChLocale, "0.25", BigDecimal.valueOf(0.25));
    assertFormat(deChLocale, "0.25", BigDecimal.valueOf(0.24879));
    assertFormat(deChLocale, "1'000.00", BigDecimal.valueOf(1000));
    assertFormat(deChLocale, "-1'000.46", BigDecimal.valueOf(-1000.456789));

    assertFormat(deDeLocale, "0,00", BigDecimal.valueOf(0));
    assertFormat(deDeLocale, "0,25", BigDecimal.valueOf(0.25));
    assertFormat(deDeLocale, "0,25", BigDecimal.valueOf(0.24879));
    assertFormat(deDeLocale, "1.000,00", BigDecimal.valueOf(1000));
    assertFormat(deDeLocale, "-1.000,46", BigDecimal.valueOf(-1000.456789));

    assertFormat(enUsLocale, "0.00", BigDecimal.valueOf(0));
    assertFormat(enUsLocale, "0.25", BigDecimal.valueOf(0.25));
    assertFormat(enUsLocale, "0.25", BigDecimal.valueOf(0.24879));
    assertFormat(enUsLocale, "1,000.00", BigDecimal.valueOf(1000));
    assertFormat(enUsLocale, "-1,000.46", BigDecimal.valueOf(-1000.456789));
  }

  @Test
  public void testFormatString() {
    assertFormat(deChLocale, "\ttest 123  ", "\ttest 123  ");
    assertFormat(deDeLocale, "\ttest 123  ", "\ttest 123  ");
    assertFormat(enUsLocale, "\ttest 123  ", "\ttest 123  ");
  }

  private static void assertFormat(Locale locale, String expected, Object o) {
    Locale oldLocale = NlsLocale.get(false);
    try {
      if (locale == null) {
        fail("locale must not be null");
      }
      NlsLocale.set(locale);
      assertEquals(expected, FormattingUtility.formatObject(o));
    }
    finally {
      NlsLocale.set(oldLocale);
    }
  }
}
