/*******************************************************************************
 * Copyright (c) 2014-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
scout.LocaleSpecHelper = function() {
  this._initDecimalFormatSymbols();
  this._initDateFormatSymbols();
  this._initDateFormatDefaultPatterns();
};

scout.LocaleSpecHelper.DEFAULT_LOCALE = 'de-CH';
scout.LocaleSpecHelper.GERMAN_LOCALE = 'de-DE';
scout.LocaleSpecHelper.DEFAULT_DATE_FORMAT_PATTERN = 'dd.MM.yyyy';
scout.LocaleSpecHelper.DEFAULT_TIME_FORMAT_PATTERN = 'HH:mm';

scout.LocaleSpecHelper.prototype.createModel = function(languageTag) {
  var model = {};
  model.languageTag = languageTag;
  return model;
};

scout.LocaleSpecHelper.prototype.createLocale = function(languageTag) {
  var model = this.createModel(languageTag);
  model.decimalFormatSymbols = this.decimalFormatSymbolsByLocale[languageTag];
  model.decimalFormatPatternDefault = "#,##0.###";
  model.dateFormatSymbols = this.dateFormatSymbolsByLocale[languageTag];
  model.dateFormatPatternDefault = this.dateFormatPatternByLocale[languageTag] || scout.LocaleSpecHelper.DEFAULT_DATE_FORMAT_PATTERN;
  model.timeFormatPatternDefault = scout.LocaleSpecHelper.DEFAULT_TIME_FORMAT_PATTERN;
  return new scout.Locale(model);
};

scout.LocaleSpecHelper.prototype._initDecimalFormatSymbols = function () {
  this.decimalFormatSymbolsByLocale = {};
  this.decimalFormatSymbolsByLocale[scout.LocaleSpecHelper.DEFAULT_LOCALE] = this.createDecimalFormatSymbolsForDeCH();
  this.decimalFormatSymbolsByLocale[scout.LocaleSpecHelper.GERMAN_LOCALE] = this.createDecimalFormatSymbolsForDeDE();
};

scout.LocaleSpecHelper.prototype.createDecimalFormatSymbolsForDeCH = function() {
  return {
    "digit": "#",
    "zeroDigit": "0",
    "decimalSeparator": ".",
    "groupingSeparator": "'",
    "minusSign": "-",
    "patternSeparator": ";"
  };
};

scout.LocaleSpecHelper.prototype.createDecimalFormatSymbolsForDeDE = function() {
  var symbols = this.createDecimalFormatSymbolsForDeCH();
  symbols.decimalSeparator = ",";
  symbols.groupingSeparator = ".";
  return symbols;
};

scout.LocaleSpecHelper.prototype._initDateFormatSymbols = function() {
  var symbols = this.createDateFormatSymbolsForDe();
  this.dateFormatSymbolsByLocale = {};
  this.dateFormatSymbolsByLocale[scout.LocaleSpecHelper.DEFAULT_LOCALE] = symbols;
  this.dateFormatSymbolsByLocale[scout.LocaleSpecHelper.GERMAN_LOCALE] = symbols;
};

scout.LocaleSpecHelper.prototype._initDateFormatDefaultPatterns = function() {
  this.dateFormatPatternByLocale = {};
  this.dateFormatPatternByLocale.de = "dd.MM.yyyy";
};

scout.LocaleSpecHelper.prototype.createDateFormatSymbolsForDe = function() {
  return {
    "weekdays": ['Sonntag', 'Montag', 'Dienstag', 'Mittwoch', 'Donnerstag', 'Freitag', 'Samstag'],
    "weekdaysShort": ['So', 'Mo', 'Di', 'Mi', 'Do', 'Fr', 'Sa'],
    "months": ['Januar', 'Februar', 'März', 'April', 'Mai', 'Juni', 'Juli', 'August', 'September', 'Oktober', 'November', 'Dezember'],
    "monthsShort": ['Jan', 'Feb', 'Mrz', 'Apr', 'Mai', 'Jun', 'Jul', 'Aug', 'Sep', 'Okt', 'Nov', 'Dez'],
    "am": "AM",
    "pm": "PM"
  };
};
