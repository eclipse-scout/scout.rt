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
scout.TimePicker = function() {
  scout.TimePicker.parent.call(this);

  // Preselected date can only be set if selectedDate is null. The preselected date is rendered differently, but
  // has no function otherwise. (It is used to indicate the day that will be selected when the user presses
  // the UP or DOWN key while no date is selected.)
  this.preselectedTime;
  this.selectedTime;
  this.resolution;
  this.$scrollable;

  this.touch = scout.device.supportsTouch();
};
scout.inherits(scout.TimePicker, scout.Widget);

scout.TimePicker.prototype._init = function(options) {
  scout.TimePicker.parent.prototype._init.call(this, options);
  this.resolution = options.timeResolution;
};

scout.TimePicker.prototype._render = function() {
  this.$container = this.$parent
    .appendDiv('time-picker')
    .toggleClass('touch', this.touch);
  this.htmlComp = scout.HtmlComponent.install(this.$container, this.session);

  this._renderTimeSelection();
  this._installScrollbars();
};

scout.TimePicker.prototype._renderTimeSelection = function() {
  var i,
    date = scout.dates.trunc(new Date()),
    now = scout.dates.ceil(new Date(), this.resolution),
    currentHours = 0,
    $hourRow,
    $time;
  var $box = this.$parent.makeDiv('day-table');
  for (i = 0; i < 24; i++) {
    // reset minutes always every hour line starts with :00
    date.setMinutes(0);
    currentHours = date.getHours();

    $hourRow = $box.appendDiv('hour-row');
    $time = $hourRow.appendDiv('cell  hours')
      .data('time', new Date(date))
      .on('click', this._onTimeClick.bind(this));
    $time.appendSpan('text')
      .text(scout.dates.format(date, this.session.locale, 'HH'));

    while (currentHours === date.getHours()) {
      $time = $hourRow.appendDiv('cell minutes')
        .data('time', new Date(date))
        .on('click', this._onTimeClick.bind(this));

      $time.appendSpan('text').text(scout.dates.format(date, this.session.locale, ':mm'));
      if (scout.dates.isSameTime(now, date)) {
        $time.addClass('now');
      }
      date.setMinutes(date.getMinutes() + this.resolution);
    }
  }
  $box.appendTo(this.$container);
  return $box;
};

scout.TimePicker.prototype._installScrollbars = function() {
  scout.scrollbars.uninstall(this.$container, this.session);

  scout.scrollbars.install(this.$container, {
    parent: this,
    axis: 'y'
  });
};

scout.TimePicker.prototype.preselectTime = function(time) {
  if (time) {
    // Clear selection when a date is preselected
    this.setSelectedTime(null);
  }
  this.setPreselectedTime(time);
};

/**
 * @internal, use preselectDate to preselect a date
 */
scout.TimePicker.prototype.setPreselectedTime = function(preselectedTime) {
  this.setProperty('preselectedTime', preselectedTime);
};

scout.TimePicker.prototype._renderPreselectedTime = function() {
  var $scrollTo;
  this.$container.find('.cell').each(function(i, elem) {
    var $time = $(elem),
      time = $time.data('time');
    $time.removeClass('preselected');
    if (this.preselectedTime) {
      if ($time.hasClass('hours') && this.preselectedTime.getHours() === time.getHours()) {
        $time.addClass('preselected');
        $scrollTo = $time;
      } else if ($time.hasClass('minutes') && scout.dates.isSameTime(this.preselectedTime, time)) {
        $time.addClass('preselected');
        $scrollTo = $time;
      }
    }
  }.bind(this));
  if ($scrollTo) {
    scout.scrollbars.scrollTo(this.$container, $scrollTo, 'center');
  }
};

scout.TimePicker.prototype.selectTime = function(time) {
  if (time) {
    // Clear selection when a date is preselected
    this.setPreselectedTime(null);
  }
  this.setSelectedTime(time);
};

/**
 * @internal, use selectDate to select a date
 */
scout.TimePicker.prototype.setSelectedTime = function(selectedTime) {
  this.setProperty('selectedTime', selectedTime);
};

scout.TimePicker.prototype._renderSelectedTime = function() {

  var $scrollTo;
  this.$container.find('.cell').each(function(i, elem) {
    var $time = $(elem),
      time = $time.data('time');
    $time.removeClass('selected');
    if (this.selectedTime) {
      if ($time.hasClass('hours') && this.selectedTime.getHours() === time.getHours()) {
        $time.addClass('selected');
        $scrollTo = $time;
      } else if ($time.hasClass('minutes') && scout.dates.isSameTime(this.selectedTime, time)) {
        $time.addClass('selected');
        $scrollTo = $time;
      }
    }
  }.bind(this));
  if ($scrollTo) {
    scout.scrollbars.scrollTo(this.$container, $scrollTo, 'center');
  }
};

scout.TimePicker.prototype.shiftViewDate = function(years, months, days) {
  var date = this.viewDate;
  date = scout.dates.shift(date, years, months, days);
  this.showDate(date);
};

scout.TimePicker.prototype.shiftSelectedTime = function(hourUnits, minuteUnits, secondUnits) {
  var time = this.preselectedTime;
  if (this.selectedTime) {
      time = scout.dates.shiftTime(this.selectedTime, hourUnits, minuteUnits * this.resolution, secondUnits);
  }
  if (!time) {
    return; // do nothing when no date was found
  }
  this.selectTime(this._snapToTimeGrid(time));
};

scout.TimePicker.prototype._snapToTimeGrid = function(time){
  if(!time){
    return time;
  }
  var min = time.getMinutes();
  min = (parseInt(min  / this.resolution) * this.resolution);
  time.setMinutes(min);
  return time;


};

scout.TimePicker.prototype._findNextAllowedDate = function(years, months, days) {
  var i, date,
    sum = years + months + days,
    dir = sum > 0 ? 1 : -1,
    now = this.selectedDate || scout.dates.trunc(new Date());

  // if we shift by year or month, shift the 'now' date and then use that date as starting point
  // to find the next allowed date.
  if (years !== 0) {
    now = scout.dates.shift(now, years, 0, 0);
  } else if (months !== 0) {
    now = scout.dates.shift(now, 0, months, 0);
  }

  if (dir === 1) { // find next allowed date, starting from currently selected date
    for (i = 0; i < this.allowedDates.length; i++) {
      date = this.allowedDates[i];
      if (scout.dates.compare(now, date) < 0) {
        return date;
      }
    }
  } else if (dir === -1) { // find previous allowed date, starting from currently selected date
    for (i = this.allowedDates.length - 1; i >= 0; i--) {
      date = this.allowedDates[i];
      if (scout.dates.compare(now, date) > 0) {
        return date;
      }
    }
  }

  return null;
};

scout.TimePicker.prototype._isDateAllowed = function(date) {
  // when allowedDates is empty or not set, any date is allowed
  if (!this.allowedDates || this.allowedDates.length === 0) {
    return true;
  }
  // when allowedDates is set, only dates contained in this array are allowed
  var allowedDateAsTimestamp,
    dateAsTimestamp = date.getTime();
  return this.allowedDates.some(function(allowedDate) {
    allowedDateAsTimestamp = allowedDate.getTime();
    return allowedDateAsTimestamp === dateAsTimestamp;
  });
};

scout.TimePicker.prototype._onNavigationMouseDown = function(event) {
  var $target = $(event.currentTarget);
  var diff = $target.data('shift');
  this.shiftViewDate(0, diff, 0);
};

scout.TimePicker.prototype._onTimeClick = function(event) {
  var $target = $(event.currentTarget);
  var time = new Date($target.data('time'));
  this.selectTime(time);
  this.trigger('timeSelect', {
    time: time
  });
};

scout.TimePicker.prototype._onMouseWheel = function(event) {
  event = event.originalEvent || this.$container.window(true).event.originalEvent;
  var wheelData = event.wheelDelta ? event.wheelDelta / 10 : -event.detail * 3;
  var diff = (wheelData >= 0 ? -1 : 1);
  this.shiftViewDate(0, diff, 0);
  event.preventDefault();
};
