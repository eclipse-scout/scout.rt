/*
 * Copyright (c) 2014-2019 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {Locale} from '@eclipse-scout/core';

export default class LocaleSpecHelper {
  static DEFAULT_LOCALE = 'de-CH';
  static GERMAN_LOCALE = 'de-DE';
  static DEFAULT_DATE_FORMAT_PATTERN = 'dd.MM.yyyy';
  static DEFAULT_TIME_FORMAT_PATTERN = 'HH:mm';

  constructor() {
    this._initDecimalFormatSymbols();
    this._initDateFormatSymbols();
    this._initDateFormatDefaultPatterns();
  }


  createModel(languageTag) {
    var model = {};
    model.languageTag = languageTag;
    return model;
  }

  createLocale(languageTag) {
    var model = this.createModel(languageTag);
    model.decimalFormatSymbols = this.decimalFormatSymbolsByLocale[languageTag];
    model.decimalFormatPatternDefault = '#,##0.###';
    model.dateFormatSymbols = this.dateFormatSymbolsByLocale[languageTag];
    model.dateFormatPatternDefault = this.dateFormatPatternByLocale[languageTag] || LocaleSpecHelper.DEFAULT_DATE_FORMAT_PATTERN;
    model.timeFormatPatternDefault = LocaleSpecHelper.DEFAULT_TIME_FORMAT_PATTERN;
    return new Locale(model);
  }

  _initDecimalFormatSymbols() {
    this.decimalFormatSymbolsByLocale = {};
    this.decimalFormatSymbolsByLocale[LocaleSpecHelper.DEFAULT_LOCALE] = this.createDecimalFormatSymbolsForDeCH();
    this.decimalFormatSymbolsByLocale[LocaleSpecHelper.GERMAN_LOCALE] = this.createDecimalFormatSymbolsForDeDE();
  }

  createDecimalFormatSymbolsForDeCH() {
    return {
      'digit': '#',
      'zeroDigit': '0',
      'decimalSeparator': '.',
      'groupingSeparator': '\'',
      'minusSign': '-',
      'patternSeparator': ';'
    };
  }

  createDecimalFormatSymbolsForDeDE() {
    var symbols = this.createDecimalFormatSymbolsForDeCH();
    symbols.decimalSeparator = ',';
    symbols.groupingSeparator = '.';
    return symbols;
  }

  _initDateFormatSymbols() {
    var symbols = this.createDateFormatSymbolsForDe();
    this.dateFormatSymbolsByLocale = {};
    this.dateFormatSymbolsByLocale[LocaleSpecHelper.DEFAULT_LOCALE] = symbols;
    this.dateFormatSymbolsByLocale[LocaleSpecHelper.GERMAN_LOCALE] = symbols;
  }

  _initDateFormatDefaultPatterns() {
    this.dateFormatPatternByLocale = {};
    this.dateFormatPatternByLocale.de = 'dd.MM.yyyy';
  }

  createDateFormatSymbolsForDe() {
    return {
      'weekdays': ['Sonntag', 'Montag', 'Dienstag', 'Mittwoch', 'Donnerstag', 'Freitag', 'Samstag'],
      'weekdaysShort': ['So', 'Mo', 'Di', 'Mi', 'Do', 'Fr', 'Sa'],
      'months': ['Januar', 'Februar', 'März', 'April', 'Mai', 'Juni', 'Juli', 'August', 'September', 'Oktober', 'November', 'Dezember'],
      'monthsShort': ['Jan', 'Feb', 'Mrz', 'Apr', 'Mai', 'Jun', 'Jul', 'Aug', 'Sep', 'Okt', 'Nov', 'Dez'],
      'am': 'AM',
      'pm': 'PM'
    };
  }
}
