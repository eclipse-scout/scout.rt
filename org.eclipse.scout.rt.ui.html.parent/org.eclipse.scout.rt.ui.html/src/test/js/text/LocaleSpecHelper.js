var LocaleSpecHelper = function() {
  this._initDecimalFormatSymbols();
  this._initDateFormatSymbols();
  this._initDecimalFormatDefaultPatterns();
  this._initDateFormatDefaultPatterns();
};

LocaleSpecHelper.prototype.createModel = function(languageTag) {
  var model = {};
  model.languageTag = languageTag;
  return model;
};

LocaleSpecHelper.prototype.createLocale = function(languageTag) {
  var model = this.createModel(languageTag);
  model.decimalFormatSymbols = this.decimalFormatSymbolsByLocale[languageTag];
  model.decimalFormatPatternDefault = this.decimalFormatPatternByLocale[languageTag];
  model.dateFormatSymbols = this.dateFormatSymbolsByLocale[languageTag];
  model.dateFormatPatternDefault = this.dateFormatPatternByLocale[languageTag];
  return new scout.Locale(model);
};


LocaleSpecHelper.prototype._initDecimalFormatSymbols = function () {
  this.decimalFormatSymbolsByLocale = {};
  this.decimalFormatSymbolsByLocale.de_CH = this.createDecimalFormatSymbolsForDeCH();
  this.decimalFormatSymbolsByLocale.de_DE = this.createDecimalFormatSymbolsForDeDE();
};

LocaleSpecHelper.prototype._initDecimalFormatDefaultPatterns = function() {
  this.decimalFormatPatternByLocale = {};
  this.decimalFormatPatternByLocale.de_CH = "#'##0.###";
  this.decimalFormatPatternByLocale.de_DE = "#.##0,###";
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
  this.dateFormatSymbolsByLocale = {};
  this.dateFormatSymbolsByLocale.de = this.createDateFormatSymbolsForDe();
};

LocaleSpecHelper.prototype._initDateFormatDefaultPatterns = function() {
  this.dateFormatPatternByLocale = {};
  this.dateFormatPatternByLocale.de = "dd.MM.yyyy";
};

LocaleSpecHelper.prototype.createDateFormatSymbolsForDe = function() {
  return {
    "weekdays": ['Sonntag', 'Montag', 'Dienstag', 'Mittwoch', 'Donnerstag', 'Freitag', 'Samstag'],
    "weekdaysShort": ['So', 'Mo', 'Di', 'Mi', 'Do', 'Fr', 'Sa'],
    "months": ['Januar', 'Februar', 'März', 'April', 'Mai', 'Juni', 'Juli', 'August', 'September', 'Oktober', 'November', 'Dezember', ''],
    "monthsShort": ['Jan', 'Feb', 'Mrz', 'Apr', 'Mai', 'Jun', 'Jul', 'Aug', 'Sep', 'Okt', 'Nov', 'Dez', ''],
    "am": "AM",
    "pm": "PM"
  };
};
