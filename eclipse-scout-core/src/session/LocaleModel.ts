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
  firstDayOfWeek?: number;
  months: string[];
  monthsShort: string[];

  weekdays: string[];
  weekdaysOrdered?: string[];

  weekdaysShort: string[];
  weekdaysShortOrdered?: string[];

  am: string;
  pm: string;
}
