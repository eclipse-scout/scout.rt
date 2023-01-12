/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {DateFormatSymbols, DecimalFormatSymbols, Locale, LocaleModel} from '../../index';

export class LocaleSpecHelper {
  static DEFAULT_LOCALE = 'de-CH';
  static GERMAN_LOCALE = 'de-DE';
  static DEFAULT_DATE_FORMAT_PATTERN = 'dd.MM.yyyy';
  static DEFAULT_TIME_FORMAT_PATTERN = 'HH:mm';
  decimalFormatSymbolsByLocale: Record<string, DecimalFormatSymbols>;
  dateFormatSymbolsByLocale: Record<string, DateFormatSymbols>;
  dateFormatPatternByLocale: Record<string, string>;

  constructor() {
    this._initDecimalFormatSymbols();
    this._initDateFormatSymbols();
    this._initDateFormatDefaultPatterns();
  }

  createModel(languageTag: string): LocaleModel {
    return {
      languageTag: languageTag
    };
  }

  createLocale(languageTag: string): Locale {
    let model = this.createModel(languageTag);
    model.decimalFormatSymbols = this.decimalFormatSymbolsByLocale[languageTag];
    model.decimalFormatPatternDefault = '#,##0.###';
    model.dateFormatSymbols = this.dateFormatSymbolsByLocale[languageTag];
    model.dateFormatPatternDefault = this.dateFormatPatternByLocale[languageTag] || LocaleSpecHelper.DEFAULT_DATE_FORMAT_PATTERN;
    model.timeFormatPatternDefault = LocaleSpecHelper.DEFAULT_TIME_FORMAT_PATTERN;
    return new Locale(model);
  }

  protected _initDecimalFormatSymbols() {
    this.decimalFormatSymbolsByLocale = {};
    this.decimalFormatSymbolsByLocale[LocaleSpecHelper.DEFAULT_LOCALE] = this.createDecimalFormatSymbolsForDeCH();
    this.decimalFormatSymbolsByLocale[LocaleSpecHelper.GERMAN_LOCALE] = this.createDecimalFormatSymbolsForDeDE();
  }

  createDecimalFormatSymbolsForDeCH(): DecimalFormatSymbols {
    return {
      'decimalSeparator': '.',
      'groupingSeparator': '\'',
      'minusSign': '-'
    };
  }

  createDecimalFormatSymbolsForDeDE(): DecimalFormatSymbols {
    let symbols = this.createDecimalFormatSymbolsForDeCH();
    symbols.decimalSeparator = ',';
    symbols.groupingSeparator = '.';
    return symbols;
  }

  protected _initDateFormatSymbols() {
    let symbols = this.createDateFormatSymbolsForDe();
    this.dateFormatSymbolsByLocale = {};
    this.dateFormatSymbolsByLocale[LocaleSpecHelper.DEFAULT_LOCALE] = symbols;
    this.dateFormatSymbolsByLocale[LocaleSpecHelper.GERMAN_LOCALE] = symbols;
  }

  protected _initDateFormatDefaultPatterns() {
    this.dateFormatPatternByLocale = {};
    this.dateFormatPatternByLocale.de = 'dd.MM.yyyy';
  }

  createDateFormatSymbolsForDe(): DateFormatSymbols {
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
