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
import {DateFormat, DecimalFormat, locales, scout} from '../index';
import LocaleModel, {DateFormatSymbols, DecimalFormatSymbols} from './LocaleModel';

export default class Locale {
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

  constructor(model?: LocaleModel) {
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

  static ensure(locale: any): Locale {
    if (!locale) {
      return locale;
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
