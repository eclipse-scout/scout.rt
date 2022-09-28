/*
 * Copyright (c) 2010-2022 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {Calendar, CalendarComponentEventMap, CalendarComponentModel, DateRange, dates, Label, Popup, Range, scout, strings, Widget, WidgetPopup} from '../index';
import $ from 'jquery';
import {JsonDateRange} from '../util/dates';

/**
 * See JsonCalendarItem.java
 */
export type CalendarItem = {
  exists: boolean;
  lastModified: number;
  itemId: any;
  owner: string;
  cssClass: string;
  subject: string;
  description: string;
  recurrencePattern: {
    lastModified: number;
    regenerate: boolean;
    startTimeMinutes: number;
    endTimeMinutes: number;
    durationMinutes: number;
    firstDate: Date;
    lastDate: Date;
    occurrences: number;
    noEndDate: boolean;
    /**
     * @see RecurrencePattern.java TYPE* constants
     */
    type: number;
    interval: number;
    /**
     * @see RecurrencePattern.java INST_* constants
     */
    instance: number;
    dayOfWeekBits: number;
    dayOfMonth: number;
    monthOfYear: number;
  };
};

export default class CalendarComponent extends Widget implements CalendarComponentModel {
  declare model: CalendarComponentModel;
  declare eventMap: CalendarComponentEventMap;
  declare parent: Calendar;

  fromDate: string;
  toDate: string;
  selected: boolean;
  fullDay: boolean;
  item: CalendarItem;
  coveredDaysRange: DateRange;
  protected _$parts: JQuery[];

  constructor() {
    super();
    this.selected = false;
    this.fullDay = false;
    this.item = null;
    this._$parts = [];
  }

  /**
   * If day of a month is smaller than 100px, the components get the class compact
   */
  static MONTH_COMPACT_THRESHOLD = 100;

  protected override _init(model: CalendarComponentModel) {
    super._init(model);

    this._syncCoveredDaysRange(model.coveredDaysRange as JsonDateRange);
  }

  protected _syncCoveredDaysRange(coveredDaysRange: JsonDateRange) {
    if (coveredDaysRange) {
      this.coveredDaysRange = new DateRange(
        dates.parseJsonDate(coveredDaysRange.from),
        dates.parseJsonDate(coveredDaysRange.to));
    }
  }

  protected override _remove() {
    // remove $parts because they're not children of this.$container
    this._$parts.forEach($part => $part.remove());
    this._$parts = [];
    super._remove();
  }

  protected _startLoopDay(): Date {
    // start date is either beginning of the component or beginning of viewRange
    if (dates.compare(this.coveredDaysRange.from, this.parent.viewRange.from) > 0) {
      return this.coveredDaysRange.from;
    }
    return this.parent.viewRange.from;
  }

  protected override _render() {
    let partDay: Date, $day: JQuery, $part: JQuery<HTMLDivElement>;
    if (!this.coveredDaysRange) {
      // coveredDaysRange is not set on current CalendarComponent. Cannot show calendar component without from and to values.
      return;
    }

    let loopDay = this._startLoopDay();

    let appointmentToDate: Date | string = dates.parseJsonDate(this.toDate);
    let appointmentFromDate = dates.parseJsonDate(this.fromDate);
    let coveredDaysRangeTo = this.coveredDaysRange.to;

    if (!this.fullDay) {
      let truncToDate = dates.trunc(appointmentToDate);
      if (!dates.isSameDay(appointmentFromDate, appointmentToDate) && dates.compare(appointmentToDate, truncToDate) === 0) {
        appointmentToDate = dates.shiftTime(appointmentToDate, 0, 0, 0, -1);
        coveredDaysRangeTo = dates.shift(coveredDaysRangeTo, 0, 0, -1);
      }
    }
    appointmentToDate = dates.toJsonDate(appointmentToDate);

    let lastComponentDay = dates.shift(coveredDaysRangeTo, 0, 0, 1);

    if (dates.compare(loopDay, lastComponentDay) > 0) {
      // start day for the while loop is greater then the exit condition
      return;
    }

    while (!dates.isSameDay(loopDay, lastComponentDay)) {
      partDay = loopDay;
      loopDay = dates.shift(loopDay, 0, 0, 1); // increase day for loop

      // check if day is in visible view range
      if (dates.compare(partDay, this.parent.viewRange.to) > 0) {
        // break condition, partDay is now out of range.
        break;
      }

      // @ts-ignore
      if (this.fullDay && !this.parent._isMonth()) {
        $day = this._findDayInGrid(partDay, this.parent.$topGrid);
      } else {
        $day = this._findDayInGrid(partDay, this.parent.$grid);
      }
      if (!$day) {
        // next day, partDay not found in grid
        continue;
      }
      $part = $day.appendDiv('calendar-component');

      $part
        .addClass(this.item.cssClass)
        .data('component', this)
        .data('partDay', partDay)
        .on('mouseup', this._onMouseUp.bind(this))
        .on('contextmenu', this._onContextMenu.bind(this));
      $part.appendDiv('calendar-component-leftcolorborder');
      $part.appendDiv('content', this.item.subject);

      this._$parts.push($part);

      // @ts-ignore
      if (this.parent._isMonth()) {
        let width = $day.data('new-width') || $day.width(); // prefer width from layoutSize
        $part.addClass('component-month')
          .toggleClass('compact', width < CalendarComponent.MONTH_COMPACT_THRESHOLD);
      } else {
        if (this.fullDay) {
          // Full day tasks are rendered in the topGrid
          let alreadyExistingTasks = $('.component-task', $day).length;
          // Offset of initial task: 30px for the day-of-month number
          // Offset of following tasks: 26px * preceding number of tasks. 26px: Task 23px high, 1px border. Spaced by 2px
          this._arrangeTask(30 + 26 * alreadyExistingTasks);
          $part.addClass('component-task');
        } else {
          let
            fromDate = dates.parseJsonDate(this.fromDate),
            toDate = dates.parseJsonDate(appointmentToDate),
            partFrom = this._getHours(this.fromDate),
            partTo = this._getHours(appointmentToDate);

          // position and height depending on start and end date
          $part.addClass('component-day');
          if (dates.isSameDay(dates.trunc(this.coveredDaysRange.from), dates.trunc(coveredDaysRangeTo))) {
            this._partPosition($part, partFrom, partTo);
          } else if (dates.isSameDay(partDay, fromDate)) {
            this._partPosition($part, partFrom, 25) // 25: indicate that it takes longer than that day
              .addClass('component-open-bottom');
          } else if (dates.isSameDay(partDay, toDate)) {
            // Start at zero: No need to indicate that it starts earlier since indicator needs no extra space
            this._partPosition($part, 0, partTo)
              .addClass('component-open-top');
          } else {

            this._partPosition($part, 0, 25) // 25: indicate that it takes longer than that day
              .addClass('component-open-top')
              .addClass('component-open-bottom');
          }
        }
      }
    }
  }

  protected _getHours(date: string): number {
    let d = dates.parseJsonDate(date);
    return d.getHours() + d.getMinutes() / 60;
  }

  getLengthInHoursDecimal(): number {
    let toTimestamp = dates.parseJsonDate(this.toDate);
    let fromTimestamp = dates.parseJsonDate(this.fromDate);
    return (toTimestamp.getTime() - fromTimestamp.getTime()) / (1000 * 60 * 60);
  }

  protected _findDayInGrid(date: Date, $grid: JQuery): JQuery {
    return $grid.find('.calendar-day')
      .filter((i, elem) => dates.isSameDay($(this).data('date'), date))
      .eq(0);
  }

  protected _isTask(): boolean {
    // @ts-ignore
    return !this.parent._isMonth() && this.fullDay;
  }

  protected _arrangeTask(taskOffset: number) {
    this._$parts.forEach($part => $part.css('top', taskOffset + 'px'));
  }

  protected _isDayPart(): boolean {
    // @ts-ignore
    return !this.parent._isMonth() && !this.fullDay;
  }

  protected _getHourRange(day: Date): Range {
    let hourRange = new Range(this._getHours(this.fromDate), this._getHours(this.toDate));
    let dateRange = new DateRange(dates.parseJsonDate(this.fromDate), dates.parseJsonDate(this.toDate));

    if (dates.isSameDay(day, dateRange.from) && dates.isSameDay(day, dateRange.to)) {
      return new Range(hourRange.from, hourRange.to);
    }
    if (dates.isSameDay(day, dateRange.from)) {
      return new Range(hourRange.from, 24);
    }
    if (dates.isSameDay(day, dateRange.to)) {
      return new Range(0, hourRange.to);
    }
    return new Range(0, 24);
  }

  getPartDayPosition(day: Date): Range {
    return this._getDisplayDayPosition(this._getHourRange(day));
  }

  protected _getDisplayDayPosition(range: Range): Range {
    // Doesn't support minutes yet...
    // @ts-ignore
    let preferredRange = new Range(this.parent._dayPosition(range.from, 0), this.parent._dayPosition(range.to, 0));
    // Fixed number of divisions...
    let minRangeSize = Math.round(100 * 100 / 24 / this.parent.numberOfHourDivisions) / 100; // Round to two digits
    if (preferredRange.size() < minRangeSize) {
      return new Range(preferredRange.from, preferredRange.from + minRangeSize);
    }
    return preferredRange;
  }

  protected _partPosition($part: JQuery, y1: number, y2: number): JQuery {
    // Compensate open bottom (height: square of 16px, rotated 45°, approx. 23px = sqrt(16^2 + 16^2)
    let compensateBottom = y2 === 25 ? 23 : 0;
    y2 = Math.min(24, y2);

    let range = new Range(y1, y2);
    let r = this._getDisplayDayPosition(range);

    // Convert to %, rounded to two decimal places
    compensateBottom = Math.round(100 * (100 / 1920 * compensateBottom)) / 100;

    return $part
      .css('top', r.from + '%')
      .css('height', r.to - r.from - compensateBottom + '%');
  }

  protected override _renderProperties() {
    super._renderProperties();
    this._renderSelected();
  }

  protected _renderSelected() {
    this._$parts.forEach($part => $part.toggleClass('comp-selected', this.selected));
  }

  setSelected(selected: boolean) {
    this.setProperty('selected', selected);
  }

  updateSelectedComponent($part: JQuery, updateScrollPosition: boolean) {
    // @ts-ignore
    this.parent._selectedComponentChanged(this, $part.data('partDay') as Date, updateScrollPosition);
  }

  protected _onMouseUp(event: JQuery.MouseUpEvent) {
    // don't show popup if dragging is in process
    // @ts-ignore
    if (this.parent._moveData && this.parent._moveData.moving) {
      return;
    }

    let $part = $(event.delegateTarget);
    this.updateSelectedComponent($part, false);

    if (event.button === 0) {
      let popup = scout.create(WidgetPopup, {
        parent: this.parent,
        $anchor: $part,
        closeOnAnchorMouseDown: true,
        closeOnMouseDownOutside: true,
        closeOnOtherPopupOpen: true,
        horizontalAlignment: Popup.Alignment.LEFT,
        verticalAlignment: Popup.Alignment.CENTER,
        trimWidth: false,
        trimHeight: false,
        horizontalSwitch: true,
        verticalSwitch: true,
        withArrow: true,
        cssClass: 'tooltip',
        scrollType: 'remove',
        location: {
          y: event.originalEvent.clientY
        },
        content: {
          objectType: Label,
          htmlEnabled: true,
          cssClass: 'tooltip-content',
          value: this._description()
        }
      });
      popup.open();
    }
  }

  protected _onContextMenu(event: JQuery.ContextMenuEvent<HTMLDivElement>) {
    // @ts-ignore
    this.parent._showContextMenu(event, 'Calendar.CalendarComponent');
  }

  protected _format(date: Date, pattern: string): string {
    return dates.format(date, this.session.locale, pattern);
  }

  protected _description(): string {
    let descParts = [],
      range = null,
      text = '',
      fromDate = dates.parseJsonDate(this.fromDate),
      toDate = dates.parseJsonDate(this.toDate);

    // subject
    if (strings.hasText(this.item.subject)) {
      descParts.push({
        text: strings.encode(this.item.subject),
        cssClass: 'calendar-component-title'
      });
    }

    // time-range
    if (this.fullDay) {
      // NOP
    } else if (dates.isSameDay(fromDate, toDate)) {
      range = this.session.text('ui.FromXToY', this._format(fromDate, 'HH:mm'), this._format(toDate, 'HH:mm'));
    } else {
      range = this.session.text('ui.FromXToY', this._format(fromDate, 'EEEE HH:mm '), this._format(toDate, 'EEEE HH:mm'));
    }

    if (strings.hasText(range)) {
      descParts.push({
        text: range,
        cssClass: 'calendar-component-intro'
      });
    }

    // description
    if (strings.hasText(this.item.description)) {
      descParts.push({
        text: strings.nl2br(this.item.description)
      });
    }

    // build text
    descParts.forEach(part => {
      text += (part.cssClass ? '<span class="' + part.cssClass + '">' + part.text + '</span>' : part.text) + '<br/>';
    });

    return text;
  }
}
