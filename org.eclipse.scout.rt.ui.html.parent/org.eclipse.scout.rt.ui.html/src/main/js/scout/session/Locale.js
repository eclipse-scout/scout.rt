scout.Locale = function(model) {
  this.languageTag = model.languageTag;
  this.decimalFormatPatternDefault = model.decimalFormatPatternDefault;
  this.decimalFormatSymbols = model.decimalFormatSymbols;

  if (this.decimalFormatPatternDefault && this.decimalFormatSymbols) {
    this.decimalFormat = new scout.DecimalFormat(model);
  }

  this.dateFormatPatternDefault = model.dateFormatPatternDefault;
  this.dateFormatSymbols = model.dateFormatSymbols;

  if (this.dateFormatPatternDefault && this.dateFormatSymbols) {
    this.dateFormat = new scout.DateFormat(model);
  }
};
