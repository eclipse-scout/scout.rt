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
scout.DatePicker = function() {
  scout.DatePicker.parent.call(this);

  // Preselected date can only be set if selectedDate is null. The preselected date is rendered differently, but
  // has no function otherwise. (It is used to indicate the day that will be selected when the user presses
  // the UP or DOWN key while no date is selected.)
  this.preselectedDate;
  this.selectedDate;
  this.dateFormat;
  this.viewDate;
  this.allowedDates;
  this.$container;
  this.$currentBox;
  this.$scrollable;
  this._scrollableLeft;
};
scout.inherits(scout.DatePicker, scout.Widget);

scout.DatePicker.prototype._init = function(options) {
  scout.DatePicker.parent.prototype._init.call(this, options);
  options = options || {};
  this.dateFormat = options.dateFormat;
  this.allowedDates = options.allowedDates;
};

scout.DatePicker.prototype._render = function($parent) {
  this.$container = $parent
    .appendDiv('date-picker')
    .on('swipe', this._onSwipe.bind(this));

  this.htmlComp = new scout.HtmlComponent(this.$container, this.session);
  this.htmlComp.setLayout(new scout.DatePickerLayout(this));

  this._$header = this._append$Header();
  this._$header
    .find('.date-picker-left-y, .date-picker-left-m, .date-picker-right-m, .date-picker-right-y')
    .mousedown(this._onNavigationMouseDown.bind(this));

  this.$container.appendDiv('date-picker-separator');
  this.$scrollable = this.$container.appendDiv('date-picker-scrollable');
  this._scrollableTop = this.$scrollable.position().top;
  this._scrollableLeft = this.$scrollable.position().left;
  // Fix the position of the scrollable in order to do proper scrollable shifting (see _appendAnimated)
  this.$scrollable.css({
    'position': 'absolute',
    left: this._scrollableLeft,
    top: this._scrollableTop
  });
};

scout.DatePicker.prototype.preselectDate = function(date, animated) {
  this.preselectedDate = date;
  this.show(date, null, animated);
};

scout.DatePicker.prototype.selectDate = function(date, animated) {
  this.show(null, date, animated);
};

scout.DatePicker.prototype.show = function(viewDate, selectedDate, animated) {
  var viewDateDiff = 0;

  this.selectedDate = selectedDate;
  if (this.selectedDate) {
    // Clear preselection when a date is selected
    this.preselectedDate = null;
  }

  viewDate = viewDate || this.selectedDate || new Date();
  if (this.viewDate) {
    viewDateDiff = scout.dates.compareMonths(viewDate, this.viewDate);
  }
  this.viewDate = viewDate;

  this._updateHeader(viewDate);

  var $box = this._build$DateBox();
  $box[0].addEventListener('mousewheel', this._onMouseWheel.bind(this), false);

  if (animated && this.$currentBox && viewDateDiff) {
    this._appendAnimated(viewDateDiff, $box);
  } else {
    // Just replace the current month box (new day in the same month has been chosen)
    if (this.$currentBox) {
      this.$currentBox.remove();
    }
    $box.appendTo(this.$scrollable);
    this.htmlComp.revalidateLayout();

  }
  this.$currentBox = $box;
};

scout.DatePicker.prototype._appendAnimated = function(viewDateDiff, $box) {
  var $currentBox = this.$currentBox;
  var newLeft = 0,
    that = this;
  var monthBoxCount = this.$scrollable.find('.date-picker-month').length + 1;

  this.htmlComp.layout._layoutMonth($box);

  this._boxWidth = $box.width();
  var scrollableWidth = monthBoxCount * this._boxWidth;

  // Fix the size of the boxes
  $currentBox
    .width(this._boxWidth)
    .height(this._boxHeight);
  $box
    .width(this._boxWidth)
    .height(this._boxHeight);

  this.$scrollable.width(scrollableWidth);
  if (viewDateDiff > 0) {
    // New view date is larger -> shift left
    $box.appendTo(this.$scrollable);
    newLeft = this._scrollableLeft - (scrollableWidth - this._boxWidth);
  } else {
    // New view date is smaller -> shift right
    this.$scrollable.cssLeft(this._scrollableLeft - this._boxWidth);
    $box.prependTo(this.$scrollable);
    newLeft = this._scrollableLeft;
  }

  // Animate
  // At first: stop existing animation when shifting multiple dates in a row (e.g. with mouse wheel)
  this.$scrollable.
  stop(true).
  animate({
    left: newLeft
  }, 300, function() {
    // Remove every month box beside the new one
    // Its important to use that.$currentBox because $box may already be removed
    // if a new day in the current month has been chosen while the animation is in progress (e.g. by holding down key)
    that.$currentBox.siblings('.date-picker-month').remove();

    // Reset scrollable settings
    that.$scrollable
      .cssLeft(that._scrollableLeft)
      .width(that._boxWidth);
  });
};

scout.DatePicker.prototype._onNavigationMouseDown = function(event) {
  var $target = $(event.currentTarget);
  var diff = $target.data('shift');
  this.shiftViewDate(0, diff, 0);
};

scout.DatePicker.prototype._onDayClick = function(event) {
  var $target = $(event.currentTarget);
  var date = $target.data('date');
  this.trigger('dateSelect', {
    date: date
  });
};

scout.DatePicker.prototype._onSwipe = function(event) {
  var direction = event.swipestop.coords[0] - event.swipestart.coords[0] >= 0 ? -1 : 1;
  this.shiftViewDate(0, direction, 0);
};

scout.DatePicker.prototype._onMouseWheel = function(event) {
  event = event || this.$container.window(true).event;
  var wheelData = event.wheelDelta ? event.wheelDelta / 10 : -event.detail * 3;
  var diff = (wheelData >= 0 ? -1 : 1);
  this.shiftViewDate(0, diff, 0);
  event.preventDefault();
};

scout.DatePicker.prototype.shiftViewDate = function(years, months, days) {
  var date = this.viewDate;

  date = scout.dates.shift(date, years, months, days);
  this.show(date, null, true);
};

scout.DatePicker.prototype.shiftSelectedDate = function(years, months, days) {
  var date = this.preselectedDate;

  if (this.selectedDate) {
    if (this.allowedDates) {
      date = this._findNextAllowedDate(years, months, days);
    } else {
      date = scout.dates.shift(this.selectedDate, years, months, days);
    }
  }

  if (!date) {
    return; // do nothing when no date was found
  }

  this.trigger('dateSelect', {
    date: date,
    shifting: true
  });
  this.selectDate(date, true);
};

scout.DatePicker.prototype._findNextAllowedDate = function(years, months, days) {
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

scout.DatePicker.prototype._build$DateBox = function() {
  var cl, i, now = new Date();
  var day, dayEnabled, dayInMonth, $day;
  var weekdays = this.dateFormat.symbols.weekdaysShortOrdered;
  var start = new Date(this.viewDate);

  var $box = this.$container
    .makeDiv('date-picker-month')
    .data('viewDate', this.viewDate);

  // Create weekday header
  for (i in weekdays) {
    $box.appendDiv('date-picker-weekday', weekdays[i]);
  }

  // Find start date (-1)
  for (var offset = 0; offset < 42; offset++) {
    start.setDate(start.getDate() - 1);
    var diff = new Date(start.getYear(), this.viewDate.getMonth(), 0).getDate() - start.getDate();
    if ((start.getDay() === 0) && (start.getMonth() !== this.viewDate.getMonth()) && (diff > 1)) {
      break;
    }
  }

  // Create days
  for (i = 0; i < 42; i++) {
    start.setDate(start.getDate() + 1);
    dayInMonth = start.getDate();

    if ((start.getDay() === 6) || (start.getDay() === 0)) {
      cl = (start.getMonth() !== this.viewDate.getMonth() ? ' date-picker-out-weekend' : ' date-picker-weekend');
    } else {
      cl = (start.getMonth() !== this.viewDate.getMonth() ? ' date-picker-out' : '');
    }

    if (scout.dates.isSameDay(start, now)) {
      cl += ' date-picker-now';
    }

    if (scout.dates.isSameDay(start, this.preselectedDate)) {
      cl += ' date-picker-preselected';
    } else if (scout.dates.isSameDay(start, this.selectedDate)) {
      cl += ' date-picker-selected';
    }

    dayEnabled = this._isDateAllowed(start);
    if (!dayEnabled) {
      cl += ' date-picker-disabled';
    }

    day = (dayInMonth <= 9 ? '0' + dayInMonth : dayInMonth);
    $day = $box
      .appendDiv('date-picker-day' + cl, day)
      .data('dayInMonth', dayInMonth)
      .data('date', new Date(start));

    if (dayEnabled) {
      $day.on('click', this._onDayClick.bind(this));
    }
  }

  return $box;
};

scout.DatePicker.prototype._isDateAllowed = function(date) {
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

scout.DatePicker.prototype._append$Header = function() {
  var headerHtml =
    '<div class="date-picker-header">' +
    '  <div class="date-picker-left-y" data-shift="-12"></div>' +
    '  <div class="date-picker-left-m" data-shift="-1"></div>' +
    '  <div class="date-picker-right-y" data-shift="12"></div>' +
    '  <div class="date-picker-right-m" data-shift="1"></div>' +
    '  <div class="date-picker-header-month"></div>' +
    '</div>';
  return this.$container
    .appendElement(headerHtml)
    .toggleClass('touch', scout.device.supportsTouch());
};

scout.DatePicker.prototype._updateHeader = function(viewDate) {
  this._$header.find('.date-picker-header-month').text(this._createHeaderText(viewDate));
};

scout.DatePicker.prototype._createHeaderText = function(viewDate) {
  var months = this.dateFormat.symbols.months;
  return months[viewDate.getMonth()] + ' ' + viewDate.getFullYear();
};
