/*
 * Copyright (c) 2014-2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {Calendar, dates, Planner, scout, scrollbars, Widget} from '../index';
import $ from 'jquery';

export default class YearPanel extends Widget {

  constructor() {
    super();

    this.$yearTitle;
    this.$yearList;
    this.selectedDate;
    this.displayMode;
    this.alwaysSelectFirstDay;
  }

  _init(model) {
    super._init(model);

    // If true, it is only possible to select the first day of a range, depending of the selected mode
    // day mode: every day may be selected
    // week, work week, calendar week mode: only first day of week may be selected
    // year, month mode: only first day of month may be selected
    this.alwaysSelectFirstDay = model.alwaysSelectFirstDay;
  }

  _render() {
    this.$container = this.$parent.appendDiv('year-panel-container');
    this.$yearTitle = this.$container.appendDiv('year-panel-title');
    this.$yearList = this.$container.appendDiv('year-panel-list');
  }

  renderContent() {
    this.removeContent();
    this._drawYear();
    this._installScrollbars({
      axis: 'y'
    });
    this.yearRendered = true;
    this._colorYear();
  }

  get$Scrollable() {
    return this.$yearList;
  }

  removeContent() {
    this._uninstallScrollbars();
    this.$yearList.empty();
    this.yearRendered = false;
  }

  _remove() {
    this.removeContent();
    super._remove();
  }

  _drawYear() {
    let first, month, $month, d, day, $day,
      year = this.selectedDate.getFullYear();

    // append 3 years
    this.$yearTitle
      .data('year', year)
      .empty();

    this.$yearTitle.appendDiv('year-title-item', year - 1)
      .data('year-diff', -1)
      .click(this._onYearClick.bind(this));

    this.$yearTitle.appendDiv('year-title-item selected', year);

    this.$yearTitle.appendDiv('year-title-item', year + 1)
      .data('year-diff', +1)
      .click(this._onYearClick.bind(this));

    // add months and days
    for (month = 0; month < 12; month++) {
      first = new Date(year, month, 1);
      $month = this.$yearList.appendDiv('year-month').attr('data-title', this._format(first, 'MMMM'));
      for (d = 1; d <= 31; d++) {
        day = new Date(year, month, d);

        // stop if day is already out of range
        if (day.getMonth() !== month) {
          break;
        }

        // add div per day
        $day = $month.appendDiv('year-day', d).data('date', day);

        if (day.getDay() === 0 || day.getDay() === 6) {
          $day.addClass('weekend');
        }

        // first day has margin depending on weekday
        if (d === 1) {
          $day.css('margin-left', ((day.getDay() + 6) % 7) * $day.outerWidth());
        }
      }
    }

    // bind events for days divs
    $('.year-day', this.$yearList)
      .click(this._onYearDayClick.bind(this))
      .hover(this._onYearHoverIn.bind(this), this._onYearHoverOut.bind(this));

    // update scrollbar
    scrollbars.update(this.$yearList);
  }

  _colorYear() {
    if (!this.yearRendered) {
      return;
    }

    // remove color information
    $('.year-day.year-range, .year-day.year-range-day', this.$yearList).removeClass('year-range year-range-day');

    // loop all days and colorize based on range and selected
    let that = this,
      $day, date;

    $('.year-day', this.$yearList).each(function() {
      $day = $(this);
      date = $day.data('date');
      if (that.displayMode !== Calendar.DisplayMode.DAY &&
        date >= that.viewRange.from && date < that.viewRange.to) {
        $day.addClass('year-range');
      }
      if (dates.isSameDay(date, that.selectedDate)) {
        $day.addClass('year-range-day');
      }
    });

    // selected has to be visible day
    this._scrollYear();
  }

  _scrollYear() {
    let top, halfMonth, halfYear,
      $day = $('.year-range-day', this.$yearList),
      $month = $day.parent(),
      $year = $day.parent().parent();

    if (!$month[0]) {
      return;
    }
    top = $month[0].offsetTop;
    halfMonth = $month.outerHeight() / 2;
    halfYear = $year.outerHeight() / 2;

    this.$yearList.animateAVCSD('scrollTop', top + halfMonth - halfYear);
  }

  _format(date, pattern) {
    return dates.format(date, this.session.locale, pattern);
  }

  selectDate(date) {
    this.selectedDate = date;

    if (this.rendered) {
      // If year shown and changed, redraw year
      if (!date || date.getFullYear() !== this.$yearTitle.data('year')) {
        this.renderContent();
      }
      this._colorYear();
    }
  }

  setDisplayMode(displayMode) {
    if (displayMode === this.displayMode) {
      return;
    }
    this._setProperty('displayMode', displayMode);
    if (this.rendered) {
      this._colorYear();
    }
  }

  setViewRange(viewRange) {
    if (viewRange === this.viewRange) {
      return;
    }
    this._setProperty('viewRange', viewRange);
    if (this.rendered) {
      this._colorYear();
    }
  }

  /* -- events ---------------------------------------- */

  _onYearClick(event) {
    let
      // we must use Planner.DisplayMode (extends Calendar.DisplayMode) here
      // because YearPanel must work for calendar and planner.
      displayMode = Planner.DisplayMode,
      diff = $(event.target).data('year-diff'),
      year = this.selectedDate.getFullYear(),
      month = this.selectedDate.getMonth(),
      date = this.selectedDate.getDate(),
      newDate = new Date(year + diff, month, date),
      oldWeek,
      newWeek,
      weekDiff;

    if (this.alwaysSelectFirstDay) {
      // find date based on mode
      if (scout.isOneOf(this.displayMode, displayMode.WEEK, displayMode.WORK_WEEK, displayMode.CALENDAR_WEEK)) {
        oldWeek = dates.weekInYear(this.selectedDate);
        newWeek = dates.weekInYear(newDate);
        weekDiff = oldWeek - newWeek;
        // shift new selection that week in year does not change and the new selection is a monday.
        newDate = dates.shift(newDate, 0, 0, weekDiff * 7);
        newDate = dates.shiftToNextOrPrevMonday(newDate, 0);
      } else if (scout.isOneOf(this.displayMode, displayMode.MONTH, displayMode.YEAR)) {
        // set to first day of month
        newDate = new Date(year + diff, month, 1);
      }
    }

    this.selectedDate = newDate;
    this.trigger('dateSelect', {
      date: this.selectedDate
    });
  }

  _onYearDayClick(event) {
    this.selectedDate = $('.year-hover-day', this.$yearList).data('date');
    if (this.selectedDate) {
      this.trigger('dateSelect', {
        date: this.selectedDate
      });
    }
  }

  _onYearHoverIn(event) {
    let $day = $(event.target),
      date1 = $day.data('date'),
      year = date1.getFullYear(),
      month = date1.getMonth(),
      date = date1.getDate(),
      day = (date1.getDay() + 6) % 7,
      startHover,
      endHover,
      $day2, date2;

    // find hover based on mode
    if (this.displayMode === Calendar.DisplayMode.DAY) {
      startHover = new Date(year, month, date);
      endHover = new Date(year, month, date);
    } else if (this.displayMode === Calendar.DisplayMode.WEEK) {
      startHover = new Date(year, month, date - day);
      endHover = new Date(year, month, date - day + 6);
    } else if (this.displayMode === Calendar.DisplayMode.WORK_WEEK) {
      startHover = new Date(year, month, date - day);
      endHover = new Date(year, month, date - day + 4);

      // don't allow selecting a weekend day
      if (date1 > endHover) {
        date1 = endHover;
      }
    } else if (this.displayMode === Calendar.DisplayMode.MONTH) {
      startHover = new Date(year, month, 1);
      endHover = new Date(year, month + 1, 0);
    } else if (this.displayMode === Planner.DisplayMode.YEAR) {
      startHover = new Date(year, month, 1);
      endHover = startHover;
    } else {
      startHover = new Date(year, month, date - day);
      endHover = startHover;
    }

    if (this.alwaysSelectFirstDay) {
      date1 = startHover;
    }

    // loop days and colorize based on hover start and hover end
    $('.year-day', this.$yearList).each(function() {
      $day2 = $(this);
      date2 = $day2.data('date');
      if (date2 >= startHover && date2 <= endHover) {
        $day2.addClass('year-hover');
      } else {
        $day2.removeClass('year-hover');
      }
      if (dates.isSameDay(date1, date2)) {
        $day2.addClass('year-hover-day');
      }
    });
  }

  // remove all hover effects
  _onYearHoverOut(event) {
    $('.year-day.year-hover, .year-day.year-hover-day', this.$yearList).removeClass('year-hover year-hover-day');
  }
}
