/*
 * Copyright (c) 2010-2022 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.testing.shared;

import java.lang.ref.WeakReference;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.util.NumberFormatProvider;
import org.eclipse.scout.rt.platform.util.SleepUtil;
import org.junit.Assert;

public final class TestingUtility {

  private TestingUtility() {
  }

  /**
   * convenience overload for
   * {@link #createLocaleSpecificNumberString(Locale, boolean, String, String, NumberStringPercentSuffix)} with
   * <code>percent=0</code>
   */
  public static String createLocaleSpecificNumberString(Locale loc, boolean minus, String integerPart, String fractionPart) {
    return createLocaleSpecificNumberString(loc, minus, integerPart, fractionPart, NumberStringPercentSuffix.NONE);
  }

  /**
   * convenience overload for
   * {@link #createLocaleSpecificNumberString(Locale, boolean, String, String, NumberStringPercentSuffix)} with
   * <code>fractionPart=null</code> and <code>percent=0</code>
   */
  public static String createLocaleSpecificNumberString(Locale loc, boolean minus, String integerPart) {
    return createLocaleSpecificNumberString(loc, minus, integerPart, null, NumberStringPercentSuffix.NONE);
  }

  public enum NumberStringPercentSuffix {
    /**
     * ""
     */
    NONE {
      @Override
      public String getSuffix(DecimalFormatSymbols symbols) {
        return "";
      }
    },
    /**
     * "%"
     */
    JUST_SYMBOL {
      @Override
      public String getSuffix(DecimalFormatSymbols symbols) {
        return String.valueOf(symbols.getPercent());
      }
    },
    /**
     * " %'
     */
    BLANK_AND_SYMBOL {
      @Override
      public String getSuffix(DecimalFormatSymbols symbols) {
        return " " + symbols.getPercent();
      }
    };

    public abstract String getSuffix(DecimalFormatSymbols symbols);
  }

  /**
   * Create a string representing a number using locale specific minus, decimalSeparator and percent symbols
   */
  public static String createLocaleSpecificNumberString(Locale loc, boolean minus, String integerPart, String fractionPart, NumberStringPercentSuffix percentSuffix) {
    DecimalFormat df = BEANS.get(NumberFormatProvider.class).getPercentInstance(loc);
    DecimalFormatSymbols symbols = df.getDecimalFormatSymbols();
    StringBuilder sb = new StringBuilder();
    if (minus) {
      sb.append(df.getNegativePrefix().replace("%", ""));
    }
    sb.append(integerPart);
    if (fractionPart != null) {
      sb.append(symbols.getDecimalSeparator()).append(fractionPart);
    }
    String suffix = percentSuffix.getSuffix(symbols);

    // special case for some magic Arabic locales in which the percent sign is an invisible character that cannot be displayed
    byte[] suffixBytes = suffix.getBytes(java.nio.charset.StandardCharsets.UTF_16BE);
    if (suffixBytes.length == 2 && suffixBytes[0] == 32 && suffixBytes[1] == 14) {
      return sb.append('%').toString();
    }

    return sb.append(suffix).toString();
  }

  /**
   * Invokes the GC several times and verifies that the object referenced by the weak reference was garbage collected.
   */
  @SuppressWarnings("squid:S1215")
  public static void assertGC(WeakReference<?> ref) {
    int maxRuns = 50;
    for (int i = 0; i < maxRuns; i++) {
      if (ref.get() == null) {
        return;
      }
      System.gc();

      SleepUtil.sleepSafe(50, TimeUnit.MILLISECONDS);
    }
    Assert.fail("Potential memory leak, object " + ref.get() + "still exists after gc");
  }
}
