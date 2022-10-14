/*
 * Copyright (c) 2010-2022 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
export default interface LocaleModel {
  languageTag: string;
  decimalFormatPatternDefault?: string;
  dateFormatPatternDefault?: string;
  timeFormatPatternDefault?: string;
  displayLanguage?: string;
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
