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
scout.Locale = function(model) {
  model = scout.nvl(model, scout.Locale.DEFAULT);

  this.languageTag = model.languageTag;
  var tags = scout.locales.splitLanguageTag(this.languageTag);
  this.language = tags[0];
  this.country = tags[1];
  this.displayLanguage = model.displayLanguage;
  this.displayCountry = model.displayCountry;

  this.decimalFormatPatternDefault = model.decimalFormatPatternDefault;
  this.decimalFormatSymbols = model.decimalFormatSymbols;

  if (this.decimalFormatPatternDefault && this.decimalFormatSymbols) {
    this.decimalFormat = new scout.DecimalFormat(model);
  }

  this.dateFormatPatternDefault = model.dateFormatPatternDefault;
  this.dateFormatSymbols = model.dateFormatSymbols;
  this.timeFormatPatternDefault = model.timeFormatPatternDefault;

  if (this.dateFormatPatternDefault && this.dateFormatSymbols) {
    this.dateFormat = new scout.DateFormat(model);
  }
};

scout.Locale.ensure = function(locale) {
  if (!locale) {
    return locale;
  }
  if (locale instanceof scout.Locale) {
    return locale;
  }
  return new scout.Locale(locale);
};

scout.Locale.DEFAULT = {
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
