/*******************************************************************************
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.platform.util;

import java.text.DateFormat;
import java.util.Locale;

import org.eclipse.scout.rt.platform.ApplicationScoped;

/**
 * Provider for {@link DateFormat} instances. Scout consistently uses this provider whenever a {@link DateFormat} is
 * needed.
 * <p>
 * This default implementation delegates to {@link DateFormat}'s static getters.
 *
 * @since 5.1
 */
@ApplicationScoped
public class DateFormatProvider {

  /**
   * delegates to {@link DateFormat#getAvailableLocales()}
   */
  public Locale[] getAvailableLocales() {
    return DateFormat.getAvailableLocales();
  }

  /**
   * delegates to {@link DateFormat#getTimeInstance(int, Locale)}
   */
  public DateFormat getTimeInstance(int style, Locale locale) {
    return DateFormat.getTimeInstance(style, locale);
  }

  /**
   * delegates to {@link DateFormat#getDateInstance(int, Locale)}
   */
  public DateFormat getDateInstance(int style, Locale locale) {
    return DateFormat.getDateInstance(style, locale);
  }

  /**
   * delegates to {@link DateFormat#getDateTimeInstance(int, int, Locale)}
   */
  public DateFormat getDateTimeInstance(int dateStyle, int timeStyle, Locale locale) {
    return DateFormat.getDateTimeInstance(dateStyle, timeStyle, locale);
  }

}
