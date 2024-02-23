/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {Calendar, DateRange, dates, HtmlComponent, InitModelOf, Planner, PlannerDisplayMode, scout, scrollbars, Widget, YearPanelEventMap, YearPanelModel} from '../index';
import $ from 'jquery';

export class YearPanel extends Widget implements YearPanelModel {
  declare model: YearPanelModel;
  declare eventMap: YearPanelEventMap;
  declare self: YearPanel;

  $yearTitle: JQuery;
  $yearList: JQuery;
  selectedDate: Date;
  displayMode: PlannerDisplayMode; // we must use Planner.DisplayMode (extends Calendar.DisplayMode) here because YearPanel must work for calendar and planner.
  alwaysSelectFirstDay: boolean;
  yearRendered: boolean;
  viewRange: DateRange;

  protected override _init(model: InitModelOf<this>) {
    super._init(model);

    // If true, it is only possible to select the first day of a range, depending on the selected mode
    // day mode: every day may be selected
    // week, work week, calendar week mode: only first day of week may be selected
    // year, month mode: only first day of month may be selected
    this.alwaysSelectFirstDay = model.alwaysSelectFirstDay;
  }

  protected override _render() {
    this.$container = this.$parent.appendDiv('year-panel-container');
    this.htmlComp = HtmlComponent.install(this.$container, this.session);
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

  override get$Scrollable(): JQuery {
    return this.$yearList;
  }

  removeContent() {
    this._uninstallScrollbars();
    this.$yearList.empty();
    this.yearRendered = false;
  }

  protected override _remove() {
    this.removeContent();
    super._remove();
  }

  protected _drawYear() {
    let first: Date, month: number, $month: JQuery, day: Date, $day: JQuery,
      year = this.selectedDate.getFullYear();

    // append 3 years
    this.$yearTitle
      .data('year', year)
      .empty();

    this.$yearTitle.appendDiv('year-title-item', (year - 1) + '')
      .data('year-diff', -1)
      .on('click', this._onYearClick.bind(this));

    this.$yearTitle.appendDiv('year-title-item selected', year + '');

    this.$yearTitle.appendDiv('year-title-item', (year + 1) + '')
      .data('year-diff', +1)
      .on('click', this._onYearClick.bind(this));

    // add months and days
    for (month = 0; month < 12; month++) {
      first = new Date(year, month, 1);
      $month = this.$yearList.appendDiv('year-month').attr('data-title', this._format(first, 'MMMM'));
      for (let d = 1; d <= 31; d++) {
        day = new Date(year, month, d);

        // stop if day is already out of range
        if (day.getMonth() !== month) {
          break;
        }

        // add div per day
        $day = $month.appendDiv('year-day', d + '').data('date', day);

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
      .on('click', this._onYearDayClick.bind(this))
      .on('mouseenter', this._onYearHoverIn.bind(this))
      .on('mouseleave', this._onYearHoverOut.bind(this));

    // update scrollbar
    scrollbars.update(this.$yearList);
  }

  protected _colorYear() {
    if (!this.yearRendered) {
      return;
    }

    // remove color information
    $('.year-day.year-range, .year-day.year-range-day', this.$yearList).removeClass('year-range year-range-day');

    // loop all days and colorize based on range and selected
    let yearPanel: YearPanel = this,
      $day, date;

    $('.year-day', this.$yearList).each(function() {
      $day = $(this);
      date = $day.data('date') as Date;
      if (yearPanel.displayMode !== Calendar.DisplayMode.DAY &&
        date >= yearPanel.viewRange.from && date < yearPanel.viewRange.to) {
        $day.addClass('year-range');
      }
      if (dates.isSameDay(date, yearPanel.selectedDate)) {
        $day.addClass('year-range-day');
      }
    });

    // selected has to be visible day
    this._scrollYear();
  }

  /** @internal */
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

    this.$yearList.animateAVCSD('scrollTop', top + halfMonth - halfYear, () => scrollbars.update(this.$yearList));
  }

  protected _format(date: Date, pattern: string): string {
    return dates.format(date, this.session.locale, pattern);
  }

  selectDate(date: Date) {
    this.selectedDate = date;

    if (this.rendered) {
      // If year shown and changed, redraw year
      if (!date || date.getFullYear() !== this.$yearTitle.data('year')) {
        this.renderContent();
      }
      this._colorYear();
    }
  }

  setDisplayMode(displayMode: PlannerDisplayMode) {
    if (displayMode === this.displayMode) {
      return;
    }
    this._setProperty('displayMode', displayMode);
    if (this.rendered) {
      this._colorYear();
    }
  }

  setViewRange(viewRange: DateRange) {
    if (viewRange === this.viewRange) {
      return;
    }
    this._setProperty('viewRange', viewRange);
    if (this.rendered) {
      this._colorYear();
    }
  }

  /* -- events ---------------------------------------- */

  protected _onYearClick(event: JQuery.ClickEvent) {
    let
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

  protected _onYearDayClick(event: JQuery.ClickEvent) {
    this.selectedDate = $('.year-hover-day', this.$yearList).data('date');
    if (this.selectedDate) {
      this.trigger('dateSelect', {
        date: this.selectedDate
      });
    }
  }

  protected _onYearHoverIn(event: JQuery.MouseEnterEvent) {
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
  protected _onYearHoverOut(event: JQuery.MouseLeaveEvent) {
    $('.year-day.year-hover, .year-day.year-hover-day', this.$yearList).removeClass('year-hover year-hover-day');
  }
}
