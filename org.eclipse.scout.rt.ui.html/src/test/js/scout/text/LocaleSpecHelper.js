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
var LocaleSpecHelper = function() {
  this._initDecimalFormatSymbols();
  this._initDateFormatSymbols();
  this._initDateFormatDefaultPatterns();
};

LocaleSpecHelper.DEFAULT_LOCALE = 'de-CH';

LocaleSpecHelper.GERMAN_LOCALE = 'de-DE';

LocaleSpecHelper.prototype.createModel = function(languageTag) {
  var model = {};
  model.languageTag = languageTag;
  return model;
};

LocaleSpecHelper.prototype.createLocale = function(languageTag) {
  var model = this.createModel(languageTag);
  model.decimalFormatSymbols = this.decimalFormatSymbolsByLocale[languageTag];
  model.decimalFormatPatternDefault = "#,##0.###";
  model.dateFormatSymbols = this.dateFormatSymbolsByLocale[languageTag];
  model.dateFormatPatternDefault = this.dateFormatPatternByLocale[languageTag];
  return new scout.Locale(model);
};

LocaleSpecHelper.prototype._initDecimalFormatSymbols = function () {
  this.decimalFormatSymbolsByLocale = {};
  this.decimalFormatSymbolsByLocale[LocaleSpecHelper.DEFAULT_LOCALE] = this.createDecimalFormatSymbolsForDeCH();
  this.decimalFormatSymbolsByLocale[LocaleSpecHelper.GERMAN_LOCALE] = this.createDecimalFormatSymbolsForDeDE();
};

LocaleSpecHelper.prototype.createDecimalFormatSymbolsForDeCH = function() {
  return {
    "digit": "#",
    "zeroDigit": "0",
    "decimalSeparator": ".",
    "groupingSeparator": "'",
    "minusSign": "-",
    "patternSeparator": ";"
  };
};

LocaleSpecHelper.prototype.createDecimalFormatSymbolsForDeDE = function() {
  var symbols = this.createDecimalFormatSymbolsForDeCH();
  symbols.decimalSeparator = ",";
  symbols.groupingSeparator = ".";
  return symbols;
};

LocaleSpecHelper.prototype._initDateFormatSymbols = function() {
  var symbols = this.createDateFormatSymbolsForDe();
  this.dateFormatSymbolsByLocale = {};
  this.dateFormatSymbolsByLocale[LocaleSpecHelper.DEFAULT_LOCALE] = symbols;
  this.dateFormatSymbolsByLocale[LocaleSpecHelper.GERMAN_LOCALE] = symbols;
};

LocaleSpecHelper.prototype._initDateFormatDefaultPatterns = function() {
  this.dateFormatPatternByLocale = {};
  this.dateFormatPatternByLocale.de = "dd.MM.yyyy";
};

LocaleSpecHelper.prototype.createDateFormatSymbolsForDe = function() {
  return {
    "weekdays": ['Sonntag', 'Montag', 'Dienstag', 'Mittwoch', 'Donnerstag', 'Freitag', 'Samstag'],
    "weekdaysShort": ['So', 'Mo', 'Di', 'Mi', 'Do', 'Fr', 'Sa'],
    "months": ['Januar', 'Februar', 'März', 'April', 'Mai', 'Juni', 'Juli', 'August', 'September', 'Oktober', 'November', 'Dezember'],
    "monthsShort": ['Jan', 'Feb', 'Mrz', 'Apr', 'Mai', 'Jun', 'Jul', 'Aug', 'Sep', 'Okt', 'Nov', 'Dez'],
    "am": "AM",
    "pm": "PM"
  };
};
