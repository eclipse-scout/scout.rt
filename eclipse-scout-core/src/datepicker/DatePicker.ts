/*
 * Copyright (c) 2010-2021 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {arrays, DateFormat, dates, Device, events, graphics, HtmlComponent, objects, scout, Widget} from '../index';
import $ from 'jquery';

export default class DatePicker extends Widget {

  constructor() {
    super();

    // Preselected date can only be set if selectedDate is null. The preselected date is rendered differently, but
    // has no function otherwise. (It is used to indicate the day that will be selected when the user presses
    // the UP or DOWN key while no date is selected.)
    this.preselectedDate = null;
    this.selectedDate = null;
    this.dateFormat = null;
    this.viewDate = null;
    this.allowedDates = [];
    this.currentMonth = null;
    this.$scrollable = null;
    // Contains the months to be rendered.
    // Only the this.currentMonth is visible, the others are needed for the swipe animation.
    // The month is an object with the properties viewDate, rendered and $container
    this.months = [];
    this._showWeekendSeparator = true;
  }

  _init(options) {
    super._init(options);
    this._setDateFormat(this.dateFormat);
    this._showWeekendSeparator = this.dateFormat.symbols.firstDayOfWeek === 1; // only add the separator if the weeks starts on Monday
  }

  _render() {
    this.$container = this.$parent
      .appendDiv('date-picker')
      .toggleClass('touch-only', Device.get().supportsOnlyTouch());
    this.htmlComp = HtmlComponent.install(this.$container, this.session);

    this._$header = this._append$Header();
    this._$header
      .find('.date-picker-left-y, .date-picker-left-m, .date-picker-right-m, .date-picker-right-y')
      .mousedown(this._onNavigationMouseDown.bind(this));

    this.$container.appendDiv('date-picker-separator');
    this.$scrollable = this.$container.appendDiv('date-picker-scrollable');
    this._registerSwipeHandlers();
  }

  _setDateFormat(dateFormat) {
    if (!dateFormat) {
      dateFormat = this.session.locale.dateFormatPatternDefault;
    }
    dateFormat = DateFormat.ensure(this.session.locale, dateFormat);
    this._setProperty('dateFormat', dateFormat);
  }

  prependMonth(month) {
    let months = this.months.slice();
    arrays.insert(months, month, 0);
    this.setMonths(months);
  }

  appendMonth(month) {
    let months = this.months.slice();
    months.push(month);
    this.setMonths(months);
  }

  /**
   * Resets the month boxes. Always render 3 months to make swiping more smooth (especially on mobile devices).
   */
  resetMonths(viewDate) {
    viewDate = viewDate || this.viewDate;
    let prevDate = dates.shift(viewDate, 0, -1, 0);
    let nextDate = dates.shift(viewDate, 0, 1, 0);
    this.setMonths([prevDate, viewDate, nextDate]);
  }

  setMonths(months) {
    months = arrays.ensure(months);
    months = months.map(function(month) {
      let viewDate = month;
      if (!(month instanceof Date)) {
        viewDate = month.viewDate;
      }
      // Use existing month object (so that $container won't be removed, see below)
      let existingMonth = this._findMonthByViewDate(viewDate);
      if (existingMonth) {
        return existingMonth;
      }
      return {
        rendered: false,
        viewDate: viewDate,
        $container: undefined
      };
    }, this);

    // Remove the obsolete months
    if (this.rendered) {
      this.months.forEach(month => {
        if (months.indexOf(month) < 0 && month.rendered) {
          month.$container.remove();
        }
      }, this);
    }
    this.setProperty('months', months);
  }

  _renderMonths() {
    // Render the months if needed
    this.months.forEach(function(month) {
      if (month.rendered) {
        this._layoutWeekendSeparator(month);
      } else {
        this._renderMonth(month);

        // move month to correct position in DOM.
        // Current month must not be moved, otherwise click event gets lost.
        if (this.currentMonth && dates.compare(month.viewDate, this.currentMonth.viewDate) < 0) {
          month.$container.insertBefore(this.currentMonth.$container);
        }
      }
    }, this);

    // Adjust size and position of the scrollable
    let scrollableWidth = this.months.length * this._boxWidth;
    this.$scrollable.width(scrollableWidth);
    if (this.currentMonth) {
      this.$scrollable.cssLeft(this._scrollableLeftForMonth(this.currentMonth));
    }
  }

  _findMonthByViewDate(viewDate) {
    return arrays.find(this.months, month => {
      return dates.compareMonths(month.viewDate, viewDate) === 0;
    });
  }

  /**
   * @returns {number} the x coordinate of the scrollable if the given month should be displayed
   */
  _scrollableLeftForMonth(month) {
    let scrollableInsets = graphics.insets(this.$scrollable);
    let monthMargins = graphics.margins(month.$container);
    return -1 * (month.$container.position().left - monthMargins.left - scrollableInsets.left);
  }

  _renderMonth(month) {
    if (month.rendered) {
      return;
    }

    let $box = this.$parent.makeDiv('date-picker-month-box');
    if (this._showWeekendSeparator) {
      month.$weekendSeparator = $box.appendDiv('date-picker-weekend-separator');
    }
    this._build$DateBox(month.viewDate).appendTo($box);
    $box.on('DOMMouseScroll mousewheel', this._onMouseWheel.bind(this))
      .appendTo(this.$scrollable);

    // Fix the size of the box
    if (!this._boxWidth) {
      this._boxWidth = $box.width();
    }
    $box.width(this._boxWidth);

    month.$container = $box;
    month.rendered = true;
  }

  /**
   * @internal, use showDate, selectDate, shiftViewDate, etc. to change the view date
   */
  setViewDate(viewDate, animated) {
    if (objects.equals(this.viewDate, viewDate)) {
      return;
    }
    this._setProperty('viewDate', viewDate);
    if (this.rendered) {
      this._renderViewDate(animated);
    }
  }

  _renderViewDate(animated) {
    let month = this._findMonthByViewDate(this.viewDate);
    let newLeft = this._scrollableLeftForMonth(month);
    if (!this.currentMonth) {
      // The first time a month is rendered, revalidate the layout.
      // Reason: When the popup opens, the datepicker is not rendered yet, thus the preferred size cannot be determined
      this.revalidateLayoutTree();
    }
    this.currentMonth = month;
    this._updateHeader(this.viewDate);

    animated = scout.nvl(animated, true);
    if (!animated) {
      this.$scrollable.cssLeft(newLeft);
      this.resetMonths();
    } else {
      // Animate
      // At first: stop existing animation when shifting multiple dates in a row (e.g. with mouse wheel)
      this.$scrollable
        .stop(true)
        .animate({
          left: newLeft
        }, 300, () => {
          this.resetMonths();
        });
    }
  }

  _layoutWeekendSeparators() {
    this.months.forEach(m => this._layoutWeekendSeparator(m));
  }

  _layoutWeekendSeparator(month) {
    if (!month.$weekendSeparator) {
      return;
    }
    let $weekdays = this.$container.find('.date-picker-weekdays').first().children();
    if ($weekdays.length < 6) {
      return;
    }

    let $friday = $weekdays.eq(4); // Friday is always pos 4 as the separator is only available if the weeks starts on Monday
    let $saturday = $weekdays.eq(5); // Saturday is always pos 5 as the separator is only available if the weeks starts on Monday

    let fridayTopRight = graphics.position($friday).x + graphics.size($friday, {includeMargin: true}).width;
    let saturdayTopLeft = graphics.position($saturday).x;
    let space = saturdayTopLeft - fridayTopRight; // space between Friday cell an Saturday cell
    let borderWidth = month.$weekendSeparator.cssBorderWidthX();

    let posLeft = Math.floor(fridayTopRight + (space / 2) - borderWidth);
    month.$weekendSeparator.cssLeft(posLeft);
  }

  preselectDate(date, animated) {
    this.showDate(date, animated);
    if (date) {
      // Clear selection when a date is preselected
      this.setSelectedDate(null);
    }
    this.setPreselectedDate(date);
  }

  /**
   * @internal, use preselectDate to preselect a date
   */
  setPreselectedDate(preselectedDate) {
    this.setProperty('preselectedDate', preselectedDate);
  }

  _renderPreselectedDate() {
    if (!this.currentMonth) {
      return;
    }
    let $box = this.currentMonth.$container;
    $box.find('.date-picker-day').each((i, elem) => {
      let $day = $(elem);
      $day.removeClass('preselected');
      if (dates.isSameDay(this.preselectedDate, $day.data('date'))) {
        $day.addClass('preselected');
      }
    });
  }

  selectDate(date, animated) {
    this.showDate(date, animated);
    if (date) {
      // Clear preselection when a date is selected
      this.setPreselectedDate(null);
    }
    this.setSelectedDate(date);
  }

  /**
   * @internal, use selectDate to select a date
   */
  setSelectedDate(selectedDate) {
    this.setProperty('selectedDate', selectedDate);
  }

  _renderSelectedDate() {
    if (!this.currentMonth) {
      return;
    }
    let $box = this.currentMonth.$container;
    $box.find('.date-picker-day').each((i, elem) => {
      let $day = $(elem);
      $day.removeClass('selected');
      if (dates.isSameDay(this.selectedDate, $day.data('date'))) {
        $day.addClass('selected');
      }
    });
  }

  /**
   * Shows the month which contains the given date.
   * @param {Date} date
   * @param {boolean} [animated] - Default is true
   */
  showDate(viewDate, animated) {
    let viewDateDiff = 0;
    if (this.viewDate) {
      viewDateDiff = dates.compareMonths(viewDate, this.viewDate);
    }

    if (this.currentMonth && viewDateDiff) {
      if (viewDateDiff < 0) {
        this.prependMonth(viewDate);
      } else {
        this.appendMonth(viewDate);
      }
    } else {
      if (!this.currentMonth) {
        // Initially (when the popup is opened), don't reset, just display one month.
        // Reason: _renderMonths may not determine the proper scroll left yet
        this.setMonths([viewDate]);
      } else {
        this.resetMonths(viewDate);
      }
    }
    this.setViewDate(viewDate, animated);
  }

  shiftViewDate(years, months, days) {
    let date = this.viewDate;
    date = dates.shift(date, years, months, days);
    this.showDate(date);
  }

  shiftSelectedDate(years, months, days) {
    let date = this.preselectedDate;

    if (this.selectedDate) {
      if (this.allowedDates.length > 0) {
        date = this._findNextAllowedDate(years, months, days);
      } else {
        date = dates.shift(this.selectedDate, years, months, days);
      }
    }

    if (!date) {
      return; // do nothing when no date was found
    }

    this.selectDate(date, true);
  }

  _findNextAllowedDate(years, months, days) {
    let i, date,
      sum = years + months + days,
      dir = sum > 0 ? 1 : -1,
      now = this.selectedDate || dates.trunc(new Date());

    // if we shift by year or month, shift the 'now' date and then use that date as starting point
    // to find the next allowed date.
    if (years !== 0) {
      now = dates.shift(now, years, 0, 0);
    } else if (months !== 0) {
      now = dates.shift(now, 0, months, 0);
    }

    if (dir === 1) { // find next allowed date, starting from currently selected date
      for (i = 0; i < this.allowedDates.length; i++) {
        date = this.allowedDates[i];
        if (dates.compare(now, date) < 0) {
          return date;
        }
      }
    } else if (dir === -1) { // find previous allowed date, starting from currently selected date
      for (i = this.allowedDates.length - 1; i >= 0; i--) {
        date = this.allowedDates[i];
        if (dates.compare(now, date) > 0) {
          return date;
        }
      }
    }

    return null;
  }

  _isDateAllowed(date) {
    // when allowedDates is empty or not set, any date is allowed
    if (this.allowedDates.length === 0) {
      return true;
    }
    // when allowedDates is set, only dates contained in this array are allowed
    let dateAsTimestamp = dates.trunc(date).getTime();
    return this.allowedDates.some(allowedDate => allowedDate.getTime() === dateAsTimestamp);
  }

  _build$DateBox(viewDate) {
    let cl, i, day, dayEnabled, dayInMonth, $day,
      now = new Date(),
      start = new Date(viewDate);

    let $box = this.$container
      .makeDiv('date-picker-month')
      .data('viewDate', viewDate);

    // Create weekday header
    let $weekdays = $box.appendDiv('date-picker-weekdays');
    this.dateFormat.symbols.weekdaysShortOrdered
      .map(weekday => weekday.length > 2 ? weekday.substr(0, 2) : weekday)
      .forEach(weekday => $weekdays.appendDiv('date-picker-weekday', weekday.toUpperCase()));

    // Find start date (-1)
    let $week;
    for (let offset = 0; offset < 42; offset++) {
      start.setDate(start.getDate() - 1);
      let diff = new Date(start.getYear(), viewDate.getMonth(), 0).getDate() - start.getDate();
      if ((start.getDay() === 0) && (start.getMonth() !== viewDate.getMonth()) && (diff > 1)) {
        break;
      }
    }

    // Create days
    for (i = 0; i < 42; i++) {
      if (i % 7 === 0) {
        $week = $box.appendDiv('date-picker-week');
      }

      start.setDate(start.getDate() + 1);
      dayInMonth = start.getDate();

      if ((start.getDay() === 6) || (start.getDay() === 0)) {
        cl = (start.getMonth() !== viewDate.getMonth() ? ' date-picker-out-weekend' : ' date-picker-weekend');
        if (this._showWeekendSeparator && start.getDay() === 6) {
          cl += ' date-picker-day-weekend-separator';
        }
      } else {
        cl = (start.getMonth() !== viewDate.getMonth() ? ' date-picker-out' : '');
      }

      if (dates.isSameDay(start, now)) {
        cl += ' date-picker-now';
      }

      if (dates.isSameDay(this.selectedDate, start)) {
        cl += ' selected';
      }

      if (dates.isSameDay(this.preselectedDate, start)) {
        cl += ' preselected';
      }

      // helps to center days between 10 and 19 nicer (especially when website is zoomed > 100%)
      if (dayInMonth > 9 && dayInMonth < 20) {
        cl += ' ten';
      }

      dayEnabled = this._isDateAllowed(start);
      if (!dayEnabled) {
        cl += ' disabled';
      }

      day = (dayInMonth <= 9 ? '0' + dayInMonth : dayInMonth);
      $day = $week
        .appendDiv('date-picker-day' + cl)
        .data('dayInMonth', dayInMonth)
        .data('date', new Date(start));
      $day.appendSpan('text', day);

      if (dayEnabled) {
        $day.on('click', this._onDayClick.bind(this));
      }
    }

    return $box;
  }

  _append$Header() {
    let headerHtml =
      '<div class="date-picker-header">' +
      '  <div class="date-picker-left-y" data-shift="-12"></div>' +
      '  <div class="date-picker-left-m" data-shift="-1"></div>' +
      '  <div class="date-picker-right-y" data-shift="12"></div>' +
      '  <div class="date-picker-right-m" data-shift="1"></div>' +
      '  <div class="date-picker-header-month">&nbsp;</div>' +
      '</div>';
    return this.$container
      .appendElement(headerHtml)
      .toggleClass('touch', this.touch);
  }

  _updateHeader(viewDate) {
    this._$header.find('.date-picker-header-month').text(this._createHeaderText(viewDate));
  }

  _createHeaderText(viewDate) {
    let months = this.dateFormat.symbols.months;
    return months[viewDate.getMonth()] + ' ' + viewDate.getFullYear();
  }

  _registerSwipeHandlers() {
    let prevDate, nextDate;
    let onDown = /** @type {SwipeCallbackEvent} */e => {
      // stop pending animations, otherwise the months may be removed by the animation stop handler before touchend is executed
      this.$scrollable.stop(true);

      // Prepare months. On the first swipe the 3 boxes are already rendered, so nothing happens when setMonths is called.
      // But on a subsequent swipe (while the pane is still moving) the next month needs to be rendered.
      prevDate = dates.shift(this.viewDate, 0, -1, 0);
      nextDate = dates.shift(this.viewDate, 0, 1, 0);
      this.setMonths([prevDate, this.viewDate, nextDate]);
      this.swiped = false;
      return true;
    };
    let onMove = /** @type {SwipeCallbackEvent} */e => {
      let minX = this.$container.width() - this.$scrollable.outerWidth();
      let newScrollableLeft = Math.max(Math.min(e.newLeft, 0), minX); // limit the drag range
      if (newScrollableLeft !== e.originalLeft) {
        this.$scrollable.cssLeft(newScrollableLeft); // set the new position
      }
      return newScrollableLeft;
    };
    let onUp = /** @type {SwipeCallbackEvent} */e => {
      // If the movement is less than this value (in px), the swipe won't happen. Instead, the value is selected.
      let minMove = 5;
      let viewDate = this.viewDate;

      // Detect in which direction the swipe happened
      if (e.deltaX < -minMove) {
        viewDate = nextDate; // dragged left -> use next month
      } else if (e.deltaX > minMove) {
        viewDate = prevDate; // dragged right -> use previous month
      }

      if (this.viewDate !== viewDate) {
        this.swiped = true;
        this.setViewDate(viewDate);
      }
    };
    events.onSwipe(this.$scrollable, 'datepickerDrag', onDown, onMove, onUp);
  }

  _onNavigationMouseDown(event) {
    let $target = $(event.currentTarget);
    let diff = $target.data('shift');
    this.shiftViewDate(0, diff, 0);
  }

  _onDayClick(event) {
    if (this.swiped) {
      // Don't handle on a swipe action
      return;
    }
    let $target = $(event.currentTarget);
    let date = $target.data('date');
    this.selectDate(date);
    this.trigger('dateSelect', {
      date: date
    });
  }

  _onMouseWheel(event) {
    event = event.originalEvent || this.$container.window(true).event.originalEvent;
    let wheelData = event.wheelDelta ? event.wheelDelta / 10 : -event.detail * 3;
    let diff = (wheelData >= 0 ? -1 : 1);
    this.shiftViewDate(0, diff, 0);
    event.preventDefault();
  }
}
