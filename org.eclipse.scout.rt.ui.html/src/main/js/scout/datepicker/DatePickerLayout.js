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
scout.DatePickerLayout = function(picker) {
  scout.DatePickerLayout.parent.call(this);
  this.picker = picker;
};
scout.inherits(scout.DatePickerLayout, scout.AbstractLayout);

/**
 * @override
 */
scout.DatePickerLayout.prototype.preferredLayoutSize = function($container, options) {
  var headerSize = scout.graphics.getSize(this.picker._$header, true);
  var $tmpMonth = this.picker.$scrollable.appendDiv('date-picker-month-box')
    .appendDiv('date-picker-month');
  var weekdaySize = this._measureWeekdaySize($tmpMonth);
  var daySize = this._measureDaySize($tmpMonth);
  var monthHeight = daySize.height * 6;

  $tmpMonth.parent().remove();
  return new scout.Dimension(headerSize.width, headerSize.height + weekdaySize.height + monthHeight);
};

/**
 * Adds a temporary day DIV (without width, height, padding or margin) to the $month element,
 * measures the size and removes the DIV immediately after measurement.
 */
scout.DatePickerLayout.prototype._measureDaySize = function($month) {
  var $tmpDay = $month
      .appendDiv('date-picker-week')
      .appendDiv('date-picker-day')
      .text('Mo'), // because the string 30 is wider than 11
    size = scout.graphics.getSize($tmpDay, true);
  $tmpDay.remove();
  return size;
};

scout.DatePickerLayout.prototype._measureWeekdaySize = function($month) {
  var $tmpDay = $month
      .appendDiv('date-picker-weekdays')
      .appendDiv('date-picker-weekday')
      .text('Mo'),
    size = scout.graphics.getSize($tmpDay, true);
  $tmpDay.remove();
  return size;
};

