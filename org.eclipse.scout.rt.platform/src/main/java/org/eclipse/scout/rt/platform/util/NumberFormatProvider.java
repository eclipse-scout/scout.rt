/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.platform.util;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

import org.eclipse.scout.rt.platform.ApplicationScoped;

/**
 * Provider for {@link NumberFormat} instances. Scout consistently uses this provider whenever a {@link NumberFormat} is
 * needed.
 * <p>
 * This default implementation delegates to {@link NumberFormat}'s static getters.
 *
 * @since 5.1
 */
@ApplicationScoped
public class NumberFormatProvider {
  /**
   * delegates to {@link NumberFormat#getAvailableLocales()}
   */
  public Locale[] getAvailableLocales() {
    return NumberFormat.getAvailableLocales();
  }

  /**
   * delegates to {@link NumberFormat#getCurrencyInstance(Locale)}
   */
  public DecimalFormat getCurrencyInstance(Locale locale) {
    return (DecimalFormat) NumberFormat.getCurrencyInstance(locale);
  }

  /**
   * delegates to {@link NumberFormat#getIntegerInstance(Locale)}
   */
  public NumberFormat getIntegerInstance(Locale locale) {
    return NumberFormat.getIntegerInstance(locale);
  }

  /**
   * delegates to {@link NumberFormat#getNumberInstance(Locale)}
   */
  public DecimalFormat getNumberInstance(Locale locale) {
    return (DecimalFormat) NumberFormat.getNumberInstance(locale);
  }

  /**
   * delegates to {@link NumberFormat#getPercentInstance(Locale)}
   */
  public DecimalFormat getPercentInstance(Locale locale) {
    return (DecimalFormat) NumberFormat.getPercentInstance(locale);
  }
}
