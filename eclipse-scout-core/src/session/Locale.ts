/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {DateFormat, DateFormatSymbols, DecimalFormat, DecimalFormatSymbols, InitModelOf, LocaleModel, locales, scout} from '../index';

export class Locale implements LocaleModel {
  declare model: LocaleModel;

  languageTag: string;
  language: string;
  country: string;
  displayLanguage: string;
  displayCountry: string;
  decimalFormatPatternDefault: string;
  decimalFormatSymbols: DecimalFormatSymbols;
  decimalFormat: DecimalFormat;
  dateFormatPatternDefault: string;
  dateFormatSymbols: DateFormatSymbols;
  timeFormatPatternDefault: string;
  dateFormat: DateFormat;

  constructor(model?: InitModelOf<Locale>) {
    model = scout.nvl(model, Locale.DEFAULT);

    this.languageTag = model.languageTag;
    let tags = locales.splitLanguageTag(this.languageTag);
    this.language = tags[0];
    this.country = tags[1];
    this.displayLanguage = model.displayLanguage;
    this.displayCountry = model.displayCountry;

    this.decimalFormatPatternDefault = model.decimalFormatPatternDefault;
    this.decimalFormatSymbols = model.decimalFormatSymbols;

    if (this.decimalFormatPatternDefault && this.decimalFormatSymbols) {
      this.decimalFormat = new DecimalFormat(this, this.decimalFormatPatternDefault);
    }

    this.dateFormatPatternDefault = model.dateFormatPatternDefault;
    this.dateFormatSymbols = model.dateFormatSymbols;
    this.timeFormatPatternDefault = model.timeFormatPatternDefault;

    if (this.dateFormatPatternDefault && this.dateFormatSymbols) {
      this.dateFormat = new DateFormat(this, this.dateFormatPatternDefault);
    }
  }

  static ensure(locale?: Locale | InitModelOf<Locale>): Locale {
    if (!locale) {
      return locale as Locale;
    }
    if (locale instanceof Locale) {
      return locale;
    }
    return new Locale(locale);
  }

  static DEFAULT: LocaleModel = {
    languageTag: 'en-US',
    decimalFormatPatternDefault: '#,##0.###',
    dateFormatPatternDefault: 'MM/dd/yyyy',
    timeFormatPatternDefault: 'h:mm a',
    decimalFormatSymbols: {
      decimalSeparator: '.',
      groupingSeparator: ',',
      minusSign: '-'
    },
    dateFormatSymbols: {
      months: [
        'January',
        'February',
        'March',
        'April',
        'May',
        'June',
        'July',
        'August',
        'September',
        'October',
        'November',
        'December'
      ],
      monthsShort: [
        'Jan',
        'Feb',
        'Mar',
        'Apr',
        'May',
        'Jun',
        'Jul',
        'Aug',
        'Sep',
        'Oct',
        'Nov',
        'Dec'
      ],
      weekdays: [
        'Sunday',
        'Monday',
        'Tuesday',
        'Wednesday',
        'Thursday',
        'Friday',
        'Saturday'
      ],
      weekdaysShort: [
        'Sun',
        'Mon',
        'Tue',
        'Wed',
        'Thu',
        'Fri',
        'Sat'
      ],
      am: 'AM',
      pm: 'PM'
    }
  };
}
