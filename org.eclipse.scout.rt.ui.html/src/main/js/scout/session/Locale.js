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
  this.decimalFormatPatternDefault = model.decimalFormatPatternDefault;
  this.decimalFormatSymbols = model.decimalFormatSymbols;
  this.timeFormatPatternDefault = model.timeFormatPatternDefault;

  if (this.decimalFormatPatternDefault && this.decimalFormatSymbols) {
    this.decimalFormat = new scout.DecimalFormat(model);
  }

  this.dateFormatPatternDefault = model.dateFormatPatternDefault;
  this.dateFormatSymbols = model.dateFormatSymbols;

  if (this.dateFormatPatternDefault && this.dateFormatSymbols) {
    this.dateFormat = new scout.DateFormat(model);
  }
};

scout.Locale.DEFAULT = {
  languageTag: 'en-US',
  decimalFormatPatternDefault: '#,##0.###',
  dateFormatPatternDefault: 'dd.MM.yyyy',
  timeFormatPatternDefault: 'HH:mm',
  decimalFormatSymbols: {
    decimalSeparator: '.',
    groupingSeparator: '\'',
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
      'June',
      'July',
      'Aug',
      'Sept',
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
      'Su',
      'Mo',
      'Tu',
      'We',
      'Th',
      'Fr',
      'Sa'
    ],
    am: 'AM',
    pm: 'PM'
  }
};