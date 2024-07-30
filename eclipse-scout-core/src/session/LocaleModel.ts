/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
export interface LocaleModel {
  /**
   * IETF BCP 47 language tag, e.g. `"en-US"` or `"de-CH"`.
   * @see https://en.wikipedia.org/wiki/IETF_language_tag
   */
  languageTag?: string;
  decimalFormatPatternDefault?: string;
  dateFormatPatternDefault?: string;
  timeFormatPatternDefault?: string;
  /**
   * A formatted display text for the language part of the {@link languageTag}. This value is optional.
   */
  displayLanguage?: string;
  /**
   * A formatted display text for the country part of the {@link languageTag}. This value is optional.
   */
  displayCountry?: string;
  decimalFormatSymbols?: DecimalFormatSymbols;
  dateFormatSymbols?: DateFormatSymbols;
}

export interface DecimalFormatSymbols {
  decimalSeparator: string;
  groupingSeparator: string;
  minusSign: string;
}

export interface DateFormatSymbols {
  /**
   * Specifies which day of the week is considered to be the start of the week.
   *
   * - 0 = Sunday
   * - 1 = Monday
   * - 2 = Tuesday
   * - etc.
   */
  firstDayOfWeek?: number;

  /**
   * The localized names of the months ("January", "February" etc.), starting with January.
   */
  months: string[];
  /**
   * The localized short names of the months ("Jan", "Feb" etc.), starting with January.
   */
  monthsShort: string[];

  /**
   * The localized names the days of the week ("Monday", "Tuesday" etc.), starting with Sunday.
   * Note that the list starts at index 0, not at 1 like in Java!
   */
  weekdays: string[];
  /**
   * The same list as {@link weekdays}, but ordered so that it starts with the first day of the week according to {@link firstDayOfWeek}.
   * @see dates#orderWeekdays
   */
  weekdaysOrdered?: string[];

  /**
   * The localized short names the days of the week ("Mon", "Tue" etc.), starting with Sunday.
   * Note that the list starts at index 0, not at 1 like in Java!
   */
  weekdaysShort: string[];
  /**
   * The same list as {@link weekdaysShort}, but ordered so that it starts with the first day of the week according to {@link firstDayOfWeek}.
   * @see dates#orderWeekdays
   */
  weekdaysShortOrdered?: string[];

  /**
   * The text to indicate times before noon, e.g. "AM"
   */
  am: string;
  /**
   * The text to indicate times after noon, e.g. "PM"
   */
  pm: string;
}
